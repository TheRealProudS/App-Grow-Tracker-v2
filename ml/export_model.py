"""Export utilities: PyTorch checkpoint -> TorchScript + ONNX (+ optional TFLite stub).

TFLite conversion of PyTorch models requires an intermediate ONNX -> (tf / tflite) path.
This script prepares artifacts and (optionally) emits calibration representative set indices.
"""
from __future__ import annotations
import argparse, json, hashlib, os, time, platform, sys
from pathlib import Path
import torch
from torchvision import models
import numpy as np


def load_model(ckpt: Path, labels_json: Path):
    labels = json.loads(labels_json.read_text())
    m = models.mobilenet_v3_large(weights=None)
    in_f = m.classifier[0].in_features
    m.classifier[-1] = torch.nn.Linear(in_f, len(labels))
    state = torch.load(ckpt, map_location='cpu')
    key = 'model' if 'model' in state else None
    if key:
        m.load_state_dict(state[key])
    else:
        m.load_state_dict(state)
    m.eval()
    return m, labels


def export_torchscript(model: torch.nn.Module, out_path: Path):
    example = torch.randn(1,3,224,224)
    traced = torch.jit.trace(model, example)
    out_path.parent.mkdir(parents=True, exist_ok=True)
    traced.save(str(out_path))


def export_onnx(model: torch.nn.Module, out_path: Path):
    example = torch.randn(1,3,224,224)
    out_path.parent.mkdir(parents=True, exist_ok=True)
    torch.onnx.export(
        model, example, out_path,
        input_names=['input'], output_names=['logits'],
        opset_version=17,
        dynamic_axes={'input': {0: 'batch'}, 'logits': {0: 'batch'}},
    )


def representative_dataset(list_file: Path, limit: int = 200):
    if not list_file.exists():
        return []
    # Expect lines: path<TAB>label, just return relative paths subset
    lines = [l for l in list_file.read_text().splitlines() if l.strip()]
    np.random.seed(1234)
    np.random.shuffle(lines)
    return [ln.split('\t')[0] for ln in lines[:limit]]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--checkpoint', type=Path, required=True)
    ap.add_argument('--labels-json', type=Path, required=True)
    ap.add_argument('--outdir', type=Path, default=Path('ml/exports'))
    ap.add_argument('--train-list', type=Path, default=Path('ml/data/splits/train.txt'))
    ap.add_argument('--rep-limit', type=int, default=200)
    ap.add_argument('--skip-onnx', action='store_true')
    ap.add_argument('--skip-ts', action='store_true')
    ap.add_argument('--benchmark', action='store_true', help='Run quick inference latency benchmark (CPU) and store in manifest')
    ap.add_argument('--bench-warmup', type=int, default=5)
    ap.add_argument('--bench-iters', type=int, default=30)
    ap.add_argument('--device', type=str, default='cpu', help='Device for benchmark (cpu|cuda). Model export itself is CPU-based for determinism.')
    args = ap.parse_args()

    model, labels = load_model(args.checkpoint, args.labels_json)

    ts_path = args.outdir / 'model_ts.pt'
    onnx_path = args.outdir / 'model.onnx'
    args.outdir.mkdir(parents=True, exist_ok=True)

    generated = {}
    if not args.skip_ts:
        print('Exporting TorchScript ->', ts_path)
        export_torchscript(model, ts_path)
        generated['torchscript'] = ts_path
    if not args.skip_onnx:
        print('Exporting ONNX ->', onnx_path)
        export_onnx(model, onnx_path)
        generated['onnx'] = onnx_path

    rep = representative_dataset(args.train_list, args.rep_limit)
    rep_path = args.outdir / 'representative_set.json'
    rep_path.write_text(json.dumps({'paths': rep}, indent=2))
    labels_out = args.outdir / 'labels.json'
    labels_out.write_text(json.dumps(labels, indent=2, ensure_ascii=False))

    # Provenance / integrity manifest
    def file_hash(p: Path):
        h = hashlib.sha256()
        with p.open('rb') as f:
            for chunk in iter(lambda: f.read(8192), b''):
                h.update(chunk)
        return h.hexdigest()

    bench_stats = None
    if args.benchmark:
        import time as _t
        dev = 'cuda' if (args.device == 'cuda' and torch.cuda.is_available()) else 'cpu'
        model_bench = model.to(dev)
        dummy = torch.randn(1,3,224,224, device=dev)
        # Warmup
        for _ in range(args.bench_warmup):
            _ = model_bench(dummy)
        times = []
        for _ in range(args.bench_iters):
            t0 = _t.perf_counter()
            _ = model_bench(dummy)
            times.append((_t.perf_counter() - t0) * 1000.0)
        import statistics as _stats
        bench_stats = {
            'device': dev,
            'warmup': args.bench_warmup,
            'iters': args.bench_iters,
            'mean_ms': sum(times)/len(times),
            'p50_ms': _stats.median(times),
            'p90_ms': sorted(times)[int(0.9*len(times))-1],
            'p95_ms': sorted(times)[int(0.95*len(times))-1],
            'std_ms': _stats.pstdev(times)
        }

    manifest = {
        'created_utc': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime()),
        'source_checkpoint': str(args.checkpoint),
        'labels_file': str(labels_out.name),
        'export': {
            k: {
                'path': str(v.name),
                'sha256': file_hash(v),
                'bytes': os.path.getsize(v)
            } for k, v in generated.items()
        },
        'representative_set': {
            'count': len(rep),
            'path': rep_path.name
        },
        'model_type': 'mobilenet_v3_large',
        'input_shape': [1,3,224,224],
        'framework': 'pytorch',
        'opset': 17 if 'onnx' in generated else None,
        'environment': {
            'python_version': sys.version.split()[0],
            'platform': platform.platform(),
            'processor': platform.processor(),
            'torch_version': getattr(__import__('torch'), '__version__', 'unknown'),
            'packages': {}
        },
        'benchmark': bench_stats
    }
    # Model stats
    total_params = sum(p.numel() for p in model.parameters())
    trainable_params = sum(p.numel() for p in model.parameters() if p.requires_grad)
    manifest['model_stats'] = {
        'total_params': int(total_params),
        'trainable_params': int(trainable_params)
    }
    # Optionally capture select package versions if installed
    for pkg in ['torchvision','numpy','pillow','scikit_learn','onnx','onnxsim']:
        try:
            mod = __import__(pkg if pkg != 'scikit_learn' else 'sklearn')
            ver = getattr(mod, '__version__', None)
            if ver:
                key = pkg if pkg != 'scikit_learn' else 'sklearn'
                manifest['environment']['packages'][key] = ver
        except Exception:
            continue
    (args.outdir / 'export_config.json').write_text(json.dumps(manifest, indent=2))

    print('Artifacts:')
    if not args.skip_ts:
        print(' - TorchScript:', ts_path)
    if not args.skip_onnx:
        print(' - ONNX:', onnx_path)
    print(' - Representative set (paths only):', len(rep))
    print(' - Labels file saved')
    print(' - Provenance manifest: export_config.json')
    print('\nTFLite conversion stub: Use ONNX -> TF / TFLite externally (e.g. keras2onnx / onnx-tf + tflite converter).')

if __name__ == '__main__':
    main()
