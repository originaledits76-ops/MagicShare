package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardSurface

@Composable
fun AppleGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp, // Extra rounded organic squircle
    elevation: Dp = 5.dp,
    backgroundColor: Color = CardSurface,
    borderColor: Color = BorderSubtle,
    isLiquidGlass: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color(0x0A007AFF),
                spotColor = Color(0x1800E5FF)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(
                border = if (isLiquidGlass) {
                    BorderStroke(
                        1.2.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.95f),
                                Color(0xFFE2E8F0),
                                Color.White.copy(alpha = 0.8f)
                            )
                        )
                    )
                } else {
                    BorderStroke(1.dp, borderColor)
                },
                shape = shape
            )
    ) {
        content()
    }
}
