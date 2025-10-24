# LeafSense Annotation & Label Schema (v1)

## 1. Ziel & Geltungsbereich
Single-Label Klassifikation einzelner Blattfotos in definierte Zustandsklassen. Mehrfachbefall wird auf die klinisch dominanteste Erscheinung reduziert. Unsichere Fälle werden konservativ einer generischen Klasse (GENERAL_CHLOROSIS / NECROSIS_EDGE) oder gar nicht aufgenommen.

## 2. Klassen (Primär v1)
```
HEALTHY
NUTRIENT_DEF_N
NUTRIENT_DEF_P
NUTRIENT_DEF_K
NUTRIENT_DEF_MG
NUTRIENT_DEF_FE
OVERWATER_STRESS
UNDERWATER_STRESS
HEAT_STRESS
COLD_STRESS
LIGHT_BURN
FUNGAL_SPOTS_GENERIC
MILDEW_LIKE
LEAF_PEST_INDICATOR
NECROSIS_EDGE
GENERAL_CHLOROSIS
```
Reserve / Zukunft: CALCIUM_DEF, SULFUR_DEF, ROOT_ISSUE_SIGNAL

## 3. Entscheidungsbaum (Kurzfassung)
1. Pudriger / mehliger Belag? → MILDEW_LIKE  
2. Runde/dunkle Spots (≠ Belag)? → FUNGAL_SPOTS_GENERIC  
3. Fraßlöcher / Saugschäden? → LEAF_PEST_INDICATOR  
4. Stark gebleichte obere Kronenbereiche? → LIGHT_BURN  
5. Randnekrosen?  
   - Mit gelbem Halo vor Braun → NUTRIENT_DEF_K  
   - Ohne klares Vorstadium → NECROSIS_EDGE  
6. Interveinale Chlorose?  
   - Junge Blätter zuerst → NUTRIENT_DEF_FE  
   - Ältere Blätter zuerst → NUTRIENT_DEF_MG  
7. Gleichmäßige Gelbfärbung älterer Blätter → NUTRIENT_DEF_N  
8. Violette / bläuliche Tönung + evtl. Stielverfärbung → NUTRIENT_DEF_P  
9. Turgor / Wasserstress?  
   - Boden trocken / eingerollt → UNDERWATER_STRESS  
   - Boden nass / glasig / geschwollen → OVERWATER_STRESS  
10. Hitze-Morphologie (canoeing / aufwärtsgekrümmt) → HEAT_STRESS  
11. Kalte Verfärbungen (dunkel/bläulich) → COLD_STRESS  
12. Diffuse unspezifische Aufhellung → GENERAL_CHLOROSIS  
13. Keine Auffälligkeit → HEALTHY

## 4. Detaillierte Klassenkriterien
### HEALTHY
Homogen grün, normale Variation ok; keine Spots, kein diffuser Chlorose- oder Nekroseanteil > 5%.  
Nicht verwenden bei: >15% gelblich / bleich.

### NUTRIENT_DEF_N
Ältere Blätter zuerst gleichmäßig gelblich (Adern nicht deutlich dunkel). Später gesamtes Blatt.  
Abgrenzung: Mg/Fe zeigen Interveinal-Muster.

### NUTRIENT_DEF_P
Dunkelgrün/bläulich oder violette Stiel-/Rippenfärbung, teils düstere Blattfläche, vereinzelte Nekrosen.  
Unsicher? → Nicht erzwingen → GENERAL_CHLOROSIS.

### NUTRIENT_DEF_K
Gelber Halo an Rändern → Übergang zu brauner Randnekrose. Zentrum länger grün.  
Ohne klare Gelbphase → NECROSIS_EDGE.

### NUTRIENT_DEF_MG
Interveinale Chlorose mittlere/untere Blattetage, Adern grün bleibend.  
Junges Blatt nicht zuerst betroffen.

### NUTRIENT_DEF_FE
Interveinale Chlorose an jungen oberen Blättern, sehr hell bis fast weiß, Adern länger grün.  
Alter wichtig für Abgrenzung zu Mg.

### OVERWATER_STRESS
Schlaff/glasig, gedunkelte Erde (wenn sichtbar), keine typische Mangelmusterung.  
Nicht mit UNDERWATER verwechseln (dort trocken/knitterig).

### UNDERWATER_STRESS
Dehydriert, eingerollte oder nach unten hängende Blätter, matte Oberfläche.  
Keine glasige Struktur.

