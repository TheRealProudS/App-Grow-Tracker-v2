"""Metrics utilities: per-class precision/recall/F1 & confusion matrix.

Usage:
tracker = MetricsTracker(num_classes)
for batch:
    tracker.update(preds, labels)
print(tracker.compute())
"""
from __future__ import annotations
import numpy as np
from dataclasses import dataclass
from typing import Dict, Any

@dataclass
class MetricsResult:
    macro_f1: float
    per_class: Dict[int, Dict[str, float]]
    confusion: np.ndarray

class MetricsTracker:
    def __init__(self, num_classes: int):
        self.num_classes = num_classes
        self.cm = np.zeros((num_classes, num_classes), dtype=np.int64)

    def update(self, preds, labels):
        # preds: tensor (N, num_classes) logits or probs OR shape (N,) indices
        # labels: (N,)
        import torch  # local import for optional dependency
        if preds.dim() == 2:
            pred_idx = preds.argmax(dim=1)
        else:
            pred_idx = preds
        pred_idx = pred_idx.detach().cpu().numpy()
        labels_np = labels.detach().cpu().numpy()
        for p, t in zip(pred_idx, labels_np):
            if 0 <= t < self.num_classes and 0 <= p < self.num_classes:
                self.cm[t, p] += 1

    def compute(self) -> MetricsResult:
        tp = np.diag(self.cm).astype(float)
        fp = self.cm.sum(axis=0) - tp
        fn = self.cm.sum(axis=1) - tp
        precision = np.divide(tp, tp + fp + 1e-9)
        recall = np.divide(tp, tp + fn + 1e-9)
        f1 = 2 * precision * recall / (precision + recall + 1e-9)
        per_class = {
            i: {
                'precision': float(precision[i]),
                'recall': float(recall[i]),
                'f1': float(f1[i]),
                'support': int(self.cm[i].sum())
            } for i in range(self.num_classes)
        }
        macro_f1 = float(np.nanmean(f1))
        return MetricsResult(macro_f1=macro_f1, per_class=per_class, confusion=self.cm.copy())
