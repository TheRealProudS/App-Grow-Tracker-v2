# Android Knowledge Integration Plan

This document outlines how to surface the knowledge/FAQ bundle on-device using the existing manifest `knowledge` block.

## 1. Manifest Contract (recap)
Example `knowledge` block inside `leafsense_model.json`:
```json
"knowledge": {
  "kb_version": "kb_cannabis_v1",
  "merged_file": "knowledge/build/merged.jsonl",
  "index_file": "knowledge/build/index.faiss",
  "entry_count": 42
}
```
Minimum required for basic functionality: `kb_version` + `merged_file`.

## 2. Packaging Strategy
Option A (Static Assets):
- Copy `knowledge/build/merged.jsonl` into `app/src/main/assets/knowledge/merged.jsonl`.
- (Optional) Copy a compact index (e.g., TF-IDF serialized JSON) for lightweight retrieval.

Option B (Dynamic Download):
- Ship only model manifest with `knowledge` block.
- On first launch: Download bundle (signed + hashed) from remote endpoint.
- Cache under `context.filesDir/knowledge/<kb_version>/`.

Recommended start: Option A (static) for offline reliability.

## 3. File Size Considerations
- JSONL with ~100 entries typically < 200 KB (compressed further if needed).
- FAISS index not ideal for first iteration (native deps); prefer simple in-memory search or TF-IDF JSON.
- Later: Distill embeddings into a small float16 matrix (< 1 MB) + cosine search.

## 4. Retrieval Approach (Phase 1)
1. Load all entries into memory (parse lines).
2. Build an in-memory inverted index (token -> list of entry ids) on first access.
3. Score = token overlap + simple boosting:
   - Exact token in question: +2
   - Token in answer: +1
   - Tag match: +3
4. Return Top-K (default 3–5) with snippet (first 180 chars of answer).

## 5. Kotlin Data Model
```kotlin
@Serializable
data class KnowledgeEntry(
    val id: String,
    val question: String,
    val answer: String,
    val tags: List<String> = emptyList(),
    val category: String? = null,
    val language: String? = null,
    val related: List<String>? = null
)
```
(Strip fields not needed on-device to reduce memory.)

## 6. Loader Utility (Pseudo-Code)
```kotlin
object KnowledgeRepository {
    private var entries: List<KnowledgeEntry> = emptyList()
    private var inverted: MutableMap<String, MutableSet<Int>> = mutableMapOf()

    suspend fun load(context: Context, assetPath: String = "knowledge/merged.jsonl") {
        if (entries.isNotEmpty()) return
        val list = mutableListOf<KnowledgeEntry>()
        context.assets.open(assetPath).bufferedReader().useLines { lines ->
            lines.forEachIndexed { idx, line ->
                if (line.isBlank()) return@forEachIndexed
                val e = json.decodeFromString<KnowledgeEntry>(line)
                list.add(e)
                indexEntry(idx, e)
            }
        }
        entries = list
    }

    private fun indexEntry(idx: Int, e: KnowledgeEntry) {
        fun add(tok: String) { inverted.getOrPut(tok) { mutableSetOf() }.add(idx) }
        (e.question + " " + e.answer).lowercase().split(" ").forEach { t ->
            val tok = t.filter { it.isLetterOrDigit() }
            if (tok.length > 2) add(tok)
        }
        e.tags.forEach { add(it.lowercase()) }
    }

    fun query(q: String, k: Int = 3): List<KnowledgeEntry> {
        val tokens = q.lowercase().split(" ").map { it.filter(Char::isLetterOrDigit) }.filter { it.length > 2 }
        val scores = mutableMapOf<Int, Int>()
        for (tok in tokens) {
            inverted[tok]?.forEach { idx -> scores[idx] = (scores[idx] ?: 0) + 2 }
        }
        // simple category/tag boost optional
        return scores.entries.sortedByDescending { it.value }.take(k).map { entries[it.key] }
    }
}
```

## 7. UI Surfacing
- Attach to results of image classification: show 1–2 relevant FAQ entries based on predicted class tag mapping (e.g., `nitrogen_deficiency` -> search tokens ["nitrogen","mangel"]).
- Provide a dedicated "Wissenssuche" screen with search bar + lazy column.

## 8. Class → FAQ Mapping
Create a mapping file `knowledge/class_mappings.json`:
```json
{
  "nitrogen_deficiency": ["nitrogen", "stickstoff"],
  "overwatering": ["überwässerung", "wurzel"],
  "pests_risk": ["schädlinge", "insekt"]
}
```
On classification event, pass mapped keywords into repository query.

## 9. Caching & Versioning
- Store last loaded `kb_version` (SharedPreferences).
- If manifest version differs → trigger background reload (for dynamic mode).
- Provide a quick validation: if `entry_count` mismatch with parsed entries → mark inconsistent.

## 10. Error Handling
Scenario | Strategy
--- | ---
Missing asset | Fallback: show neutral text "FAQ nicht verfügbar".
Corrupt line | Skip line; continue.
Empty result | Suggest alternative Tokens oder zeigen Popover "Keine Treffer".

## 11. Extension (Phase 2+)
- Add lightweight on-device embedding: Distill to 64-dim vectors; cosine similarity.
- Feedback capture (thumbs up/down) stored local JSON; optional future federated aggregation.
- Multi-Language: Partition entries by `language`, load only matching device locale.

## 12. Build Integration
Add a Gradle copy task (if knowledge artifacts generated outside):
```
tasks.register<Copy>("copyKnowledge") {
    from("knowledge/build/merged.jsonl")
    into("app/src/main/assets/knowledge")
}
preBuild { dependsOn("copyKnowledge") }
```
(Conditionally check file existence.)

## 13. Security / Privacy
- No user queries transmitted (offline first). If adding remote retrieval, anonymize / hash queries.
- Ensure no personally identifiable info inside entries.

## 14. Minimal Acceptance Criteria
- Load + query returns relevant sample entry within < 50 ms after warm load.
- Memory overhead < 2 MB for 100 entries.
- Fallback path if bundle absent.

## 15. Next Implementation Steps
1. Add assets folder `app/src/main/assets/knowledge/` + merge output.
2. Implement `KnowledgeRepository` Kotlin file.
3. Wire classification result → suggestion strip (Composable).
4. Add simple search screen.
5. Add optional instrumentation log (query, resultCount) – debug build only.

---
This plan can now be executed incrementally. Start with static asset + inverted index approach.
