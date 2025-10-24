package com.growtracker.app.ui.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import com.growtracker.app.ui.ai.LeafSenseResult

/** Central design helpers for LeafSense UI (colors, gradients, shapes, surfaces). */
object LeafSenseDesignTokens {
    // Base category colors (semantic anchors)
    val health = Color(0xFF2E7D32)
    val deficiency = Color(0xFFF57F17)
    val stress = Color(0xFF0288D1)
    val pest = Color(0xFFB00020)

    fun categoryColor(cat: LeafSenseResult.Category): Color = when(cat) {
        LeafSenseResult.Category.HEALTH -> health
        LeafSenseResult.Category.DEFICIENCY -> deficiency
        LeafSenseResult.Category.STRESS -> stress
        LeafSenseResult.Category.PEST -> pest
    }

    fun categoryGradient(cat: LeafSenseResult.Category): Brush = when(cat) {
        LeafSenseResult.Category.HEALTH -> Brush.linearGradient(listOf(health.copy(alpha=0.85f), health.copy(alpha=0.35f)))
        LeafSenseResult.Category.DEFICIENCY -> Brush.linearGradient(listOf(deficiency.copy(alpha=0.9f), deficiency.copy(alpha=0.30f)))
        LeafSenseResult.Category.STRESS -> Brush.linearGradient(listOf(stress.copy(alpha=0.9f), stress.copy(alpha=0.30f)))
        LeafSenseResult.Category.PEST -> Brush.linearGradient(listOf(pest.copy(alpha=0.9f), pest.copy(alpha=0.30f)))
    }

    val overlayPillShape = RoundedCornerShape(28.dp)
    val cardShapeLarge = RoundedCornerShape(26.dp)
    val cardShapeMedium = RoundedCornerShape(20.dp)
    val cardShapeSmall = RoundedCornerShape(14.dp)

    /**
     * Derives a severity-adjusted tint for a base category color using confidence (0..1).
     * Lower confidence -> lighter desaturated; higher -> vivid & slightly darkened.
     */
    fun severityTint(cat: LeafSenseResult.Category, confidence: Float): Color {
        val base = categoryColor(cat)
        val c = confidence.coerceIn(0f,1f)
        // Linear interpolate toward a neutral at low confidence, darken & saturate slightly at high.
        val neutral = Color(0xFFBDBDBD)
        return if (c < 0.5f) {
            // Mix neutral -> base
            val t = c / 0.5f
            lerpColor(neutral, base, t * 0.85f)
        } else {
            val t = (c - 0.5f) / 0.5f
            // Darken a bit and boost saturation
            val darkened = base.copy(
                red = (base.red * (1f - 0.12f * t)),
                green = (base.green * (1f - 0.12f * t)),
                blue = (base.blue * (1f - 0.12f * t))
            )
            lerpColor(base, darkened, t * 0.65f)
        }
    }
}

private fun lerpColor(a: Color, b: Color, t: Float): Color {
    val clamped = t.coerceIn(0f,1f)
    return Color(
        red = a.red + (b.red - a.red) * clamped,
        green = a.green + (b.green - a.green) * clamped,
        blue = a.blue + (b.blue - a.blue) * clamped,
        alpha = a.alpha + (b.alpha - a.alpha) * clamped
    )
}

@Composable
fun GradientHeroBackground(category: LeafSenseResult.Category?): Brush {
    val cat = category ?: LeafSenseResult.Category.HEALTH
    return LeafSenseDesignTokens.categoryGradient(cat)
}

/** Semi-translucent overlay pill background; can be reused for floating control groups. */
@Composable
fun overlayPillBackground(): Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f)

/** Elevated layered translucent surface for content over a busy background (camera). */
@Composable
fun elevatedTranslucentBackground(): Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f)
