package com.growtracker.app.ui.ai

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation test for runtime model integrity flags.
 * Will skip (pass) if manifest or model not present in assets.
 */
@RunWith(AndroidJUnit4::class)
class ModelIntegrityTest {
    @Test
    fun testIntegrityFlagsIfModelPresent() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val analyzer = TFLiteLeafSenseAnalyzer(ctx)
        // Trigger a lightweight knowledge query to force lazy load without requiring an image
        runCatching { analyzer.queryKnowledge("healthy", 1) }
        val hash = analyzer.modelRuntimeHash()
        val verified = analyzer.modelIntegrityVerified()
        val mismatch = analyzer.modelIntegrityMismatch()
        // If no hash present in manifest, we accept null hash & verified=false
        if (hash == null) {
            Assert.assertFalse("Mismatch shouldn't be true when hash is absent", mismatch)
        } else {
            // We have a runtime hash; either it's verified or mismatch flagged
            Assert.assertTrue("Either verified or mismatch must be set when hash computed", verified || mismatch)
            if (verified) {
                Assert.assertFalse("Cannot be both verified and mismatch", mismatch)
            }
        }
    }
}
