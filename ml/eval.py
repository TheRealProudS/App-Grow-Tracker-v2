#!/usr/bin/env python3
"""Evaluation Script for LeafSense Models.

Loads the dataset & config, reconstructs train/val/test split with identical logic
(random_split + same seed), loads best checkpoint, and computes metrics on
validation & test sets. Writes results to eval_results.json.

Usage:
  python ml/eval.py --config ml/configs/baseline.yaml --meta datasets/plant_v1/meta.csv \
    --root datasets/plant_v1/images --checkpoint ml/outputs/run_001/best.pt --out ml/outputs/run_001

Notes:
- Consistency requires identical meta.csv and class list as training.
- If multi-label in Zukunft: adjust dataset & metrics accordingly.
"""
from __future__ import annotations
import argparse, json, os
from pathlib import Path

import numpy as np

try:
    import torch
    from torch.utils.data import random_split, DataLoader
    from torch import nn
except ImportError:
    raise SystemExit("torch nicht installiert – eval nicht möglich.")

from dataset import PlantDataset, build_transforms
from metrics import MetricsTracker
import yaml

def load_config(path: str):
    with open(path, 'r', encoding='utf-8') as f:
        return yaml.safe_load(f)

CLASS_NAMES = [
    'healthy','nitrogen_deficiency','calcium_deficiency','overwatering','underwatering',
    'heat_stress','pest_suspect','fungal_suspect','nutrient_other','unknown'
]

def build_model(name: str, num_classes: int):
    from torchvision import models
    if name == 'mobilenet_v3_small':
        m = models.mobilenet_v3_small(weights=models.MobileNet_V3_Small_Weights.DEFAULT)
        m.classifier[3] = nn.Linear(m.classifier[3].in_features, num_classes)
        return m
    # fallback simple
    return nn.Sequential(
        nn.Conv2d(3,16,3,padding=1), nn.ReLU(), nn.MaxPool2d(2),
        nn.Conv2d(16,32,3,padding=1), nn.ReLU(), nn.MaxPool2d(2),
        nn.AdaptiveAvgPool2d((1,1)), nn.Flatten(), nn.Linear(32,num_classes)
    )

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--config', required=True)
    ap.add_argument('--root', required=True)
    ap.add_argument('--meta', required=True)
    ap.add_argument('--checkpoint', required=True)
    ap.add_argument('--out', required=True, help='Output directory (will write eval_results.json)')
    args = ap.parse_args()

    cfg = load_config(args.config)
    device_pref = cfg.get('experiment', {}).get('device', 'auto')
    if device_pref == 'auto':
        device = 'cuda' if torch.cuda.is_available() else 'cpu'
    else:
        device = device_pref

    train_tf, val_tf = build_transforms(image_size=cfg.get('model', {}).get('image_size', 224), augment_cfg=cfg.get('augment'))
    full_ds = PlantDataset(args.root, args.meta, CLASS_NAMES, transform=train_tf)

    # replicate split ratios (must match train.py)
    val_ratio = 0.15
    test_ratio = 0.15
    n = len(full_ds)
    n_val = int(n * val_ratio)
    n_test = int(n * test_ratio)
    n_train = n - n_val - n_test
    g = torch.Generator().manual_seed(42)
    train_ds, val_ds, test_ds = random_split(full_ds, [n_train, n_val, n_test], generator=g)
    # apply validation transforms for val/test
    if val_tf:
        val_ds.dataset.transform = val_tf  # type: ignore
        test_ds.dataset.transform = val_tf  # type: ignore

    batch_size = cfg.get('train', {}).get('batch_size', 32)
    num_workers = cfg.get('train', {}).get('num_workers', 2)
    val_loader = DataLoader(val_ds, batch_size=batch_size, shuffle=False, num_workers=num_workers)
    test_loader = DataLoader(test_ds, batch_size=batch_size, shuffle=False, num_workers=num_workers)

    model_name = cfg.get('model', {}).get('name', 'mobilenet_v3_small')
    num_classes = cfg.get('model', {}).get('num_classes', len(CLASS_NAMES))
    model = build_model(model_name, num_classes).to(device)

    ckpt_path = Path(args.checkpoint)
    if not ckpt_path.exists():
        raise SystemExit(f"Checkpoint nicht gefunden: {ckpt_path}")
    state = torch.load(ckpt_path, map_location=device)
    sd = state.get('model', state)
    model.load_state_dict(sd, strict=False)

    criterion = torch.nn.CrossEntropyLoss()

    def run(loader):
        model.eval()
        tracker = MetricsTracker(num_classes)
        total_loss = 0.0
        with torch.no_grad():
            for x, y in loader:
                x, y = x.to(device), y.to(device)
                out = model(x)
                loss = criterion(out, y)
                total_loss += float(loss.item()) * x.size(0)
                tracker.update(out, y)
        res = tracker.compute()
        return {
            'loss': total_loss / len(loader.dataset),
            'macro_f1': res.macro_f1,
            'per_class': res.per_class,
            'confusion': res.confusion.tolist()
        }

    val_metrics = run(val_loader)
    test_metrics = run(test_loader)

    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)
    results = {
        'checkpoint': str(ckpt_path),
        'device': device,
        'val': val_metrics,
        'test': test_metrics
    }
    with open(out_dir / 'eval_results.json', 'w', encoding='utf-8') as f:
        json.dump(results, f, indent=2)
    print('[EVAL] Ergebnisse gespeichert ->', out_dir / 'eval_results.json')
    print(f"[VAL] macro_f1={val_metrics['macro_f1']:.4f}  [TEST] macro_f1={test_metrics['macro_f1']:.4f}")

if __name__ == '__main__':
    main()
