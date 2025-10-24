package com.growtracker.app.ui.ai

import android.content.Context
import android.graphics.Bitmap
import com.growtracker.app.ui.ai.KnowledgeRepository.query
import com.growtracker.app.ui.ai.KnowledgeRepository.ensureLoaded as ensureKbLoaded
import com.growtracker.app.data.upload.AppDatabase
import com.growtracker.app.data.upload.UploadRepository
import com.growtracker.app.data.upload.UploadStatus
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.growtracker.app.work.ImageUploadWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * TensorFlow Lite based implementation scaffold.
 * NOTE: Model file is not yet bundled; this class will gracefully fallback if model load fails.
 */
class TFLiteLeafSenseAnalyzer(
    private val context: Context,
    private val modelAssetName: String = "leafsense_model.tflite", // legacy fallback
    private val labelsAssetName: String = "leafsense_labels.txt",
    private val manifestName: String = "leafsense_model.json",
    private var inputSize: Int = 224,
    private val numChannels: Int = 3
) : LeafSenseAnalyzer {

    /** Optional two-stage mode: Stage 0 cannabis filter (binary) -> Stage 1 condition classifier. */
    enum class PipelineMode { DIRECT, TWO_STAGE }
    @Volatile var pipelineMode: PipelineMode = PipelineMode.DIRECT
    /** Threshold for Stage 0 to accept as cannabis. */
    @Volatile var stage0AcceptThreshold: Float = 0.60f
    /** If below this, but above soft floor, we may return NO_CANNABIS with confidence. */
    @Volatile var stage0SoftFloor: Float = 0.40f
    /** Label placeholder when Stage0 rejects. */
    private val noCannabisLabel = "(Kein Cannabisblatt erkannt)"
    /** Optional separate Stage 0 model assets; if not found falls back to heuristic (currently none). */
    private val stage0ModelNames = listOf("cannabis_filter_int8.tflite", "cannabis_filter_fp32.tflite")
    private var stage0Interpreter: Interpreter? = null
    private var stage0InputSize: Int = 160

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private val pixelArrayRef = java.util.concurrent.atomic.AtomicReference<IntArray?>()
    private val inputBufferRef = java.util.concurrent.atomic.AtomicReference<ByteBuffer?>()
    private var manifest: ModelManifest? = null
    // Temperature scaling for calibrated probabilities (T=1.0 -> no change)
    private val calibrationAssetName: String = "leafsense_calibration.json"
    @Volatile private var temperature: Float = 1.0f
    @Volatile private var loadAttempted: Boolean = false

    private suspend fun ensureLoaded() = withContext(Dispatchers.IO) {
        if (interpreter != null) return@withContext
        loadAttempted = true

        // Try manifest first
        val manifest = runCatching { loadManifest(manifestName) }.getOrNull().also { this@TFLiteLeafSenseAnalyzer.manifest = it }
        val modelPriority = mutableListOf<String>()
        if (manifest != null) {
            manifest.modelFile?.let { modelPriority.add(it) }
        }
        // Add conventional priority order
        modelPriority.addAll(
            listOf(
                "model_int8_full.tflite",
                "model_int8_dynamic.tflite",
                "model_fp32.tflite",
                modelAssetName // legacy single name
            )
        )

        labels = if (manifest?.classes?.isNotEmpty() == true) {
            manifest.classes
        } else runCatching { loadLabels(labelsAssetName) }.getOrElse { emptyList() }

        if (manifest?.inputSize != null) {
            inputSize = manifest.inputSize
        }

        // Deduplicate while preserving order
        val tried = mutableSetOf<String>()
        val ordered = modelPriority.filter { tried.add(it) }
        for (candidate in ordered) {
            val mapped = runCatching { loadModel(candidate) }.getOrNull()
            if (mapped != null) {
                interpreter = runCatching { Interpreter(mapped) }.getOrNull()
                if (interpreter != null) {
                    selectedModelName = candidate
                    break
                }
            }
        }

        // Try Stage 0 model(s) (non-fatal if missing)
        for (candidate in stage0ModelNames) {
            if (stage0Interpreter != null) break
            val mapped = runCatching { loadModel(candidate) }.getOrNull() ?: continue
            stage0Interpreter = runCatching { Interpreter(mapped) }.getOrNull()
            if (stage0Interpreter != null) {
                selectedStage0Name = candidate
            }
        }

        // Attempt to load knowledge base if declared and not yet loaded
        manifest?.knowledge?.merged_file?.let { merged ->
            runCatching { KnowledgeRepository.ensureLoaded(context, merged) }
        }

        // Load optional calibration temperature (non-fatal)
        temperature = runCatching { loadTemperature(calibrationAssetName) }.getOrNull()?.takeIf { it.isFinite() && it > 0f } ?: 1.0f
    }

    private var selectedModelName: String? = null
    private var selectedStage0Name: String? = null
    @Volatile private var lastStage0Prob: Float? = null

    private data class ModelManifest(
        val model_version: String? = null,
        val modelFile: String? = null,
        val input_size: Int? = null,
        val inputSize: Int? = null, // allow both spellings
        val quantization: String? = null,
        val classes: List<String>? = null,
        val normalization: NormSpec? = null,
        val knowledge: Knowledge? = null,
        val model_sha256: String? = null
    ) {
        data class NormSpec(val mean: List<Double>?, val std: List<Double>?)
        data class Knowledge(
            val kb_version: String? = null,
            val merged_file: String? = null,
            val index_file: String? = null,
            val entry_count: Int? = null
        )
    }

    private fun loadManifest(assetName: String): ModelManifest? = try {
        context.assets.open(assetName).bufferedReader().use { br ->
            val text = br.readText()
            // Minimal JSON parse (avoid adding a full JSON lib if not already present)
            // We fallback silently on failure.
            org.json.JSONObject(text).let { obj ->
                fun nullableString(o: org.json.JSONObject, key: String): String? =
                    if (o.has(key)) o.optString(key).takeIf { it.isNotBlank() } else null
                val classes = if (obj.has("classes")) {
                    val arr = obj.getJSONArray("classes")
                    (0 until arr.length()).map { arr.getString(it) }
                } else null
                val norm = if (obj.has("normalization")) {
                    val n = obj.getJSONObject("normalization")
                    val meanArr = n.optJSONArray("mean")?.let { a -> (0 until a.length()).map { a.getDouble(it) } }
                    val stdArr = n.optJSONArray("std")?.let { a -> (0 until a.length()).map { a.getDouble(it) } }
                    ModelManifest.NormSpec(meanArr, stdArr)
                } else null
                val knowledge = if (obj.has("knowledge")) {
                    try {
                        val k = obj.getJSONObject("knowledge")
                        ModelManifest.Knowledge(
                            kb_version = nullableString(k, "kb_version"),
                            merged_file = nullableString(k, "merged_file"),
                            index_file = nullableString(k, "index_file"),
                            entry_count = if (k.has("entry_count")) k.optInt("entry_count") else null
                        )
                    } catch (_: Exception) { null }
                } else null
                ModelManifest(
                    model_version = nullableString(obj, "model_version"),
                    modelFile = nullableString(obj, "model_file") ?: nullableString(obj, "modelFile"),
                    input_size = if (obj.has("input_size")) obj.optInt("input_size") else null,
                    inputSize = if (obj.has("inputSize")) obj.optInt("inputSize") else null,
                    quantization = nullableString(obj, "quantization"),
                    classes = classes,
                    normalization = norm,
                    knowledge = knowledge
                )
            }
        }
    } catch (e: Exception) { null }

    /** Runtime-computed hash (hex lowercase) of the selected model file, if verification attempted */
    @Volatile private var runtimeModelHash: String? = null
    /** True if manifest provided a model_sha256 and it matched the runtime computed hash */
    @Volatile private var integrityVerified: Boolean = false
    /** True if a mismatch occurred (manifest hash present but different) */
    @Volatile private var integrityMismatch: Boolean = false

    fun modelRuntimeHash(): String? = runtimeModelHash
    fun modelIntegrityVerified(): Boolean = integrityVerified
    fun modelIntegrityMismatch(): Boolean = integrityMismatch

    /**
     * Query on-device knowledge base (if present). Will attempt a lazy load using manifest. Returns empty list if unavailable.
     */
    suspend fun queryKnowledge(text: String, topK: Int = 3): List<KnowledgeRepository.Entry> {
        if (text.isBlank()) return emptyList()
        ensureLoaded() // make sure manifest parsed & maybe knowledge attempted
        val merged = manifest?.knowledge?.merged_file ?: return emptyList()
        // ensure knowledge loaded (idempotent)
        withContext(Dispatchers.IO) { KnowledgeRepository.ensureLoaded(context, merged) }
        return KnowledgeRepository.query(text, topK)
    }

    var enableEnqueueUploads: Boolean = true
    var uploadBaseUrl: String = "https://api.example.com" // TODO external config

    private suspend fun enqueuePredictionArtifact(top: LeafSenseResult, image: LeafSenseImage.BitmapRef) {
        if (!enableEnqueueUploads) return
        // Persist bitmap to a temp file (JPEG)
        val dir = context.cacheDir.resolve("pending_uploads").apply { mkdirs() }
        val file = kotlin.io.path.createTempFile(dir.toPath(), prefix = "leaf_", suffix = ".jpg").toFile()
        runCatching {
            val fos = file.outputStream()
            image.bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush(); fos.close()
        }.onFailure { return }
        val db = AppDatabase.get(context)
        val repo = UploadRepository(db.uploadDao())
        val confidenceBucket = when {
            top.confidence >= 0.8f -> "HIGH"
            top.confidence >= 0.5f -> "MID"
            else -> "LOW"
        }
        repo.enqueue(
            localUri = file.toURI().toString(),
            predictionLabel = top.label,
            top1Score = top.confidence,
            confidenceBucket = confidenceBucket,
            modelVersion = manifest?.model_version
        )
        // Trigger one-off worker so we don't wait for periodic
        val work = OneTimeWorkRequestBuilder<ImageUploadWorker>().build()
        WorkManager.getInstance(context).enqueue(work)
    }

    override suspend fun analyze(image: LeafSenseImage): List<LeafSenseResult> {
        ensureLoaded()
        val interp = interpreter
        val lbls = labels
        // If model not present yet, return placeholder to indicate fallback.
        if (interp == null || lbls.isEmpty()) {
            return listOf(
                LeafSenseResult("(Model nicht geladen)", 0.0f),
                LeafSenseResult("Demo Healthy", 0.75f, LeafSenseResult.Category.HEALTH)
            )
        }
        val bitmap = when(image) {
            is LeafSenseImage.BitmapRef -> image.bitmap
        }
        // Stage 0 cannabis gating (if enabled & filter model available)
        if (pipelineMode == PipelineMode.TWO_STAGE && stage0Interpreter != null) {
            val accept = runStage0(bitmap)
            lastStage0Prob = accept
            if (accept < stage0SoftFloor) {
                return listOf(LeafSenseResult(noCannabisLabel, accept.coerceIn(0f,1f), LeafSenseResult.Category.STRESS))
            } else if (accept < stage0AcceptThreshold) {
                // Provide both noCannabis and proceed? For clarity return gating label only.
                return listOf(LeafSenseResult(noCannabisLabel, accept.coerceIn(0f,1f), LeafSenseResult.Category.STRESS))
            }
        } else if (pipelineMode == PipelineMode.DIRECT) {
            lastStage0Prob = null
        }
        val resized = resizeCenterCrop(bitmap, inputSize, inputSize)
        val inputBuffer = bitmapToFloatBuffer(resized, inputSize, inputSize, numChannels)
        val output = Array(1) { FloatArray(lbls.size) }
        withContext(Dispatchers.IO) {
            interp.run(inputBuffer, output)
        }
        // Apply temperature scaling if configured
        val logits = output[0]
        val scores = if (temperature != 1.0f) softmaxTemp(logits, temperature) else softmax(logits)
        // Build top-N (for now N = all, sorted)
        val results = lbls.indices.map { idx ->
            val label = lbls[idx]
            val score = scores.getOrNull(idx) ?: 0f
            LeafSenseResult(
                label = labelReadable(label),
                confidence = score.coerceIn(0f,1f),
                category = mapLabelToCategory(label)
            )
        }.sortedByDescending { it.confidence }.take(5)
        // Enqueue top1 if available and image type supports
        (image as? LeafSenseImage.BitmapRef)?.let { bmpRef ->
            results.firstOrNull()?.let { top ->
                enqueuePredictionArtifact(top, bmpRef)
            }
        }
        return results
    }

    private fun runStage0(bitmap: Bitmap): Float {
        val stage0 = stage0Interpreter ?: return 1f // If missing, allow pass-through
        val sz = stage0InputSize
        val resized = resizeCenterCrop(bitmap, sz, sz)
        val buffer = bitmapToFloatBuffer(resized, sz, sz, numChannels)
        val output = Array(1) { FloatArray(2) }
        runCatching { stage0.run(buffer, output) }
        val logits = output[0]
        // Assume index 1 = cannabis probability; if uncertain fallback
        return if (logits.size == 2) {
            val exp0 = kotlin.math.exp(logits[0].toDouble())
            val exp1 = kotlin.math.exp(logits[1].toDouble())
            (exp1 / (exp0 + exp1)).toFloat()
        } else 1f
    }

    // softmax now provided by top-level function in Softmax.kt

    /** Softmax with temperature scaling (T > 0). When T=1, identical to regular softmax. */
    private fun softmaxTemp(values: FloatArray, temp: Float): FloatArray {
        val t = if (temp.isFinite() && temp > 0f) temp else 1f
        if (t == 1f) return softmax(values)
        val scaled = FloatArray(values.size)
        for (i in values.indices) scaled[i] = values[i] / t
        return softmax(scaled)
    }

    private fun labelReadable(raw: String): String {
        return when(raw) {
            // Legacy
            "healthy" -> "Gesund"
            "nitrogen_deficiency" -> "Stickstoff-Mangel"
            "overwatering" -> "Überwässerung"
            "pests_risk" -> "Schädlings-Risiko"
            "leaf_spot" -> "Leaf Spot"
            "ph_imbalance" -> "pH Ungleichgewicht"
            // New taxonomy (rough German localized surface forms)
            "HEALTHY" -> "Gesund"
            "NUTRIENT_DEF_N" -> "N-Mangel"
            "NUTRIENT_DEF_P" -> "P-Mangel"
            "NUTRIENT_DEF_K" -> "K-Mangel"
            "NUTRIENT_DEF_MG" -> "Mg-Mangel"
            "NUTRIENT_DEF_FE" -> "Fe-Mangel"
            "OVERWATER_STRESS" -> "Überwässerung"
            "UNDERWATER_STRESS" -> "Unterwässerung"
            "HEAT_STRESS" -> "Hitze-Stress"
            "COLD_STRESS" -> "Kälte-Stress"
            "LIGHT_BURN" -> "Licht-Burn"
            "FUNGAL_SPOTS_GENERIC" -> "Pilzflecken"
            "MILDEW_LIKE" -> "Mehltau-Verdacht"
            "LEAF_PEST_INDICATOR" -> "Schädlings-Indikator"
            "NECROSIS_EDGE" -> "Randnekrose"
            "GENERAL_CHLOROSIS" -> "Chlorose"
            else -> raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.replace('_',' ')
        }
    }

    private fun mapLabelToCategory(label: String): LeafSenseResult.Category {
        // Support both legacy lowercase labels and new uppercase taxonomy
        val raw = label.trim()
        return when {
            // New taxonomy (uppercase)
            raw == "HEALTHY" -> LeafSenseResult.Category.HEALTH
            raw.startsWith("NUTRIENT_DEF_") || raw == "GENERAL_CHLOROSIS" -> LeafSenseResult.Category.DEFICIENCY
            raw.endsWith("_STRESS") || raw in setOf("LIGHT_BURN") -> LeafSenseResult.Category.STRESS
            raw in setOf("LEAF_PEST_INDICATOR", "FUNGAL_SPOTS_GENERIC", "MILDEW_LIKE") -> LeafSenseResult.Category.PEST
            raw == "NECROSIS_EDGE" -> LeafSenseResult.Category.STRESS
            // Legacy lowercase labels
            raw == "healthy" -> LeafSenseResult.Category.HEALTH
            raw == "nitrogen_deficiency" -> LeafSenseResult.Category.DEFICIENCY
            raw == "overwatering" -> LeafSenseResult.Category.STRESS
            raw == "pests_risk" -> LeafSenseResult.Category.PEST
            else -> LeafSenseResult.Category.STRESS
        }
    }

    private fun resizeCenterCrop(src: Bitmap, targetW: Int, targetH: Int): Bitmap {
        val scale = maxOf(targetW.toFloat() / src.width, targetH.toFloat() / src.height)
        val scaledW = (src.width * scale).toInt()
        val scaledH = (src.height * scale).toInt()
        val scaled = Bitmap.createScaledBitmap(src, scaledW, scaledH, true)
        val xOff = (scaledW - targetW) / 2
        val yOff = (scaledH - targetH) / 2
        return Bitmap.createBitmap(scaled, xOff, yOff, targetW, targetH)
    }

    private fun bitmapToFloatBuffer(bitmap: Bitmap, w: Int, h: Int, channels: Int): ByteBuffer {
        val neededBytes = 4 * w * h * channels
        val buffer = inputBufferRef.get()?.takeIf { it.capacity() >= neededBytes } ?: run {
            val buf = ByteBuffer.allocateDirect(neededBytes).order(ByteOrder.nativeOrder())
            inputBufferRef.set(buf)
            buf
        }
        buffer.rewind()
        val intPixels = pixelArrayRef.get()?.takeIf { it.size >= w * h } ?: run {
            val arr = IntArray(w * h)
            pixelArrayRef.set(arr)
            arr
        }
        bitmap.getPixels(intPixels, 0, w, 0, 0, w, h)
        var i = 0
        val norm = manifest?.normalization
        val mean = norm?.mean
        val std = norm?.std
        val applyNorm = mean != null && std != null && mean.size >= 3 && std.size >= 3 && !mean.any { it.isNaN() } && !std.any { it == 0.0 || it.isNaN() }
        while (i < intPixels.size) {
            val p = intPixels[i]
            val r = (p shr 16 and 0xFF) / 255f
            val g = (p shr 8 and 0xFF) / 255f
            val b = (p and 0xFF) / 255f
            if (applyNorm) {
                buffer.putFloat(((r - mean!![0]) / std!![0]).toFloat())
                buffer.putFloat(((g - mean[1]) / std[1]).toFloat())
                buffer.putFloat(((b - mean[2]) / std[2]).toFloat())
            } else {
                buffer.putFloat(r)
                buffer.putFloat(g)
                buffer.putFloat(b)
            }
            i++
        }
        buffer.rewind()
        return buffer
    }

    private fun loadLabels(assetName: String): List<String> =
        context.assets.open(assetName).bufferedReader().useLines { seq ->
            seq.filter { it.isNotBlank() }.map { it.trim() }.toList()
        }

    private fun loadModel(assetName: String): MappedByteBuffer? = try {
        context.assets.openFd(assetName).use { fd ->
            fd.createInputStream().channel.use { channel ->
                channel.map(FileChannel.MapMode.READ_ONLY, 0, fd.length)
            }
        }
    } catch (e: Exception) { null }

    private fun loadTemperature(assetName: String): Float? = try {
        context.assets.open(assetName).bufferedReader().use { br ->
            val txt = br.readText()
            val obj = org.json.JSONObject(txt)
            when {
                obj.has("temperature") -> obj.optDouble("temperature").toFloat()
                obj.has("T") -> obj.optDouble("T").toFloat()
                else -> null
            }
        }
    } catch (_: Exception) { null }

    private fun sha256(buffer: MappedByteBuffer): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        // Duplicate buffer so position changes don't affect original
        val dup = buffer.duplicate()
        dup.position(0)
        val chunk = ByteArray(1024 * 1024)
        while (dup.hasRemaining()) {
            val toRead = minOf(dup.remaining(), chunk.size)
            dup.get(chunk, 0, toRead)
            md.update(chunk, 0, toRead)
        }
        return md.digest().joinToString("") { ((it.toInt() and 0xFF).toString(16)).padStart(2,'0') }
    }

    suspend fun warmUp(sample: Bitmap? = null) {
        ensureLoaded()
        // Optional one-off dry run if model loaded and sample provided
        if (sample != null && interpreter != null && labels.isNotEmpty()) {
            val resized = resizeCenterCrop(sample, inputSize, inputSize)
            val inputBuffer = bitmapToFloatBuffer(resized, inputSize, inputSize, numChannels)
            val output = Array(1) { FloatArray(labels.size) }
            withContext(Dispatchers.IO) { runCatching { interpreter?.run(inputBuffer, output) } }
        }
    }
    fun isModelReady(): Boolean = interpreter != null && labels.isNotEmpty()
    fun loadedModelName(): String? = selectedModelName
    fun loadedStage0Name(): String? = selectedStage0Name
    fun lastStage0Probability(): Float? = lastStage0Prob
    fun modelVersion(): String? = manifest?.model_version
    fun loadAttempted(): Boolean = loadAttempted
}
