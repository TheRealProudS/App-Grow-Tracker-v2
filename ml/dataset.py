"""Dataset & Transforms for LeafSense training.

Assumptions:
- meta.csv contains at least: filename,label
- Images located under data_root / <label> / <filename>  OR data_root / <filename>
  (We attempt label-subfolder first, then flat structure fallback.)

Future extensions:
- multi-label (labels column with semicolon)
- quality flags filtering
- plant_id grouping for stratified split
"""
from __future__ import annotations
import csv
from dataclasses import dataclass
from pathlib import Path
from typing import List, Tuple, Dict, Callable, Optional

try:
    import torch
    from torch.utils.data import Dataset
    from PIL import Image
except ImportError:  # pragma: no cover - training env required
    Dataset = object  # type: ignore
    Image = None      # type: ignore

@dataclass
class Sample:
    path: Path
    label_index: int
    label_name: str

class PlantDataset(Dataset):  # type: ignore
    def __init__(self,
                 data_root: str,
                 meta_csv: str,
                 class_names: List[str],
                 transform: Optional[Callable] = None,
                 flat_structure: bool = False):
        self.root = Path(data_root)
        self.meta_csv = Path(meta_csv)
        self.class_names = class_names
        self.class_to_idx = {c: i for i, c in enumerate(class_names)}
        self.transform = transform
        self.samples: List[Sample] = []
        self._load(flat_structure=flat_structure)

    def _load(self, flat_structure: bool):
        if not self.meta_csv.exists():
            raise FileNotFoundError(f"meta.csv nicht gefunden: {self.meta_csv}")
        with self.meta_csv.open('r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            if 'filename' not in reader.fieldnames or 'label' not in reader.fieldnames:
                raise ValueError("meta.csv benötigt Spalten: filename,label")
            for row in reader:
                label = row['label'].strip()
                fname = row['filename'].strip()
                if label not in self.class_to_idx:
                    continue  # unbekannte Labels überspringen
                # Primär: label-Unterordner
                p = self.root / label / fname
                if flat_structure or not p.exists():
                    # Alternative: flacher Pfad
                    flat = self.root / fname
                    if flat.exists():
                        p = flat
                if not p.exists():
                    continue
                self.samples.append(Sample(p, self.class_to_idx[label], label))
        if not self.samples:
            raise RuntimeError("Keine gültigen Samples geladen (prüfe Pfade & meta.csv)")

    def __len__(self) -> int:
        return len(self.samples)

    def __getitem__(self, idx: int):
        s = self.samples[idx]
        img = Image.open(s.path).convert('RGB')  # type: ignore
        if self.transform:
            img = self.transform(img)
        return img, s.label_index


def build_transforms(image_size: int = 224, augment_cfg: Dict | None = None):
    """Return training & validation transform pipelines.
    augment_cfg keys (subset): horizontal_flip_prob, color_jitter, gaussian_blur_prob.
    """
    try:
        import torchvision.transforms as T
    except ImportError:  # pragma: no cover
        return None, None

    aug = []
    aug.append(T.Resize(int(image_size * 1.15)))
    aug.append(T.CenterCrop(image_size))  # replaced by RandomResizedCrop if enabled

    if augment_cfg:
        if augment_cfg.get('random_resized_crop', True):
            aug[1] = T.RandomResizedCrop(image_size, scale=(0.75, 1.0))
        if (p := augment_cfg.get('horizontal_flip_prob', 0)) > 0:
            aug.append(T.RandomHorizontalFlip(p=p))
        cj = augment_cfg.get('color_jitter')
        if cj and cj.get('prob', 0) > 0:
            aug.append(T.RandomApply([T.ColorJitter(brightness=cj['brightness'], contrast=cj['contrast'],
                                                    saturation=cj['saturation'], hue=cj['hue'])], p=cj['prob']))
        if (gp := augment_cfg.get('gaussian_blur_prob', 0)) > 0:
            aug.append(T.RandomApply([T.GaussianBlur(kernel_size=3)], p=gp))
    aug.extend([T.ToTensor(), T.Normalize(mean=[0.485,0.456,0.406], std=[0.229,0.224,0.225])])

    train_tf = T.Compose(aug)
    val_tf = T.Compose([
        T.Resize(int(image_size*1.15)),
        T.CenterCrop(image_size),
        T.ToTensor(),
        T.Normalize(mean=[0.485,0.456,0.406], std=[0.229,0.224,0.225])
    ])
    return train_tf, val_tf
