# Knowledge Base (FAQ / Guidance)

This module provides a lightweight, offline-capable FAQ / guidance system for plant (and optionally cannabis-specific) health, care, and diagnostic information.

## Goals
- Curated, versioned entries (immutable history)
- Structured schema for consistent ingestion
- Vector index for semantic retrieval (optional fallback: TF‑IDF)
- Manifest linkage so the app knows which knowledge bundle it ships with
- Clear licensing & source attribution

## Directory Structure
```
knowledge/
  README.md
  schema.json          # JSON Schema for entries
  entries/             # Source entries (one JSON or Markdown per entry)
  build/               # Generated artifacts (merged.jsonl, embeddings, index)
  scripts/             # Ingestion & retrieval scripts
```

## Entry Anatomy
Each entry captures a single Q&A or a focused guidance topic.

Core required fields:
- id (string, unique, stable)
- question (string) – user-facing phrasing
- answer (markdown string) – rich text
- tags (array[string]) – e.g. ["nutrient","nitrogen"]
- category (string) – e.g. "deficiency", "pest", "environment"
- sources (array[object]) – { name, url, license }
- created_utc (ISO 8601)
- version (int) – increment when content meaningfully changes
- language (ISO 639-1) – e.g. "de" or "en"
- disclaimers (array[string]) – safety / legal notes
- related (array[string]) – other entry ids

Optional fields:
- stage (string) – plant growth phase
- severity_levels (array[string]) – e.g. ["mild","moderate","severe"]
- remediation_steps (array[string]) – ordered actionable steps
- detection_signals (array[string]) – visual / environmental cues

## Build Pipeline
1. Author entries in `entries/` (JSON or Markdown with front‑matter)
2. Run `scripts/ingest.py` → Validates & produces `build/merged.jsonl`
3. Run `scripts/build_embeddings.py` → Generates `build/embeddings.npy` + `build/index.faiss` (if faiss available) or `tfidf.pkl`
4. Run `scripts/retrieve.py --query "..."` for local testing

## Manifest Integration
Later the model manifest may embed:
```
"knowledge": {
  "kb_version": "cannabis_v1",
  "entry_count": 42,
  "index_file": "knowledge/build/index.faiss",
  "merged_file": "knowledge/build/merged.jsonl"
}
```

## Licensing & Compliance
- Track every source; if a source forbids model training, do not include.
- Provide a `NOTICE.md` if required by upstream licenses.

## Roadmap
- Add multilingual support
- Confidence scoring & reranking
- On-device distilled embedding model
- Feedback loop (thumbs up/down) persistence
