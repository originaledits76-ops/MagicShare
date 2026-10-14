package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleOrange
import com.example.ui.theme.AppleTeal
import com.example.ui.theme.BodyFont
import com.example.ui.theme.DisplayFont
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

enum class LiquidEventType {
    CAPTURE_SPLASH, // Astral screen grab into Courier Buffer
    SEND_STREAM // Celestial starlight stream beaming to receiver
}

@Composable
fun LiquidAnimationOverlay(
    eventType: LiquidEventType?,
    targetDeviceName: String?,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = eventType != null,
        enter = fadeIn(tween(250)) + scaleIn(tween(300, easing = FastOutSlowInEasing), initialScale = 0.92f),
        exit = fadeOut(tween(300)) + scaleOut(tween(250), targetScale = 0.95f),
        modifier = modifier.fillMaxSize()
    ) {
        if (eventType == null) return@AnimatedVisibility

        // Auto dismiss after 3 seconds
        LaunchedEffect(eventType) {
            delay(3000)
            onAnimationComplete()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            // Full-screen magical celestial stardust wave canvas
            FullScreenMagicalStardustCanvas()

            // Dismiss button in corner
            IconButton(
                onClick = onAnimationComplete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.95f))
                    .shadow(6.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Animation",
                    tint = Color(0xFF1E293B)
                )
            }

            // Magical Glass Container
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color.White.copy(alpha = 0.96f))
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(Color.White, Color(0xFFE0E7FF), Color(0xFFFDF4FF), Color.White)
                        ),
                        RoundedCornerShape(36.dp)
                    )
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(36.dp),
                        ambientColor = Color(0x306366F1),
                        spotColor = Color(0x40A855F7)
                    )
                    .padding(26.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (eventType == LiquidEventType.CAPTURE_SPLASH) {
                        CaptureMagicalVortex()
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Celestial Screen Grab",
                            fontFamily = DisplayFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 22.sp,
                            color = Color(0xFF0F172A),
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Screen magically coalesced into Courier Buffer",
                            fontFamily = BodyFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    } else {
                        SendMagicalStream(targetDeviceName = targetDeviceName ?: "Peer Device")
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Celestial Starlight Beam",
                            fontFamily = DisplayFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 22.sp,
                            color = Color(0xFF0F172A),
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Direct peer-to-peer transmission to $targetDeviceName",
                            fontFamily = BodyFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScreenMagicalStardustCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "magical_stardust")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "stardust_pulse"
    )

    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val topY = 0f

        // Expanding concentric celestial aura rings
        for (i in 0..3) {
            val ringProgress = ((pulse + i * 0.25f) % 1f)
            val radius = ringProgress * height * 0.9f
            val alpha = ((1f - ringProgress) * 0.6f).coerceIn(0f, 1f)

            val colors = when (i % 3) {
                0 -> listOf(Color(0x00A855F7), Color(0xFF6366F1).copy(alpha = alpha), Color(0x0000E5FF))
                1 -> listOf(Color(0x0000E5FF), Color(0xFF38BDF8).copy(alpha = alpha), Color(0x00A855F7))
                else -> listOf(Color(0x00F43F5E), Color(0xFFA855F7).copy(alpha = alpha), Color(0x006366F1))
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = colors,
                    center = Offset(centerX, topY),
                    radius = radius.coerceAtLeast(1f)
                ),
                radius = radius,
                center = Offset(centerX, topY),
                style = Stroke(width = 3.dp.toPx() * (1f - ringProgress * 0.5f))
            )
        }

        // Shimmering 4-point magical stars floating downward
        val starSeeds = listOf(
            Triple(0.2f, 0.25f, 24f),
            Triple(0.8f, 0.35f, 18f),
            Triple(0.15f, 0.65f, 22f),
            Triple(0.85f, 0.72f, 26f),
            Triple(0.5f, 0.85f, 20f),
            Triple(0.35f, 0.45f, 16f),
            Triple(0.68f, 0.55f, 28f)
        )

        for ((index, seed) in starSeeds.withIndex()) {
            val (relX, relY, starSize) = seed
            val currentY = (relY * height + pulse * 60f * density) % height
            val currentX = relX * width + sin(pulse * 6.28f + index).toFloat() * 15f * density
            val starAlpha = (sin(pulse * 6.28f + index * 1.2f).toFloat() * 0.4f + 0.6f).coerceIn(0.1f, 1f)

            rotate(degrees = spinAngle + index * 45f, pivot = Offset(currentX, currentY)) {
                draw4PointSparkle(
                    center = Offset(currentX, currentY),
                    radius = starSize * density * (0.8f + 0.2f * sin(pulse * 6.28f).toFloat()),
                    color = when (index % 3) {
                        0 -> Color(0xFFFDE047).copy(alpha = starAlpha) // Gold
                        1 -> Color(0xFF38BDF8).copy(alpha = starAlpha) // Cyan
                        else -> Color(0xFFE879F9).copy(alpha = starAlpha) // Violet
                    }
                )
            }
        }
    }
}

