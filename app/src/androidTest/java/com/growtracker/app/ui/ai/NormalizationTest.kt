package com.growtracker.app.ui.ai

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NormalizationTest {

    @Test
    fun normalization_appliesImagenetDefaults() {
        val bmp = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888)
        // Pure red pixel
        bmp.setPixel(0,0, 0xFFFF0000.toInt())
        val mean = listOf(0.485,0.456,0.406)
        val std = listOf(0.229,0.224,0.225)
        val (r,g,b) = PreprocessUtil.extractNormalizedRGB(bmp, mean, std)
        // Raw red (1,0,0) normalized
        val expectedR = ((1f - mean[0]) / std[0]).toFloat()
        val expectedG = ((0f - mean[1]) / std[1]).toFloat()
        val expectedB = ((0f - mean[2]) / std[2]).toFloat()
        assertEquals(expectedR, r, 1e-5f)
        assertEquals(expectedG, g, 1e-5f)
        assertEquals(expectedB, b, 1e-5f)
    }

    @Test
    fun normalization_skipsWhenNoMeanStd() {
        val bmp = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888)
        bmp.setPixel(0,0, 0xFF00FF00.toInt()) // pure green
        val (r,g,b) = PreprocessUtil.extractNormalizedRGB(bmp, null, null)
        assertEquals(0f, r, 1e-6f)
        assertEquals(1f, g, 1e-6f)
        assertEquals(0f, b, 1e-6f)
    }
}
