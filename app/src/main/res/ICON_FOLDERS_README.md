# 📝 README - Icon Ordner erstellt

Die folgenden Ordner wurden für PNG-Icons erstellt:

## 📁 Icon-Ordner Structure:
```
mipmap-mdpi/     (48x48px)   - Niedrige Auflösung
mipmap-hdpi/     (72x72px)   - Hohe Auflösung  
mipmap-xhdpi/    (96x96px)   - Extra hohe Auflösung
mipmap-xxhdpi/   (144x144px) - Extra extra hohe Auflösung
mipmap-xxxhdpi/  (192x192px) - Ultra hohe Auflösung
```

## 🎯 So fügst du PNG-Icons hinzu:

1. **Erstelle dein Icon in 512x512px**
2. **Skaliere es auf die verschiedenen Größen:**
   - 48x48px → speichere als `ic_launcher.png` in `mipmap-mdpi/`
   - 72x72px → speichere als `ic_launcher.png` in `mipmap-hdpi/`
   - 96x96px → speichere als `ic_launcher.png` in `mipmap-xhdpi/`
   - 144x144px → speichere als `ic_launcher.png` in `mipmap-xxhdpi/`
   - 192x192px → speichere als `ic_launcher.png` in `mipmap-xxxhdpi/`

3. **Für runde Icons (optional):**
   - Erstelle auch `ic_launcher_round.png` in allen Ordnern

## ⚠️ Wichtige Hinweise:

- **Dateiname:** Exakt `ic_launcher.png` (kleinschreibung!)
- **Format:** PNG mit transparentem Hintergrund
- **Design:** Sollte bei 48px noch gut erkennbar sein
- **Installation:** App muss neu installiert werden für Icon-Änderungen

## 🔄 Alternative - Vector Drawable:
Falls du Vector Drawables bevorzugst, sind diese bereits angepasst:
- `ic_launcher_background.xml` - Dunkelgrüner Gradient
- `ic_launcher_foreground.xml` - Moderne Pflanze mit Wachstums-Indikatoren

## 🛠️ Tools zum Skalieren:
- **Online:** https://resizeimage.net/
- **App:** GIMP, Photoshop, Figma
- **Android Studio:** Rechtsklick → New → Image Asset
