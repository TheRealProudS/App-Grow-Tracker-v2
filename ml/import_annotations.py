#!/usr/bin/env python3
"""Merge human annotations into a base meta CSV.

Assumes an annotations CSV produced by a labeling tool with at least columns:
  image,label

Merges into base meta (matching on image/path/file) and outputs a new enriched CSV.

Usage:
  python ml/import_annotations.py \
    --base snapshots/plants_v1_20250930/splits/train.csv \
    --annotations labeling/round1/labels.csv \
    --out snapshots/plants_v1_20250930/splits/train_enriched.csv \
    --image-col image --label-col label
"""
from __future__ import annotations
import argparse, csv
from pathlib import Path

def load_csv(path: Path):
    with path.open('r', encoding='utf-8') as f:
        r = csv.DictReader(f)
        rows = list(r)
    return rows, r.fieldnames

def write_csv(path: Path, rows, fieldnames):
    with path.open('w', newline='', encoding='utf-8') as f:
        w = csv.DictWriter(f, fieldnames=fieldnames)
        w.writeheader()
        for row in rows:
            w.writerow(row)

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--base', required=True)
    ap.add_argument('--annotations', required=True)
    ap.add_argument('--out', required=True)
    ap.add_argument('--image-col', default='image')
    ap.add_argument('--label-col', default='label')
    ap.add_argument('--output-label-field', default='human_label')
    args = ap.parse_args()

    base_rows, base_fields = load_csv(Path(args.base))
    ann_rows, ann_fields = load_csv(Path(args.annotations))

    # Build index of annotations
    ann_index = {}
    for r in ann_rows:
        key = r.get(args.image_col)
        if key:
            ann_index[key] = r.get(args.label_col)

    out_fields = list(base_fields)
    if args.output_label_field not in out_fields:
        out_fields.append(args.output_label_field)

    merged = []
    missing = 0
    for r in base_rows:
        # Try multiple key names
        img_key = r.get(args.image_col) or r.get('path') or r.get('file')
        label = ann_index.get(img_key)
        if label is None:
            missing += 1
        new_r = dict(r)
        new_r[args.output_label_field] = label
        merged.append(new_r)

    write_csv(Path(args.out), merged, out_fields)
    print(f"[IMPORT] Wrote {len(merged)} rows. Missing annotations: {missing}")

if __name__ == '__main__':
    main()
