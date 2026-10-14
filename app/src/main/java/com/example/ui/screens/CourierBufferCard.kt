package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entity.PairedDevice
import com.example.data.model.CourierItem
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.BadgePill
import com.example.ui.components.LiquidWaveCanvas
import com.example.ui.components.TactileButton
import com.example.ui.components.TactileButtonStyle
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleOrange
import com.example.ui.theme.AppleRed
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardSurface
import com.example.ui.theme.FrostedSurface
import com.example.ui.theme.RoundedBodyFont
import com.example.ui.theme.RoundedDisplayFont
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.util.CryptoUtils

@Composable
fun CourierBufferCard(
    courierItem: CourierItem?,
    targetDevice: PairedDevice?,
    onCaptureClick: () -> Unit,
    onReleaseClick: () -> Unit,
    onDiscardClick: () -> Unit,
    onPreviewLiquidStream: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AppleGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("courier_buffer_card"),
        cornerRadius = 28.dp, // Fluid organic rounded squircle
        elevation = 6.dp,
        isLiquidGlass = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (courierItem != null) {
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFED7AA), Color(0xFFFDBA74))
                                    )
                                } else {
                                    Brush.radialGradient(
                                        listOf(Color(0xFFBAE6FD), Color(0xFF7DD3FC))
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (courierItem != null) Icons.Default.WaterDrop else Icons.Default.PhotoCamera,
                            contentDescription = "Courier Icon",
                            tint = if (courierItem != null) Color(0xFFC2410C) else AppleBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Courier Buffer",
                            fontFamily = RoundedDisplayFont,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = if (courierItem != null) "Magical hand holding screenshot" else "Hand empty • Ready to capture",
                            fontFamily = RoundedBodyFont,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (courierItem != null) {
                    BadgePill(
                        text = if (courierItem.isTransferring) "BEAMING..." else "HOLDING",
                        backgroundColor = if (courierItem.isTransferring) Color(0xFFE0F2FE) else Color(0xFFFFF7ED),
                        textColor = if (courierItem.isTransferring) AppleBlue else AppleOrange,
                        dotColor = if (courierItem.isTransferring) AppleBlue else AppleOrange
                    )
                } else {
                    BadgePill(
                        text = "EMPTY",
                        backgroundColor = FrostedSurface,
                        textColor = TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = courierItem,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "courier_content"
            ) { item ->
                if (item != null) {
                    HoldingPayloadView(
                        item = item,
                        targetDevice = targetDevice,
                        onReleaseClick = onReleaseClick,
                        onDiscardClick = onDiscardClick,
                        onPreviewLiquidStream = onPreviewLiquidStream
                    )
                } else {
                    EmptyBufferPromptView(
                        targetDevice = targetDevice,
                        onCaptureClick = onCaptureClick
                    )
                }
            }
        }
    }
}

@Composable
private fun HoldingPayloadView(
    item: CourierItem,
    targetDevice: PairedDevice?,
    onReleaseClick: () -> Unit,
    onDiscardClick: () -> Unit,
    onPreviewLiquidStream: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Thumbnail & Metadata Container with Liquid Wave backing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF0F9FF))
                .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(20.dp))
        ) {
            // Ambient liquid wave in card background
            LiquidWaveCanvas(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(20.dp)),
                fillLevel = if (item.isTransferring) (item.transferProgress.coerceAtLeast(0.15f)) else 0.35f,
                waveColor1 = Color(0x180284C7),
                waveColor2 = Color(0x2538BDF8),
                glowColor = Color(0x40BAE6FD)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail Preview
                Box(
                    modifier = Modifier
                        .size(68.dp, 92.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.2.dp, Color.White, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.bitmap != null) {
                        Image(
                            bitmap = item.bitmap.asImageBitmap(),
                            contentDescription = "Captured screenshot preview",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = TextTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.fileName,
                        fontFamily = RoundedDisplayFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.width}×${item.height} • ${CryptoUtils.formatBytes(item.fileSizeBytes)}",
                        fontFamily = RoundedBodyFont,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Cryptographic integrity pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.85f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "SHA-256",
                            tint = AppleGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SHA-256: ${item.sha256Hash.take(12)}...",
                            fontSize = 10.sp,
                            fontFamily = RoundedBodyFont,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Transfer Progress Bar if active
        if (item.isTransferring) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Streaming magical payload to ${targetDevice?.deviceName ?: "peer"}...",
                        fontFamily = RoundedBodyFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppleBlue
                    )
                    Text(
                        text = "${(item.transferProgress * 100).toInt()}%",
                        fontFamily = RoundedDisplayFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppleBlue
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { item.transferProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = AppleBlue,
                    trackColor = Color(0xFFE0F2FE)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Error message if any
        if (item.transferError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEF2F2))
                    .padding(10.dp)
            ) {
                Text(
                    text = "Transfer paused: ${item.transferError}. Payload safely retained.",
                    fontFamily = RoundedBodyFont,
                    fontSize = 11.sp,
                    color = AppleRed,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TactileButton(
                onClick = onReleaseClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag("beam_transfer_button"),
                style = TactileButtonStyle.PRIMARY,
                enabled = !item.isTransferring
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Release & Beam",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (targetDevice != null) "Beam to ${targetDevice.deviceName.take(12)}" else "Beam Transfer",
                        fontFamily = RoundedDisplayFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }

            TactileButton(
                onClick = onPreviewLiquidStream,
                style = TactileButtonStyle.SECONDARY,
                modifier = Modifier.testTag("liquid_preview_button")
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Watch Liquid Animation",
                    tint = AppleBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            TactileButton(
                onClick = onDiscardClick,
                style = TactileButtonStyle.DESTRUCTIVE,
                enabled = !item.isTransferring,
                modifier = Modifier.testTag("discard_buffer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Discard Buffer",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyBufferPromptView(
    targetDevice: PairedDevice?,
    onCaptureClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Graphic with Liquid Wave Glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(FrostedSurface)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_fistshot_hero),
                contentDescription = "FistShot Gestures",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )

            // Liquid wave meniscus overlay
            LiquidWaveCanvas(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(20.dp)),
                fillLevel = 0.28f,
                waveColor1 = Color(0x15007AFF),
                waveColor2 = Color(0x2200E5FF),
                glowColor = Color(0x4038BDF8)
            )

            // Floating gesture instruction pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            ) {
                BadgePill(
                    text = "Close fist in front of camera to grab",
                    backgroundColor = Color.White.copy(alpha = 0.95f),
                    textColor = AppleBlue,
                    dotColor = AppleBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Target Device Status Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF0F9FF))
                .border(1.dp, Color(0xFFE0F2FE), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Target",
                    tint = AppleBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (targetDevice != null) "Ready receiver: ${targetDevice.deviceName}" else "No recipient selected",
                    fontFamily = RoundedBodyFont,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppleBlue
                )
            }

            if (targetDevice != null) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AppleGreen)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Manual Capture Button
        TactileButton(
            onClick = onCaptureClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("manual_capture_button"),
            style = TactileButtonStyle.SECONDARY
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Take Screenshot",
                    tint = AppleBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Grab Current Screen to Buffer",
                    fontFamily = RoundedDisplayFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = AppleBlue
                )
            }
        }
    }
}
