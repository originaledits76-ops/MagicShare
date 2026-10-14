package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.sin

/**
 * Renders an animated organic fluid wave canvas with layered translucent liquid crests.
 */
@Composable
fun LiquidWaveCanvas(
    modifier: Modifier = Modifier,
    fillLevel: Float = 0.5f, // 0.0 to 1.0 (water level)
    waveColor1: Color = Color(0x33007AFF),
    waveColor2: Color = Color(0x5500C7BE),
    glowColor: Color = Color(0x8800E5FF)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_waves")

    val wavePhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val wavePhase2 by infiniteTransition.animateFloat(
        initialValue = (Math.PI).toFloat(),
        targetValue = (3 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val baseWaterY = height * (1f - fillLevel.coerceIn(0f, 1f))
        val amplitude = 12f * density

        // 1. Back fluid wave
        val path1 = Path().apply {
            moveTo(0f, height)
            lineTo(0f, baseWaterY)
            var x = 0f
            while (x <= width) {
                val y = baseWaterY + sin((x / width) * 2 * Math.PI + wavePhase1).toFloat() * amplitude
                lineTo(x, y)
                x += 10f
            }
            lineTo(width, height)
            close()
        }

        drawPath(
            path = path1,
            brush = Brush.verticalGradient(
                listOf(waveColor1, waveColor1.copy(alpha = 0.1f)),
                startY = baseWaterY - amplitude,
                endY = height
            )
        )

        // 2. Front fluid wave
        val path2 = Path().apply {
            moveTo(0f, height)
            lineTo(0f, baseWaterY)
            var x = 0f
            while (x <= width) {
                val y = baseWaterY + sin((x / width) * 3 * Math.PI + wavePhase2).toFloat() * (amplitude * 0.85f)
                lineTo(x, y)
                x += 10f
            }
            lineTo(width, height)
            close()
        }

        drawPath(
            path = path2,
            brush = Brush.verticalGradient(
                listOf(glowColor, waveColor2),
                startY = baseWaterY - amplitude,
                endY = height
            )
        )
    }
}
