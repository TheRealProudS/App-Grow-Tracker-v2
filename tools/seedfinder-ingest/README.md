SeedFinder Alphabetical Parser

This tiny Kotlin/JVM tool scrapes SeedFinder alphabetical pages (A–Z) and outputs a JSON map of breeder -> list of strains.

Usage

- Run with a URL (recommended one letter at a time):
  gradlew :tools:seedfinder-ingest:run --args='https://seedfinder.eu/de/database/strains/alphabetical/X'

- Run with multiple sources at once (space-separated):
  gradlew :tools:seedfinder-ingest:run --args='https://seedfinder.eu/de/database/strains/alphabetical/T https://seedfinder.eu/de/database/strains/alphabetical/U'

- Run against a local HTML file:
  gradlew :tools:seedfinder-ingest:run --args='file:///C:/path/to/T.html'

Notes

- Output is printed to stdout as JSON with stable ordering.
- Paste relevant breeders/strains into StrainRepository (THC/CBD left blank) and normalize names as needed.
- This is a dev-only helper; it is not bundled into the Android app.
