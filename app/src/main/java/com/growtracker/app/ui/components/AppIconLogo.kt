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
 * App-Icon und Splash Screen Logo-Komponente
 * 
 * Diese Komponente ist für App-Icons und Startup-Screens gedacht:
 * 1. custom_logo.png (Benutzerdefiniertes App-Icon/Splash Logo) - HÖCHSTE PRIORITÄT
 * 2. custom_logo (Vector Fallback wenn PNG nicht vorhanden)
 * 
 * 📱 Verwendung:
 * - App-Icon (das Symbol auf dem Handy)
 * - Splash Screen / Startup Logo (beim App-Start)
 * - Loading Screens
 * 
 * 🎨 So fügst du dein eigenes App-Icon hinzu:
 * 1. Erstelle dein Logo als PNG
 *    - Empfohlene Größe: 512x512px (quadratisch für App-Icons)
 *    - Transparenter oder farbiger Hintergrund
 *    - Hochauflösend für scharfe Darstellung
 * 2. Benenne es EXAKT "custom_logo.png"
 * 3. Platziere es in: app/src/main/res/drawable/custom_logo.png
 * 4. Die App verwendet automatisch dein App-Icon!
 * 
 * ⚠️ Wichtig: Nach dem Hinzufügen neuer Ressourcen muss die App neu kompiliert werden.
 */
@Composable
fun AppIconLogo(
    modifier: Modifier = Modifier,
    size: Int = 120,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current
    
    // Bestimme welches App-Icon Logo verwendet werden soll
    val logoResource = remember {
        // Verwende custom_logo (PNG bevorzugt)
        val customLogoId = context.resources.getIdentifier("custom_logo", "drawable", context.packageName)
        customLogoId // Nur custom_logo verwenden, kein Fallback
    }
    
    Image(
        painter = painterResource(id = logoResource),
        contentDescription = "App Icon Logo",
        modifier = modifier.height(size.dp),
        contentScale = contentScale
    )
}

/**
 * Splash Screen Logo für den App-Start
 */
@Composable
fun SplashLogo(
    modifier: Modifier = Modifier
) {
    AppIconLogo(
        modifier = modifier,
        size = 150,
        contentScale = ContentScale.Fit
    )
}
