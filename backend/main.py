from __future__ import annotations
import os, json, hashlib, time
from pathlib import Path
from typing import Optional
from fastapi import FastAPI, UploadFile, File, Form, HTTPException, Header
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from fastapi import Response
from security import KeyManager, RateLimiter

API_KEY = os.getenv("INGEST_API_KEY", "dev-key")  # legacy single-key fallback
DATA_ROOT = Path(os.getenv("DATA_ROOT", "data"))
UPLOAD_DIR = DATA_ROOT / "uploads"
META_LOG = DATA_ROOT / "meta_log.jsonl"
KEYS_FILE = DATA_ROOT / "keys.json"

# Initialize key manager (if keys.json absent, seed with legacy API_KEY)
key_manager = KeyManager(KEYS_FILE)
if not key_manager.list_keys():
    # Add legacy key infinite expiry for backward compat
    key_manager.add_key(API_KEY, name="legacy", expires=0)

# Simple rate limiter (e.g. 300 uploads / 3600s per key)
RATE_LIMIT = int(os.getenv("RATE_LIMIT", "300"))
RATE_WINDOW = int(os.getenv("RATE_WINDOW", "3600"))
rate_limiter = RateLimiter(limit=RATE_LIMIT, window_seconds=RATE_WINDOW)

app = FastAPI(title="GrowTracker Ingest API", version="0.1")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
DATA_ROOT.mkdir(parents=True, exist_ok=True)

@app.get("/health")
async def health():
    return {"status": "ok"}

@app.post("/v1/ingest/image")
async def ingest_image(
    response: Response,
    image: UploadFile = File(...),
    meta: Optional[str] = Form(None),
    x_api_key: Optional[str] = Header(None)
):
    # Validate API key via key manager
    if not key_manager.validate(x_api_key):
        raise HTTPException(status_code=401, detail="unauthorized")
    # Rate limiting per key
    allowed, count, limit = rate_limiter.check(x_api_key)
    remaining = max(0, limit - count)
    response.headers["X-RateLimit-Limit"] = str(limit)
    response.headers["X-RateLimit-Remaining"] = str(remaining)
    if not allowed:
        retry_after = rate_limiter.retry_after(x_api_key)
        response.headers["Retry-After"] = str(retry_after)
        raise HTTPException(status_code=429, detail="rate limit exceeded")
    # Basic validation
    if not image.filename:
        raise HTTPException(status_code=400, detail="missing filename")

    # Read bytes
    content = await image.read()
    if len(content) == 0:
        raise HTTPException(status_code=400, detail="empty file")
    sha256 = hashlib.sha256(content).hexdigest()

    # Store file (avoid collisions)
    ts = int(time.time() * 1000)
    safe_name = f"{ts}_{sha256[:12]}_{image.filename.replace(os.sep,'_')}"
    out_path = UPLOAD_DIR / safe_name
    with out_path.open('wb') as f:
        f.write(content)

    # Parse metadata JSON if provided
    meta_obj = {}
    if meta:
        try:
            meta_obj = json.loads(meta)
        except Exception:
            meta_obj = {"_raw": meta, "_parse_error": True}

    record = {
        "ts": ts,
        "file": out_path.name,
        "bytes": len(content),
        "sha256": sha256,
        "meta": meta_obj
    }
    # Append log line
    with META_LOG.open('a', encoding='utf-8') as logf:
        logf.write(json.dumps(record, ensure_ascii=False) + "\n")

    return JSONResponse({"status": "stored", "sha256": sha256, "rate_limit_remaining": remaining})

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
