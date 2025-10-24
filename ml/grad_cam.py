#!/usr/bin/env python3
"""Grad-CAM utility for LeafSense models.

Usage example:
  python ml/grad_cam.py \
    --checkpoint ml/outputs/run_010/best.pt \
    --image path/to/image.jpg \
    --out cam.jpg

Multiple images:
  python ml/grad_cam.py --checkpoint best.pt --image imgs/a.jpg --image imgs/b.jpg --out-dir cams/

Notes:
- Works with torchvision MobileNetV3; last conv layer auto-detected.
- Falls back to first suitable Conv2d if specific name not found.
- Produces heatmap overlay (jet) blended with original image.
"""
from __future__ import annotations
import argparse, os
from pathlib import Path
import torch
from torch import nn
from PIL import Image
import numpy as np

try:
    import torchvision.transforms as T
    from torchvision import models
except Exception as e:  # pragma: no cover
    raise SystemExit(f"torchvision erforderlich: {e}")


def load_model(checkpoint: str, num_classes: int | None = None):
    ckpt = torch.load(checkpoint, map_location='cpu')
    # Assume MobileNetV3 Small architecture by default
    model = models.mobilenet_v3_small(weights=None)
    if num_classes is not None:
        model.classifier[3] = nn.Linear(model.classifier[3].in_features, num_classes)
    sd = ckpt.get('model', ckpt)
    model.load_state_dict(sd, strict=False)
    model.eval()
    return model


def find_target_layer(model: nn.Module):
    # Try typical last conv blocks for mobilenet_v3
    candidates = []
    for name, module in model.named_modules():
        if isinstance(module, nn.Conv2d):
            candidates.append((name, module))
    # Choose the last conv
    if not candidates:
        raise RuntimeError('Keine Conv2d Layer gefunden')
    return candidates[-1][1]


def preprocess_image(path: Path, image_size: int = 224):
    tf = T.Compose([
        T.Resize(int(image_size * 1.15)),
        T.CenterCrop(image_size),
        T.ToTensor(),
        T.Normalize(mean=[0.485,0.456,0.406], std=[0.229,0.224,0.225])
    ])
    img = Image.open(path).convert('RGB')
    return tf(img), img


def grad_cam(model: nn.Module, img_tensor: torch.Tensor, target_layer: nn.Module, target_class: int | None = None):
    activations = {}
    gradients = {}
    def fwd_hook(_, __, output):
        activations['value'] = output.detach()
    def bwd_hook(_, grad_in, grad_out):
        gradients['value'] = grad_out[0].detach()
    h1 = target_layer.register_forward_hook(fwd_hook)
    h2 = target_layer.register_full_backward_hook(bwd_hook)
    try:
        img_tensor = img_tensor.unsqueeze(0)
        img_tensor.requires_grad_(True)
        out = model(img_tensor)
        if target_class is None:
            target_class = int(out.argmax(dim=1).item())
        score = out[0, target_class]
        model.zero_grad()
        score.backward()
        acts = activations['value']  # (1,C,H,W)
        grads = gradients['value']   # (1,C,H,W)
        weights = grads.mean(dim=(2,3), keepdim=True)  # (1,C,1,1)
        cam = (weights * acts).sum(dim=1, keepdim=True)  # (1,1,H,W)
        cam = torch.relu(cam)
        cam = torch.nn.functional.interpolate(cam, size=img_tensor.shape[2:], mode='bilinear', align_corners=False)
        cam_np = cam[0,0].cpu().numpy()
        cam_np -= cam_np.min(); cam_np += 1e-8; cam_np /= cam_np.max()
        return cam_np, target_class
    finally:
        h1.remove(); h2.remove()


def overlay_heatmap(orig: Image.Image, cam: np.ndarray, alpha: float = 0.45):
    import matplotlib.cm as cm
    cmap = cm.get_cmap('jet')
    heat = cmap(cam)[:, :, :3]
    heat_img = Image.fromarray((heat * 255).astype(np.uint8))
    heat_img = heat_img.resize(orig.size, Image.BILINEAR)
    blend = Image.blend(orig, heat_img, alpha)
    return blend


def process_images(model, target_layer, image_paths, out_dir: Path | None, out_file: Path | None, image_size: int):
    results = []
    for p in image_paths:
        tens, orig = preprocess_image(p, image_size)
        cam, pred_class = grad_cam(model, tens, target_layer)
        blended = overlay_heatmap(orig, cam)
        if out_dir:
            out_dir.mkdir(parents=True, exist_ok=True)
            save_path = out_dir / (p.stem + f"_cam_cls{pred_class}.jpg")
            blended.save(save_path, quality=92)
            results.append(save_path)
        elif out_file:
            blended.save(out_file, quality=92)
            results.append(out_file)
    return results


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--checkpoint', required=True)
    ap.add_argument('--image', action='append', required=True, help='Eingabebild (mehrfach)')
    ap.add_argument('--out', help='Einzelnes Ausgabe-Bild (nur wenn eine Input-Datei)')
    ap.add_argument('--out-dir', help='Ausgabe-Verzeichnis für mehrere Bilder')
    ap.add_argument('--image-size', type=int, default=224)
    ap.add_argument('--num-classes', type=int, default=None)
    args = ap.parse_args()

    if args.out and len(args.image) > 1:
        raise SystemExit('--out nur bei genau einem --image erlaubt; sonst --out-dir nutzen.')

    model = load_model(args.checkpoint, num_classes=args.num_classes)
    target_layer = find_target_layer(model)
    images = [Path(p) for p in args.image]
    out_dir = Path(args.out_dir) if args.out_dir else None
    out_file = Path(args.out) if args.out else None
    res = process_images(model, target_layer, images, out_dir, out_file, args.image_size)
    print(f"[OK] CAM Bilder: {[p.as_posix() for p in res]}")

if __name__ == '__main__':
    main()
