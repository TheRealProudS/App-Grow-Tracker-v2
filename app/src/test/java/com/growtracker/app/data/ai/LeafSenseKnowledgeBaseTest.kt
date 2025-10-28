package com.growtracker.app.data.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class LeafSenseKnowledgeBaseTest {

    @Test
    fun lookup_exact_and_caseInsensitive() {
        val e1 = LeafSenseKnowledgeBase.lookup("Healthy Leaf")
        val e2 = LeafSenseKnowledgeBase.lookup("healthy leaf")
        assertNotNull(e1)
        assertNotNull(e2)
        assertEquals(e1, e2)
        assertEquals(Category.HEALTH, e1!!.category)
    }

    @Test
    fun lookup_fallback_startsWith_and_contains() {
        // startsWith
        val e1 = LeafSenseKnowledgeBase.lookup("Nutri")
        assertNotNull(e1)
        // contains (should find e.g., LIGHT_BURN by "burn")
        val e2 = LeafSenseKnowledgeBase.lookup("burn")
        assertNotNull(e2)
        assertEquals("LIGHT_BURN", e2!!.label)
    }

    @Test
    fun loadFromJson_replacesEntries_and_setsMetadata() = runBlocking {
        val json = """
            {"version":"1.2","entries":[
                {"label":"Test Label","category":"HEALTH","short":"ok","symptoms":[],"causes":[],"actions":[],"priority":"LOW","tags":[]}
            ]}
        """.trimIndent()
        LeafSenseKnowledgeBase.loadFromJson(json, newSource = "test-asset", newVersion = "1.2")
        val all = LeafSenseKnowledgeBase.all()
        assertEquals(1, all.size)
        assertEquals("1.2", LeafSenseKnowledgeBase.version)
        assertEquals("test-asset", LeafSenseKnowledgeBase.source)
        val e = LeafSenseKnowledgeBase.lookup("Test Label")
        assertNotNull(e)
        assertEquals(Category.HEALTH, e!!.category)
    }
}