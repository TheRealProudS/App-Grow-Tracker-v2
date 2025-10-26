package com.growtracker.app.data.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * LeafSenseKnowledgeBase
 *
 * Lightweight domain knowledge layer that maps raw model labels to structured guidance.
 * Initially in-memory with optional JSON bootstrap (future: remote sync / cache / version hash).
 */
object LeafSenseKnowledgeBase {
    private val json = Json { ignoreUnknownKeys = true }

    // In-memory registry keyed by canonical label (lowercase)
    private val entries: MutableMap<String, KnowledgeEntry> = mutableMapOf()

    // Version / metadata (can be exposed in statistics screen later)
    @Volatile var version: String = "0.1-local"
        private set
    @Volatile var source: String = "embedded"
        private set

    // Minimal default seed (placeholder content)
    private val seed: List<KnowledgeEntry> = listOf(
        KnowledgeEntry(
            label = "Healthy Leaf",
            category = Category.HEALTH,
            short = "Alles ok",
            symptoms = listOf("Sattes Grün", "Keine Flecken", "Normale Blattspannung"),
            causes = emptyList(),
            actions = listOf(
                Action("Beobachten", "Regelmäßig weiter dokumentieren"),
            ),
            priority = Priority.LOW
        ),
        KnowledgeEntry(
            label = "Nitrogen Deficiency",
            category = Category.DEFICIENCY,
            short = "Stickstoffmangel Hinweis",
            symptoms = listOf("Aufhellung älterer Blätter", "Allgemeine Blässe", "Verlangsamtes Wachstum"),
            causes = listOf("Zu wenig N im Substrat", "Zu hohe pH Bindung", "Ausgelaugtes Medium"),
            actions = listOf(
                Action("Düngung prüfen", "Leicht N-betonten Dünger ergänzen"),
                Action("pH messen", "Substrat/Drain pH prüfen (Zielbereich 6.2 - 6.5)"),
                Action("Beobachten", "Neue Blätter sollten grüner nachwachsen"),
            ),
            priority = Priority.MEDIUM
        ),
        KnowledgeEntry(
            label = "Leaf Stress",
            category = Category.STRESS,
            short = "Allgemeiner Stressfaktor",
            symptoms = listOf("Leichte Blattkräuselung", "Etwas mattes Gewebe"),
            causes = listOf("Hitze / Lichtintensität", "Unregelmäßige Bewässerung"),
            actions = listOf(
                Action("Umgebung prüfen", "Temperatur & VPD checken"),
                Action("Gießrhythmus anpassen", "Konstante leichte Feuchte halten"),
            ),
            priority = Priority.MEDIUM
        ),
        KnowledgeEntry(
            label = "Pest Suspicion",
            category = Category.PEST,
            short = "Schädlingsverdacht",
            symptoms = listOf("Punktförmige Läsionen", "Feine Sprenkel", "Blattunterseite auffällig"),
            causes = listOf("Spinnmilben / Thripse / Blattläuse"),
            actions = listOf(
                Action("Blattunterseiten prüfen", "Mit Lupe / Makro genauer inspizieren"),
                Action("Isolation erwägen", "Pflanze separieren bis Klarheit besteht"),
                Action("Sanfte Behandlung", "Neem / Kaliseife (Blattunterseiten)"),
            ),
            priority = Priority.HIGH
        ),
        // --- New Taxonomy Entries (generic safe guidance) ---
        KnowledgeEntry(
            label = "NUTRIENT_DEF_N",
            category = Category.DEFICIENCY,
            short = "Ältere Blätter hell – N Hinweis",
            symptoms = listOf("Gleichmäßige Aufhellung unten", "Schwächeres Wachstum"),
            causes = listOf("Nährstoffabbau", "Ungleichgewicht im Medium"),
            actions = listOf(
                Action("Nährstoffstatus prüfen", "Substrat / EC / pH kontrollieren"),
                Action("Moderate Ergänzung", "Leichte Erhöhung der Stickstoff-Zufuhr"),
                Action("Verlauf beobachten", "Neue Blätter sollten kräftiger nachwachsen")
            ),
            priority = Priority.MEDIUM
        ),
        KnowledgeEntry(
            label = "NUTRIENT_DEF_P",
            category = Category.DEFICIENCY,
            short = "P-Anzeichen (dunkel / violett)",
            symptoms = listOf("Dunkelgrüne / bläuliche Töne", "Evtl. violette Stiele"),
            causes = listOf("Phosphorbindung im Medium", "Zu niedrige Verfügbarkeit"),
            actions = listOf(
                Action("pH optimieren", "Bereich prüfen & anpassen"),
                Action("Moderate Zuführung", "Phosphorquelle kontrolliert ergänzen"),
                Action("Temperatur prüfen", "Kühle kann Aufnahme hemmen")
            ),
            priority = Priority.MEDIUM
        ),
        KnowledgeEntry(
            label = "NUTRIENT_DEF_K",
            category = Category.DEFICIENCY,
            short = "Randchlorose / Nekrose – K Verdacht",
            symptoms = listOf("Gelbhalo am Blattrand", "Später braune Ränder"),
            causes = listOf("Ungleichgewicht anderer Kationen", "Verbrauch durch Wachstum"),
            actions = listOf(
                Action("Nährstoffprofil prüfen", "Verhältnis zu Ca/Mg ausbalancieren"),
                Action("Sanfte Ergänzung", "Kalium nicht sprunghaft erhöhen"),
                Action("Randverlauf beobachten", "Ausbreitung dokumentieren")
            ),
            priority = Priority.MEDIUM
        ),
        KnowledgeEntry(
            label = "NUTRIENT_DEF_MG",
            category = Category.DEFICIENCY,
            short = "Interveinal hell (ältere Blätter)",
            symptoms = listOf("Zwischenadern hell", "Adern bleiben grün"),
            causes = listOf("Antagonismus durch Kalium", "Auswaschung"),
            actions = listOf(
                Action("Verhältnis K/Mg prüfen", "Überversorgung anderer Ionen vermeiden"),
                Action("Moderate Ergänzung", "Mg-Quelle gering dosieren"),
                Action("Fortschritt tracken", "Fotos im Abstand vergleichen")
            ),
            priority = Priority.MEDIUM
        ),
        KnowledgeEntry(
            label = "NUTRIENT_DEF_FE",
            category = Category.DEFICIENCY,
            short = "Interveinal hell (junge Blätter)",
            symptoms = listOf("Obere neue Blätter blass", "Adern grün"),
            causes = listOf("Hoher pH reduziert Verfügbarkeit", "Wurzelaufnahme limitiert"),
            actions = listOf(
                Action("pH Bereich prüfen", "Zu hoher pH kann Aufnahme hemmen"),
                Action("Sanfte Korrektur", "Keine Überreaktion bei frühen Stadien"),
                Action("Neue Triebe beobachten", "Verbesserung zeigt sich oben")
            ),
            priority = Priority.MEDIUM
        ),
        KnowledgeEntry(
            label = "OVERWATER_STRESS",
            category = Category.STRESS,
            short = "Überwässerungs-Stress",
            symptoms = listOf("Schlaffe / glasige Blätter", "Nasses Substrat"),
            causes = listOf("Zu geringe Trocknungsphasen", "Schwache Drainage"),
            actions = listOf(
                Action("Trocknungsintervall verlängern", "Erst gießen wenn obere Schicht abgetrocknet"),
                Action("Drainage verbessern", "Staunässe vermeiden"),
                Action("Wurzelbereich lüften", "Leichte Substratauflockerung")
            ),
            priority = Priority.MEDIUM
        ),
        KnowledgeEntry(
            label = "UNDERWATER_STRESS",
            category = Category.STRESS,
            short = "Unterwässerungs-Stress",
            symptoms = listOf("Eingerollte Blätter", "Trockene Oberfläche"),
            causes = listOf("Zu lange Intervalle", "Geringe Wasserspeicherung"),
            actions = listOf(
                Action("Gießrhythmus anpassen", "Regelmäßiger in kleineren Mengen"),
                Action("Substrat prüfen", "Struktur & Wasserhaltevermögen"),
                Action("Verlauf dokumentieren", "Turgor nach Gießen beobachten")
            ),
            priority = Priority.MEDIUM
        ),
        KnowledgeEntry(
            label = "HEAT_STRESS",
            category = Category.STRESS,
            short = "Hitze-Stresszeichen",
            symptoms = listOf("Aufwärtskrümmung", "Mattes Gewebe"),
            causes = listOf("Hohe Temperatur", "Geringer Luftaustausch"),
            actions = listOf(
                Action("Temperatur senken", "Luftstrom / Abluft optimieren"),
                Action("Lichtdistanz prüfen", "Übermäßige Strahlung vermeiden"),
                Action("Feuchte kontrollieren", "Extrem niedrige RH vermeiden")
            ),
            priority = Priority.MEDIUM
        ),
        KnowledgeEntry(
            label = "COLD_STRESS",
            category = Category.STRESS,
            short = "Kälte-Stresszeichen",
            symptoms = listOf("Bläuliche Tönung", "Wachstumsstauchung"),
            causes = listOf("Niedrige Umgebungstemp", "Zugluft"),
            actions = listOf(
                Action("Temperatur stabilisieren", "Nachtabsenkung moderat halten"),
                Action("Isolieren", "Kalte Bodenzonen vermeiden"),
                Action("Neue Blätter beobachten", "Farbe normalisiert sich bei Besserung")
            ),
            priority = Priority.MEDIUM
        ),
        KnowledgeEntry(
            label = "LIGHT_BURN",
            category = Category.STRESS,
            short = "Lichtstress / Bleaching",
            symptoms = listOf("Aufhellung oberes Kronendach", "Scharfe Übergänge"),
            causes = listOf("Zu hohe Intensität", "Geringer Abstand"),
            actions = listOf(
                Action("Distanz erhöhen", "Leuchte etwas anheben"),
                Action("Intensität anpassen", "Dimmen oder Photoperiode optimieren"),
                Action("Langsam reagieren", "Keine abrupten großen Änderungen")
            ),
            priority = Priority.MEDIUM
        ),
        KnowledgeEntry(
            label = "FUNGAL_SPOTS_GENERIC",
            category = Category.PEST,
            short = "Flecken – Pilzverdacht",
            symptoms = listOf("Runde / unregelmäßige Spots", "Teilweise konzentrisch"),
            causes = listOf("Hohe Blattfeuchte", "Geringe Luftbewegung"),
            actions = listOf(
                Action("Blattflächen trocknen", "Staunässe vermeiden"),
                Action("Luftzirkulation erhöhen", "Leichter Umluftstrom"),
                Action("Ausbreitung beobachten", "Weitere Blätter prüfen"),
            ),
            priority = Priority.HIGH
        ),
        KnowledgeEntry(
            label = "MILDEW_LIKE",
            category = Category.PEST,
            short = "Pudriger Belag Hinweis",
            symptoms = listOf("Heller Belag", "Ausdehnung über Fläche"),
            causes = listOf("Hohe LF + wenig Luftbewegung"),
            actions = listOf(
                Action("Feuchte senken", "Zielbereich einhalten"),
                Action("Belüftung verbessern", "Leichter Luftstrom"),
                Action("Befall verfolgen", "Entwicklung täglich checken")
            ),
            priority = Priority.HIGH
        ),
        KnowledgeEntry(
            label = "LEAF_PEST_INDICATOR",
            category = Category.PEST,
            short = "Schadsignal an Blatt",
            symptoms = listOf("Fraßstellen", "Punktuelle Saugschäden"),
            causes = listOf("Unspezifische Insektenaktivität"),
            actions = listOf(
                Action("Unterseiten inspizieren", "Feine Spinnweben / Punkte suchen"),
                Action("Früh reagieren", "Leichter mechanischer oder sanfter Mittel-Einsatz"),
                Action("Ausbreitung dokumentieren", "Mehrfach prüfen"),
            ),
            priority = Priority.HIGH
        ),
        KnowledgeEntry(
            label = "NECROSIS_EDGE",
            category = Category.STRESS,
            short = "Unspezifische Randnekrose",
            symptoms = listOf("Dunkle Ränder", "Teils ohne Gelbhalo"),
            causes = listOf("Trocken-/Feuchtewechsel", "Salzakkumulation"),
            actions = listOf(
                Action("Substrat spülen prüfen", "Moderate Maßnahme bei starker Salzlast"),
                Action("Bewässerung stabilisieren", "Regelmäßigkeit erhöhen"),
                Action("Fortschritt tracken", "Vergleichsbilder anlegen")
            ),
            priority = Priority.MEDIUM
        ),
        KnowledgeEntry(
            label = "GENERAL_CHLOROSIS",
            category = Category.DEFICIENCY,
            short = "Diffuse Aufhellung (unspezifisch)",
            symptoms = listOf("Helle Blattflächen", "Ohne klares Interveinalmuster"),
            causes = listOf("Frühes Ungleichgewicht", "Leichte Stresskombination"),
            actions = listOf(
                Action("Nicht überreagieren", "Gezielt weitere Beobachtungen sammeln"),
                Action("Basisparameter prüfen", "pH / Feuchte / Temperatur"),
                Action("Neu austreibende Blätter checken", "Verbesserung zeigt Korrektheit")
            ),
            priority = Priority.LOW
        )
    )

