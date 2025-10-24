#!/usr/bin/env python3
"""Build a dataset snapshot manifest from a meta CSV and images directory.

Generates:
  <out>/dataset_manifest.json
  <out>/splits/train.csv, val.csv, test.csv

Basic stratified split (if class column given) else random.

Usage:
  python ml/build_dataset_snapshot.py \
    --images datasets/plant_v1/images \
    --meta datasets/plant_v1/meta.csv \
    --out snapshots/plants_v1_20250930 \
    --class-col label \
    --val-ratio 0.15 --test-ratio 0.10
"""
from __future__ import annotations
import argparse, csv, json, hashlib, random
from pathlib import Path
from collections import defaultdict

RANDOM_SEED = 42


def sha256_file(p: Path) -> str:
    h = hashlib.sha256()
    with p.open('rb') as f:
        for chunk in iter(lambda: f.read(1<<20), b''):
            h.update(chunk)
    return h.hexdigest()


def load_meta(meta_path: Path):
    with meta_path.open('r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        rows = list(reader)
    return rows


def stratified_split(rows, class_col, val_ratio, test_ratio):
    random.seed(RANDOM_SEED)
    by_class = defaultdict(list)
    for r in rows:
        by_class[r[class_col]].append(r)
    train, val, test = [], [], []
    for cls, items in by_class.items():
        random.shuffle(items)
        n = len(items)
        n_test = int(n * test_ratio)
        n_val = int(n * val_ratio)
        test.extend(items[:n_test])
        val.extend(items[n_test:n_test+n_val])
        train.extend(items[n_test+n_val:])
    return train, val, test


def random_split(rows, val_ratio, test_ratio):
    random.seed(RANDOM_SEED)
    rows = rows[:]
    random.shuffle(rows)
    n = len(rows)
    n_test = int(n * test_ratio)
    n_val = int(n * val_ratio)
    test = rows[:n_test]
    val = rows[n_test:n_test+n_val]
    train = rows[n_test+n_test:]
    return train, val, test


def write_csv(path: Path, rows, fieldnames):
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open('w', newline='', encoding='utf-8') as f:
        w = csv.DictWriter(f, fieldnames=fieldnames)
        w.writeheader()
        for r in rows:
            w.writerow(r)


def compute_class_stats(rows, class_col):
    stats = defaultdict(int)
    for r in rows:
        stats[r[class_col]] += 1
    return {k: stats[k] for k in sorted(stats)}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--images', required=True)
    ap.add_argument('--meta', required=True)
    ap.add_argument('--out', required=True)
    ap.add_argument('--class-col', default=None)
    ap.add_argument('--val-ratio', type=float, default=0.15)
    ap.add_argument('--test-ratio', type=float, default=0.10)
    ap.add_argument('--min-per-class', type=int, default=0)
    ap.add_argument('--hash-limit', type=int, default=200, help='Limit number of files to hash for integrity sample (0=off)')
    args = ap.parse_args()

    images_dir = Path(args.images)
    meta_path = Path(args.meta)
    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)

    rows = load_meta(meta_path)
    if not rows:
        raise SystemExit('Meta CSV empty')

    fieldnames = rows[0].keys()

    if args.class_col and args.class_col not in fieldnames:
        raise SystemExit(f"class column {args.class_col} not in meta header")

    if args.class_col:
        train, val, test = stratified_split(rows, args.class_col, args.val_ratio, args.test_ratio)
        class_stats_all = compute_class_stats(rows, args.class_col)
        class_stats_train = compute_class_stats(train, args.class_col)
    else:
        train, val, test = random_split(rows, args.val_ratio, args.test_ratio)
        class_stats_all = None
        class_stats_train = None

    # Basic constraints
    if args.min_per_class and class_stats_train:
        viol = {c:n for c,n in class_stats_train.items() if n < args.min_per_class}
        if viol:
            print(f"[WARN] Classes below min-per-class: {viol}")

    splits_dir = out_dir / 'splits'
    write_csv(splits_dir / 'train.csv', train, fieldnames)
    write_csv(splits_dir / 'val.csv', val, fieldnames)
    write_csv(splits_dir / 'test.csv', test, fieldnames)

    sample_hashes = []
    if args.hash_limit > 0:
        # Hash up to hash_limit image files (first subset) for spot integrity
        count = 0
        for r in rows:
            img_rel = r.get('image') or r.get('path') or r.get('file')
            if not img_rel:
                continue
            p = images_dir / img_rel
            if p.exists():
                sample_hashes.append({'file': img_rel, 'sha256': sha256_file(p)})
                count += 1
                if count >= args.hash_limit:
                    break

    manifest = {
        'dataset_version': out_dir.name,
        'total': len(rows),
        'splits': {
            'train': len(train),
            'val': len(val),
            'test': len(test)
        },
        'class_column': args.class_col,
        'class_distribution': class_stats_all,
        'train_class_distribution': class_stats_train,
        'val_ratio': args.val_ratio,
        'test_ratio': args.test_ratio,
        'seed': RANDOM_SEED,
        'sample_hashes': sample_hashes,
    }
    (out_dir / 'dataset_manifest.json').write_text(json.dumps(manifest, indent=2), encoding='utf-8')
    print(f"[SNAPSHOT] Created dataset snapshot in {out_dir}")

if __name__ == '__main__':
    main()
