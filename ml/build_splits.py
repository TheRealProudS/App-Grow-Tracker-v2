"""Build stratified train/val/test splits from metadata JSONL.

Expected input:
  ml/data/metadata/metadata.jsonl  (one JSON object per line)
Each object MUST contain at least: {"path": relative image path from data root, "label": class label}
Optional fields: split (if pre-assigned), weight, stage, notes.

Outputs:
  ml/data/splits/train.txt
  ml/data/splits/val.txt
  ml/data/splits/test.txt
Each line: <relative_path>\t<label>

Logs stats to stdout + ml/data/splits/stats.json
"""
from __future__ import annotations
import argparse, json, math, random
from pathlib import Path
from collections import defaultdict, Counter

RANDOM_SEED = 42

def load_records(meta_path: Path):
    for line in meta_path.read_text(encoding='utf-8').splitlines():
        if not line.strip():
            continue
        rec = json.loads(line)
        if 'path' not in rec or 'label' not in rec:
            continue
        yield rec

def stratified_split(items, labels, ratios):
    # ratios: (train, val, test)
    label_to_items = defaultdict(list)
    for it, lbl in zip(items, labels):
        label_to_items[lbl].append(it)
    splits = {'train': [], 'val': [], 'test': []}
    for lbl, bucket in label_to_items.items():
        random.shuffle(bucket)
        n = len(bucket)
        n_train = int(n * ratios[0])
        n_val = int(n * ratios[1])
        # ensure at least 1 sample flows if possible
        remaining = n - n_train - n_val
        n_test = remaining
        # Adjust for rounding drift
        if n_train + n_val + n_test < n:
            n_test += n - (n_train + n_val + n_test)
        splits['train'].extend(bucket[:n_train])
        splits['val'].extend(bucket[n_train:n_train+n_val])
        splits['test'].extend(bucket[n_train+n_val:n_train+n_val+n_test])
    return splits


def write_split(file_path: Path, entries):
    file_path.parent.mkdir(parents=True, exist_ok=True)
    with file_path.open('w', encoding='utf-8') as f:
        for e in entries:
            f.write(f"{e['path']}\t{e['label']}\n")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--meta', type=Path, default=Path('ml/data/metadata/metadata.jsonl'))
    ap.add_argument('--outdir', type=Path, default=Path('ml/data/splits'))
    ap.add_argument('--train-ratio', type=float, default=0.70)
    ap.add_argument('--val-ratio', type=float, default=0.15)
    ap.add_argument('--test-ratio', type=float, default=0.15)
    ap.add_argument('--min-per-class', type=int, default=5, help='Warn if class count below this')
    args = ap.parse_args()

    assert math.isclose(args.train_ratio + args.val_ratio + args.test_ratio, 1.0, rel_tol=1e-3), 'Ratios must sum to 1.'

    random.seed(RANDOM_SEED)

    records = list(load_records(args.meta))
    if not records:
        raise SystemExit('No valid records loaded.')

    # Pre-filter invalid or missing labels
    items = [r for r in records if r.get('label')]
    labels = [r['label'] for r in items]

    class_counts = Counter(labels)
    for lbl, cnt in class_counts.items():
        if cnt < args.min_per_class:
            print(f"WARNING: class '{lbl}' has only {cnt} samples (< {args.min_per_class})")

    splits = stratified_split(items, labels, (args.train_ratio, args.val_ratio, args.test_ratio))

    for split_name in ('train','val','test'):
        write_split(args.outdir / f'{split_name}.txt', splits[split_name])

    stats = {
        'total': len(items),
        'classes': class_counts,
        'splits': {k: len(v) for k,v in splits.items()},
        'ratio_actual': {k: len(v)/len(items) for k,v in splits.items()}
    }
    (args.outdir / 'stats.json').write_text(json.dumps(stats, indent=2, ensure_ascii=False))
    print(json.dumps(stats, indent=2, ensure_ascii=False))

if __name__ == '__main__':
    main()
