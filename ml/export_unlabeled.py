#!/usr/bin/env python3
"""Export uncertain / candidate images for annotation.

Assumes a meta CSV (with columns at least: image, predictionLabel, top1Score) produced from
inference logging or ingestion enrichment. Selects rows based on uncertainty criteria.

Usage:
  python ml/export_unlabeled.py \
    --meta snapshots/plants_v1_20250930/splits/train.csv \
    --images datasets/plant_v1/images \
    --out export_batch_001 \
    --strategy entropy \
    --limit 200

Strategies:
  - margin      : sort ascending by (p1 - p2) margin (requires columns p1,p2 or probabilities JSON)
  - confidence  : sort ascending by top1Score
  - entropy     : compute entropy from probs column (JSON list in column 'probs')
"""
from __future__ import annotations
import argparse, csv, json, math, shutil
from pathlib import Path

def load_rows(meta_path: Path):
    with meta_path.open('r', encoding='utf-8') as f:
        r = csv.DictReader(f)
        rows = list(r)
    return rows, r.fieldnames


def entropy(probs):
    return -sum(p * math.log(p + 1e-12) for p in probs)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--meta', required=True)
    ap.add_argument('--images', required=True)
    ap.add_argument('--out', required=True)
    ap.add_argument('--strategy', choices=['confidence','margin','entropy'], default='confidence')
    ap.add_argument('--limit', type=int, default=100)
    ap.add_argument('--copy-images', action='store_true')
    args = ap.parse_args()

    rows, fieldnames = load_rows(Path(args.meta))
    if not rows:
        raise SystemExit('No rows in meta')

    # Ensure required fields
    if args.strategy == 'confidence' and 'top1Score' not in fieldnames:
        raise SystemExit('Need top1Score column for confidence strategy')

    def margin_value(r):
        # Accept either explicit p1,p2 columns or JSON in probs
        if 'p1' in r and 'p2' in r:
            try:
                return float(r['p1']) - float(r['p2'])
            except Exception:
                return 1.0
        if 'probs' in r and r['probs'].strip():
            try:
                probs = json.loads(r['probs'])
                sp = sorted(probs, reverse=True)
                if len(sp) >= 2:
                    return sp[0] - sp[1]
            except Exception:
                pass
        # Fallback high margin
        return 1.0

    def entropy_value(r):
        if 'probs' in r and r['probs'].strip():
            try:
                probs = json.loads(r['probs'])
                return entropy(probs)
            except Exception:
                return 0.0
        return 0.0

    if args.strategy == 'confidence':
        rows.sort(key=lambda r: float(r.get('top1Score') or 1.0))
    elif args.strategy == 'margin':
        rows.sort(key=margin_value)
    else:  # entropy
        rows.sort(key=entropy_value, reverse=True)  # higher entropy first

    selected = rows[:args.limit]
    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)
    # Write selection CSV
    sel_path = out_dir / 'selection.csv'
    with sel_path.open('w', newline='', encoding='utf-8') as f:
        w = csv.DictWriter(f, fieldnames=fieldnames)
        w.writeheader()
        for r in selected:
            w.writerow(r)

    if args.copy_images:
        img_root = Path(args.images)
        imgs_out = out_dir / 'images'
        imgs_out.mkdir(exist_ok=True)
        copied = 0
        for r in selected:
            img_rel = r.get('image') or r.get('path') or r.get('file')
            if not img_rel:
                continue
            src = img_root / img_rel
            if src.exists():
                dst = imgs_out / Path(img_rel).name
                dst.parent.mkdir(parents=True, exist_ok=True)
                try:
                    shutil.copy2(src, dst)
                    copied += 1
                except Exception:
                    pass
        print(f"[EXPORT] Copied {copied} images")

    print(f"[EXPORT] Selection written: {sel_path} ({len(selected)} rows)")

if __name__ == '__main__':
    main()
