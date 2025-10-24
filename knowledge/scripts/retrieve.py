#!/usr/bin/env python3
"""Retrieve top-k FAQ entries.

Usage:
  python knowledge/scripts/retrieve.py --merged knowledge/build/merged.jsonl --query "gelbe blaetter" --k 3

Automatically chooses embedding or TF-IDF depending on available artifacts.
"""
from __future__ import annotations
import argparse, json, sys, math
from pathlib import Path

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
except ImportError:
    pickle = None  # type: ignore

try:
    from sentence_transformers import SentenceTransformer  # type: ignore
except ImportError:
    SentenceTransformer = None  # type: ignore


def load_entries(path: Path):
    entries = []
    with path.open('r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line:
                entries.append(json.loads(line))
    by_id = {e['id']: e for e in entries}
    return entries, by_id


def softmax(x):
    m = max(x)
    ex = [math.exp(v-m) for v in x]
    s = sum(ex)
    return [v/s for v in ex]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--merged', required=True)
    ap.add_argument('--query', required=True)
    ap.add_argument('--k', type=int, default=3)
    ap.add_argument('--field', default='question')
    args = ap.parse_args()

    merged_path = Path(args.merged)
    if not merged_path.exists():
        print('[ERROR] merged fehlt')
        sys.exit(1)

    entries, by_id = load_entries(merged_path)

    # Attempt embedding pipeline first
    embed_meta_path = Path('knowledge/build/embedding_meta.json')
    results = []
    if embed_meta_path.exists() and np is not None:
        meta = json.loads(embed_meta_path.read_text(encoding='utf-8'))
        method = meta.get('method')
        if method == 'sentence-transformers' and SentenceTransformer is not None and faiss is not None:
            model_name = meta['model']
            model = SentenceTransformer(model_name)
            index_path = Path('knowledge/build/index.faiss')
            if index_path.exists():
                index = faiss.read_index(index_path.as_posix())
                q_emb = model.encode([args.query]).astype('float32')
                D, I = index.search(q_emb, args.k)
                for score, idx in zip(D[0], I[0]):
                    if idx < 0 or idx >= len(entries):
                        continue
                    e = entries[idx]
                    results.append({'id': e['id'], 'score': float(score), 'question': e['question']})
        elif method == 'tfidf' and pickle is not None:
            with open('knowledge/build/tfidf.pkl', 'rb') as f:
                obj = pickle.load(f)
            vec = obj['vectorizer']
            mat = obj['matrix']
            ids = obj['ids']
            qv = vec.transform([args.query])
            import numpy as _n  # local alias
            sims = (mat @ qv.T).toarray().ravel()
            top_idx = sims.argsort()[::-1][:args.k]
            for i in top_idx:
                e = by_id[ids[i]]
                results.append({'id': e['id'], 'score': float(sims[i]), 'question': e['question']})

    if not results:
        # Fallback simple substring / token overlap
        q = args.query.lower().split()
        scored = []
        for e in entries:
            text = (e['question'] + ' ' + e.get('answer','')).lower()
            overlap = sum(1 for t in q if t in text)
            if overlap:
                scored.append((overlap, e['id']))
        scored.sort(reverse=True)
        for s, _id in scored[:args.k]:
            results.append({'id': _id, 'score': float(s), 'question': by_id[_id]['question']})

    # Normalize scores with softmax for cosmetic probability-like numbers
    if results:
        scores = [r['score'] for r in results]
        probs = softmax(scores)
        for r, p in zip(results, probs):
            r['prob'] = round(p, 4)
    print(json.dumps({'query': args.query, 'results': results}, ensure_ascii=False, indent=2))

if __name__ == '__main__':
    main()
