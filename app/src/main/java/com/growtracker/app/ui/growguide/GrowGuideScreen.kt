package com.growtracker.app.ui.growguide

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
// use fully-qualified material icon references to avoid receiver/import mismatches
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growtracker.app.ui.language.LanguageManager
import com.growtracker.app.ui.language.Strings
import com.growtracker.app.ui.language.getString

// Data class for guide categories
data class GuideCategory(
    val title: String,
    val icon: ImageVector,
    val intro: String,
    val bullets: List<String> = emptyList(),
    val tip: String? = null
)

@Composable
fun ExpandableGuideCard(category: GuideCategory, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Zuklappen" else "Aufklappen",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expandable Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Intro
                    Text(
                        text = category.intro,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    // Bullets
                    if (category.bullets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            category.bullets.forEach { b ->
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Filled.Circle,
                                        contentDescription = null,
                                        modifier = Modifier.size(8.dp).padding(top = 6.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = b, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // Highlighted Tip
                    category.tip?.let { t ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Filled.Lightbulb, contentDescription = "Tipp", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = "Praxis‑Tipp", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = t, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowGuideScreen(
    modifier: Modifier = Modifier,
    languageManager: LanguageManager,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = getString(Strings.overview_grow_guide, languageManager),
                    fontWeight = FontWeight.Bold
                )
            },
                navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Zurück"
                    )
                }
            }

        )

        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val guideCategories = listOf(
                GuideCategory(
                    title = "Grundausstattung",
                    icon = Icons.Filled.Build,
                    intro = "Kurzer Überblick und Prioritäten.",
                    bullets = listOf(
                        "Raum & Sicherheit: stabile Stromversorgung, ausreichend abgesicherte Steckdosen, Brandschutz beachten",
                        "Beleuchtung & Luft: hochwertige Lichtquelle und Frischluftversorgung als Kerninvestition",
                        "Substrat & Töpfe: gut drainierende Erde oder abgestimmte Hydro/Coco-Systeme",
                        "Messgeräte: Timer, Hygrometer/Thermometer, pH‑Meter, EC/PPM‑Messgerät"
                    ),
                    tip = "Plane Kabelführung sicher und kaufe robuste Befestigungen; messe Klima und pH regelmäßig statt zu raten."
                ),
                GuideCategory(
                    title = "Beleuchtungsguide",
                    icon = Icons.Filled.WbSunny,
                    intro = "Wichtige Kenngrößen für Lichtplanung.",
                    bullets = listOf(
                        "PPFD‑Zielwerte (Richtwerte): Keimling 100–250; Vegetativ 250–500; Blüte 500–900 µmol/m²/s",
                        "Spektrum: Vollspektrum bevorzugt; mehr Rot fördert Blüte, Blau för kompaktere Pflanzen",
                        "Abstand und Dimmung nach Hersteller, gleichmäßige Abdeckung ist wichtiger als reine Leistung",
                        "Laufzeiten: Vegetativ ~18/6, Blüte 12/12 (an Sorte anpassen)"
                    ),
                    tip = "Messe PPFD, wenn möglich, und optimiere Lichtposition für gleichmäßige Intensität; berechne Energiebedarf im Budget."
                ),
                GuideCategory(
                    title = "Belüftung & Klima",
                    icon = Icons.Filled.AcUnit,
                    intro = "Frischluft, Abluft und stabile Klimakontrolle sind zentral.",
                    bullets = listOf(
                        "Temperatur: Vegetativ 22–28°C, Blüte 20–26°C",
                        "Relative Luftfeuchte: Sämling 65–70%, Vegetativ 40–60%, Blüte 40–50%",
                        "VPD beachten: beeinflusst Transpiration und Nährstoffaufnahme",
                        "System: Umluft für Grenzschichtabbau; Abluft mit Filter bei Geruchsproblemen"
                    ),
                    tip = "Nutze Hygrostat und konstante Messung (Temp/RH); dimensioniere Abluft nach Raumgröße und Wärmeabgabe der Lampen."
                ),
                GuideCategory(
                    title = "Gieß-Guide",
                    icon = Icons.Filled.WaterDrop,
                    intro = "Gießrhythmus richtet sich nach Substrat und Pflanzenbedarf.",
                    bullets = listOf(
                        "Erde: seltener, Durchfeuchtung bis Drainage, dann warten bis leicht angetrocknet",
                        "Coco/Hydro: häufigere, präzisere Wassergaben in kleineren Portionen",
                        "pH‑Ziele: Erde 6,0–7,0; Coco/Hydro 5,5–6,5",
                        "EC/PPM: starte bei 25–50% Herstellerempfehlung und steigere je Reaktion"
                    ),
                    tip = "Gewicht des Topfes prüfen und Blattbild beobachten; vermeide dauerhaft nasses Substrat oder starke Trockenphasen."
                ),
                GuideCategory(
                    title = "Dünger-Guide",
                    icon = Icons.Filled.Science,
                    intro = "Nährstoffe richtig dosieren nach Phase und Pflanzenreaktion.",
                    bullets = listOf(
                        "Sämlinge: sehr schwache Nährlösung (25–50%)",
                        "Vegetativ: stärker N‑betont; Blüte: mehr P und K",
                        "Bei Verbrennungszeichen: Dosierung sofort reduzieren und evtl. spülen",
                        "Flushing: gezielt vor Ernte, nicht routinemäßig"
                    ),
                    tip = "Führe ein Düngelog (Dosierung, EC, sichtbare Reaktion) — so findest du die optimale Kurve für deine Setup/Sorte."
                ),
                GuideCategory(
                    title = "Wachstumsphasen",
                    icon = Icons.Filled.Timeline,
                    intro = "Übersicht der Phasen und worauf zu achten ist.",
                    bullets = listOf(
                        "Keimung (1–7 Tage): warm, feucht, kaum Nährstoffe",
                        "Sämling (1–3 Wochen): vorsichtig gießen, Licht moderat",
                        "Vegetativ (2–8+ Wochen): kräftiges Wachstum, Training möglich",
                        "Blüte (6–12+ Wochen): Fokus auf Blüten, P/K‑betonte Düngung",
                        "Ernte & Curing: Trichom‑Reife als Maßstab, langsames Trocknen & Aushärten"
                    ),
                    tip = "Beobachte Pflanzenmerkmale (Internodienabstand, Blattfarbe, Trichome) statt starrer Kalender."
                ),
                GuideCategory(
                    title = "Ertragsoptimierung",
                    icon = Icons.Filled.TrendingUp,
                    intro = "Mehr Ertrag durch Training und Lichtmanagement.",
                    bullets = listOf(
                        "Training (Topping, FIM, LST) für mehrere gleichwertige Kolas",
                        "SCROG: Netz für gleichmäßige Lichtverteilung und Flächenausbeute",
                        "Timing: Wunden Zeit zur Heilung geben, nicht zu viele Eingriffe vor Blüte"
                    ),
                    tip = "Setze auf gleichmäßige Lichtverteilung statt nur mehr Watt; Qualität hängt von Licht, Nährstoffbalance und Stressmanagement ab."
                ),
                GuideCategory(
                    title = "Ernte und Fermentierung",
                    icon = Icons.Filled.Agriculture,
                    intro = "Erntezeitpunkte und schonendes Aushärten (Curing).",
                    bullets = listOf(
                        "Ernte: Trichome überwiegend milchig, kleiner Anteil bernsteinfarben als Orientierung",
                        "Trocknung: langsam 7–14 Tage bei 15–21°C und 45–55% RH",
                        "Curing: in Gläsern initial 2× täglich lüften, danach seltener; mehrere Wochen ideal"
                    ),
                    tip = "Langsames Trocknen und korrektes Curing verbessern Aroma und Brennverhalten deutlich."
                ),
                GuideCategory(
                    title = "Schädlingsbekämpfung",
                    icon = Icons.Filled.BugReport,
                    intro = "Prävention und frühzeitige Identifikation sind entscheidend.",
                    bullets = listOf(
                        "Vorbeugung: Quarantäne neuer Pflanzen, saubere Flächen, Sticky Traps",
                        "Häufige Schädlinge: Spinnmilben, Thripse, Blattläuse, Trauermücken — früh erkennen",
                        "Behandlung: bevorzugt biologisch (Nützlinge, Neem); chemisch nur gezielt und getestet"
                    ),
                    tip = "Teste Mittel zuerst an einer Pflanze; kombiniere Monitoring mit klimatischer Kontrolle zur Reduktion von Befallsdruck."
                ),
                GuideCategory(
                    title = "Problembehandlung",
                    icon = Icons.Filled.Help,
                    intro = "Schnellcheck und strukturiertes Vorgehen bei Symptomen.",
                    bullets = listOf(
                        "Gelbe Blätter: häufig N‑Mangel oder Überwässerung — pH prüfen",
                        "Braune Blattspitzen: oft Überdüngung oder Salzaufbau (hohes EC)",
                        "Hängende Blätter: zu nass oder zu trocken — Topfgewicht prüfen",
                        "Flecken/Verfärbungen: auf Schädlinge oder Pilze untersuchen"
                    ),
                    tip = "Vorgehen: 1) pH & EC messen, 2) Klima prüfen, 3) betroffene Pflanze isolieren, 4) kleine Korrektur und beobachten."
                )
            )

            items(guideCategories) { category ->
                ExpandableGuideCard(category = category)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
