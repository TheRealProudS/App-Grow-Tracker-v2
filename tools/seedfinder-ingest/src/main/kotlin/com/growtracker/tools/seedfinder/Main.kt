package com.growtracker.tools.seedfinder

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File

/**
 * SeedFinder alphabetical page parser.
 *
 * Usage examples:
 *  - Parse a URL: gradlew :tools:seedfinder-ingest:run --args="https://seedfinder.eu/de/database/strains/alphabetical/T"
 *  - Parse a local HTML file: gradlew :tools:seedfinder-ingest:run --args="file:///C:/path/to/T.html"
 *  - Multiple sources: provide space-separated URLs/paths
 *
 * Output: JSON to stdout of the form { "manufacturer": ["Strain A", "Strain B", ...], ... }
 * You can redirect stdout to a file and post-process or paste into StrainRepository manually.
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        System.err.println("Please provide one or more SeedFinder alphabetical page URLs or file paths.")
        System.exit(2)
    }

    val sources = mutableListOf<String>()
    var knownPath: String? = null
    var outPath: String? = null
    args.forEach { arg ->
        when {
            arg.startsWith("--known=") -> knownPath = arg.removePrefix("--known=")
            arg.startsWith("--out=") -> outPath = arg.removePrefix("--out=")
            arg.startsWith("--") -> { /* ignore unknown flags */ }
            else -> sources += arg
        }
    }
    if (sources.isEmpty()) {
        System.err.println("No sources provided. Pass URLs/file paths, e.g. --args='https://.../X' or a local file path.")
        System.exit(2)
    }

    val map = linkedMapOf<String, MutableSet<String>>()

    sources.forEach { input ->
        val doc = loadDocument(input)
        val rows = selectTableRows(doc)
        for (row in rows) {
            val cols = row.select("td")
            if (cols.size < 2) continue
            val strain = cols[0].text().trim()
            val breeder = cols[1].text().trim()
            if (strain.isBlank() || breeder.isBlank()) continue
            map.getOrPut(breeder) { linkedSetOf() }.add(strain)
        }
    }

    // Optional: load known manufacturers to compute a delta summary
    val knownManufacturers = knownPath?.let { loadKnownManufacturers(it) } ?: emptySet()
    if (knownPath != null) {
        val newManufacturers = map.keys.filterNot { it in knownManufacturers }.sortedWith(String.CASE_INSENSITIVE_ORDER)
        System.err.println("Known manufacturers loaded: ${knownManufacturers.size}")
        System.err.println("New manufacturers found: ${newManufacturers.size}")
        if (newManufacturers.isNotEmpty()) {
            System.err.println(newManufacturers.joinToString(prefix = " - ", separator = "\n - "))
        }
    }

    // Emit stable JSON (sorted manufacturers and strains)
    val sorted = map.toSortedMap(compareBy(String.CASE_INSENSITIVE_ORDER, { it }))
    val json = buildString {
        append("{\n")
        var firstM = true
        for ((breeder, strains) in sorted) {
            if (!firstM) append(",\n")
            firstM = false
            append("  \"").append(escape(breeder)).append("\": ")
            val ordered = strains.toList().sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
            append("[")
            append(ordered.joinToString(", ") { "\"" + escape(it) + "\"" })
            append("]")
        }
        append("\n}\n")
    }
    if (outPath == null) {
        println(json)
    } else {
        try {
            val outFile = File(outPath!!)
            outFile.parentFile?.mkdirs()
            outFile.writeText(json, Charsets.UTF_8)
            System.err.println("Wrote SeedFinder JSON to: ${outFile.absolutePath}")
        } catch (e: Exception) {
            System.err.println("Failed to write output file: ${e.message}")
            System.exit(3)
        }
    }
}

private fun loadDocument(input: String): Document {
    return try {
        if (input.startsWith("http://") || input.startsWith("https://")) {
            Jsoup.connect(input)
                .userAgent("Mozilla/5.0 (SeedFinderIngest/1.0; +https://github.com)")
                .timeout(30_000)
                .get()
        } else if (input.startsWith("file://")) {
            val path = input.removePrefix("file://")
            Jsoup.parse(File(path), Charsets.UTF_8.name())
        } else {
            // treat as local file path
            Jsoup.parse(File(input), Charsets.UTF_8.name())
        }
    } catch (e: Exception) {
        throw RuntimeException("Failed to load $input: ${e.message}", e)
    }
}

private fun selectTableRows(doc: Document) =
    run {
        // SeedFinder uses tables with column headers including "Strain" and "Breeder"; find the first matching headered table
        val headerTables = doc.select("table:has(th)")
        val target = headerTables.firstOrNull { t ->
            val headers = t.select("th").map { it.text().lowercase() }
            headers.any { it.contains("strain") } && headers.any { it.contains("breeder") }
        } ?: headerTables.firstOrNull() ?: doc.selectFirst("table")
        target?.select("tbody tr, tr") ?: emptyList()
    }

private fun escape(s: String): String = buildString {
    for (ch in s) when (ch) {
        '\\' -> append("\\\\")
        '"' -> append("\\\"")
        '\n' -> append("\\n")
        '\r' -> append("\\r")
        '\t' -> append("\\t")
        else -> append(ch)
    }
}

private fun loadKnownManufacturers(path: String): Set<String> {
    return try {
        val file = if (path.startsWith("file://")) File(path.removePrefix("file://")) else File(path)
        if (!file.exists()) emptySet() else {
            val regex = Regex("name\\s*=\\s*\"([^\"]+)\"", RegexOption.IGNORE_CASE)
            file.readLines().mapNotNull { line ->
                val m = regex.find(line)
                m?.groupValues?.getOrNull(1)
            }.toSet()
        }
    } catch (_: Exception) {
        emptySet()
    }
}
