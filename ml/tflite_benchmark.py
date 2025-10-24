"""Simple TFLite latency benchmark.

Usage:
  python ml/tflite_benchmark.py --model path/to/model.tflite --warmup 10 --iters 100 --json-out bench.json

Optional:
  --delegate nnapi | xnnpack | gpu  (falls supported)
  --input-shape 1,224,224,3  (NHWC; default 1,224,224,3)

Outputs summary stats (mean, p50, p90, p95, std) and optionally JSON.
"""
from __future__ import annotations
import argparse, json, statistics, time, sys
from pathlib import Path

try:
    import numpy as np
    import tensorflow as tf
except Exception as e:  # pragma: no cover
    print('ERROR: Requires tensorflow + numpy installed:', e)
    sys.exit(1)


def parse_shape(s: str):
    parts = [int(p) for p in s.split(',')]
    return parts

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--model', type=Path, required=True)
    ap.add_argument('--warmup', type=int, default=10)
    ap.add_argument('--iters', type=int, default=50)
    ap.add_argument('--input-shape', type=str, default='1,224,224,3')
    ap.add_argument('--delegate', type=str, choices=['nnapi','xnnpack','gpu'], help='Optional delegate')
    ap.add_argument('--json-out', type=Path)
    args = ap.parse_args()

    if not args.model.exists():
        print('Model file not found:', args.model)
        sys.exit(1)

    shape = parse_shape(args.input_shape)
    if len(shape) != 4:
        print('Input shape must be 4D NHWC.')
        sys.exit(2)

    delegates = []
    if args.delegate == 'nnapi':
        try:
            delegates.append(tf.lite.experimental.load_delegate('libnnapi_delegate.so'))
        except Exception:
            print('WARNING: NNAPI delegate not available; continuing w/o delegate.')
    elif args.delegate == 'xnnpack':
        # XNNPACK enabled by default; can adjust threads if needed
        pass
    elif args.delegate == 'gpu':
        try:
            delegates.append(tf.lite.experimental.load_delegate('libtensorflowlite_gpu_delegate.so'))
        except Exception:
            print('WARNING: GPU delegate not available; continuing CPU.')

    interpreter = tf.lite.Interpreter(model_path=str(args.model), experimental_delegates=delegates or None, num_threads=1)
    interpreter.allocate_tensors()
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    if len(input_details) != 1:
        print('Only single-input models supported in this simple benchmark.')
        sys.exit(3)

    in_idx = input_details[0]['index']
    expected_shape = input_details[0]['shape']
    if list(expected_shape) != shape:
        print(f'WARNING: Provided shape {shape} != model input shape {list(expected_shape)}; using model shape.')
        shape = list(expected_shape)

    dummy = (np.random.rand(*shape).astype(input_details[0]['dtype'].as_numpy_dtype))

    def run_once():
        interpreter.set_tensor(in_idx, dummy)
        interpreter.invoke()
        return [interpreter.get_tensor(o['index']) for o in output_details]

    # Warmup
    for _ in range(args.warmup):
        run_once()

    times = []
    for _ in range(args.iters):
        t0 = time.perf_counter()
        run_once()
        times.append((time.perf_counter() - t0) * 1000.0)

    times_sorted = sorted(times)
    mean_ms = sum(times)/len(times)
    p50 = statistics.median(times)
    p90 = times_sorted[int(0.9*len(times))-1]
    p95 = times_sorted[int(0.95*len(times))-1]
    std = statistics.pstdev(times)

    summary = {
        'model': str(args.model),
        'delegate': args.delegate or 'none',
        'warmup': args.warmup,
        'iters': args.iters,
        'mean_ms': mean_ms,
        'p50_ms': p50,
        'p90_ms': p90,
        'p95_ms': p95,
        'std_ms': std
    }

    print(json.dumps(summary, indent=2))
    if args.json_out:
        args.json_out.parent.mkdir(parents=True, exist_ok=True)
        args.json_out.write_text(json.dumps(summary, indent=2))

if __name__ == '__main__':
    main()
