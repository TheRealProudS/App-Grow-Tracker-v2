package com.growtracker.app.data.history

import android.content.Context
import android.graphics.Bitmap
import com.growtracker.app.security.SecurityUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Serializable
data class ScanRecord(
    val id: String = UUID.randomUUID().toString(),
    val timestampEpochMs: Long,
    val label: String?,
    val confidence: Float?,
    val pipelineMode: String?,
    val stage0Probability: Float?,
    val modelName: String?,
    val modelVersion: String?,
    val imageFileName: String? = null,
)

object ScanHistoryRepository {
    private const val DIR = "scan_history"
    private const val RECORDS_DIR = "records_enc"
    private const val IMG_DIR = "images_enc"
    private const val LEGACY_LOG_FILE = "history.jsonl" // kept for backward compatibility reads

    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    private fun baseDir(context: Context): File = File(context.filesDir, DIR).apply { if (!exists()) mkdirs() }
    private fun recordsDir(context: Context): File = File(baseDir(context), RECORDS_DIR).apply { if (!exists()) mkdirs() }
    private fun imageDir(context: Context): File = File(baseDir(context), IMG_DIR).apply { if (!exists()) mkdirs() }
    private fun legacyLogFile(context: Context): File = File(baseDir(context), LEGACY_LOG_FILE)

    /** Save one encrypted JSON record and optional encrypted JPEG thumbnail. */
    suspend fun save(context: Context, record: ScanRecord, bitmap: Bitmap?) {
        // Write encrypted JSON record per file
        val recFile = File(recordsDir(context), "${record.id}.json.enc")
        runCatching {
            val payload = json.encodeToString(record)
            SecurityUtils.writeEncryptedText(context, recFile, payload)
        }
        // Write encrypted image if provided
        if (bitmap != null) {
            runCatching {
                val outFile = File(imageDir(context), "${record.id}.jpg.enc")
                val bytes = java.io.ByteArrayOutputStream().use { baos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    baos.toByteArray()
                }
                SecurityUtils.writeEncryptedBytes(context, outFile, bytes)
            }
        }
        // Optional: remove legacy plaintext log to reduce exposure going forward (best-effort)
        runCatching { legacyLogFile(context).delete() }
    }

    /** Load last N records from encrypted store; fallback to legacy JSONL if no encrypted records exist. */
    fun loadRecent(context: Context, maxItems: Int): List<ScanRecord> {
        val recDir = recordsDir(context)
        val files = recDir.listFiles()?.filter { it.isFile && it.name.endsWith(".json.enc") }?.sortedByDescending { it.lastModified() }
        if (!files.isNullOrEmpty()) {
            return files.take(maxItems).mapNotNull { f ->
                runCatching {
                    val txt = SecurityUtils.readDecryptedText(context, f)
                    json.decodeFromString(ScanRecord.serializer(), txt)
                }.getOrNull()
            }
        }
        // Fallback: read legacy plaintext log if present
        val legacy = legacyLogFile(context)
        if (legacy.exists()) {
            return runCatching {
                val lines = legacy.readLines().takeLast(maxItems)
                lines.mapNotNull { line -> runCatching { json.decodeFromString(ScanRecord.serializer(), line) }.getOrNull() }
                    .reversed()
            }.getOrElse { emptyList() }
        }
        return emptyList()
    }
}
