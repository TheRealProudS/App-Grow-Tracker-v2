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
    val content: String
)

@Composable
fun ExpandableGuideCard(
    category: GuideCategory,
    modifier: Modifier = Modifier
) {
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
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = category.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
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
                    content = "Basis-Setup, Beleuchtung, Belüftung, Töpfe, Budget"
                ),
                GuideCategory(
                    title = "Beleuchtungsguide",
                    icon = Icons.Filled.WbSunny,
                    content = "Empfehlungen für PPFD, Abstand und Zeitpläne"
                ),
                GuideCategory(
                    title = "Belüftung & Klima",
                    icon = Icons.Filled.Air,
                    content = "Temperatur, Luftfeuchtigkeit und VPD Hinweise"
                ),
                GuideCategory(
                    title = "Gieß-Guide",
                    icon = Icons.Filled.WaterDrop,
                    content = "pH, EC und Gießfrequenz für verschiedene Medien"
                ),
                GuideCategory(
                    title = "Dünger-Guide",
                    icon = Icons.Filled.Science,
                    content = "NPK Empfehlungen und Düngepläne"
                ),
                GuideCategory(
                    title = "Wachstumsphasen",
                    icon = Icons.Filled.Timeline,
                    content = "Keimung, Sämling, Vegetativ, Blüte und Ernte"
                ),
                GuideCategory(
                    title = "Ertragsoptimierung",
                    icon = Icons.Filled.TrendingUp,
                    content = "Training, SCROG, LST und Ertragssteigerung"
                ),
                GuideCategory(
                    title = "Ernte und Fermentierung",
                    icon = Icons.Filled.Agriculture,
                    content = "Tipps zur Ernte, Trocknung und Fermentierung"
                ),
                GuideCategory(
                    title = "Schädlingsbekämpfung",
                    icon = Icons.Filled.BugReport,
                    content = "Vorbeugung und Behandlung von Schädlingen"
                ),
                GuideCategory(
                    title = "Problembehandlung",
                    icon = Icons.Filled.Help,
                    content = "Häufige Probleme und deren Lösungen"
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
