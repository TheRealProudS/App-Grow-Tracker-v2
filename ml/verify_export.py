"""Verify integrity of exported model artifacts using export_config.json manifest.

Usage:
  python ml/verify_export.py --manifest ml/exports/run_123/export_config.json
Exit codes:
 0 OK
 1 Missing file
 2 Hash mismatch
 3 Manifest parse error / invalid format
"""
from __future__ import annotations
import argparse, json, hashlib, os, sys, time
from pathlib import Path


def sha256_file(p: Path) -> str:
    h = hashlib.sha256()
    with p.open('rb') as f:
        for chunk in iter(lambda: f.read(8192), b''):
            h.update(chunk)
    return h.hexdigest()

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--manifest', type=Path, required=True)
    ap.add_argument('--json-out', type=Path, help='Optional path to write JSON summary for CI parsing')
    ap.add_argument('--baseline', type=Path, help='Optional export_config.json to diff environment against')
    ap.add_argument('--fail-on-env-diff', action='store_true', help='Exit with code 4 if environment provenance differs from baseline')
    args = ap.parse_args()

    try:
        data = json.loads(args.manifest.read_text())
    except Exception as e:
        print('ERROR: cannot parse manifest:', e)
        sys.exit(3)

    if 'export' not in data or not isinstance(data['export'], dict):
        print('ERROR: invalid manifest structure (missing export)')
        sys.exit(3)

    base_dir = args.manifest.parent
    results = {}
    for name, meta in data['export'].items():
        path = base_dir / meta['path']
        if not path.exists():
            print(f'MISSING: {name} -> {path}')
            results[name] = {'status': 'missing'}
            if args.json_out:
                summary = {'ok': False, 'error': 'missing_file', 'artifact': name, 'timestamp': time.time(), 'artifacts': results}
                args.json_out.parent.mkdir(parents=True, exist_ok=True)
                args.json_out.write_text(json.dumps(summary, indent=2))
            sys.exit(1)
        actual = sha256_file(path)
        expected = meta.get('sha256')
        if expected != actual:
            print(f'HASH MISMATCH: {name} expected {expected} got {actual}')
            results[name] = {'status': 'hash_mismatch', 'expected': expected, 'actual': actual}
            if args.json_out:
                summary = {'ok': False, 'error': 'hash_mismatch', 'artifact': name, 'timestamp': time.time(), 'artifacts': results}
                args.json_out.parent.mkdir(parents=True, exist_ok=True)
                args.json_out.write_text(json.dumps(summary, indent=2))
            sys.exit(2)
        else:
            print(f'OK {name}: {path.name} ({meta.get("bytes","?")} bytes)')
            results[name] = {'status': 'ok', 'bytes': meta.get('bytes'), 'sha256': expected}

    print('All artifacts verified.')

    env_diff = None
    if args.baseline and args.baseline.exists():
        try:
            baseline = json.loads(args.baseline.read_text())
            cur_env = data.get('environment') or {}
            base_env = baseline.get('environment') or {}
            # Compare key fields
            keys = sorted(set(cur_env.keys()) | set(base_env.keys()))
            differences = {}
            for k in keys:
                if cur_env.get(k) != base_env.get(k):
                    differences[k] = {'current': cur_env.get(k), 'baseline': base_env.get(k)}
            # Deep compare packages if present
            cur_pkgs = cur_env.get('packages', {})
            base_pkgs = base_env.get('packages', {})
            pkg_keys = sorted(set(cur_pkgs.keys()) | set(base_pkgs.keys()))
            pkg_diff = {}
            for pk in pkg_keys:
                if cur_pkgs.get(pk) != base_pkgs.get(pk):
                    pkg_diff[pk] = {'current': cur_pkgs.get(pk), 'baseline': base_pkgs.get(pk)}
            if differences or pkg_diff:
                env_diff = {'fields': differences, 'packages': pkg_diff}
                print('Environment differences detected:')
                if differences:
                    for k,v in differences.items():
                        print(f'  {k}: current={v["current"]} baseline={v["baseline"]}')
                if pkg_diff:
                    for k,v in pkg_diff.items():
                        print(f'  pkg {k}: current={v["current"]} baseline={v["baseline"]}')
                if args.fail_on_env_diff:
                    # Write json summary before exit if required
                    if args.json_out:
                        summary = {'ok': False, 'error': 'env_diff', 'env_diff': env_diff, 'artifacts': results}
                        args.json_out.parent.mkdir(parents=True, exist_ok=True)
                        args.json_out.write_text(json.dumps(summary, indent=2))
                    sys.exit(4)
            else:
                print('Environment provenance matches baseline.')
        except Exception as e:
            print('WARNING: baseline comparison failed:', e)

    if args.json_out:
        summary = {'ok': True, 'timestamp': time.time(), 'artifacts': results, 'env_diff': env_diff}
        args.json_out.parent.mkdir(parents=True, exist_ok=True)
        args.json_out.write_text(json.dumps(summary, indent=2))
    sys.exit(0)

if __name__ == '__main__':
    main()
