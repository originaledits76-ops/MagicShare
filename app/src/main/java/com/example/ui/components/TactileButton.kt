package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppleBlue

enum class TactileButtonStyle {
    PRIMARY, // Filled Apple Blue with soft liquid glow
    SECONDARY, // Frosted Blue tint
    DESTRUCTIVE, // Apple Red tint
    OUTLINE // Subtle border
}

@Composable
fun TactileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TactileButtonStyle = TactileButtonStyle.PRIMARY,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tactile_scale"
    )

    val (bgColor, contentColor) = when (style) {
        TactileButtonStyle.PRIMARY -> if (enabled) AppleBlue to Color.White else Color(0xFFC7C7CC) to Color.White
        TactileButtonStyle.SECONDARY -> if (enabled) Color(0xFFE0F2FE) to AppleBlue else Color(0xFFF1F5F9) to Color(0xFF94A3B8)
        TactileButtonStyle.DESTRUCTIVE -> Color(0xFFFEF2F2) to Color(0xFFEF4444)
        TactileButtonStyle.OUTLINE -> Color.Transparent to Color(0xFF1E293B)
    }

    Surface(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp), // Fluid organic squircle
        color = bgColor,
        contentColor = contentColor,
        shadowElevation = if (style == TactileButtonStyle.PRIMARY && enabled) 3.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
