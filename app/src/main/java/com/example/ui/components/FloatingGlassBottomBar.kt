package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.RoundedDisplayFont

@Composable
fun FloatingGlassBottomBar(
    onDevicesClick: () -> Unit,
    onQuickCaptureClick: () -> Unit,
    onLiquidReplayClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(34.dp),
                ambientColor = Color(0x18000000),
                spotColor = Color(0x24007AFF)
            )
            .clip(RoundedCornerShape(34.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .border(
                1.2.dp,
                Brush.linearGradient(
                    listOf(Color.White, Color(0xFFE2E8F0), Color.White)
                ),
                RoundedCornerShape(34.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("floating_glass_nav_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CupertinoNavTab(
                icon = Icons.Default.Devices,
                label = "Devices",
                onClick = onDevicesClick,
                testTag = "nav_tab_devices"
            )

            // Center Primary Liquid Capture Action Button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(8.dp, CircleShape, ambientColor = Color(0x30007AFF), spotColor = Color(0x40007AFF))
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFF007AFF))
                        )
                    )
                    .clickable(onClick = onQuickCaptureClick)
                    .testTag("nav_quick_capture"),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✊", fontSize = 24.sp)
            }

            CupertinoNavTab(
                icon = Icons.Default.WaterDrop,
                label = "Magic",
                onClick = onLiquidReplayClick,
                testTag = "nav_tab_liquid"
            )

            CupertinoNavTab(
                icon = Icons.Default.History,
                label = "History",
                onClick = onHistoryClick,
                testTag = "nav_tab_history"
            )
        }
    }
}

@Composable
private fun CupertinoNavTab(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF475569),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = RoundedDisplayFont,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
        )
    }
}
