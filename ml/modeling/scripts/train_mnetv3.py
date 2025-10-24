"""Training skeleton for LeafSense MobileNetV3-Large v1.
Fill in dataset loading & augmentation specifics once images are available.
"""
from __future__ import annotations
import argparse
from pathlib import Path
import json

import torch
import torch.nn as nn
from torch.utils.data import DataLoader

# Optional: torchvision >= 0.15
from torchvision import models, transforms


def build_model(num_classes: int) -> nn.Module:
    model = models.mobilenet_v3_large(weights=models.MobileNet_V3_Large_Weights.IMAGENET1K_V1)
    in_features = model.classifier[0].in_features
    model.classifier = nn.Sequential(
        nn.Linear(in_features, 1024),
        nn.Hardswish(),
        nn.Dropout(p=0.15),
        nn.Linear(1024, num_classes)
    )
    return model


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--data-root', type=Path, required=True)
    parser.add_argument('--labels-csv', type=Path, required=True)
    parser.add_argument('--out-dir', type=Path, required=True)
    parser.add_argument('--epochs', type=int, default=35)
    parser.add_argument('--batch-size', type=int, default=64)
    parser.add_argument('--lr-head', type=float, default=3e-3)
    parser.add_argument('--lr-ft', type=float, default=8e-4)
    parser.add_argument('--seed', type=int, default=42)
    args = parser.parse_args()

    args.out_dir.mkdir(parents=True, exist_ok=True)
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

    # TODO: load label map & dataset splits
    # Placeholder: discover classes from subfolders
    class_names = sorted([d.name for d in args.data_root.iterdir() if d.is_dir()])
    with open(args.out_dir / 'classes.json', 'w') as f:
        json.dump(class_names, f, indent=2)

    model = build_model(num_classes=len(class_names))
    model.to(device)

    # Freeze backbone (Phase 1)
    for name, p in model.named_parameters():
        if not name.startswith('classifier'):
            p.requires_grad = False

    # Dataloaders (PLACEHOLDER)
    transform_train = transforms.Compose([
        transforms.Resize(256),
        transforms.RandomResizedCrop(224, scale=(0.85, 1.0)),
        transforms.RandomHorizontalFlip(p=0.25),
        transforms.ColorJitter(brightness=0.2, contrast=0.2, saturation=0.25, hue=0.02),
        transforms.ToTensor(),
        # TODO: add normalization once stats computed
    ])
    transform_val = transforms.Compose([
        transforms.Resize(256),
        transforms.CenterCrop(224),
        transforms.ToTensor(),
    ])

    # TODO dataset class + Weighted sampler
    train_loader = DataLoader([], batch_size=args.batch_size)
    val_loader = DataLoader([], batch_size=args.batch_size)

    criterion = nn.CrossEntropyLoss()  # TODO: weighted CE after class counts
    optimizer = torch.optim.AdamW(filter(lambda p: p.requires_grad, model.parameters()), lr=args.lr_head, weight_decay=1e-4)
    scheduler = torch.optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=args.epochs)

    for epoch in range(args.epochs):
        model.train()
        # TRAIN LOOP PLACEHOLDER
        # for batch in train_loader: ...

        model.eval()
        # VAL LOOP PLACEHOLDER
        # with torch.no_grad(): ...

        scheduler.step()

    torch.save(model.state_dict(), args.out_dir / 'model_phase1.pth')
    print('Training skeleton complete (no real data).')


if __name__ == '__main__':
    main()
