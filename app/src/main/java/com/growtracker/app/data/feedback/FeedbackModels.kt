package com.growtracker.app.data.feedback

import android.content.Context
import android.graphics.Bitmap
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Feedback domain models for Active Learning preparation.
 */
@Serializable
data class FeedbackRecord(
    val id: String = UUID.randomUUID().toString(),
    val timestampEpochMs: Long,
    val analyzerModelName: String?,
    val analyzerModelVersion: String?,
    val originalLabel: String?,
    val originalConfidence: Float?,
    val correctedLabel: String?,
    val reason: FeedbackReason?,
    val userNote: String?,
    val wasConfirmed: Boolean,
    val imageFileName: String?,
    val pipelineMode: String? = null,
    val stage0Probability: Float? = null,
)

@Serializable
enum class FeedbackReason { WRONG_CLASS, LOW_CONFIDENCE, MULTIPLE_ISSUES, OTHER }

object FeedbackRepository {
    private const val DIR = "feedback"
    private const val IMG_DIR = "images"
    private const val LOG_FILE = "records.jsonl"

    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    private fun baseDir(context: Context): File = File(context.filesDir, DIR).apply { if (!exists()) mkdirs() }
    private fun imageDir(context: Context): File = File(baseDir(context), IMG_DIR).apply { if (!exists()) mkdirs() }
    private fun logFile(context: Context): File = File(baseDir(context), LOG_FILE)

    /**
     * Saves a feedback record + optional bitmap (JPEG). Non-fatal on individual IO failures.
     */
    suspend fun save(context: Context, record: FeedbackRecord, bitmap: Bitmap?) {
        // Write image first (best-effort)
        val imageName = if (bitmap != null) "${record.id}.jpg" else null
        if (bitmap != null) {
            runCatching {
                val outFile = File(imageDir(context), imageName!!)
                FileOutputStream(outFile).use { fos -> bitmap.compress(Bitmap.CompressFormat.JPEG, 82, fos) }
            }.onFailure { /* swallow; image optional */ }
        }
        // Append json line
        val finalRecord = record.copy(imageFileName = imageName)
        runCatching {
            logFile(context).appendText(json.encodeToString(finalRecord) + "\n")
        }
    }
}
