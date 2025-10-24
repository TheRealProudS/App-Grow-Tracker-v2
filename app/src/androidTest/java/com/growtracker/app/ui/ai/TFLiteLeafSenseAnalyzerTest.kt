package com.growtracker.app.ui.ai

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Basic instrumentation tests for TFLiteLeafSenseAnalyzer focusing on:
 * 1. Manifest + knowledge loading (queryKnowledge returns entries)
 * 2. analyze() fallback behavior when no real model file is present
 */
@RunWith(AndroidJUnit4::class)
class TFLiteLeafSenseAnalyzerTest {

    @Test
    fun knowledgeQuery_returnsResults_whenManifestAndKBPresent() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().context
        val analyzer = TFLiteLeafSenseAnalyzer(ctx, modelAssetName = "leafsense_model.tflite", manifestName = "leafsense_model.json")
        val results = analyzer.queryKnowledge("Stickstoff")
        assertTrue("Expected at least one KB match", results.isNotEmpty())
        assertTrue(results.any { it.question.contains("Stickstoff", ignoreCase = true) })
    }

    @Test
    fun analyze_returnsFallback_whenModelMissing() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().context
        val analyzer = TFLiteLeafSenseAnalyzer(ctx, modelAssetName = "leafsense_model.tflite", manifestName = "leafsense_model.json")
        val fakeBitmap = android.graphics.Bitmap.createBitmap(32,32, android.graphics.Bitmap.Config.ARGB_8888)
        val out = analyzer.analyze(LeafSenseImage.BitmapRef(fakeBitmap))
        assertTrue("Should contain placeholder result", out.any { it.label.contains("Model", ignoreCase = true) })
    }
}