@Composable
private fun CaptureMagicalVortex() {
    val infiniteTransition = rememberInfiniteTransition(label = "capture_magic")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )

    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    Box(
        modifier = Modifier
            .size(180.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE).copy(alpha = 0.5f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxR = size.width / 2f

            // Concentric celestial ripples
            for (i in 1..3) {
                val r = (maxR * ((pulseScale + i * 0.33f) % 1.0f))
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x00F43F5E), Color(0x666366F1), Color(0x88A855F7)),
                        center = center,
                        radius = r.coerceAtLeast(1f)
                    ),
                    radius = r,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Swirling orbiting stardust runes & stars
            for (p in 0 until 6) {
                val angle = (p * Math.PI / 3 + spinAngle * Math.PI / 180f).toFloat()
                val particleR = maxR * 0.7f
                val px = center.x + cos(angle) * particleR
                val py = center.y + sin(angle) * particleR

                rotate(degrees = spinAngle * 2f + p * 30f, pivot = Offset(px, py)) {
                    draw4PointSparkle(
                        center = Offset(px, py),
                        radius = 8.dp.toPx(),
                        color = if (p % 2 == 0) Color(0xFFFDE047) else Color(0xFF00E5FF)
                    )
                }
            }
        }

        // Center Fist Emblem in shimmering celestial orb
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFA855F7), Color(0xFF6366F1), Color(0xFF0284C7))
                    )
                )
                .border(2.dp, Color.White, CircleShape)
                .shadow(10.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "✊", fontSize = 32.sp)
        }
    }
}

@Composable
private fun SendMagicalStream(targetDeviceName: String) {
    val progress = remember { Animatable(0.05f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 2400, easing = FastOutSlowInEasing)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "stream_flow")
    val flowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flow_anim"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Device to Device Celestial Stream Channel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
                .padding(14.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val startX = 42f
                val endX = size.width - 42f
                val midY = size.height / 2f

                // Starlight conduit line
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(startX, midY),
                    end = Offset(endX, midY),
                    strokeWidth = 10.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // Active flowing celestial starlight beam
                val currentX = startX + (endX - startX) * progress.value
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFF00E5FF)),
                        startX = startX,
                        endX = currentX
                    ),
                    start = Offset(startX, midY),
                    end = Offset(currentX, midY),
                    strokeWidth = 10.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // Flowing magical star sparkles along the beam
                for (i in 0..5) {
                    val dropRatio = ((flowOffset + i * 0.2f) % 1f) * progress.value
                    val dropX = startX + (endX - startX) * dropRatio
                    val wobble = sin(dropRatio * 12).toFloat() * 7f

                    draw4PointSparkle(
                        center = Offset(dropX, midY + wobble),
                        radius = 6.dp.toPx(),
                        color = Color(0xFFFDE047)
                    )
                }
            }

            // Sender Device Icon (Left)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color(0xFF6366F1), CircleShape)
                    .shadow(4.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📱", fontSize = 20.sp)
            }

            // Receiver Device Icon (Right)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (progress.value > 0.85f) Color(0xFFFAF5FF) else Color.White)
                    .border(
                        2.dp,
                        if (progress.value > 0.85f) Color(0xFFA855F7) else Color(0xFFCBD5E1),
                        CircleShape
                    )
                    .shadow(4.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📟", fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress & Cryptographic Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${(progress.value * 100).toInt()}% Transferred",
                    fontFamily = DisplayFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF6366F1)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (progress.value >= 1f) AppleGreen else Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (progress.value >= 1f) "SHA-256 VERIFIED" else "HASHING CHUNKS...",
                    fontFamily = DisplayFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = if (progress.value >= 1f) AppleGreen else Color(0xFF64748B)
                )
            }
        }
    }
}

/**
 * Draws a crisp 4-pointed magical star sparkle.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.draw4PointSparkle(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticBezierTo(center.x, center.y, center.x + radius, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y + radius)
        quadraticBezierTo(center.x, center.y, center.x - radius, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path, color)
}
