"""Security utilities: API key rotation + in-memory rate limiting.

Key storage format (JSON file):
{
  "keys": [
    {"key": "abc123", "active": true, "name": "dev", "expires": 0},
    {"key": "oldKey", "active": false, "name": "revoked", "expires": 0}
  ]
}

Expiration: expires epoch millis (0 = no expiry).
Rotation strategy: add new key (active), deploy to clients, then deactivate old key after grace period.
"""
from __future__ import annotations
import time, threading, json
from pathlib import Path
from typing import Optional, Dict, Any, Tuple

class KeyManager:
    def __init__(self, path: Path):
        self.path = path
        self._lock = threading.Lock()
        self._cache = None  # type: Optional[Dict[str, Any]]
        self._load()

    def _load(self):
        if self.path.exists():
            try:
                self._cache = json.loads(self.path.read_text(encoding='utf-8'))
            except Exception:
                self._cache = {"keys": []}
        else:
            self._cache = {"keys": []}

    def _save(self):
        self.path.parent.mkdir(parents=True, exist_ok=True)
        tmp = self.path.with_suffix('.tmp')
        tmp.write_text(json.dumps(self._cache, indent=2), encoding='utf-8')
        tmp.replace(self.path)

    def validate(self, key: Optional[str]) -> bool:
        if not key:
            return False
        with self._lock:
            now = int(time.time() * 1000)
            for entry in self._cache.get('keys', []):
                if entry.get('key') == key:
                    if not entry.get('active', False):
                        return False
                    exp = int(entry.get('expires', 0) or 0)
                    if exp and now > exp:
                        return False
                    return True
        return False

    def list_keys(self):
        with self._lock:
            return list(self._cache.get('keys', []))

    def add_key(self, key: str, name: str = '', expires: int = 0):
        with self._lock:
            # deactivate duplicates
            for k in self._cache['keys']:
                if k.get('key') == key:
                    k['active'] = False
            self._cache['keys'].append({
                'key': key,
                'active': True,
                'name': name,
                'expires': expires
            })
            self._save()

    def deactivate(self, key: str):
        with self._lock:
            for k in self._cache['keys']:
                if k.get('key') == key:
                    k['active'] = False
            self._save()

class RateLimiter:
    def __init__(self, limit: int, window_seconds: int):
        self.limit = limit
        self.window = window_seconds
        self._lock = threading.Lock()
        # key -> {window_start, count}
        self._state: Dict[str, Dict[str, int]] = {}

    def check(self, key: str) -> Tuple[bool, int, int]:
        now = int(time.time())
        with self._lock:
            st = self._state.get(key)
            if st is None:
                self._state[key] = {'window_start': now, 'count': 1}
                return True, 1, self.limit
            ws = st['window_start']
            if now - ws >= self.window:
                # reset window
                st['window_start'] = now
                st['count'] = 1
                return True, 1, self.limit
            # same window
            if st['count'] >= self.limit:
                return False, st['count'], self.limit
            st['count'] += 1
            return True, st['count'], self.limit

    def retry_after(self, key: str) -> int:
        now = int(time.time())
        st = self._state.get(key)
        if not st:
            return 0
        delta = now - st['window_start']
        remaining = self.window - delta
        return max(0, remaining)
