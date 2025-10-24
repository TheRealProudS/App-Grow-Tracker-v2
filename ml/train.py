#!/usr/bin/env python3
"""Minimaler Trainings-Stub für LeafSense.

Funktionen:
- Liest YAML Config
- Setzt Seed
- Platzhalter für Dataset / Modell / Training Loop
- Druckt geplante Schritte & Export-Platzhalter

Erweitern:
- PyTorch Dataset + Augmentierungen
- MobileNetV3 Laden (torchvision / timm)
- Logging (TensorBoard / W&B)
- Checkpointing & Metriken
- ONNX / TFLite Export
"""
from __future__ import annotations
import argparse, yaml, random, json, os, time
from dataclasses import dataclass
from pathlib import Path
import numpy as np

try:
    import torch
    from torch import nn
    from torch.utils.data import DataLoader, random_split
except ImportError:
    torch = None  # type: ignore

@dataclass
class Config:
    raw: dict

    @property
    def experiment_id(self):
        return self.raw.get('experiment', {}).get('id', 'exp')


def load_config(path: str) -> Config:
    with open(path, 'r', encoding='utf-8') as f:
        data = yaml.safe_load(f)
    return Config(data)

def set_seed(seed: int):
    random.seed(seed)
    np.random.seed(seed)
    if torch is not None:
        torch.manual_seed(seed)
        if torch.cuda.is_available():
            torch.cuda.manual_seed_all(seed)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--config', required=True, help='Pfad zur YAML Konfiguration')
    ap.add_argument('--out', required=True, help='Ausgabeverzeichnis')
    ap.add_argument('--fast-dev-run', action='store_true', help='Nur wenige Batches (2 Train/1 Val) für schnellen Smoke-Test')
    ap.add_argument('--resume', type=str, default=None, help='Pfad zu Checkpoint (.pt) für Resume')
    ap.add_argument('--deterministic', action='store_true', help='cuDNN deterministisch (geringere Performance möglich)')
    ap.add_argument('--log-dir', type=str, default=None, help='Optionales TensorBoard Log Verzeichnis')
    ap.add_argument('--lr-find', action='store_true', help='Führe Learning Rate Range Test durch und beende danach')
    ap.add_argument('--lr-find-min', type=float, default=1e-6, help='Start-LR für Range Test')
    ap.add_argument('--lr-find-max', type=float, default=1.0, help='End-LR für Range Test')
    ap.add_argument('--lr-find-steps', type=int, default=200, help='Maximale Schritte im LR Range Test')
    ap.add_argument('--pr-curve', action='store_true', help='Erzeuge Precision/Recall Kurve (macro) über Val-Set am Ende')
    ap.add_argument('--html-report', action='store_true', help='Erzeuge training_report.html (Metriken, LR, Confusion, PR falls aktiviert)')
    ap.add_argument('--median-early-stop-window', type=int, default=0, help='Fenstergröße für gleitenden Median (0=deaktiviert)')
    ap.add_argument('--median-early-stop-patience', type=int, default=0, help='Patience basierend auf Median-Verbesserung (0=deaktiviert)')
    ap.add_argument('--median-min-delta', type=float, default=0.0, help='Minimale Verbesserung über Median um als Fortschritt zu zählen')
    ap.add_argument('--roc-curve', action='store_true', help='Erzeuge macro ROC Kurve & roc_curve.json')
    ap.add_argument('--save-miscls', type=int, default=0, help='Speichere Top-K Fehlklassifikationen mit höchsten Verlusten als Thumbnails')
    args = ap.parse_args()

    cfg = load_config(args.config)
    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)

    seed = cfg.raw.get('experiment', {}).get('seed', 42)
    set_seed(seed)
    if args.deterministic and torch is not None and torch.backends.cudnn.is_available():  # type: ignore
        torch.backends.cudnn.deterministic = True  # type: ignore
        torch.backends.cudnn.benchmark = False     # type: ignore
        print('[INFO] Deterministic cuDNN aktiviert')

    print(f"[INFO] Experiment: {cfg.experiment_id}")
    print(f"[INFO] Output Dir: {out_dir}")

    if torch is None:
        print('[WARN] torch nicht installiert – Fallback auf Stub. Installiere torch für echtes Training.')
        # Minimal stub fallback
        planned = {'phases': ['load_dataset','build_model','train_loop','validate','export']}
        (out_dir / 'plan.json').write_text(json.dumps(planned, indent=2), encoding='utf-8')
        return

    device_pref = cfg.raw.get('experiment', {}).get('device', 'auto')
    if device_pref == 'auto':
        device = 'cuda' if torch.cuda.is_available() else 'cpu'
    else:
        device = device_pref
    print(f"[INFO] Device gewählt: {device}")

    # Dataset laden
    data_cfg = cfg.raw.get('data', {})
    data_root = data_cfg.get('root')
    meta_csv = data_cfg.get('meta_csv')
    if not data_root or not meta_csv:
        raise SystemExit('data.root und data.meta_csv in Config erforderlich')

    # Klassen (aus data_spec.md manuell übernehmen oder dynamisch aus meta.csv parsen – hier statisch für Repro)
    class_names = [
        'healthy','nitrogen_deficiency','calcium_deficiency','overwatering','underwatering',
        'heat_stress','pest_suspect','fungal_suspect','nutrient_other','unknown'
    ]
    from dataset import PlantDataset, build_transforms  # local import
    train_tf, val_tf = build_transforms(
        image_size=cfg.raw.get('model', {}).get('image_size', 224),
        augment_cfg=cfg.raw.get('augment')
    )
    full_ds = PlantDataset(data_root, meta_csv, class_names, transform=train_tf)
    val_ratio = 0.15
    test_ratio = 0.15
    n = len(full_ds)
    n_val = int(n * val_ratio)
    n_test = int(n * test_ratio)
    n_train = n - n_val - n_test
    train_ds, val_ds, test_ds = random_split(full_ds, [n_train, n_val, n_test], generator=torch.Generator().manual_seed(42))
    # override val/test transforms
    if val_tf:
        val_ds.dataset.transform = val_tf  # type: ignore
    batch_size = cfg.raw.get('train', {}).get('batch_size', 32)
    num_workers = cfg.raw.get('train', {}).get('num_workers', 2)
    train_loader = DataLoader(train_ds, batch_size=batch_size, shuffle=True, num_workers=num_workers)
    val_loader = DataLoader(val_ds, batch_size=batch_size, shuffle=False, num_workers=num_workers)

    # Modell bauen
    model_name = cfg.raw.get('model', {}).get('name', 'mobilenet_v3_small')
    num_classes = cfg.raw.get('model', {}).get('num_classes', len(class_names))
    model = None
    try:
        from torchvision import models
        if model_name == 'mobilenet_v3_small':
            model = models.mobilenet_v3_small(weights=models.MobileNet_V3_Small_Weights.DEFAULT)
            model.classifier[3] = nn.Linear(model.classifier[3].in_features, num_classes)
        else:
            model = models.mobilenet_v3_small(weights=models.MobileNet_V3_Small_Weights.DEFAULT)
            model.classifier[3] = nn.Linear(model.classifier[3].in_features, num_classes)
    except Exception as e:
        print(f"[WARN] torchvision Modell-Laden fehlgeschlagen: {e}. Fallback auf einfache ConvNet.")
        model = nn.Sequential(
            nn.Conv2d(3, 16, 3, padding=1), nn.ReLU(), nn.MaxPool2d(2),
            nn.Conv2d(16, 32, 3, padding=1), nn.ReLU(), nn.MaxPool2d(2),
            nn.AdaptiveAvgPool2d((1,1)), nn.Flatten(), nn.Linear(32, num_classes)
        )
    model.to(device)

    # Optimizer / Scheduler
    opt_cfg = cfg.raw.get('optimizer', {})
    lr = opt_cfg.get('lr', 1e-3)
    wd = opt_cfg.get('weight_decay', 0.0)
    optimizer = torch.optim.AdamW(model.parameters(), lr=lr, weight_decay=wd)
    epochs = cfg.raw.get('train', {}).get('epochs', 10)
    criterion = nn.CrossEntropyLoss(label_smoothing=cfg.raw.get('loss', {}).get('label_smoothing', 0.0))
    # Class Weights Handling (list or path)
    cw_cfg = cfg.raw.get('loss', {}).get('class_weights')
    if cw_cfg:
        try:
            import json as _json
            if isinstance(cw_cfg, str) and os.path.isfile(cw_cfg):
                with open(cw_cfg, 'r', encoding='utf-8') as f:
                    weights_list = _json.load(f)
            else:
                weights_list = cw_cfg
            w_tensor = torch.tensor(weights_list, dtype=torch.float32, device=device)
            criterion = nn.CrossEntropyLoss(weight=w_tensor, label_smoothing=cfg.raw.get('loss', {}).get('label_smoothing', 0.0))
            print('[LOSS] Class Weights aktiviert')
        except Exception as e:
            print(f"[LOSS][WARN] Konnte Class Weights nicht laden: {e}")

    from metrics import MetricsTracker
    tracker = MetricsTracker(num_classes)
    best_macro = -1.0
    patience = cfg.raw.get('train', {}).get('early_stop_patience', 5)
    no_improve = 0
    median_window = args.median_early_stop_window
    median_patience = args.median_early_stop_patience
    median_min_delta = args.median_min_delta
    f1_history: list[float] = []
    median_no_improve = 0
    use_amp = bool(cfg.raw.get('experiment', {}).get('mixed_precision', False) and device == 'cuda')
    if use_amp:
        print('[AMP] Mixed Precision aktiviert')
        scaler = torch.cuda.amp.GradScaler()
    else:
        scaler = None
        if cfg.raw.get('experiment', {}).get('mixed_precision', False):
            print('[AMP][INFO] GPU nicht verfügbar – AMP deaktiviert (Fallback).')

    def validate():
        model.eval()
        tracker.cm[:] = 0
        total_loss = 0.0
        with torch.no_grad():
            for x, y in val_loader:
                x, y = x.to(device), y.to(device)
                out = model(x)
                loss = criterion(out, y)
                total_loss += float(loss.item()) * x.size(0)
                tracker.update(out, y)
        res = tracker.compute()
        return total_loss / len(val_loader.dataset), res

    warmup_epochs = opt_cfg.get('warmup_epochs', 0)
    cosine = opt_cfg.get('cosine_schedule', False)
    min_lr = opt_cfg.get('min_lr', lr * 0.05)
    lr_history = []

    def adjust_lr(epoch: int):
        # epoch starts at 1
        if warmup_epochs > 0 and epoch <= warmup_epochs:
            warm_frac = epoch / float(max(1, warmup_epochs))
            current = lr * warm_frac
        elif cosine:
            progress = (epoch - warmup_epochs) / float(max(1, epochs - warmup_epochs))
            import math
            current = min_lr + 0.5 * (lr - min_lr) * (1 + math.cos(math.pi * progress))
        else:
            current = lr
        for pg in optimizer.param_groups:
            pg['lr'] = current
        lr_history.append(current)
        return current

    # TensorBoard Setup (lazy import)
    # LR Finder (Leslie Smith Range Test)
    if args.lr_find:
        print(f"[LRFIND] Starte Range Test von {args.lr_find_min} bis {args.lr_find_max} over {args.lr_find_steps} steps")
        lrs = []
        losses = []
        best_loss = float('inf')
        mult = (args.lr_find_max / args.lr_find_min) ** (1 / max(1, args.lr_find_steps - 1))
        lr_current = args.lr_find_min
        for pg in optimizer.param_groups:
            pg['lr'] = lr_current
        steps_done = 0
        model.train()
        for batch, (x, y) in enumerate(train_loader):
            x, y = x.to(device), y.to(device)
            optimizer.zero_grad(set_to_none=True)
            out = model(x)
            loss = criterion(out, y)
            loss.backward()
            optimizer.step()
            l = float(loss.item())
            lrs.append(lr_current)
            losses.append(l)
            if l < best_loss:
                best_loss = l
            if l > best_loss * 5:
                print('[LRFIND] Abbruch: Verlust zu stark gestiegen')
                break
            lr_current *= mult
            for pg in optimizer.param_groups:
                pg['lr'] = lr_current
            steps_done += 1
            if steps_done >= args.lr_find_steps:
                break
        # Save results
        lr_find_path = out_dir / 'lr_find.json'
        with open(lr_find_path, 'w', encoding='utf-8') as f:
            json.dump({'lrs': lrs, 'losses': losses, 'best_loss': best_loss}, f, indent=2)
        print(f"[LRFIND] Fertig – Ergebnisse: {lr_find_path}")
        if tb_writer:
            for i, (lr_v, loss_v) in enumerate(zip(lrs, losses), start=1):
                tb_writer.add_scalar('lr_find/loss', loss_v, i)
                tb_writer.add_scalar('lr_find/lr', lr_v, i)
        if tb_writer:
            tb_writer.close()
        return
    tb_writer = None
    if args.log_dir:
        try:
            from torch.utils.tensorboard import SummaryWriter
            log_dir = Path(args.log_dir)
            log_dir.mkdir(parents=True, exist_ok=True)
            tb_writer = SummaryWriter(log_dir.as_posix())
            print(f"[TB] Logging nach {log_dir}")
        except Exception as e:
            print(f"[TB][WARN] TensorBoard nicht verfügbar: {e}")

    print(f"[INFO] Starte Training: {epochs} Epochen, {n_train} Train-Samples, {n_val} Val-Samples (warmup={warmup_epochs}, cosine={cosine})")
    start_epoch = 1
    # Resume Support
    if args.resume:
        ckpt_path = Path(args.resume)
        if ckpt_path.exists():
            try:
                ckpt = torch.load(ckpt_path, map_location='cpu')
                model.load_state_dict(ckpt['model'])
                best_macro = float(ckpt.get('macro_f1', best_macro))
                start_epoch = int(ckpt.get('epoch', 0)) + 1
                print(f"[RESUME] Geladen ab Epoch {start_epoch-1} best_macro={best_macro:.4f}")
            except Exception as e:
                print(f"[RESUME][WARN] Konnte Checkpoint nicht laden: {e}")

    # Mixup / Cutmix config
    aug_cfg = cfg.raw.get('augment', {})
    mixup_cfg = aug_cfg.get('mixup', {}) or {}
    cutmix_cfg = aug_cfg.get('cutmix', {}) or {}
    use_mixup = bool(mixup_cfg.get('enabled', False))
    mixup_alpha = float(mixup_cfg.get('alpha', 0.2))
    use_cutmix = bool(cutmix_cfg.get('enabled', False))
    cutmix_alpha = float(cutmix_cfg.get('alpha', 1.0))

    def apply_mixup_cutmix(x, y):
        if not (use_mixup or use_cutmix):
            return x, y, None
        lam = 1.0
        mode = None
        if use_mixup and use_cutmix:
            # random choose
            if random.random() < 0.5:
                mode = 'mixup'
            else:
                mode = 'cutmix'
        elif use_mixup:
            mode = 'mixup'
        else:
            mode = 'cutmix'
        indices = torch.randperm(x.size(0))
        if mode == 'mixup':
            lam = np.random.beta(mixup_alpha, mixup_alpha)
            x = lam * x + (1 - lam) * x[indices]
        else:  # cutmix
            lam = np.random.beta(cutmix_alpha, cutmix_alpha)
            bbx1, bby1, bbx2, bby2 = rand_bbox(x.size(), lam)
            x[:, :, bby1:bby2, bbx1:bbx2] = x[indices, :, bby1:bby2, bbx1:bbx2]
            # Adjust lam to exact pixel ratio
            lam = 1 - ((bbx2 - bbx1) * (bby2 - bby1) / (x.size(-1) * x.size(-2)))
        y_shuffled = y[indices]
        return x, (y, y_shuffled, lam, mode), mode

    def mixup_criterion(pred, target_tuple):
        y_a, y_b, lam, mode = target_tuple
        return lam * criterion(pred, y_a) + (1 - lam) * criterion(pred, y_b)

    def rand_bbox(size, lam):
        W = size[3]
        H = size[2]
        cut_rat = np.sqrt(1. - lam)
        cut_w = int(W * cut_rat)
        cut_h = int(H * cut_rat)
        # uniform center
        cx = np.random.randint(W)
        cy = np.random.randint(H)
        bbx1 = np.clip(cx - cut_w // 2, 0, W)
        bby1 = np.clip(cy - cut_h // 2, 0, H)
        bbx2 = np.clip(cx + cut_w // 2, 0, W)
        bby2 = np.clip(cy + cut_h // 2, 0, H)
        return bbx1, bby1, bbx2, bby2

    for epoch in range(start_epoch, epochs+1):
        current_lr = adjust_lr(epoch)
        if epoch == 1 or epoch % 5 == 0 or epoch == epochs:
            print(f"[LR] Epoch {epoch} lr={current_lr:.6f}")
        model.train()
        running = 0.0
        max_train_batches = 2 if args.fast_dev_run else None
        for i, (x, y) in enumerate(train_loader):
            if max_train_batches is not None and i >= max_train_batches:
                break
            x, y = x.to(device), y.to(device)
            optimizer.zero_grad(set_to_none=True)
            if use_amp:
                with torch.cuda.amp.autocast():
                    x_aug, target_aug, mode = apply_mixup_cutmix(x, y)
                    out = model(x_aug)
                    if target_aug is not None:
                        loss = mixup_criterion(out, target_aug)
                    else:
                        loss = criterion(out, y)
                scaler.scale(loss).backward()
                torch.nn.utils.clip_grad_norm_(model.parameters(), cfg.raw.get('train', {}).get('gradient_clip_norm', 5.0))
                scaler.step(optimizer)
                scaler.update()
            else:
                x_aug, target_aug, mode = apply_mixup_cutmix(x, y)
                out = model(x_aug)
                if target_aug is not None:
                    loss = mixup_criterion(out, target_aug)
                else:
                    loss = criterion(out, y)
                loss.backward()
                torch.nn.utils.clip_grad_norm_(model.parameters(), cfg.raw.get('train', {}).get('gradient_clip_norm', 5.0))
                optimizer.step()
            running += float(loss.item())
            if (i+1) % cfg.raw.get('logging', {}).get('interval_steps', 50) == 0:
                print(f"[E{epoch}][{i+1}] loss={running/(i+1):.4f}")
        # Fast dev run: only 1 val batch
        if args.fast_dev_run:
            # manual mini-val loop
            model.eval(); tracker.cm[:] = 0; total_loss = 0.0
            with torch.no_grad():
                for j, (vx, vy) in enumerate(val_loader):
                    if j > 0: break
                    vx, vy = vx.to(device), vy.to(device)
                    vout = model(vx)
                    vloss = criterion(vout, vy)
                    total_loss += float(vloss.item()) * vx.size(0)
                    tracker.update(vout, vy)
            metrics_res = tracker.compute()
            val_loss = total_loss / ((j+1) * val_loader.batch_size)
        else:
            val_loss, metrics_res = validate()
        if use_amp and scaler is not None:
            print(f"[VAL][Epoch {epoch}] loss={val_loss:.4f} macro_f1={metrics_res.macro_f1:.4f} scaler_scale={scaler.get_scale():.2f}")
        else:
            print(f"[VAL][Epoch {epoch}] loss={val_loss:.4f} macro_f1={metrics_res.macro_f1:.4f}")
        if tb_writer:
            global_step = (epoch - 1) * len(train_loader)
            tb_writer.add_scalar('val/loss', val_loss, epoch)
            tb_writer.add_scalar('val/macro_f1', metrics_res.macro_f1, epoch)
            tb_writer.add_scalar('train/last_lr', current_lr, epoch)
            # Confusion matrix as image (small helper)
            try:
                import matplotlib.pyplot as plt
                import io
                fig, ax = plt.subplots(figsize=(4,4))
                cm_img = metrics_res.confusion
                ax.imshow(cm_img, cmap='Blues')
                ax.set_title('Confusion')
                ax.set_xlabel('Pred'); ax.set_ylabel('True')
                plt.tight_layout()
                buf = io.BytesIO()
                fig.savefig(buf, format='png')
                buf.seek(0)
                import numpy as np
                import PIL.Image as PILImage
                img = PILImage.open(buf)
                img_np = np.array(img).transpose(2,0,1)  # CHW
                tb_writer.add_image('val/confusion_matrix', img_np, epoch)
                plt.close(fig)
            except Exception as e:
                print(f"[TB][WARN] Confusion Matrix Logging fehlgeschlagen: {e}")
        f1_history.append(metrics_res.macro_f1)
        if metrics_res.macro_f1 > best_macro:
            best_macro = metrics_res.macro_f1
            no_improve = 0
            torch.save({'model': model.state_dict(), 'macro_f1': best_macro, 'epoch': epoch}, out_dir / 'best.pt')
            print(f"[SAVE] Neuer Bestwert macro_f1={best_macro:.4f}")
        else:
            no_improve += 1
        # Median Early Stopping
        if median_window > 0 and len(f1_history) >= median_window:
            import numpy as _np
            recent = f1_history[-median_window:]
            med = float(_np.median(recent))
            # Wenn aktueller F1 nicht mindestens med + delta ist -> zählen
            if metrics_res.macro_f1 < med + median_min_delta:
                median_no_improve += 1
            else:
                median_no_improve = 0
            if median_patience > 0 and median_no_improve >= median_patience:
                print(f"[EARLY STOP MEDIAN] Keine median-basierte Verbesserung nach {median_no_improve} Epochen (Window={median_window}).")
                break
        # Klassischer Early Stop
        if no_improve >= patience:
            print(f"[EARLY STOP] Keine Verbesserung {no_improve} Epochen.")
            break

    # Export ONNX / TFLite (Stub if conversion fails)
    export_cfg = cfg.raw.get('export', {})
    model.eval()
    dummy = torch.randn(1, 3, cfg.raw.get('model', {}).get('image_size', 224), cfg.raw.get('model', {}).get('image_size', 224), device=device)
    if export_cfg.get('onnx'):
        onnx_path = out_dir / 'model.onnx'
        try:
            torch.onnx.export(model, dummy, onnx_path.as_posix(), input_names=['input'], output_names=['logits'], opset_version=17)
            print(f"[EXPORT] ONNX: {onnx_path}")
            if export_cfg.get('simplify_onnx', False):
                try:
                    import onnx
                    from onnxsim import simplify
                    print('[ONNXSIM] Starte Vereinfachung...')
                    m = onnx.load(onnx_path.as_posix())
                    sm, ok = simplify(m, check_n=1)
                    if not ok:
                        print('[ONNXSIM][WARN] simplify meldet success=False – speichere trotzdem.')
                    simp_path = onnx_path.with_name(onnx_path.stem + '_simplified.onnx')
                    onnx.save(sm, simp_path.as_posix())
                    red = (1 - simp_path.stat().st_size / onnx_path.stat().st_size) * 100
                    print(f"[ONNXSIM] Vereinfachung abgeschlossen: {simp_path.name} (Size Reduktion {red:.2f}%)")
                except Exception as e:
                    print(f"[ONNXSIM][ERROR] Vereinfachung fehlgeschlagen: {e}")
        except Exception as e:
            print(f"[WARN] ONNX Export fehlgeschlagen: {e}")
    if export_cfg.get('tflite'):
        # Placeholder: tatsächliche Konvertierung erfordert TF oder onnx2tf Pipeline
        (out_dir / 'model_tflite_placeholder.txt').write_text('Konvertierung noch nicht implementiert.')
        print('[EXPORT] TFLite Placeholder erzeugt.')

    # Optional Test Evaluation mit Best-Checkpoint
    test_metrics = None
    try:
        if 'test_ds' in locals() and (out_dir / 'best.pt').exists():
            best_ckpt = torch.load(out_dir / 'best.pt', map_location=device)
            model.load_state_dict(best_ckpt['model'])
            model.eval(); tracker.cm[:] = 0
            test_loader = DataLoader(test_ds, batch_size=batch_size, shuffle=False, num_workers=num_workers)
            test_loss_accum = 0.0
            with torch.no_grad():
                for tx, ty in test_loader:
                    tx, ty = tx.to(device), ty.to(device)
                    tout = model(tx)
                    tloss = criterion(tout, ty)
                    test_loss_accum += float(tloss.item()) * tx.size(0)
                    tracker.update(tout, ty)
            t_res = tracker.compute()
            test_metrics = {
                'loss': test_loss_accum / len(test_loader.dataset),
                'macro_f1': t_res.macro_f1,
                'confusion': t_res.confusion.tolist(),
                'per_class': t_res.per_class
            }
            print(f"[TEST] loss={test_metrics['loss']:.4f} macro_f1={test_metrics['macro_f1']:.4f}")
    except Exception as e:
        print(f"[TEST][WARN] Test-Eval fehlgeschlagen: {e}")

    # PR Curve (Val) falls angefordert
    pr_curve = None
    roc_curve_data = None
    if args.pr_curve:
        try:
            print('[PR] Berechne Precision/Recall Curve...')
            from sklearn.metrics import precision_recall_curve, auc
            model.eval(); tracker.cm[:] = 0
            # Sammle alle logits und Labels im Val-Set
            all_logits = []
            all_labels = []
            with torch.no_grad():
                for vx, vy in val_loader:
                    vx = vx.to(device)
                    out = model(vx)
                    all_logits.append(out.detach().cpu())
                    all_labels.append(vy.detach().cpu())
            import torch as _t
            logits_cat = _t.cat(all_logits, dim=0)
            labels_cat = _t.cat(all_labels, dim=0)
            probs = logits_cat.softmax(dim=1)
            # Macro: Durchschnitt über Klassen P/R vs Schwelle – wir mitteln diskrete P/R Werte über Klassen
            num_cls = probs.size(1)
            thresholds_set = None
            import numpy as _np
            pr_points = []
            for c in range(num_cls):
                y_true = (labels_cat == c).numpy()
                y_score = probs[:, c].numpy()
                prec, rec, thr = precision_recall_curve(y_true, y_score)
                if thresholds_set is None:
                    thresholds_set = thr
                # Interpolate rec/prec onto common threshold grid if shapes differ – simple slicing/truncation
                pr_points.append((prec, rec, thr))
            # Build macro curve by sampling a fixed set of thresholds from 0..1
            grid = _np.linspace(0, 1, 101)
            macro_prec = []
            macro_rec = []
            for t in grid:
                cls_prec = []
                cls_rec = []
                for prec, rec, thr in pr_points:
                    # Choose closest threshold index
                    if len(thr) == 0:
                        continue
                    idx = min(range(len(thr)), key=lambda i: abs(thr[i]-t)) if len(thr) > 0 else 0
                    # precision/recall arrays have len(thr)+1; align by idx
                    p_val = prec[min(idx, len(prec)-1)]
                    r_val = rec[min(idx, len(rec)-1)]
                    cls_prec.append(p_val)
                    cls_rec.append(r_val)
                if cls_prec:
                    macro_prec.append(float(_np.mean(cls_prec)))
                    macro_rec.append(float(_np.mean(cls_rec)))
                else:
                    macro_prec.append(0.0); macro_rec.append(0.0)
            pr_auc = auc(macro_rec, macro_prec) if len(macro_rec) > 1 else 0.0
            pr_curve = {
                'thresholds': grid.tolist(),
                'macro_precision': macro_prec,
                'macro_recall': macro_rec,
                'macro_pr_auc': pr_auc
            }
            with open(out_dir / 'pr_curve.json', 'w', encoding='utf-8') as f:
                json.dump(pr_curve, f, indent=2)
            print(f"[PR] Fertig (macro PR AUC={pr_auc:.4f}) -> pr_curve.json")
            if tb_writer:
                try:
                    import matplotlib.pyplot as plt, io
                    fig, ax = plt.subplots()
                    ax.plot(macro_rec, macro_prec)
                    ax.set_xlabel('Recall'); ax.set_ylabel('Precision'); ax.set_title(f'Macro PR (AUC={pr_auc:.3f})')
                    buf = io.BytesIO(); fig.savefig(buf, format='png'); buf.seek(0)
                    import numpy as _np2, PIL.Image as _PIL
                    img = _PIL.open(buf)
                    tb_writer.add_image('val/macro_pr_curve', _np2.array(img).transpose(2,0,1), 0)
                    plt.close(fig)
                except Exception as e:
                    print(f"[PR][WARN] PR Curve TB Logging fehlgeschlagen: {e}")
        except Exception as e:
            print(f"[PR][WARN] Berechnung fehlgeschlagen: {e}")

    # Speichere finale Metriken
    metrics_json = {
        'best_macro_f1': best_macro,
        'confusion': metrics_res.confusion.tolist(),
        'per_class': metrics_res.per_class,
        'lr_history': lr_history,
        'mixed_precision': use_amp,
        'device': device,
        'epochs_trained': epoch,
        'resume_start_epoch': start_epoch,
        'test': test_metrics,
        'pr_curve': pr_curve,
        'roc_curve': roc_curve_data,
        'early_stopping': {
            'classic_no_improve_patience': patience,
            'classic_last_no_improve': no_improve,
            'median_window': median_window,
            'median_patience': median_patience,
            'median_no_improve': median_no_improve
        }
    }
    with open(out_dir / 'metrics_final.json', 'w', encoding='utf-8') as f:
        json.dump(metrics_json, f, indent=2)
    print('[DONE] Training abgeschlossen.')
    if tb_writer:
        tb_writer.close()

    # HTML Report (optional)
    if args.html_report:
        try:
            html_path = out_dir / 'training_report.html'
            def _table_row(k,v):
                return f"<tr><td>{k}</td><td>{v}</td></tr>"
            rows = []
            rows.append(_table_row('Best Macro F1', f"{best_macro:.4f}"))
            if test_metrics:
                rows.append(_table_row('Test Macro F1', f"{test_metrics['macro_f1']:.4f}"))
            if pr_curve and pr_curve.get('macro_pr_auc') is not None:
                rows.append(_table_row('Macro PR AUC', f"{pr_curve['macro_pr_auc']:.4f}"))
            html = ["<html><head><meta charset='utf-8'><title>Training Report</title>",
                    "<style>body{font-family:Arial;max-width:900px;margin:40px auto;}table{border-collapse:collapse;}td,th{border:1px solid #ccc;padding:4px 8px;}h1{margin-top:0;}code{background:#f5f5f5;padding:2px 4px;border-radius:4px;}</style>",
                    "</head><body>",
                    "<h1>LeafSense Training Report</h1>",
                    f"<p><strong>Experiment:</strong> {cfg.experiment_id}</p>",
                    "<h2>Key Metrics</h2><table>" + ''.join(rows) + "</table>"]
            # LR History inline sparkline (simple)
            if lr_history:
                import math as _m
                lr_norm = [ (x / max(lr_history)) for x in lr_history ]
                svg_pts = ' '.join(f"{i},{1 - v}" for i,v in enumerate(lr_norm))
                html.append("<h2>LR Verlauf</h2>")
                html.append(f"<svg width='400' height='80' viewBox='0 0 {len(lr_norm)} 1' preserveAspectRatio='none'><polyline fill='none' stroke='#0074D9' stroke-width='0.01' points='{svg_pts}' /></svg>")
            if pr_curve:
                html.append("<h2>Macro PR Curve</h2>")
                # simple ascii-esque summary: top 5 points
                thr = pr_curve['thresholds']; mp = pr_curve['macro_precision']; mr = pr_curve['macro_recall']
                html.append("<pre>")
                for i in range(0, len(thr), 25):
                    html.append(f"t={thr[i]:.2f} P={mp[i]:.3f} R={mr[i]:.3f}")
                html.append("</pre>")
            if roc_curve_data:
                html.append("<h2>Macro ROC Curve</h2>")
                roc_fpr = roc_curve_data['fpr']; roc_tpr = roc_curve_data['tpr']
                html.append("<pre>")
                for i in range(0, len(roc_fpr), max(1, len(roc_fpr)//6)):
                    html.append(f"FPR={roc_fpr[i]:.3f} TPR={roc_tpr[i]:.3f}")
                html.append("</pre>")
            html.append("<h2>Early Stopping</h2>")
            html.append(f"<p>Klassisch Patience={patience}, Median Window={median_window}, Median Patience={median_patience}</p>")
            html.append("<h2>JSON Artefakte</h2><ul>")
            if pr_curve: html.append("<li>pr_curve.json</li>")
            if roc_curve_data: html.append("<li>roc_curve.json</li>")
            if test_metrics: html.append("<li>metrics_final.json (mit Test Block)</li>")
            html.append("</ul>")
            html.append("</body></html>")
            html_path.write_text('\n'.join(html), encoding='utf-8')
            print(f"[REPORT] HTML erstellt: {html_path}")
        except Exception as e:
            print(f"[REPORT][WARN] HTML Report fehlgeschlagen: {e}")

    # ROC Curve (Val) after PR if requested
    if args.roc_curve:
        try:
            print('[ROC] Berechne ROC Kurve...')
            from sklearn.metrics import roc_curve, auc
            model.eval()
            all_logits = []
            all_labels = []
            with torch.no_grad():
                for vx, vy in val_loader:
                    vx = vx.to(device)
                    out = model(vx)
                    all_logits.append(out.detach().cpu())
                    all_labels.append(vy.detach().cpu())
            import torch as _t
            logits_cat = _t.cat(all_logits, dim=0)
            labels_cat = _t.cat(all_labels, dim=0)
            probs = logits_cat.softmax(dim=1)
            num_cls = probs.size(1)
            import numpy as _np
            # One-vs-rest macro averaging
            tprs = []
            fprs = []
            for c in range(num_cls):
                y_true = (labels_cat == c).numpy().astype(int)
                y_score = probs[:, c].numpy()
                fpr, tpr, _ = roc_curve(y_true, y_score)
                # Interpolate onto unified grid
                grid = _np.linspace(0,1,101)
                tpr_interp = _np.interp(grid, fpr, tpr)
                tprs.append(tpr_interp)
            macro_tpr = _np.mean(tprs, axis=0).tolist()
            macro_fpr = grid.tolist()
            roc_auc = auc(grid, _np.mean(tprs, axis=0))
            roc_curve_data = {'fpr': macro_fpr, 'tpr': macro_tpr, 'macro_roc_auc': float(roc_auc)}
            with open(out_dir / 'roc_curve.json', 'w', encoding='utf-8') as f:
                json.dump(roc_curve_data, f, indent=2)
            print(f"[ROC] Fertig (macro ROC AUC={roc_auc:.4f}) -> roc_curve.json")
            if tb_writer:
                try:
                    import matplotlib.pyplot as plt, io, numpy as _np2, PIL.Image as _PIL
                    fig, ax = plt.subplots()
                    ax.plot(macro_fpr, macro_tpr)
                    ax.set_xlabel('FPR'); ax.set_ylabel('TPR'); ax.set_title(f'Macro ROC (AUC={roc_auc:.3f})')
                    buf = io.BytesIO(); fig.savefig(buf, format='png'); buf.seek(0)
                    img = _PIL.open(buf)
                    tb_writer.add_image('val/macro_roc_curve', _np2.array(img).transpose(2,0,1), 0)
                    plt.close(fig)
                except Exception as e:
                    print(f"[ROC][WARN] ROC TB Logging fehlgeschlagen: {e}")
        except Exception as e:
            print(f"[ROC][WARN] Berechnung fehlgeschlagen: {e}")

    # Save misclassification thumbnails
    if args.save_miscls > 0:
        try:
            from PIL import Image
            print(f"[MISCLS] Sammle Fehlklassifikationen (Top {args.save_miscls}) ...")
            model.eval()
            mis = []  # (loss, path, true, pred)
            softmax = torch.nn.Softmax(dim=1)
            with torch.no_grad():
                for s in val_ds.indices:  # type: ignore
                    img_t, label = val_ds.dataset[s]  # type: ignore
                    if not isinstance(img_t, torch.Tensor):
                        continue
                    x = img_t.unsqueeze(0).to(device)
                    out = model(x)
                    prob = softmax(out)
                    loss_val = float(criterion(out, torch.tensor([label], device=device)))
                    pred = int(out.argmax(dim=1).item())
                    if pred != label:
                        sample_obj = full_ds.samples[s]  # type: ignore
                        mis.append((loss_val, sample_obj.path, label, pred))
            mis.sort(key=lambda x: x[0], reverse=True)
            export_dir = out_dir / 'misclassifications'
            export_dir.mkdir(exist_ok=True, parents=True)
            for idx, (loss_val, path, y_true, y_pred) in enumerate(mis[:args.save_miscls]):
                try:
                    im = Image.open(path).convert('RGB')
                    im.thumbnail((256,256))
                    save_name = f"{idx:03d}_loss{loss_val:.3f}_t{y_true}_p{y_pred}.jpg"
                    im.save(export_dir / save_name, format='JPEG', quality=90)
                except Exception:
                    pass
            print(f"[MISCLS] Gespeichert: {min(len(mis), args.save_miscls)} Thumbnails -> {export_dir}")
        except Exception as e:
            print(f"[MISCLS][WARN] Export fehlgeschlagen: {e}")

if __name__ == '__main__':
    main()
