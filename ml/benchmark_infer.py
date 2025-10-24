#!/usr/bin/env python3
"""Inference Benchmark Script

Measures:
- Cold start load time (PyTorch .pt + ONNX)
- Warm inference latency (mean / p50 / p90 / p95 / p99)
- Throughput (images/sec) for batch=1 (focus: on-device style)
- Optional CUDA sync for accurate GPU timing

Usage:
  python ml/benchmark_infer.py \
    --config ml/configs/baseline.yaml \
    --checkpoint ml/outputs/run_001/best.pt \
    --onnx ml/outputs/run_001/model.onnx \
    --repeats 150

Outputs JSON: benchmark_results.json in same dir as checkpoint by default (override with --out).
"""
from __future__ import annotations
import argparse, json, time, statistics, os
from pathlib import Path

import numpy as np

try:
    import torch
except ImportError:
    torch = None  # type: ignore

try:
    import onnxruntime as ort
except ImportError:
    ort = None  # type: ignore

import yaml

def load_config(path: str):
    with open(path, 'r', encoding='utf-8') as f:
        return yaml.safe_load(f)

CLASS_NAMES = [
    'healthy','nitrogen_deficiency','calcium_deficiency','overwatering','underwatering',
    'heat_stress','pest_suspect','fungal_suspect','nutrient_other','unknown'
]

def build_torch_model(name: str, num_classes: int):
    from torch import nn
    from torchvision import models
    if name == 'mobilenet_v3_small':
        m = models.mobilenet_v3_small(weights=models.MobileNet_V3_Small_Weights.DEFAULT)
        m.classifier[3] = nn.Linear(m.classifier[3].in_features, num_classes)
        return m
    return nn.Sequential(
        nn.Conv2d(3,16,3,padding=1), nn.ReLU(), nn.MaxPool2d(2),
        nn.Conv2d(16,32,3,padding=1), nn.ReLU(), nn.MaxPool2d(2),
        nn.AdaptiveAvgPool2d((1,1)), nn.Flatten(), nn.Linear(32,num_classes)
    )

def percentile(lst, p):
    if not lst: return None
    k = (len(lst)-1) * (p/100)
    f = int(np.floor(k)); c = int(np.ceil(k))
    if f == c: return lst[f]
    return lst[f] + (lst[c]-lst[f]) * (k-f)

def benchmark_torch(ckpt_path: Path, cfg, repeats: int, image_size: int, device: str):
    if torch is None:
        return None
    from torch import nn
    model_name = cfg.get('model', {}).get('name', 'mobilenet_v3_small')
    num_classes = cfg.get('model', {}).get('num_classes', len(CLASS_NAMES))
    model = build_torch_model(model_name, num_classes)
    state = torch.load(ckpt_path, map_location='cpu')
    sd = state.get('model', state)
    model.load_state_dict(sd, strict=False)
    start_load = time.time()
    model.eval().to(device)
    load_time = time.time() - start_load
    dummy = torch.randn(1,3,image_size,image_size, device=device)
    timings = []
    # Warmup
    for _ in range(10):
        with torch.no_grad():
            _ = model(dummy)
    torch.cuda.synchronize() if (device=='cuda' and torch.cuda.is_available()) else None
    for _ in range(repeats):
        t0 = time.time()
        with torch.no_grad():
            _ = model(dummy)
        if device=='cuda' and torch.cuda.is_available():
            torch.cuda.synchronize()
        t1 = time.time()
        timings.append((t1 - t0)*1000.0)
    timings.sort()
    return {
        'framework': 'pytorch',
        'device': device,
        'load_ms': load_time*1000.0,
        'mean_ms': statistics.mean(timings),
        'p50_ms': percentile(timings,50),
        'p90_ms': percentile(timings,90),
        'p95_ms': percentile(timings,95),
        'p99_ms': percentile(timings,99),
        'throughput_fps': 1000.0 / statistics.mean(timings)
    }

def benchmark_onnx(onnx_path: Path, repeats: int, image_size: int, providers):
    if ort is None or not onnx_path.exists():
        return None
    t0 = time.time()
    sess = ort.InferenceSession(onnx_path.as_posix(), providers=providers)
    load_ms = (time.time() - t0)*1000.0
    dummy = np.random.randn(1,3,image_size,image_size).astype(np.float32)
    # Warmup
    for _ in range(10):
        _ = sess.run(None, {'input': dummy})
    timings = []
    for _ in range(repeats):
        s = time.time()
        _ = sess.run(None, {'input': dummy})
        e = time.time()
        timings.append((e - s)*1000.0)
    timings.sort()
    return {
        'framework': 'onnxruntime',
        'providers': providers,
        'load_ms': load_ms,
        'mean_ms': statistics.mean(timings),
        'p50_ms': percentile(timings,50),
        'p90_ms': percentile(timings,90),
        'p95_ms': percentile(timings,95),
        'p99_ms': percentile(timings,99),
        'throughput_fps': 1000.0 / statistics.mean(timings)
    }

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--config', required=True)
    ap.add_argument('--checkpoint', required=True)
    ap.add_argument('--onnx', required=False)
    ap.add_argument('--repeats', type=int, default=150)
    ap.add_argument('--device', default='auto', help='cuda | cpu | auto')
    ap.add_argument('--out', default=None)
    args = ap.parse_args()

    cfg = load_config(args.config)
    image_size = cfg.get('model', {}).get('image_size', 224)

    if args.device == 'auto':
        device = 'cuda' if (torch is not None and torch.cuda.is_available()) else 'cpu'
    else:
        device = args.device

    ckpt_path = Path(args.checkpoint)
    if not ckpt_path.exists():
        raise SystemExit(f"Checkpoint nicht gefunden: {ckpt_path}")

    results = {}
    torch_res = benchmark_torch(ckpt_path, cfg, args.repeats, image_size, device)
    if torch_res:
        results['pytorch'] = torch_res
    if args.onnx:
        onnx_path = Path(args.onnx)
        onnx_cpu = benchmark_onnx(onnx_path, args.repeats, image_size, providers=['CPUExecutionProvider'])
        if onnx_cpu:
            results['onnx_cpu'] = onnx_cpu
        # Optional GPU provider
        if ort is not None:
            providers = ort.get_available_providers()
            if 'CUDAExecutionProvider' in providers:
                onnx_gpu = benchmark_onnx(onnx_path, args.repeats, image_size, providers=['CUDAExecutionProvider'])
                if onnx_gpu:
                    results['onnx_cuda'] = onnx_gpu

    out_dir = Path(args.out) if args.out else ckpt_path.parent
    out_dir.mkdir(parents=True, exist_ok=True)
    out_file = out_dir / 'benchmark_results.json'
    with open(out_file, 'w', encoding='utf-8') as f:
        json.dump(results, f, indent=2)
    print('[BENCH] Ergebnisse gespeichert ->', out_file)
    for k,v in results.items():
        print(f"  {k}: mean={v['mean_ms']:.2f}ms p95={v['p95_ms']:.2f}ms fps={v['throughput_fps']:.1f}")

if __name__ == '__main__':
    main()
