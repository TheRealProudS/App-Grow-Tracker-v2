"""Training script for LeafSense Stage 1 multi-class condition classifier.
Minimal scaffold (flesh out as dataset becomes available).
"""
from __future__ import annotations
import argparse, json, time, math
from pathlib import Path
from dataclasses import dataclass
import random

import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms, models
from PIL import Image

# Labels will be loaded dynamically from labels_v1.json copied into ml/ or specified via CLI.

def seed_everything(seed: int = 42):
    random.seed(seed)
    torch.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)

@dataclass
class Sample:
    path: Path
    label: int

class SimpleImageDataset(Dataset):
    def __init__(self, root: Path, list_file: Path, label_map: dict[str,int], transform=None):
        self.root = root
        self.transform = transform
        self.samples: list[Sample] = []
        for line in list_file.read_text().strip().splitlines():
            rel, lbl = line.split('\t') if '\t' in line else (line, None)
            if lbl is None:
                # fallback: derive from parent folder
                lbl = Path(rel).parent.name
            if lbl not in label_map:
                continue
            self.samples.append(Sample(root / rel, label_map[lbl]))
    def __len__(self): return len(self.samples)
    def __getitem__(self, idx):
        s = self.samples[idx]
        img = Image.open(s.path).convert('RGB')
        if self.transform:
            img = self.transform(img)
        return img, s.label


def build_model(num_classes: int):
    m = models.mobilenet_v3_large(weights=models.MobileNet_V3_Large_Weights.IMAGENET1K_V2)
    in_f = m.classifier[0].in_features
    m.classifier[-1] = nn.Linear(in_f, num_classes)
    return m


def train_one_epoch(model, loader, criterion, optimizer, device):
    model.train()
    running = 0.0
    correct = 0
    total = 0
    for imgs, labels in loader:
        imgs, labels = imgs.to(device), labels.to(device)
        optimizer.zero_grad()
        out = model(imgs)
        loss = criterion(out, labels)
        loss.backward()
        optimizer.step()
        running += loss.item() * imgs.size(0)
        preds = out.argmax(1)
        correct += (preds == labels).sum().item()
        total += imgs.size(0)
    return running / max(1,total), correct / max(1,total)

@torch.no_grad()
def evaluate(model, loader, criterion, device):
    model.eval()
    running = 0.0
    correct = 0
    total = 0
    for imgs, labels in loader:
        imgs, labels = imgs.to(device), labels.to(device)
        out = model(imgs)
        loss = criterion(out, labels)
        running += loss.item() * imgs.size(0)
        preds = out.argmax(1)
        correct += (preds == labels).sum().item()
        total += imgs.size(0)
    return running / max(1,total), correct / max(1,total)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--data-root', type=Path, default=Path('ml/data/processed'))
    ap.add_argument('--train-list', type=Path, default=Path('ml/data/splits/train.txt'))
    ap.add_argument('--val-list', type=Path, default=Path('ml/data/splits/val.txt'))
    ap.add_argument('--labels-json', type=Path, default=Path('ml/labels_v1.json'))
    ap.add_argument('--epochs', type=int, default=5)
    ap.add_argument('--batch-size', type=int, default=32)
    ap.add_argument('--lr', type=float, default=3e-4)
    ap.add_argument('--outdir', type=Path, default=Path('ml/experiments'))
    ap.add_argument('--seed', type=int, default=42)
    args = ap.parse_args()

    seed_everything(args.seed)
    device = 'cuda' if torch.cuda.is_available() else 'cpu'

    label_names = json.loads(args.labels_json.read_text())
    label_map = {name: i for i, name in enumerate(label_names)}

    transform = transforms.Compose([
        transforms.RandomResizedCrop(224, scale=(0.70, 1.0)),
        transforms.RandomHorizontalFlip(),
        transforms.ColorJitter(0.15,0.15,0.15,0.08),
        transforms.ToTensor(),
        transforms.Normalize([0.485,0.456,0.406],[0.229,0.224,0.225])
    ])

    train_ds = SimpleImageDataset(args.data_root, args.train_list, label_map, transform=transform)
    val_ds = SimpleImageDataset(args.data_root, args.val_list, label_map, transform=transforms.Compose([
        transforms.Resize(256), transforms.CenterCrop(224), transforms.ToTensor(),
        transforms.Normalize([0.485,0.456,0.406],[0.229,0.224,0.225])
    ]))

    train_loader = DataLoader(train_ds, batch_size=args.batch_size, shuffle=True, num_workers=4, pin_memory=True)
    val_loader = DataLoader(val_ds, batch_size=args.batch_size, shuffle=False, num_workers=4, pin_memory=True)

    model = build_model(len(label_map)).to(device)
    criterion = nn.CrossEntropyLoss()
    optimizer = optim.AdamW(model.parameters(), lr=args.lr)
    scheduler = optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=args.epochs)

    run_dir = args.outdir / f"run_{int(time.time())}"
    run_dir.mkdir(parents=True, exist_ok=True)
    (run_dir / 'labels.json').write_text(json.dumps(label_names, indent=2, ensure_ascii=False))

    best_acc = 0
    for epoch in range(1, args.epochs+1):
        tr_loss, tr_acc = train_one_epoch(model, train_loader, criterion, optimizer, device)
        val_loss, val_acc = evaluate(model, val_loader, criterion, device)
        scheduler.step()
        print(f"Epoch {epoch}: train_loss={tr_loss:.4f} train_acc={tr_acc:.3f} val_loss={val_loss:.4f} val_acc={val_acc:.3f}")
        with open(run_dir / 'log.txt', 'a', encoding='utf-8') as f:
            f.write(f"{epoch}\t{tr_loss:.4f}\t{tr_acc:.4f}\t{val_loss:.4f}\t{val_acc:.4f}\n")
        if val_acc > best_acc:
            best_acc = val_acc
            torch.save({'model': model.state_dict(), 'epoch': epoch}, run_dir / 'best.pt')

    # Export final
    torch.save({'model': model.state_dict(), 'epoch': epoch}, run_dir / 'last.pt')
    print('Training complete. Best acc:', best_acc)

if __name__ == '__main__':
    main()
