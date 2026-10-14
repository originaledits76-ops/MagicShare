package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.BadgePill
import com.example.ui.components.DynamicIslandBar
import com.example.ui.components.FloatingGlassBottomBar
import com.example.ui.components.LiquidAnimationOverlay
import com.example.ui.components.LiquidEventType
import com.example.ui.components.NameDropAuraCanvas
import com.example.ui.components.TactileButton
import com.example.ui.components.TactileButtonStyle
import com.example.ui.screens.CourierBufferCard
import com.example.ui.screens.DevicePairingSheet
import com.example.ui.screens.GestureRadarCard
import com.example.ui.screens.TransferHistorySheet
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.FistShotTheme
import com.example.ui.theme.QuartzBackground
import com.example.ui.theme.RoundedBodyFont
import com.example.ui.theme.RoundedDisplayFont
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FistShotTheme {
                val viewModel: MainViewModel = viewModel()
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                // State collections
                val courierItem by viewModel.courierItem.collectAsStateWithLifecycle()
                val targetDevice by viewModel.currentTargetDevice.collectAsStateWithLifecycle()
                val pairedDevices by viewModel.pairedDevices.collectAsStateWithLifecycle()
                val gestureState by viewModel.gestureState.collectAsStateWithLifecycle()
                val transferHistory by viewModel.transferHistory.collectAsStateWithLifecycle()
                val discoveredPeers by viewModel.discoveryManager.discoveredPeers.collectAsStateWithLifecycle()
                val localIp by viewModel.localIp.collectAsStateWithLifecycle()
                val serverPort by viewModel.serverPort.collectAsStateWithLifecycle()

                // Dialog and Sheet toggles
                var showDevicesSheet by remember { mutableStateOf(false) }
                var showHistorySheet by remember { mutableStateOf(false) }

                // Fluid liquid animation event trigger
                var activeLiquidEvent by remember { mutableStateOf<LiquidEventType?>(null) }

                // Camera permission launcher
                var hasCameraPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasCameraPermission = isGranted
                    if (!isGranted) {
                        Toast.makeText(context, "Camera permission needed for gesture recognition", Toast.LENGTH_SHORT).show()
                    }
                }

                // MediaProjection screen capture intent launcher
                val screenCaptureLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == RESULT_OK && result.data != null) {
                        viewModel.captureManager.setMediaProjectionResult(result.resultCode, result.data!!)
                        viewModel.captureScreenToBuffer(this@MainActivity)
                        activeLiquidEvent = LiquidEventType.CAPTURE_SPLASH
                    } else {
                        // Fallback to in-app capture
                        viewModel.captureScreenToBuffer(this@MainActivity)
                        activeLiquidEvent = LiquidEventType.CAPTURE_SPLASH
                    }
                }

                // Auto trigger fluid liquid capture splash when screenshot arrives in courier
                LaunchedEffect(courierItem?.id) {
                    if (courierItem != null && !courierItem!!.isTransferring) {
                        activeLiquidEvent = LiquidEventType.CAPTURE_SPLASH
                    }
                }

                // Auto trigger fluid liquid stream when beaming starts
                LaunchedEffect(courierItem?.isTransferring) {
                    if (courierItem?.isTransferring == true) {
                        activeLiquidEvent = LiquidEventType.SEND_STREAM
                    }
                }

                // Request camera permission on startup if not yet granted
                LaunchedEffect(Unit) {
                    if (!hasCameraPermission) {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }

                // Collect user feedback events
                LaunchedEffect(Unit) {
                    viewModel.userFeedback.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                Box(modifier = Modifier.fillMaxSize().background(QuartzBackground)) {
                    // Apple iOS 17 NameDrop ambient liquid aurora at top of screen
                    NameDropAuraCanvas(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isActive = true
                    )

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .statusBarsPadding()
                                    .padding(horizontal = 20.dp)
                                    .padding(top = 10.dp, bottom = 120.dp), // Extra bottom padding for floating bar
                                verticalArrangement = Arrangement.spacedBy(18.dp)
                            ) {
                                // 1. Apple Dynamic Island Capsule
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    DynamicIslandBar(
                                        gestureState = gestureState,
                                        courierItem = courierItem,
                                        targetDevice = targetDevice,
                                        onClick = { showDevicesSheet = true }
                                    )
                                }

                                // 2. Cupertino Large Title Header
                                CupertinoLargeTitle(
                                    localIp = localIp,
                                    serverPort = serverPort,
                                    targetDevice = targetDevice,
                                    onPairClick = { showDevicesSheet = true }
                                )

                                // Camera Permission Request Banner if not granted
                                AnimatedVisibility(
                                    visible = !hasCameraPermission,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    AppleGlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        cornerRadius = 24.dp,
                                        backgroundColor = Color(0xFFFFFBEB),
                                        borderColor = Color(0xFFFDE68A)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Camera Access Required",
                                                    fontFamily = RoundedDisplayFont,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFB45309)
                                                )
                                                Text(
                                                    text = "Required for on-device hand gesture tracking. Video never leaves device.",
                                                    fontFamily = RoundedBodyFont,
                                                    fontSize = 12.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            TactileButton(
                                                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                                style = TactileButtonStyle.PRIMARY,
                                                modifier = Modifier.height(38.dp)
                                            ) {
                                                Text(
                                                    "Grant",
                                                    fontFamily = RoundedDisplayFont,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                // 3. Courier Buffer Stage (The virtual hand with organic liquid wave)
                                CourierBufferCard(
                                    courierItem = courierItem,
                                    targetDevice = targetDevice,
                                    onCaptureClick = {
                                        val projIntent = viewModel.captureManager.createProjectionIntent()
                                        if (projIntent != null && !viewModel.captureManager.isProjectionReady) {
                                            screenCaptureLauncher.launch(projIntent)
                                        } else {
                                            viewModel.captureScreenToBuffer(this@MainActivity)
                                            activeLiquidEvent = LiquidEventType.CAPTURE_SPLASH
                                        }
                                    },
                                    onReleaseClick = {
                                        activeLiquidEvent = LiquidEventType.SEND_STREAM
                                        viewModel.releaseCourierToTarget()
                                    },
                                    onDiscardClick = {
                                        viewModel.discardCourier()
                                    },
                                    onPreviewLiquidStream = {
                                        activeLiquidEvent = LiquidEventType.SEND_STREAM
                                    }
                                )

                                // 4. Gesture Radar & CameraX Telemetry Card
                                GestureRadarCard(
                                    gestureState = gestureState,
                                    gestureEngine = viewModel.gestureEngine,
                                    hasCameraPermission = hasCameraPermission,
                                    onSimulateFist = {
                                        activeLiquidEvent = LiquidEventType.CAPTURE_SPLASH
                                        viewModel.simulateFist()
                                    },
                                    onSimulateOpenHand = {
                                        if (courierItem != null) {
                                            activeLiquidEvent = LiquidEventType.SEND_STREAM
                                        }
                                        viewModel.simulateOpenHand()
                                    }
                                )

                                // 5. Recipient Status & Inset Grouped Device Card
                                InsetCupertinoDeviceCard(
                                    currentTarget = targetDevice,
                                    pairedCount = pairedDevices.size,
                                    onManageClick = { showDevicesSheet = true }
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Floating Cupertino Glass Bottom Bar
                            FloatingGlassBottomBar(
                                onDevicesClick = { showDevicesSheet = true },
                                onQuickCaptureClick = {
                                    val projIntent = viewModel.captureManager.createProjectionIntent()
                                    if (projIntent != null && !viewModel.captureManager.isProjectionReady) {
                                        screenCaptureLauncher.launch(projIntent)
                                    } else {
                                        viewModel.captureScreenToBuffer(this@MainActivity)
                                        activeLiquidEvent = LiquidEventType.CAPTURE_SPLASH
                                    }
                                },
                                onLiquidReplayClick = {
                                    activeLiquidEvent = LiquidEventType.CAPTURE_SPLASH
                                },
                                onHistoryClick = { showHistorySheet = true },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(bottom = 14.dp)
                            )
                        }
                    }

                    // Full-screen iOS 17 NameDrop liquid ripple overlay for capture and send
                    LiquidAnimationOverlay(
                        eventType = activeLiquidEvent,
                        targetDeviceName = targetDevice?.deviceName,
                        onAnimationComplete = { activeLiquidEvent = null }
                    )
                }

                // Modal Sheets
                if (showDevicesSheet) {
                    DevicePairingSheet(
                        myDeviceName = viewModel.myDeviceName,
                        myDeviceId = viewModel.myDeviceId,
                        myIp = localIp,
                        myPort = serverPort,
                        myPin = viewModel.myPairingPin,
                        pairedDevices = pairedDevices,
                        discoveredPeers = discoveredPeers,
                        onSelectTarget = { deviceId ->
                            viewModel.setTargetDevice(deviceId)
                        },
                        onRemoveDevice = { device ->
                            viewModel.removeDevice(device)
                        },
                        onPairDirect = { name, ip, port, pin ->
                            viewModel.pairDeviceDirect(name, ip, port, pin)
                        },
                        onPairQr = { uri ->
                            viewModel.pairDeviceWithQr(uri)
                        },
                        onDismiss = { showDevicesSheet = false }
                    )
                }

                if (showHistorySheet) {
                    TransferHistorySheet(
                        history = transferHistory,
                        onRetryTransfer = { record ->
                            activeLiquidEvent = LiquidEventType.SEND_STREAM
                            viewModel.retryFailedTransfer(record)
                        },
                        onDismiss = { showHistorySheet = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun CupertinoLargeTitle(
    localIp: String,
    serverPort: Int,
    targetDevice: com.example.data.entity.PairedDevice?,
    onPairClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FistShot",
                    fontFamily = RoundedDisplayFont,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A),
                    letterSpacing = (-0.8).sp
                )
                Text(
                    text = "Touchless Screen Beam • Zero-Cloud P2P",
                    fontFamily = RoundedBodyFont,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }

            // Quick QR button with subtle glass ring
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
                    .shadow(4.dp, CircleShape)
                    .clickable(onClick = onPairClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "Pairing QR",
                    tint = AppleBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Connection metadata pill row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgePill(
                text = "$localIp:$serverPort",
                backgroundColor = Color.White.copy(alpha = 0.85f),
                textColor = Color(0xFF334155),
                dotColor = AppleGreen
            )

            BadgePill(
                text = "mDNS READY",
                backgroundColor = Color(0xFFE0F2FE),
                textColor = AppleBlue
            )
        }
    }
}

@Composable
private fun InsetCupertinoDeviceCard(
    currentTarget: com.example.data.entity.PairedDevice?,
    pairedCount: Int,
    onManageClick: () -> Unit
) {
    AppleGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onManageClick() }
            .testTag("quick_device_bar"),
        cornerRadius = 28.dp,
        elevation = 3.dp,
        isLiquidGlass = true
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = AppleBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = if (currentTarget != null) currentTarget.deviceName else "No Recipient Selected",
                        fontFamily = RoundedDisplayFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = if (currentTarget != null) "${currentTarget.ipAddress}:${currentTarget.port} • Trusted Receiver" else "Tap to connect via QR or 6-digit PIN",
                        fontFamily = RoundedBodyFont,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                BadgePill(
                    text = if (currentTarget != null) "TARGET" else "PAIR",
                    backgroundColor = if (currentTarget != null) Color(0xFFE7F9ED) else Color(0xFFE0F2FE),
                    textColor = if (currentTarget != null) AppleGreen else AppleBlue
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Manage",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
