package com.example.ui.screens

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.model.GestureState
import com.example.data.model.HandPose
import com.example.gesture.GestureDetectorEngine
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.BadgePill
import com.example.ui.components.TactileButton
import com.example.ui.components.TactileButtonStyle
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleOrange
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.FrostedSurface
import com.example.ui.theme.RoundedBodyFont
import com.example.ui.theme.RoundedDisplayFont
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GestureRadarCard(
    gestureState: GestureState,
    gestureEngine: GestureDetectorEngine,
    hasCameraPermission: Boolean,
    onSimulateFist: () -> Unit,
    onSimulateOpenHand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraProviderInstance by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var isCameraHardwareAvailable by remember { mutableStateOf(false) }
    var cameraInitError by remember { mutableStateOf<String?>(null) }
    var selectedSelector by remember { mutableStateOf<CameraSelector?>(null) }
    var useVirtualRadarMode by remember { mutableStateOf(false) }

    // Check camera availability when permission is granted
    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            isCameraHardwareAvailable = false
            return@LaunchedEffect
        }

        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val provider = cameraProviderFuture.get()
                    cameraProviderInstance = provider

                    val hasFront = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                    val hasBack = provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)

                    when {
                        hasFront -> {
                            selectedSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                            isCameraHardwareAvailable = true
                        }
                        hasBack -> {
                            selectedSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            isCameraHardwareAvailable = true
                        }
                        else -> {
                            isCameraHardwareAvailable = false
                            useVirtualRadarMode = true
                            cameraInitError = "No camera hardware detected (Virtual Sensor Active)"
                        }
                    }
                } catch (e: Exception) {
                    Log.w("GestureRadar", "Camera hardware probe failed", e)
                    isCameraHardwareAvailable = false
                    useVirtualRadarMode = true
                    cameraInitError = "Camera unavailable: ${e.message}"
                }
            }, ContextCompat.getMainExecutor(context))
        } catch (e: Exception) {
            isCameraHardwareAvailable = false
            useVirtualRadarMode = true
            cameraInitError = e.message
        }
    }

    // Clean up camera sessions on disposal
    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraProviderInstance?.unbindAll()
            } catch (e: Exception) {
                Log.w("GestureRadar", "Error unbinding camera on disposal", e)
            }
        }
    }

    AppleGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("gesture_radar_card"),
        cornerRadius = 28.dp,
        elevation = 4.dp,
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PanTool,
                            contentDescription = "Gesture Sensor",
                            tint = AppleBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Gesture Radar",
                            fontFamily = RoundedDisplayFont,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = if (isCameraHardwareAvailable && !useVirtualRadarMode) "On-device optical stream • 30 fps" else "Virtual gesture telemetry sensor",
                            fontFamily = RoundedBodyFont,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Pose badge
                val (poseLabel, poseColor) = when (gestureState.currentPose) {
                    HandPose.FIST_CLOSED -> "FIST DETECTED" to AppleOrange
                    HandPose.OPEN_PALM -> "OPEN PALM" to AppleGreen
                    HandPose.HAND_DETECTED -> "HAND TRACKED" to AppleBlue
                    HandPose.IDLE -> "SEARCHING" to Color(0xFF8E8E93)
                }

                BadgePill(
                    text = poseLabel,
                    backgroundColor = poseColor.copy(alpha = 0.12f),
                    textColor = poseColor,
                    dotColor = poseColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Viewfinder / Virtual Radar Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F1117))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
            ) {
                if (hasCameraPermission && isCameraHardwareAvailable && !useVirtualRadarMode && selectedSelector != null) {
                    // Safe CameraX Viewfinder with graceful error guard
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }

                            val provider = cameraProviderInstance
                            val selector = selectedSelector
                            if (provider != null && selector != null) {
                                try {
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }

                                    val imageAnalyzer = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also {
                                            it.setAnalyzer(ContextCompat.getMainExecutor(ctx), gestureEngine)
                                        }

                                    provider.unbindAll()
                                    provider.bindToLifecycle(
                                        lifecycleOwner,
                                        selector,
                                        preview,
                                        imageAnalyzer
                                    )
                                } catch (e: Exception) {
                                    Log.e("GestureRadar", "Camera bind failed, switching to virtual radar", e)
                                    cameraInitError = e.localizedMessage
                                    useVirtualRadarMode = true
                                    try {
                                        provider.unbindAll()
                                    } catch (ignored: Exception) {}
                                }
                            }
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Camera Switch or Toggle to Virtual Mode
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val provider = cameraProviderInstance ?: return@IconButton
                                val newSelector = if (selectedSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                                    if (provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) CameraSelector.DEFAULT_BACK_CAMERA else null
                                } else {
                                    if (provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) CameraSelector.DEFAULT_FRONT_CAMERA else null
                                }
                                if (newSelector != null) {
                                    selectedSelector = newSelector
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Switch Camera",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Targeting Reticle
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .align(Alignment.Center)
                            .border(
                                width = 2.dp,
                                color = when (gestureState.currentPose) {
                                    HandPose.FIST_CLOSED -> AppleOrange
                                    HandPose.OPEN_PALM -> AppleGreen
                                    else -> Color.White.copy(alpha = 0.4f)
                                },
                                shape = RoundedCornerShape(20.dp)
                            )
                    )
                } else {
                    // Virtual Radar Display (Used on emulators, devices without physical camera, or when camera fails)
                    VirtualRadarCanvas(
                        gestureState = gestureState,
                        errorMessage = cameraInitError
                    )
                }

                // Bottom HUD Metrics Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = gestureState.statusFeedback,
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Conf: ${(gestureState.confidence * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Medium,
                            fontFamily = RoundedBodyFont
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Testing & Simulator Buttons
            Text(
                text = "Interactive Gesture Simulator (Test Bench)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TactileButton(
                    onClick = onSimulateFist,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("simulate_fist_button"),
                    style = TactileButtonStyle.SECONDARY
                ) {
                    Text(
                        text = "✊ Simulate Fist (Grab)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppleOrange
                    )
                }

                TactileButton(
                    onClick = onSimulateOpenHand,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("simulate_release_button"),
                    style = TactileButtonStyle.SECONDARY
                ) {
                    Text(
                        text = "✋ Simulate Open (Beam)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppleBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun VirtualRadarCanvas(
    gestureState: GestureState,
    errorMessage: String?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_anim"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Radar concentric grid rings
            for (r in listOf(30f, 60f, 90f)) {
                drawCircle(
                    color = Color(0xFF1E2330),
                    radius = r * density,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Radar sweep pulse
            drawCircle(
                color = when (gestureState.currentPose) {
                    HandPose.FIST_CLOSED -> AppleOrange.copy(alpha = 0.25f)
                    HandPose.OPEN_PALM -> AppleGreen.copy(alpha = 0.25f)
                    else -> AppleBlue.copy(alpha = 0.15f)
                },
                radius = pulseRadius * density,
                center = center
            )
        }

        // Center gesture hand emblem
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        when (gestureState.currentPose) {
                            HandPose.FIST_CLOSED -> AppleOrange.copy(alpha = 0.2f)
                            HandPose.OPEN_PALM -> AppleGreen.copy(alpha = 0.2f)
                            else -> Color(0xFF1F2430)
                        }
                    )
                    .border(
                        1.5.dp,
                        when (gestureState.currentPose) {
                            HandPose.FIST_CLOSED -> AppleOrange
                            HandPose.OPEN_PALM -> AppleGreen
                            else -> Color(0xFF3B4254)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (gestureState.currentPose) {
                        HandPose.FIST_CLOSED -> "✊"
                        HandPose.OPEN_PALM -> "✋"
                        else -> "🖐"
                    },
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when (gestureState.currentPose) {
                    HandPose.FIST_CLOSED -> "CLOSED FIST (GRAB TRIGGER)"
                    HandPose.OPEN_PALM -> "OPEN PALM (RELEASE TRIGGER)"
                    else -> "VIRTUAL GESTURE RADAR"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = when (gestureState.currentPose) {
                    HandPose.FIST_CLOSED -> AppleOrange
                    HandPose.OPEN_PALM -> AppleGreen
                    else -> Color(0xFF8E95A5)
                },
                letterSpacing = 0.5.sp
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = errorMessage,
                    fontSize = 10.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}
