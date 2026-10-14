package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Renders an Apple iOS 17 NameDrop style liquid chromatic aura at the top edge of the screen.
 * Soft iridescent glow that undulates fluidly across the device header.
 */
@Composable
fun NameDropAuraCanvas(
    modifier: Modifier = Modifier,
    isActive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "namedrop_aura")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val waveWobble by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        val width = size.width
        val centerX = width / 2f
        val centerY = -20f * density

        // Chromatic aurora gradient layers (iOS 17 NameDrop colors: Electric Cyan, Blue, Violet, Magenta)
        val glowRadius = (width * 0.75f * waveWobble)

        val offsetX = sin(phase).toFloat() * 30f * density

        // Layer 1: Ambient deep violet glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x356366F1),
                    Color(0x18A855F7),
                    Color(0x00000000)
                ),
                center = Offset(centerX + offsetX, centerY),
                radius = glowRadius * 1.2f
            ),
            radius = glowRadius * 1.2f,
            center = Offset(centerX + offsetX, centerY)
        )

        // Layer 2: Vibrant Electric Cyan and Apple Blue liquid core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x5500E5FF),
                    Color(0x35007AFF),
                    Color(0x00000000)
                ),
                center = Offset(centerX - offsetX * 0.5f, centerY),
                radius = glowRadius
            ),
            radius = glowRadius,
            center = Offset(centerX - offsetX * 0.5f, centerY)
        )

        // Layer 3: Specular bright liquid crest right at the top bezel
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x60FFFFFF),
                    Color(0x2038BDF8),
                    Color(0x00000000)
                ),
                center = Offset(centerX, centerY + 10f * density),
                radius = glowRadius * 0.5f
            ),
            radius = glowRadius * 0.5f,
            center = Offset(centerX, centerY + 10f * density)
        )
    }
}
