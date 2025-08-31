package com.growtracker.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.growtracker.app.R

/**
 * Logo-Komponente für In-App Verwendung (Overview-Screen)
 * 
 * Diese Komponente verwendet eine Prioritätsliste für In-App Logos:
 * 1. logo.png (Standard PNG-Logo für In-App Verwendung)
 * 2. custom_logo (Fallback - verwendet custom_logo.png oder Vector)
 * 
 * 📱 Hinweis: custom_logo ist jetzt der Standard-Fallback!
 * 
 * 🎨 So änderst du das In-App Logo:
 * 1. Erstelle dein Logo als PNG (empfohlen: 300x100px oder ähnliches 3:1 Verhältnis)
 * 2. Benenne es "logo.png" 
 * 3. Platziere es in: app/src/main/res/drawable/logo.png
 * 4. Die App verwendet automatisch dein In-App Logo!
 */
@Composable
fun StartupLogo(
    modifier: Modifier = Modifier,
    height: Int = 85,
    widthFraction: Float = 0.85f,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current
    
    // Bestimme welches In-App Logo verwendet werden soll
    val logoResource = remember {
        // Prüfe ob logo.png existiert (In-App Logo)
        val logoId = context.resources.getIdentifier("logo", "drawable", context.packageName)
        if (logoId != 0) logoId else {
            // Fallback zu custom_logo (PNG bevorzugt)
            val customLogoId = context.resources.getIdentifier("custom_logo", "drawable", context.packageName)
            customLogoId // Verwende custom_logo oder 0 wenn nicht gefunden
        }
    }
    
    Image(
        painter = painterResource(id = logoResource),
        contentDescription = "In-App Logo",
        modifier = modifier
            .height(height.dp)
            .fillMaxWidth(widthFraction),
        contentScale = contentScale
    )
}

/**
 * Kompakte Logo-Komponente für kleinere Bereiche (Navigation, etc.)
 */
@Composable
fun CompactLogo(
    modifier: Modifier = Modifier,
    size: Int = 50
) {
    StartupLogo(
        modifier = modifier,
        height = size,
        widthFraction = 1f,
        contentScale = ContentScale.Fit
    )
}

/**
 * Logo für die Titelleiste oder Header-Bereiche
 */
@Composable
fun HeaderLogo(
    modifier: Modifier = Modifier
) {
    StartupLogo(
        modifier = modifier,
        height = 40,
        widthFraction = 0.6f,
        contentScale = ContentScale.Fit
    )
}
