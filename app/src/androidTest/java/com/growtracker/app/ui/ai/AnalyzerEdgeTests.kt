package com.growtracker.app.ui.ai

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class AnalyzerEdgeTests {

    private fun ctx() = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun missingModelFile_manifestStillLoadsClasses_andFallbackAnalyze() = runBlocking {
        val analyzer = TFLiteLeafSenseAnalyzer(
            context = ctx(),
            modelAssetName = "does_not_exist.tflite",
            manifestName = "edge/leafsense_model_missing_model.json"
        )
        val bmp = Bitmap.createBitmap(8,8, Bitmap.Config.ARGB_8888)
        val out = analyzer.analyze(LeafSenseImage.BitmapRef(bmp))
        assertTrue(out.isNotEmpty())
        assertTrue(out.any { it.label.contains("Model", ignoreCase = true) })
    }

    @Test
    fun corruptedKnowledge_linesSkipped_validEntriesRemain() = runBlocking {
        val analyzer = TFLiteLeafSenseAnalyzer(ctx(), manifestName = "edge/leafsense_model_missing_model.json")
        // Trigger knowledge load
        val results = analyzer.queryKnowledge("Stickstoff")
        // Even with one malformed line we should get at least one valid entry
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun concurrency_analyzeAndQuery_noCrash() = runBlocking {
        val analyzer = TFLiteLeafSenseAnalyzer(ctx(), manifestName = "edge/leafsense_model_missing_model.json")
        val bmp = Bitmap.createBitmap(16,16, Bitmap.Config.ARGB_8888)
        val jobs = (1..10).map {
            launch(Dispatchers.Default) {
                repeat(5) {
                    analyzer.queryKnowledge("healthy")
                    analyzer.analyze(LeafSenseImage.BitmapRef(bmp))
                }
            }
        }
        jobs.joinAll()
        // If we reach here without exception it's ok
        assertTrue(true)
    }

    @Test
    fun performance_fallbackAnalyzeUnderThreshold() = runBlocking {
        val analyzer = TFLiteLeafSenseAnalyzer(ctx(), manifestName = "edge/leafsense_model_missing_model.json")
        val bmp = Bitmap.createBitmap(32,32, Bitmap.Config.ARGB_8888)
        // Warmup
        analyzer.analyze(LeafSenseImage.BitmapRef(bmp))
        val runs = 25
        var totalMs = 0L
        repeat(runs) {
            val t = measureTimeMillis { runBlocking { analyzer.analyze(LeafSenseImage.BitmapRef(bmp)) } }
            totalMs += t
        }
        val avg = totalMs / runs.toDouble()
        // Fallback path should be very fast (< 15ms on host/emulator typically). Allow generous threshold 40ms.
        assertTrue("Average fallback analyze too slow: $avg ms", avg < 40.0)
    }
}
