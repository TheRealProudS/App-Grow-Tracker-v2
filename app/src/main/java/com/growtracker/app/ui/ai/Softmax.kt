package com.growtracker.app.ui.ai

/**
 * Stable softmax implementation for FloatArray.
 * - Subtracts max for numerical stability
 * - Returns zeroed array if input sum underflows
 */
fun softmax(values: FloatArray): FloatArray {
    if (values.isEmpty()) return values
    val max = values.maxOrNull() ?: 0f
    var sum = 0.0
    val exp = DoubleArray(values.size)
    for (i in values.indices) {
        val e = kotlin.math.exp((values[i] - max).toDouble())
        exp[i] = e
        sum += e
    }
    val out = FloatArray(values.size)
    if (sum == 0.0) return out
    for (i in values.indices) out[i] = (exp[i] / sum).toFloat()
    return out
}
