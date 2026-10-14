package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppleBlue
import com.example.util.QrCodeGenerator

@Composable
fun QrCanvas(
    payload: String,
    modifier: Modifier = Modifier,
    darkColor: Color = Color(0xFF1C1D21),
    lightColor: Color = Color.White
) {
    val matrix = remember(payload) {
        QrCodeGenerator.generateMatrix(payload)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = minOf(size.width, size.height)
            val cellSize = canvasSize / matrix.size

            // Draw white quiet zone background
            drawRoundRect(
                color = lightColor,
                topLeft = Offset.Zero,
                size = Size(canvasSize, canvasSize),
                cornerRadius = CornerRadius(16f, 16f)
            )

            // Draw modules
            for (y in 0 until matrix.size) {
                for (x in 0 until matrix.size) {
                    if (matrix.isDark(x, y)) {
                        // Use Apple Blue for corners finder highlights
                        val isFinderCorner = (x < 7 && y < 7) || (x >= matrix.size - 7 && y < 7) || (x < 7 && y >= matrix.size - 7)
                        val color = if (isFinderCorner) AppleBlue else darkColor

                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x * cellSize + 0.5f, y * cellSize + 0.5f),
                            size = Size(cellSize - 1f, cellSize - 1f),
                            cornerRadius = CornerRadius(2.5f, 2.5f)
                        )
                    }
                }
            }
        }
    }
}
