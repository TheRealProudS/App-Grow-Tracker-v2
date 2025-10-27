# Changelog

All notable changes to this project will be documented in this file.

## v1.0.2 (BETA)

- Daten
	- Vollständiger SeedFinder A–Z Import als JSON (res/raw) integriert und zur Laufzeit mit Basisdaten + kuratierten Ergänzungen gemergt.
	- Alias-Normalisierung für Hersteller (z. B. „barneys farm“ → „Barney’s Farm“, „white label“ → „White Label Seeds“, „greenhouse seeds“ → „Green House Seeds“, „th seeds“ → „T.H.Seeds“, „world of seeds bank“ → „World Of Seeds“, „zambeza“ → „Zambeza Seeds“).
	- Duplikatvermeidung bei Strains (case-insensitive) im Merge-Prozess.
- UI/UX
	- Hersteller-Dropdown zeigt die komplette Liste; Strain-Dropdown abhängig vom gewählten Hersteller.
	- Suchbox im Strain-Dropdown und jetzt auch im Hersteller-Dropdown.
	- Strain-Anzahl je Hersteller im Hersteller-Dropdown sichtbar.
	- Manuelles Öffnen der Strain-Liste (kein Auto-Open), Verhalten wie gewünscht beibehalten.
- Performance
	- Dropdown-Rendering optimiert (scrollbare Liste mit begrenzter Höhe) für flüssiges Scrollen bei sehr großen Listen.
	- Vorab-Berechnung der Strain-Counts zur Vermeidung teurer Lookups pro Item.
- Stabilität
	- Fix für seltenen Absturz beim Öffnen von Dropdowns (sichere Scroll-Variante im Menü und entfernte Press-basierte Sofort-Umschaltung).
	- Ersetzung der deprecated API: Divider() → HorizontalDivider().
- Guide
	- Beleuchtungsguide: Hinweis zur Photone-App (PAR, PPFD, DLI, Lux, Foot‑Candles, Farbtemperatur) ergänzt.
	- Zusatz: Für genaue Messungen ist ein Diffusor erforderlich (kaufbar oder DIY).
- Tooling
	- SeedFinder-Ingestion-Tool (Kotlin/JSoup) erweitert: Ausgabe als stabile JSON, --out Flag, A–Z Batch unterstützt.
- Meta
	- Version auf 1.0.2 (versionCode 3) angehoben; Standard Release Notes auf „BETA-Version 1.0.2“ aktualisiert.

## v1.0.1 (BETA)

- Data: Added Royal Queen Seeds "Orion F1" (CBD <1, THC not published on product page).
- UI: Fixed FloatingActionButton overlap on plant detail entries when the calendar is expanded.
- Build: Removed AGP warning by disabling minify on debuggable macrobenchmark release build; app release remains minified and non-debuggable.
- Meta: Bumped versionName to 1.0.1 and versionCode to 2; updated default App Distribution release notes.

## v1.0.0 (BETA)

- Initial beta release preparations: optional Firebase integration (Analytics/Crashlytics), signing & CI release workflow, dataset expansions (RQS, SpeedRunSeeds), and general hardening.
