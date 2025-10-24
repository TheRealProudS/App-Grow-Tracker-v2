package com.growtracker.app.ui.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Simple rule-based recommendation engine mapping analyzer results to user-facing tips.
 * Later this can be data-driven (JSON rules) and incorporate plant history.
 */
object LeafSenseRecommendations {

    data class Recommendation(
        val title: String,
        val message: String,
        val priority: Priority = Priority.MEDIUM
    ) {
        enum class Priority { LOW, MEDIUM, HIGH }
    }

    fun generate(results: List<LeafSenseResult>, threshold: Float): List<Recommendation> {
        if (results.isEmpty()) return emptyList()
        val recs = mutableListOf<Recommendation>()

        val topHealthy = results.firstOrNull { it.category == LeafSenseResult.Category.HEALTH }
        if (topHealthy != null && topHealthy.confidence >= 0.6f) {
            recs += Recommendation(
                title = "Gesund",
                message = "Deine Pflanze wirkt insgesamt gesund. Weiter so – konsistente Pflege beibehalten.",
                priority = Recommendation.Priority.LOW
            )
        }

        fun addIf(labelContains: String, title: String, msg: String, prio: Recommendation.Priority) {
            results.firstOrNull { it.label.contains(labelContains, ignoreCase = true) && it.confidence >= threshold }?.let {
                recs += Recommendation(title, msg, prio)
            }
        }

        addIf("Stickstoff", "Stickstoff optimieren", "Leichter N-Mangel erkannt. Prüfe Düngerplan und erwäge moderate Stickstoffgabe.", Recommendation.Priority.MEDIUM)
        addIf("Überwässer", "Bewässerung anpassen", "Anzeichen für Überwässerung. Lasse das Substrat vor der nächsten Gabe stärker abtrocknen.", Recommendation.Priority.HIGH)
        addIf("Schädlings", "Auf Schädlinge prüfen", "Hinweis auf mögliches Schädlingsrisiko. Blätter (Unterseiten) und Stiele inspizieren.", Recommendation.Priority.HIGH)
        addIf("pH", "pH Wert prüfen", "pH Ungleichgewicht möglich. Messe Drainage oder Substrat und justiere falls nötig.", Recommendation.Priority.MEDIUM)
        addIf("Spot", "Blattflecken beobachten", "Blattflecken erkannt – überwache Ausbreitung und entferne stark betroffene Blätter.", Recommendation.Priority.MEDIUM)

        // De-duplicate by title keeping highest priority occurrence
        return recs.groupBy { it.title }.map { (_, list) ->
            list.maxBy { it.priority.ordinal }
        }.sortedByDescending { it.priority.ordinal }
    }
}

// --- UI Helpers for Knowledge Suggestions ---

data class KnowledgeSuggestionUi(
    val id: String,
    val question: String,
    val answer: String,
    val category: String? = null
)

/**
 * Minimal list rendering for knowledge suggestions. Provide data already mapped from KnowledgeRepository.Entry.
 */
@Composable
fun KnowledgeSuggestionsList(
    suggestions: List<KnowledgeSuggestionUi>,
    modifier: Modifier = Modifier,
    maxAnswerPreviewChars: Int = 180,
) {
    if (suggestions.isEmpty()) return
    Column(modifier = modifier) {
        Text(
            text = "Relevante Hinweise",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height((suggestions.size.coerceAtMost(4) * 120).dp)
        ) {
            items(suggestions, key = { it.id }) { s ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = s.question.ifBlank { s.category ?: "Hinweis" },
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val ans = if (s.answer.length > maxAnswerPreviewChars) {
                            s.answer.take(maxAnswerPreviewChars) + "…"
                        } else s.answer
                        Text(
                            text = ans,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
