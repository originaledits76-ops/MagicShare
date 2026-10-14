package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.PairedDevice
import com.example.data.model.CourierItem
import com.example.data.model.GestureState
import com.example.data.model.HandPose
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleOrange
import com.example.ui.theme.RoundedDisplayFont

@Composable
fun DynamicIslandBar(
    gestureState: GestureState,
    courierItem: CourierItem?,
    targetDevice: PairedDevice?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTransferring = courierItem?.isTransferring == true
    val isHolding = courierItem != null && !isTransferring
    val isFistDetected = gestureState.currentPose == HandPose.FIST_CLOSED

    Box(
        modifier = modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color(0x30000000),
                spotColor = Color(0x40000000)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF0C0D12))
            .border(1.dp, Color(0xFF262933), RoundedCornerShape(32.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("dynamic_island_capsule"),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = when {
                isTransferring -> "TRANSFERRING"
                isHolding -> "HOLDING"
                isFistDetected -> "FIST"
                else -> "DEFAULT"
            },
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "island_content"
        ) { state ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                when (state) {
                    "TRANSFERRING" -> {
                        CircularProgressIndicator(
                            progress = { courierItem?.transferProgress ?: 0.5f },
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF00E5FF),
                            trackColor = Color(0xFF1E293B),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Beaming ${(courierItem?.transferProgress?.times(100))?.toInt()}% → ${targetDevice?.deviceName?.take(10) ?: "Device"}",
                            fontFamily = RoundedDisplayFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                    "HOLDING" -> {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AppleOrange)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Holding Capture • ${courierItem?.width}×${courierItem?.height}",
                            fontFamily = RoundedDisplayFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SHA-OK",
                            fontSize = 10.sp,
                            fontFamily = RoundedDisplayFont,
                            fontWeight = FontWeight.Medium,
                            color = AppleGreen
                        )
                    }
                    "FIST" -> {
                        Text(text = "✊", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fist Closed • Grabbing Screen",
                            fontFamily = RoundedDisplayFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppleOrange
                        )
                    }
                    else -> {
                        // Default Idle State
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (targetDevice != null) AppleGreen else Color(0xFF94A3B8))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FistShot",
                            fontFamily = RoundedDisplayFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (targetDevice != null) targetDevice.deviceName.take(14) else "No Target",
                            fontFamily = RoundedDisplayFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }
    }
}