### HEAT_STRESS
Aufwärts gebogene (canoeing) oder verbogene Blattenden, leichte Spitzennekrosen.  
Ohne flächiges Bleaching (das wäre LIGHT_BURN).

### COLD_STRESS
Dunkelgrün/bläulich, punktuelle dunkle Nekrosen, ggf. Wachstumsstauchung.  
Vorsicht vs. P-Mangel: P hat häufiger violette Stiele.

### LIGHT_BURN
Bleaching / Weißaufhellung oberer Blattetagen, scharfe Grenzen gesund ↔ gebleicht.  
Nicht mit N-Mangel (gleichmäßig) verwechseln.

### FUNGAL_SPOTS_GENERIC
Diskrete Läsionen (Flecken), runde/irreguläre Spots, kein flächiger Belag.  
Frühe Mikro-Flecken (<3 unscharf) → lieber ignorieren.

### MILDEW_LIKE
Pulvrig-mehliger, heller Überzug. Anfang: kleine diffuse Punkte.  
Nicht mit Staub/Schmutz verwechseln (Textur beachten).

### LEAF_PEST_INDICATOR
Fraßlöcher, Skelettierung, silbrige Saugspuren, punktförmig entchlorophyllisierte Stellen.  
Keine eindeutig pathogene Fleckenstruktur.

### NECROSIS_EDGE
Braune/schwarze Randnekrose ohne klaren vorherigen gelben Halo → unspezifisch.  
Wenn deutlicher gelber Saum: NUTRIENT_DEF_K.

### GENERAL_CHLOROSIS
Diffus hell, kein scharfes Interveinalmuster, keine dominanten anderen Marker.  
Verwendung statt Ratespiel bei Unsicherheit.

## 5. Ausschlusskriterien
Bild verwerfen wenn:
- Starke Bewegungsunschärfe (Konturen unlesbar)
- <50% Blatt sichtbar oder dominante Occlusion
- Dominante Schatten verdecken Muster >30%
- Aggressiver Filter / Farbstich (künstlich)

## 6. Metadaten & CSV
`labels_v1.csv` Felder:
```
file_name,class_label,source,lighting,leaf_position,notes
```
- lighting: sonne_diffus|sonne_direkt|kunst_led|misch|unbekannt
- leaf_position: oben|mitte|unten|unbekannt
- notes: frei, keine Kommata (oder escapen)

## 7. File Naming Empfehlung
`<class>__<hash|uuid>__src-<code>__seq-###.jpg`

## 8. Qualitätskontrolle
Automatisch:
- pHash (Hamming Dist <4) → mögliche Duplikate
- Blur (Laplacian Var < threshold) → Review
- Histogram Clipping (Über-/Unterbelichtung) → Flag

Manuell (Stichprobe ≥100):
- Fehlklassifikationen <5%
- Cohen's Kappa (Doppelannotiert) ≥0.8

## 9. Severity (Optional, v2)
Skala (mild|moderate|severe):
- mild <15% Fläche
- moderate 15–40%
- severe >40% oder funktional kritisch
Nicht aktivieren solange Unsicherheit hoch.

## 10. Häufige Verwechslungen
- Mg vs Fe → Blattalter prüfen.
- Light Burn vs N-Mangel → Scharfer Übergang + nur Kronendach → Light Burn.
- K-Mangel vs Necrosis Edge → Gelbhalo vorhanden? Ja → K, Nein → Necrosis Edge.
- P-Mangel vs Cold Stress → Violette Stiele vs. dunkle bläuliche Gesamttönung.

## 11. Review Workflow
1. Annotator A erstellt Label.
2. 10–15% Sample Annotator B.
3. Konflikte -> Regel in Abschnitt 4 präzisieren.
4. Freeze → `split_v1.csv` generieren.

## 12. Versionierung
- Keine stillen Änderungen im Freeze; stattdessen dataset minor/pach update.
- Änderungen an Klassendefinition → MAJOR increment.

## 13. Integrität
Nach Freeze: SHA256 Liste aller Dateien + Größen in `metadata/integrity_v1.txt`.

## 14. Do & Don't Quick Ref
Do:
- Konservativ labeln bei Unsicherheit.
- Kontext (Blattalter) nutzen.
- Unscharfe / halbe Blätter ausfiltern.
Don't:
- Zwei Klassen auf ein Bild erzwingen.
- Leichte JPEG Artefakte als Spots interpretieren.
- Hintergrundartefakte als krankhafte Muster verwechseln.

---
*Maintainers aktualisieren dieses Dokument bei jeder Regelverfeinerung.*
