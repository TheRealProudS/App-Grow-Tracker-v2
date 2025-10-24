"""Temperature scaling + threshold selection for open-set / rejection.
Assumes logits & labels exported from a validation set.
"""
from __future__ import annotations
import argparse, json
from pathlib import Path
import torch
import torch.nn as nn
import numpy as np

class TemperatureScaler(nn.Module):
    def __init__(self):
        super().__init__()
        self.temperature = nn.Parameter(torch.ones(1))
    def forward(self, logits):
        return logits / self.temperature.clamp(min=1e-3)

@torch.no_grad()
def expected_calibration_error(probs, labels, n_bins=15):
    bins = torch.linspace(0,1,n_bins+1)
    ece = 0.0
    for i in range(n_bins):
        m = (probs > bins[i]) & (probs <= bins[i+1])
        if m.any():
            acc = (labels[m] == probs[m].argmax(1)).float().mean().item()
            conf = probs[m].max(1).values.mean().item()
            ece += (m.float().mean().item()) * abs(acc - conf)
    return ece

@torch.no_grad()
def auroc_binary(in_scores, out_scores):
    labels = np.concatenate([np.ones_like(in_scores), np.zeros_like(out_scores)])
    scores = np.concatenate([in_scores, out_scores])
    order = np.argsort(-scores)
    labels = labels[order]
    tp = 0; fp = 0
    P = labels.sum(); N = len(labels)-P
    tprs = []; fprs = []
    for l in labels:
        if l == 1: tp += 1
        else: fp += 1
        tprs.append(tp / P if P>0 else 0)
        fprs.append(fp / N if N>0 else 0)
    # Trapezoidal area
    auc = 0.0
    for i in range(1,len(tprs)):
        auc += (fprs[i]-fprs[i-1]) * (tprs[i]+tprs[i-1]) * 0.5
    return auc


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--logits-npy', type=Path, required=True, help='(N,C) float32 logits array for in-distribution validation set')
    ap.add_argument('--labels-npy', type=Path, required=True, help='(N,) int64 labels for logits')
    ap.add_argument('--ood-logits-npy', type=Path, help='(M,C) logits for OOD samples (optional)')
    ap.add_argument('--out', type=Path, default=Path('ml/exports/calibration.json'))
    args = ap.parse_args()

    logits = torch.from_numpy(np.load(args.logits_npy))
    labels = torch.from_numpy(np.load(args.labels_npy))
    model = TemperatureScaler()

    # Optimize temperature (simple LBFGS or Adam)
    optimizer = torch.optim.LBFGS(model.parameters(), lr=0.01, max_iter=60)
    criterion = nn.CrossEntropyLoss()

    def closure():
        optimizer.zero_grad()
        loss = criterion(model(logits), labels)
        loss.backward()
        return loss

    optimizer.step(closure)

    with torch.no_grad():
        scaled = model(logits)
        probs = torch.softmax(scaled, dim=1)
        ece = expected_calibration_error(probs, labels)
        max_probs = probs.max(1).values
        # Default reject threshold: 5th percentile of correct-class confidences
        preds = probs.argmax(1)
        correct_mask = preds == labels
        threshold = max_probs[correct_mask].quantile(0.05).item() if correct_mask.any() else 0.0

        ood_auc = None
        if args.ood_logits_npy and args.ood_logits_npy.exists():
            ood_logits = torch.from_numpy(np.load(args.ood_logits_npy))
            ood_probs = torch.softmax(model(ood_logits), dim=1)
            in_scores = max_probs.numpy()
            out_scores = ood_probs.max(1).values.numpy()
            ood_auc = auroc_binary(in_scores, out_scores)

    args.out.parent.mkdir(parents=True, exist_ok=True)
    args.out.write_text(json.dumps({
        'temperature': model.temperature.item(),
        'ece': ece,
        'reject_threshold': threshold,
        'ood_auc': ood_auc
    }, indent=2))
    print('Saved calibration to', args.out)

if __name__ == '__main__':
    main()
