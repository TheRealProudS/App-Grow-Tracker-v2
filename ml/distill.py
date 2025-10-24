#!/usr/bin/env python3
"""Knowledge Distillation Training Script.

Train a student model using a (pretrained) teacher via soft targets.
Loss = alpha * CE(student_logits, hard_labels) + (1-alpha) * KD(soft_student, soft_teacher)
Where KD is KL-Divergence between softened distributions (T temperature).

Usage Example:
  python ml/distill.py \
    --config ml/configs/baseline.yaml \
    --out ml/outputs/run_002_distill \
    --teacher-checkpoint ml/outputs/run_teacher/best.pt

Config additions (distillation block):
  distillation:
    enabled: true
    teacher_checkpoint: path/or/null
    teacher_arch: mobilenet_v3_large
    temperature: 4.0
    alpha: 0.7
    freeze_teacher: true
    student_from_scratch: true
    kd_loss: kl_div

Outputs similar to train.py: best.pt, metrics_final.json, plus distill_meta.json with KD settings.
"""
from __future__ import annotations
import argparse, json, math, os, random, time
from pathlib import Path
from typing import Dict, Any, Tuple

import yaml

try:
    import torch
    import torch.nn as nn
    import torch.nn.functional as F
    from torch.utils.data import DataLoader
    import torchvision.models as models
except ImportError as e:  # pragma: no cover
    raise SystemExit('PyTorch Umgebung erforderlich: pip install torch torchvision')

from dataset import PlantDataset, build_transforms
from metrics import MetricsTracker

# ---------------------------------------------------------------------------
# Utility
# ---------------------------------------------------------------------------

def set_seed(seed: int):
    random.seed(seed)
    torch.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)


def load_config(path: str) -> Dict[str, Any]:
    with open(path, 'r', encoding='utf-8') as f:
        return yaml.safe_load(f)


def select_device(pref: str):
    if pref in ('cuda','gpu') and torch.cuda.is_available():
        return torch.device('cuda')
    if pref == 'mps' and torch.backends.mps.is_available():  # type: ignore[attr-defined]
        return torch.device('mps')
    if pref == 'auto':
        if torch.cuda.is_available():
            return torch.device('cuda')
        if hasattr(torch.backends, 'mps') and torch.backends.mps.is_available():  # type: ignore
            return torch.device('mps')
    return torch.device('cpu')


def build_model(name: str, num_classes: int) -> nn.Module:
    if not hasattr(models, name):
        raise ValueError(f'Modell unbekannt: {name}')
    net_fn = getattr(models, name)
    model = net_fn(weights=None)
    # Replace classifier heuristically (covers common torchvision nets)
    if hasattr(model, 'classifier') and isinstance(model.classifier, nn.Sequential):
        last = model.classifier[-1]
        if isinstance(last, nn.Linear):
            in_f = last.in_features
            model.classifier[-1] = nn.Linear(in_f, num_classes)
    elif hasattr(model, 'fc') and isinstance(model.fc, nn.Linear):
        in_f = model.fc.in_features
        model.fc = nn.Linear(in_f, num_classes)
    else:
        # fallback attempt
        for attr in ('head','heads'):  # some timm-style
            if hasattr(model, attr):
                layer = getattr(model, attr)
                if isinstance(layer, nn.Linear):
                    setattr(model, attr, nn.Linear(layer.in_features, num_classes))
    return model


def kd_loss_fn(student_logits, teacher_logits, T: float, reduction='batchmean'):
    # KL Divergence between softened distributions
    # use log_softmax for student, softmax for teacher
    log_s = F.log_softmax(student_logits / T, dim=1)
    t = F.softmax(teacher_logits / T, dim=1)
    return F.kl_div(log_s, t, reduction=reduction) * (T * T)

# ---------------------------------------------------------------------------
# Training Loop
# ---------------------------------------------------------------------------

def train_one_epoch(student, teacher, loader, optimizer, device, scaler, cfg_distill, ce_loss, mixed_precision):
    student.train()
    if teacher is not None and cfg_distill['freeze_teacher']:
        teacher.eval()
    total_loss = 0.0
    total_ce = 0.0
    total_kd = 0.0
    steps = 0
    for imgs, labels in loader:
        imgs = imgs.to(device)
        labels = labels.to(device)
        optimizer.zero_grad(set_to_none=True)
        with torch.cuda.amp.autocast(enabled=mixed_precision and device.type=='cuda'):
            s_logits = student(imgs)
            ce = ce_loss(s_logits, labels)
            if teacher is not None:
                with torch.no_grad() if cfg_distill['freeze_teacher'] else torch.enable_grad():
                    t_logits = teacher(imgs)
                kd = kd_loss_fn(s_logits, t_logits, cfg_distill['temperature'])
            else:
                kd = torch.tensor(0.0, device=device)
            loss = cfg_distill['alpha'] * ce + (1 - cfg_distill['alpha']) * kd
        if scaler is not None:
            scaler.scale(loss).backward()
            scaler.step(optimizer)
            scaler.update()
        else:
            loss.backward()
            optimizer.step()
        total_loss += loss.item()
        total_ce += ce.item()
        total_kd += kd.item()
        steps += 1
    return {
        'loss': total_loss/steps,
        'ce_loss': total_ce/steps,
        'kd_loss': total_kd/steps
    }