    init {
        seed.forEach { entries[it.label.lowercase()] = it }
    }

    suspend fun loadFromJson(raw: String, newSource: String = "asset", newVersion: String? = null) = withContext(Dispatchers.Default) {
        val wrapper = json.decodeFromString(KnowledgeFile.serializer(), raw)
        entries.clear()
        wrapper.entries.forEach { entries[it.label.lowercase()] = it }
        source = newSource
        newVersion?.let { version = it }
    }

    fun lookup(label: String): KnowledgeEntry? {
        val key = label.lowercase()
        entries[key]?.let { return it }
        // Fuzzy fallback: simple startsWith / contains scan (can be replaced by better scoring)
        return entries.values.firstOrNull { existing ->
            existing.label.equals(label, ignoreCase = true) ||
                existing.label.lowercase().startsWith(key) ||
                existing.label.lowercase().contains(key)
        }
    }

    fun all(): List<KnowledgeEntry> = entries.values.sortedBy { it.label }
}

@Serializable
data class KnowledgeFile(
    val version: String = "0.1",
    val entries: List<KnowledgeEntry>
)

@Serializable
data class KnowledgeEntry(
    val label: String,
    val category: Category,
    val short: String,
    val symptoms: List<String> = emptyList(),
    val causes: List<String> = emptyList(),
    val actions: List<Action> = emptyList(),
    val priority: Priority = Priority.LOW,
    val tags: List<String> = emptyList()
)

@Serializable
data class Action(
    val title: String,
    val detail: String,
    @SerialName("hint") val hint: String? = null
)

@Serializable
enum class Category { HEALTH, DEFICIENCY, STRESS, PEST }

@Serializable
enum class Priority { LOW, MEDIUM, HIGH }
