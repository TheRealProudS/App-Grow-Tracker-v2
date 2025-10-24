package com.growtracker.app.ui.ai

/**
 * Contract for LeafSense plant health analysis.
 * Later we can provide multiple implementations: on-device TFLite, cloud, hybrid.
 */
interface LeafSenseAnalyzer {
    suspend fun analyze(image: LeafSenseImage): List<LeafSenseResult>
}

/**
 * Wrapper for input images to the analyzer.
 * For now only a Bitmap variant is used; future variants could include raw YUV buffers
 * to avoid an intermediate ARGB conversion or GPU tensor handles.
 */
sealed class LeafSenseImage {
    data class BitmapRef(val bitmap: android.graphics.Bitmap) : LeafSenseImage()
}

data class LeafSenseResult(
    val label: String,
    val confidence: Float, // 0..1
    val category: Category = Category.HEALTH
) {
    enum class Category { HEALTH, DEFICIENCY, STRESS, PEST }
}

/**
 * Dummy implementation returning deterministic sample predictions.
 */
class DummyLeafSenseAnalyzer : LeafSenseAnalyzer {
    override suspend fun analyze(image: LeafSenseImage): List<LeafSenseResult> = when(image) {
        is LeafSenseImage.BitmapRef -> {
            val w = image.bitmap.width
            val h = image.bitmap.height
            val sizeFactor = (w * h).coerceAtMost(2_000_000) / 2_000_000f // 0..1 approx
            listOf(
                LeafSenseResult("Gesund", 0.80f + 0.05f * sizeFactor, LeafSenseResult.Category.HEALTH),
                LeafSenseResult("Leichter Stickstoff-Mangel", 0.40f * (1f - 0.2f * sizeFactor), LeafSenseResult.Category.DEFICIENCY),
                LeafSenseResult("Überwässerung Verdacht", 0.22f, LeafSenseResult.Category.STRESS)
            )
        }
    }
}