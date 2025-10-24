package com.growtracker.app.ui.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader

/**
 * Lightweight in-memory knowledge base loader.
 * Expects a JSONL file (one compact JSON object per line) placed under assets (default path from manifest knowledge.merged_file).
 * Only loads subset of fields needed for on-device lookup.
 */
object KnowledgeRepository {

    data class Entry(
        val id: String,
        val question: String,
        val answer: String,
        val tags: List<String> = emptyList(),
        val category: String? = null
    )

    @Volatile private var loaded = false
    private val entries = mutableListOf<Entry>()
    private val inverted = mutableMapOf<String, MutableSet<Int>>()

    suspend fun ensureLoaded(context: Context, assetPath: String): Boolean = withContext(Dispatchers.IO) {
        if (loaded) return@withContext true
        runCatching {
            context.assets.open(assetPath).bufferedReader().use { br -> loadLines(br) }
            loaded = entries.isNotEmpty()
        }.onFailure { loaded = false }
        loaded
    }

    private fun loadLines(br: BufferedReader) {
        var line: String?
        var idx = 0
        while (true) {
            line = br.readLine() ?: break
            if (line.isBlank()) continue
            try {
                val obj = JSONObject(line)
                val id = obj.optString("id", "id_$idx")
                val q = obj.optString("question", "")
                val a = obj.optString("answer", "")
                val tagsArr = obj.optJSONArray("tags")
                val tags = mutableListOf<String>()
                if (tagsArr != null) {
                    for (i in 0 until tagsArr.length()) tags.add(tagsArr.getString(i))
                }
                val catRaw = if (obj.has("category")) obj.optString("category") else null
                val cat = catRaw?.takeIf { it.isNotBlank() }
                val e = Entry(id, q, a, tags, cat)
                entries.add(e)
                indexEntry(idx, e)
                idx++
            } catch (_: Exception) {
                // skip malformed line
            }
        }
    }

    private fun indexEntry(index: Int, e: Entry) {
        fun add(tok: String) { if (tok.length > 2) inverted.getOrPut(tok) { mutableSetOf() }.add(index) }
        (e.question + " " + e.answer).lowercase().split(" ").forEach { raw ->
            val tok = raw.filter { it.isLetterOrDigit() }
            add(tok)
        }
        e.tags.forEach { add(it.lowercase()) }
    }

    fun query(text: String, k: Int = 3): List<Entry> {
        if (entries.isEmpty()) return emptyList()
        val tokens = text.lowercase().split(" ").map { it.filter(Char::isLetterOrDigit) }.filter { it.length > 2 }
        if (tokens.isEmpty()) return emptyList()
        val scores = mutableMapOf<Int, Int>()
        for (tok in tokens) {
            inverted[tok]?.forEach { idx -> scores[idx] = (scores[idx] ?: 0) + 2 }
        }
        return scores.entries.sortedByDescending { it.value }.take(k).map { entries[it.key] }
    }
}
