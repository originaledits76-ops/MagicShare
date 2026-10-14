package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.PairedDevice
import com.example.network.PeerDiscoveryManager
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.BadgePill
import com.example.ui.components.QrCanvas
import com.example.ui.components.TactileButton
import com.example.ui.components.TactileButtonStyle
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleRed
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardSurface
import com.example.ui.theme.FrostedSurface
import com.example.ui.theme.RoundedBodyFont
import com.example.ui.theme.RoundedDisplayFont
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.QrCodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicePairingSheet(
    myDeviceName: String,
    myDeviceId: String,
    myIp: String,
    myPort: Int,
    myPin: String,
    pairedDevices: List<PairedDevice>,
    discoveredPeers: List<PeerDiscoveryManager.DiscoveredPeer>,
    onSelectTarget: (String) -> Unit,
    onRemoveDevice: (PairedDevice) -> Unit,
    onPairDirect: (String, String, Int, String) -> Unit,
    onPairQr: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Pair form states
    var peerNameInput by remember { mutableStateOf("") }
    var peerIpInput by remember { mutableStateOf("") }
    var peerPortInput by remember { mutableStateOf("8888") }
    var peerPinInput by remember { mutableStateOf("") }
    var qrTextInput by remember { mutableStateOf("") }

    val myPairingUri = remember(myDeviceId, myDeviceName, myIp, myPort, myPin) {
        QrCodeGenerator.createPairingUri(
            deviceId = myDeviceId,
            deviceName = myDeviceName,
            ipAddress = myIp,
            port = myPort,
            pin = myPin
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF8F9FD),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD1D5DB))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Device Pairing & Network",
                        fontFamily = RoundedDisplayFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "Zero-cloud direct peer-to-peer connection",
                        fontFamily = RoundedBodyFont,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = FrostedSurface,
                indicator = {},
                divider = {},
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                listOf("My Device (Receive)", "Paired Devices", "+ Pair New").forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) AppleBlue else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            when (selectedTab) {
                0 -> {
                    // Receiver Mode: Show QR Code & PIN
                    MyDeviceReceiverView(
                        deviceName = myDeviceName,
                        ipAddress = myIp,
                        port = myPort,
                        pin = myPin,
                        pairingUri = myPairingUri
                    )
                }
                1 -> {
                    // Paired Devices & NSD
                    PairedDevicesListView(
                        pairedDevices = pairedDevices,
                        discoveredPeers = discoveredPeers,
                        onSelectTarget = onSelectTarget,
                        onRemoveDevice = onRemoveDevice,
                        onPairDiscovered = { peer ->
                            onPairDirect(peer.serviceName, peer.ipAddress, peer.port, "000000")
                        }
                    )
                }
                2 -> {
                    // Manual Connect Form
                    ManualPairingView(
                        peerName = peerNameInput,
                        onPeerNameChange = { peerNameInput = it },
                        peerIp = peerIpInput,
                        onPeerIpChange = { peerIpInput = it },
                        peerPort = peerPortInput,
                        onPeerPortChange = { peerPortInput = it },
                        peerPin = peerPinInput,
                        onPeerPinChange = { peerPinInput = it },
                        qrText = qrTextInput,
                        onQrTextChange = { qrTextInput = it },
                        onConnect = {
                            if (qrTextInput.isNotBlank()) {
                                onPairQr(qrTextInput.trim())
                            } else {
                                onPairDirect(
                                    peerNameInput.ifBlank { "Tablet" },
                                    peerIpInput,
                                    peerPortInput.toIntOrNull() ?: 8888,
                                    peerPinInput
                                )
                            }
                            selectedTab = 1
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MyDeviceReceiverView(
    deviceName: String,
    ipAddress: String,
    port: Int,
    pin: String,
    pairingUri: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            elevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Scan to Pair on Receiver Device",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                // High Contrast QR Matrix Canvas
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                ) {
                    QrCanvas(payload = pairingUri)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 6-digit numeric PIN card
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(FrostedSurface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "6-DIGIT PAIRING PIN: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Text(
                        text = pin,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = RoundedBodyFont,
                        color = AppleBlue,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Network Coordinates
                Text(
                    text = "$deviceName • $ipAddress:$port",
                    fontSize = 12.sp,
                    fontFamily = RoundedBodyFont,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PairedDevicesListView(
    pairedDevices: List<PairedDevice>,
    discoveredPeers: List<PeerDiscoveryManager.DiscoveredPeer>,
    onSelectTarget: (String) -> Unit,
    onRemoveDevice: (PairedDevice) -> Unit,
    onPairDiscovered: (PeerDiscoveryManager.DiscoveredPeer) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "TRUSTED RECIPIENTS (${pairedDevices.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
        }

        if (pairedDevices.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No devices paired yet. Scan QR or enter PIN to add.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(pairedDevices) { device ->
                AppleGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTarget(device.id) },
                    cornerRadius = 16.dp,
                    elevation = if (device.isCurrentTarget) 3.dp else 1.dp,
                    borderColor = if (device.isCurrentTarget) AppleBlue else BorderSubtle
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (device.isCurrentTarget) Color(0xFFE8F2FF) else FrostedSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Devices,
                                    contentDescription = null,
                                    tint = if (device.isCurrentTarget) AppleBlue else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = device.deviceName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    if (device.isCurrentTarget) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        BadgePill(
                                            text = "TARGET",
                                            backgroundColor = AppleBlue,
                                            textColor = Color.White
                                        )
                                    }
                                }
                                Text(
                                    text = "${device.ipAddress}:${device.port}",
                                    fontSize = 11.sp,
                                    fontFamily = RoundedBodyFont,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(onClick = { onRemoveDevice(device) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove Device",
                                tint = AppleRed.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Live NSD Discovered Peers
        if (discoveredPeers.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "DISCOVERED ON LOCAL WI-FI (mDNS)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppleBlue,
                    letterSpacing = 0.5.sp
                )
            }
            items(discoveredPeers) { peer ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FrostedSurface)
                        .clickable { onPairDiscovered(peer) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = AppleBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = peer.serviceName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "${peer.ipAddress}:${peer.port}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    BadgePill(
                        text = "+ PAIR",
                        backgroundColor = Color(0xFFE8F2FF),
                        textColor = AppleBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun ManualPairingView(
    peerName: String,
    onPeerNameChange: (String) -> Unit,
    peerIp: String,
    onPeerIpChange: (String) -> Unit,
    peerPort: String,
    onPeerPortChange: (String) -> Unit,
    peerPin: String,
    onPeerPinChange: (String) -> Unit,
    qrText: String,
    onQrTextChange: (String) -> Unit,
    onConnect: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = qrText,
            onValueChange = onQrTextChange,
            label = { Text("Paste QR Pairing URI (Optional)", fontSize = 12.sp) },
            placeholder = { Text("fistshot://pair?id=...&ip=...", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "— OR ENTER DETAILS MANUALLY —",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = peerName,
            onValueChange = onPeerNameChange,
            label = { Text("Device Name", fontSize = 12.sp) },
            placeholder = { Text("e.g. Pixel Tablet", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = peerIp,
                onValueChange = onPeerIpChange,
                label = { Text("IP Address", fontSize = 12.sp) },
                placeholder = { Text("192.168.1.50", fontSize = 12.sp) },
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = peerPort,
                onValueChange = onPeerPortChange,
                label = { Text("Port", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = peerPin,
            onValueChange = onPeerPinChange,
            label = { Text("6-Digit Pairing PIN", fontSize = 12.sp) },
            placeholder = { Text("123456", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        TactileButton(
            onClick = onConnect,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("submit_pair_button"),
            style = TactileButtonStyle.PRIMARY,
            enabled = qrText.isNotBlank() || peerIp.isNotBlank()
        ) {
            Text(
                text = "Pair & Trust Device",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}
