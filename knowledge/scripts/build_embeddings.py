#!/usr/bin/env python3
"""Build embeddings or TF-IDF index for knowledge base.

If sentence-transformers available -> use MiniLM model.
Else fallback to TF-IDF.

Outputs:
  build/embeddings.npy (if transformer)
  build/index.faiss (if faiss present)
  build/tfidf.pkl (fallback)
  build/embedding_meta.json (model + dim + method)

Usage:
  python knowledge/scripts/build_embeddings.py --merged knowledge/build/merged.jsonl
"""
from __future__ import annotations
import argparse, json, sys
from pathlib import Path

try:
    from sentence_transformers import SentenceTransformer  # type: ignore
except ImportError:
    SentenceTransformer = None  # type: ignore

try:
    import numpy as np
except ImportError:
    np = None  # type: ignore

try:
    import faiss  # type: ignore
except ImportError:
    faiss = None  # type: ignore

try:
    import pickle
    from sklearn.feature_extraction.text import TfidfVectorizer  # type: ignore
except ImportError:
    pickle = None  # type: ignore
    TfidfVectorizer = None  # type: ignore


def load_entries(path: Path):
    entries = []
    with path.open('r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            entries.append(json.loads(line))
    return entries


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--merged', required=True)
    ap.add_argument('--out-dir', default='knowledge/build')
    ap.add_argument('--field', default='question', help='Field to embed (question|answer|concat)')
    args = ap.parse_args()

    merged_path = Path(args.merged)
    if not merged_path.exists():
        print('[ERROR] merged file fehlt')
        sys.exit(1)
    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    entries = load_entries(merged_path)
    if not entries:
        print('[ERROR] keine Einträge')
        sys.exit(1)

    texts = []
    for e in entries:
        if args.field == 'question':
            texts.append(e['question'])
        elif args.field == 'answer':
            texts.append(e['answer'])
        else:
            texts.append(e['question'] + '\n' + e['answer'])

    meta = {}

    if SentenceTransformer and np is not None:
        model_name = 'sentence-transformers/all-MiniLM-L6-v2'
        print('[INFO] Lade Embedding Modell:', model_name)
        model = SentenceTransformer(model_name)
        embeddings = model.encode(texts, show_progress_bar=False)
        embeddings = embeddings.astype('float32')
        if faiss is not None:
            index = faiss.IndexFlatIP(embeddings.shape[1])
            index.add(embeddings)
            faiss.write_index(index, (out_dir / 'index.faiss').as_posix())
            print('[OK] FAISS Index geschrieben')
        if np is not None:
            import numpy as np as _np  # noqa
        # Save embeddings raw (optional)
        if np is not None:
            import numpy as _n
            _n.save(out_dir / 'embeddings.npy', embeddings)
        meta = {'method': 'sentence-transformers', 'model': model_name, 'dim': int(embeddings.shape[1])}
    else:
        if TfidfVectorizer is None or pickle is None:
            print('[ERROR] weder sentence-transformers noch sklearn verfügbar')
            sys.exit(1)
        print('[INFO] Fallback TF-IDF')
        vec = TfidfVectorizer(max_features=4096, ngram_range=(1,2))
        mat = vec.fit_transform(texts)
        with open(out_dir / 'tfidf.pkl', 'wb') as f:
            pickle.dump({'vectorizer': vec, 'matrix': mat, 'ids': [e['id'] for e in entries]}, f)
        meta = {'method': 'tfidf', 'features': int(mat.shape[1])}

    with (out_dir / 'embedding_meta.json').open('w', encoding='utf-8') as f:
        json.dump(meta, f, indent=2)
    print('[DONE] Embedding Build:', meta)

if __name__ == '__main__':
    main()
