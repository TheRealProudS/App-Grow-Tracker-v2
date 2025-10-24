package com.growtracker.app.ui.ai

import android.graphics.Bitmap

/** Utility functions to aid testing of normalization logic */
object PreprocessUtil {
    /**
     * Extracts normalized RGB triple from a 1x1 bitmap using provided mean/std (ImageNet defaults if null).
     * If mean/std null -> returns raw 0..1 scaled values.
     */
    fun extractNormalizedRGB(
        bitmap: Bitmap,
        mean: List<Double>? = null,
        std: List<Double>? = null
    ): Triple<Float,Float,Float> {
        require(bitmap.width == 1 && bitmap.height == 1) { "Bitmap must be 1x1 for probe" }
        val p = bitmap.getPixel(0,0)
        val r = (p shr 16 and 0xFF) / 255f
        val g = (p shr 8 and 0xFF) / 255f
        val b = (p and 0xFF) / 255f
        return if (mean != null && std != null && mean.size >=3 && std.size >=3) {
            Triple(
                ((r - mean[0]) / std[0]).toFloat(),
                ((g - mean[1]) / std[1]).toFloat(),
                ((b - mean[2]) / std[2]).toFloat()
            )
        } else Triple(r,g,b)
    }
}
