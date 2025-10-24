package com.growtracker.app.ui.ai

/** Simple thread-safe holder for the TFLite analyzer instance so other screens (e.g., statistics) can access integrity flags without reflection. */
object AnalyzerHolder {
    @Volatile private var analyzer: TFLiteLeafSenseAnalyzer? = null

    fun set(instance: TFLiteLeafSenseAnalyzer) {
        analyzer = instance
    }

    fun get(): TFLiteLeafSenseAnalyzer? = analyzer
}
