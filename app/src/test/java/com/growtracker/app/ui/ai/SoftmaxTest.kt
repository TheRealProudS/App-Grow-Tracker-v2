package com.growtracker.app.ui.ai

import org.junit.Assert.*
import org.junit.Test

class SoftmaxTest {

    @Test
    fun softmax_sumIsOne() {
        val probs = softmax(floatArrayOf(1f, 2f, 3f))
        val sum = probs.sum()
        assertTrue("Sum should be ~1 but was $sum", kotlin.math.abs(1f - sum) < 1e-5)
    }

    @Test
    fun softmax_orderPreserved() {
        val input = floatArrayOf(-2f, 0f, 5f, 1f)
        val probs = softmax(input)
        val maxIdxInput = input.indices.maxBy { input[it] }
        val maxIdxProbs = probs.indices.maxBy { probs[it] }
        assertEquals(maxIdxInput, maxIdxProbs)
    }

    @Test
    fun softmax_emptyInputReturnsSame() {
        val arr = floatArrayOf()
        val out = softmax(arr)
        assertTrue(out.isEmpty())
    }

    @Test
    fun softmax_handlesLargeValuesWithoutNaN() {
        val arr = floatArrayOf(1000f, 999f, 998f)
        val out = softmax(arr)
        assertFalse(out.any { it.isNaN() })
        val sum = out.sum()
        assertTrue(kotlin.math.abs(sum - 1f) < 1e-5)
    }
}
