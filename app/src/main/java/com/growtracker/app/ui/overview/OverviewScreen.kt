@file:Suppress("DEPRECATION")

package com.growtracker.app.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
// using fully-qualified icon references in this file
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import com.growtracker.app.ui.grow.GrowDataStore
import com.growtracker.app.data.EntryType as DataEntryType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.growtracker.app.ui.components.StartupLogo
import com.growtracker.app.ui.language.LanguageManager
import com.growtracker.app.ui.language.Strings
import com.growtracker.app.ui.language.getString

data class OverviewItem(
    val title: String,
    val icon: ImageVector,
    val description: String,
    val onClick: () -> Unit,
    val gradientColors: List<Color>
)

@Composable
fun OverviewScreen(
    modifier: Modifier = Modifier,
    languageManager: LanguageManager,
    onOpenGrowGuide: () -> Unit = {}
) {
    var showGallery by remember { mutableStateOf(false) }
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Hintergrund-Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Kompaktes Logo
            StartupLogo(
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                height = 160,
                widthFraction = 1.0f
            )
            
            // Feature-Bereiche - kompakter
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Guide Card - kompakter
                    FeatureCard(
                    title = getString(Strings.overview_grow_guide, languageManager),
                    description = "Expertenwissen für erfolgreichen Cannabis-Anbau",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                    backgroundColor = Color(0xFF4CAF50),
                    onClick = onOpenGrowGuide,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Zweite Reihe - kompakter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        FeatureCard(
                        title = getString(Strings.overview_placeholder_1, languageManager),
                        description = "",
                            icon = Icons.Filled.Analytics,
                        backgroundColor = Color(0xFF2196F3),
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        isCompact = true
                    )
                    
                        FeatureCard(
                        title = "Trocknung",
                        description = "",
                            icon = Icons.Filled.AcUnit,
                        backgroundColor = Color(0xFFFF9800),
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        isCompact = true
                    )
                }
                
                // Dritte Reihe - neue Features
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
        FeatureCard(
            title = "Galerie",
            description = "",
    icon = Icons.Filled.PhotoLibrary,
            backgroundColor = Color(0xFF9C27B0),
            onClick = { showGallery = true },
            modifier = Modifier.weight(1f),
            isCompact = true
            )
                    
            FeatureCard(
                        title = "Fermentierung",
                        description = "",
                icon = Icons.Filled.LocalBar,
                        backgroundColor = Color(0xFF795548),
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        isCompact = true
                    )
                }
            }
        }
    }

    if (showGallery) {
        GalleryDialog(onClose = { showGallery = false })
    }
}

@Composable
fun GalleryDialog(onClose: () -> Unit) {
    // collect all photos from GrowDataStore
    val plants = remember { GrowDataStore.plants }
    val photos = remember(plants) { plants.flatMap { it.photos } }
    var fullScreenPath by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onClose) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Galerie", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onClose) { Icon(imageVector = Icons.Filled.Close, contentDescription = "Schließen") }
                }

                if (photos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Keine Bilder vorhanden") }
                } else {
                    LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(4.dp)) {
                        items(photos) { p ->
                            val bmp = runCatching { android.graphics.BitmapFactory.decodeFile(p.uri) }.getOrNull()
                            if (bmp != null) {
                                Image(bitmap = bmp.asImageBitmap(), contentDescription = p.description, modifier = Modifier.size(120.dp).padding(4.dp).clickable { fullScreenPath = p.uri }, contentScale = ContentScale.Crop)
                            } else {
                                Icon(imageVector = Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(120.dp).padding(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (fullScreenPath != null) {
        Dialog(onDismissRequest = { fullScreenPath = null }) {
            val bmp = runCatching { android.graphics.BitmapFactory.decodeFile(fullScreenPath) }.getOrNull()
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (bmp != null) Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit) else Text("Bild nicht verfügbar")
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(if (isCompact) 100.dp else 80.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (isCompact) {
                // Vertikales Layout für kompakte Cards - Samsung-Style
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(9.dp),
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    
                    if (description.isNotEmpty()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            } else {
                // Horizontales Layout für große Cards - Samsung-Style
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(12.dp),
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (description.isNotEmpty()) {
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "Öffnen",
                        modifier = Modifier.size(20.dp),
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.2f),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier
                    .size(20.dp)
                    .padding(10.dp),
                tint = color
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun OverviewButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