def evaluate(model, loader, device):
    model.eval()
    tracker = MetricsTracker(num_classes=model.fc.out_features if hasattr(model,'fc') else 0)  # will adjust later
    all_cm_classes = None
    with torch.no_grad():
        for imgs, labels in loader:
            imgs = imgs.to(device)
            labels = labels.to(device)
            logits = model(imgs)
            if tracker.num_classes == 0:
                tracker.num_classes = logits.shape[1]
                tracker.cm = torch.zeros((tracker.num_classes, tracker.num_classes), dtype=torch.int64).numpy()
            tracker.update(logits, labels)
    res = tracker.compute()
    return res

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--config', required=True)
    ap.add_argument('--out', required=True)
    ap.add_argument('--teacher-checkpoint', help='Optional Pfad falls von Config abweichend')
    args = ap.parse_args()

    cfg = load_config(args.config)
    if not cfg.get('distillation', {}).get('enabled', False):
        print('[ERROR] distillation.enabled=false in Config – aktivieren zum Start.')
        return

    exp = cfg['experiment']
    distill_cfg = cfg['distillation']
    device = select_device(exp.get('device','auto'))
    set_seed(exp.get('seed',42))

    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)

    # Dataset
    data = cfg['data']
    train_tf, val_tf = build_transforms(cfg['model']['image_size'], cfg.get('augment'))
    # We reuse meta.csv splits (simple approach); for production consider stratified split reuse.
    dataset = PlantDataset(data['root'], data['meta_csv'], list(range(cfg['model']['num_classes'])), transform=train_tf)  # placeholder class names numeric
    # Quick naive split 90/10 (since original splitter in train.py not reused; could import logic)
    indices = list(range(len(dataset)))
    random.shuffle(indices)
    split = int(len(indices)*0.9)
    train_idx, val_idx = indices[:split], indices[split:]
    from torch.utils.data import Subset
    train_ds = Subset(dataset, train_idx)
    val_ds = Subset(dataset, val_idx)
    train_loader = DataLoader(train_ds, batch_size=cfg['train']['batch_size'], shuffle=True, num_workers=cfg['train']['num_workers'])
    val_loader = DataLoader(val_ds, batch_size=cfg['train']['batch_size'], shuffle=False, num_workers=cfg['train']['num_workers'])

    # Student
    student = build_model(cfg['model']['name'], cfg['model']['num_classes']).to(device)

    # Teacher
    teacher = None
    teacher_ckpt = args.teacher_checkpoint or distill_cfg.get('teacher_checkpoint')
    if teacher_ckpt and Path(teacher_ckpt).exists():
        # Attempt to load architecture from config teacher_arch
        t_arch = distill_cfg.get('teacher_arch', cfg['model']['name'])
        teacher = build_model(t_arch, cfg['model']['num_classes']).to(device)
        state = torch.load(teacher_ckpt, map_location='cpu')
        # Accept both plain state_dict or checkpoint dict
        if 'model_state' in state:
            teacher.load_state_dict(state['model_state'])
        else:
            teacher.load_state_dict(state)
        print(f'[INFO] Teacher geladen: {t_arch} von {teacher_ckpt}')
    else:
        print('[WARN] Kein Teacher Checkpoint gefunden – Distillation degradiert zu normalem Training')

    # Freeze teacher if configured
    if teacher and distill_cfg.get('freeze_teacher', True):
        for p in teacher.parameters():
            p.requires_grad_(False)

    # Optimizer
    opt_cfg = cfg['optimizer']
    optimizer = torch.optim.AdamW(student.parameters(), lr=opt_cfg['lr'], weight_decay=opt_cfg.get('weight_decay',0.01), betas=tuple(opt_cfg.get('betas',[0.9,0.999])))

    ce_loss = nn.CrossEntropyLoss(label_smoothing=cfg.get('loss',{}).get('label_smoothing',0.0))

    scaler = torch.cuda.amp.GradScaler(enabled=exp.get('mixed_precision', False) and device.type=='cuda')

    best_f1 = -1.0
    history = []
    epochs = cfg['train']['epochs']
    patience = cfg['train'].get('early_stop_patience', 5)
    bad_epochs = 0

    for epoch in range(1, epochs+1):
        t0 = time.time()
        train_stats = train_one_epoch(student, teacher, train_loader, optimizer, device, scaler, distill_cfg, ce_loss, exp.get('mixed_precision', False))
        val_metrics = evaluate(student, val_loader, device)
        macro_f1 = val_metrics['macro_f1']
        improved = macro_f1 > best_f1
        if improved:
            best_f1 = macro_f1
            torch.save({'model_state': student.state_dict(), 'epoch': epoch, 'macro_f1': macro_f1}, out_dir / 'best.pt')
            bad_epochs = 0
        else:
            bad_epochs += 1
        history.append({'epoch': epoch, 'train': train_stats, 'val_macro_f1': macro_f1})
        dt = time.time()-t0
        print(f"[E{epoch}] loss={train_stats['loss']:.4f} ce={train_stats['ce_loss']:.4f} kd={train_stats['kd_loss']:.4f} val_f1={macro_f1:.4f} {'*' if improved else ''} ({dt:.1f}s)")
        if bad_epochs >= patience:
            print('[INFO] Early Stopping ausgelöst.')
            break

    # Final evaluation on validation split (already computed last epoch)
    with open(out_dir / 'metrics_final.json', 'w', encoding='utf-8') as f:
        json.dump({'history': history, 'best_macro_f1': best_f1}, f, indent=2)
    with open(out_dir / 'distill_meta.json', 'w', encoding='utf-8') as f:
        json.dump({'distillation': distill_cfg}, f, indent=2)
    print('[DONE] Distillation abgeschlossen. Artefakte gespeichert.')

if __name__ == '__main__':
    main()
