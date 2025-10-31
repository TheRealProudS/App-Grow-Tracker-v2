# Grow Tracker

Ein moderner, komplett offline nutzbarer Grow-Manager für Android (Jetpack Compose). Verwalte Pflanzen, protokolliere Einträge, plane Phasen, behalte Energieverbrauch und Kosten im Blick – und drucke QR-Labels, die deine Pflanzen in der App direkt öffnen.

## Highlights

- Komplett offline nutzbar (lokale Datenhaltung, keine Cloud-Pflicht)
- QR-Labels pro Pflanze: Scannen öffnet die App direkt auf der Pflanzenseite
  - App installiert → offline Deep Link: `growtracker://p/{plantId}`
  - App nicht installiert → Fallback-Link (Discord Community)
- Live-Statistiken: per Sekunden-Ticker, roter “LIVE”-Indikator
- Energie & Kosten: pro Gerät/Plan täglich/monatlich kWh und €
- Geräte-Modelle: Lampe, Ventilator, Heizung, Befeuchter, Duty-Cycle/Timer
- Phasen & Workflow: Wachstum, Blüte, Trocknen, Fermentation
- UX: Material 3, dunkles/helles Thema, mehrsprachig vorbereitet

## QR-Labels

- Inhalt (Android Intent URI mit App-Fallback):
  ```
  intent://p/{plantId}#Intent;scheme=growtracker;package=com.growtracker.app;S.browser_fallback_url=https://discord.gg/s8qyyWZV4W;end
  ```
- Verhalten:
  - App installiert: Offline-Öffnung über `growtracker://p/{plantId}`
  - App nicht installiert: Öffnet Discord-Link
- In-App-Generator: In den Pflanzeneinstellungen “QR-Code anzeigen & speichern”
  - Vorschau in der App
  - Speichern als PNG in “Downloads” zum Ausdrucken (ECC-Q, geringer Rand)

## Offline-Modus

- Alle Kernfunktionen sind offline nutzbar.
- QR-Links funktionieren offline, sofern die App installiert ist.
- Falls eine Pflanze mit der ID nicht lokal existiert, erscheint ein Hinweis (“Pflanze nicht gefunden”).

## Installation

- Minimum: Android 7.0 (API 24)
- Lade die aktuelle APK aus den Releases herunter und installiere sie.
- Beim ersten Start kannst du direkt Pflanzen anlegen; keine Anmeldung nötig.

## Berechtigungen

- Kamera: nur für die LeafSense-Funktion
- Speichern: QR-Bilder werden über MediaStore in “Downloads” geschrieben

## Entwicklung

- Stack: Kotlin, Jetpack Compose, Navigation, DataStore, WorkManager
- Compose Compiler: 2.0.20; Ziel-SDK: 35; minSdk: 24; JDK: 17
- Build: Gradle Wrapper (AGP 8.x)

### Build lokal

- Debug: `./gradlew :app:assembleDebug`
- Release: `./gradlew :app:assembleRelease`

## Tests & CI

- Unit-Tests: `app/src/test` (werden im Release-Workflow vor dem Build ausgeführt)
- UI-Tests (Compose): `app/src/androidTest` (optional, Emulator erforderlich)
- GitHub Actions Workflow: SDK-Setup, Lizenzannahme, Tests, APK-Build

## Lizenz

Copyright 2025 TheRealProudS - Grow-Tracker

Lizenziert unter der Apache License, Version 2.0 (Apache-2.0). Siehe die Datei `LICENSE` für den vollständigen Lizenztext.

Du darfst die Software frei nutzen, verändern und verbreiten (auch kommerziell), solange du den Lizenztext und Copyright-Hinweise beibehältst. Eine Patentlizenz ist enthalten.

## Haftungsausschluss

Die App dient ausschließlich zur Dokumentation und Planung. Beachte die Gesetze deines Landes hinsichtlich Anbau und Besitz.
