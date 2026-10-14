package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.capture.ScreenCaptureManager
import com.example.data.database.AppDatabase
import com.example.data.entity.PairedDevice
import com.example.data.entity.TransferDirection
import com.example.data.entity.TransferRecord
import com.example.data.entity.TransferStatus
import com.example.data.model.CourierItem
import com.example.data.model.GestureState
import com.example.data.model.GestureTriggerEvent
import com.example.gesture.GestureDetectorEngine
import com.example.network.NetworkUtils
import com.example.network.PeerDiscoveryManager
import com.example.network.PeerTransferClient
import com.example.network.PeerTransferServer
import com.example.util.CryptoUtils
import com.example.util.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val pairedDeviceDao = db.pairedDeviceDao()
    private val transferRecordDao = db.transferRecordDao()

    val captureManager = ScreenCaptureManager(application)
    val gestureEngine = GestureDetectorEngine()
    val discoveryManager = PeerDiscoveryManager(application)
    val transferServer = PeerTransferServer(application)
    private val transferClient = PeerTransferClient()

    // Database Flows
    val pairedDevices: StateFlow<List<PairedDevice>> = pairedDeviceDao.getAllDevices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentTargetDevice: StateFlow<PairedDevice?> = pairedDeviceDao.getTargetDevice()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transferHistory: StateFlow<List<TransferRecord>> = transferRecordDao.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Courier Buffer State (The "virtual hand" holding content)
    private val _courierItem = MutableStateFlow<CourierItem?>(null)
    val courierItem: StateFlow<CourierItem?> = _courierItem.asStateFlow()

    // Notification / Toast Feedback Event Flow
    private val _userFeedback = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val userFeedback: SharedFlow<String> = _userFeedback.asSharedFlow()

    // Gesture State
    val gestureState: StateFlow<GestureState> = gestureEngine.gestureState

    // Device Network Info
    private val _localIp = MutableStateFlow(NetworkUtils.getLocalIpAddress())
    val localIp: StateFlow<String> = _localIp.asStateFlow()

    private val _serverPort = MutableStateFlow(8888)
    val serverPort: StateFlow<Int> = _serverPort.asStateFlow()

    val myDeviceName = NetworkUtils.getDeviceDisplayName()
    val myPairingPin = CryptoUtils.generatePairingPin()
    val myDeviceId = UUID.randomUUID().toString().take(8)

    init {
        // Start TCP Server
        val port = transferServer.start(viewModelScope, 8888)
        _serverPort.value = port

        // Start NSD Discovery & Advertising
        discoveryManager.startAdvertising("FistShot-$myDeviceName", port)
        discoveryManager.startDiscovery()

        // Listen for incoming peer transfers
        viewModelScope.launch {
            transferServer.incomingTransfers.collect { transfer ->
                handleIncomingTransfer(transfer)
            }
        }

        // Listen for gesture triggers from engine
        viewModelScope.launch {
            gestureEngine.gestureState.collect { state ->
                when (state.triggerEvent) {
                    GestureTriggerEvent.CAPTURE_SCREEN_TRIGGERED -> {
                        gestureEngine.consumeTriggerEvent()
                        captureScreenToBuffer(null)
                    }
                    GestureTriggerEvent.RELEASE_TRANSFER_TRIGGERED -> {
                        gestureEngine.consumeTriggerEvent()
                        releaseCourierToTarget()
                    }
                    else -> {}
                }
            }
        }

    }

    fun refreshNetworkInfo() {
        _localIp.value = NetworkUtils.getLocalIpAddress()
    }

    /**
     * Closes fist: Grabs the screen into the Courier Buffer.
     */
    fun captureScreenToBuffer(activity: Activity?) {
        viewModelScope.launch(Dispatchers.Main) {
            triggerHapticPulse(50)
            try {
                val item = captureManager.captureToCourierBuffer(activity)
                _courierItem.value = item
                _userFeedback.tryEmit("Hand closed: Screen captured into Courier Buffer!")
                triggerHapticPulse(100)
            } catch (e: Exception) {
                Log.e(TAG, "Screen capture failed", e)
                _userFeedback.tryEmit("Capture failed: ${e.message}")
            }
        }
    }

    /**
     * Opens hand: Releases and beams the buffered screen to the selected target device.
     */
    fun releaseCourierToTarget() {
        val current = _courierItem.value
        if (current == null) {
            _userFeedback.tryEmit("Courier Buffer is empty. Close fist to capture first!")
            return
        }

        val target = currentTargetDevice.value
        if (target == null) {
            _userFeedback.tryEmit("No receiving device selected. Select a target first!")
            return
        }

        beamCourierToDevice(target)
    }

    fun beamCourierToDevice(target: PairedDevice) {
        val current = _courierItem.value ?: return
        viewModelScope.launch {
            _courierItem.value = current.copy(isTransferring = true, transferProgress = 0.05f)
            triggerHapticPulse(40)

            val recordId = UUID.randomUUID().toString()
            val initialRecord = TransferRecord(
                id = recordId,
                fileName = current.fileName,
                fileSizeBytes = current.fileSizeBytes,
                sha256Hash = current.sha256Hash,
                targetDeviceName = target.deviceName,
                targetIp = target.ipAddress,
                direction = TransferDirection.SENT,
                status = TransferStatus.IN_PROGRESS,
                localFilePath = current.filePath
            )
            transferRecordDao.insertRecord(initialRecord)

            val file = File(current.filePath)
            val result = transferClient.sendScreenshot(
                targetIp = target.ipAddress,
                targetPort = target.port,
                file = file,
                sha256Hash = current.sha256Hash,
                senderName = myDeviceName
            ) { progress ->
                _courierItem.value = _courierItem.value?.copy(transferProgress = progress)
            }

            if (result.isSuccess) {
                triggerHapticPulse(150)
                _courierItem.value = null // Cleared after verified delivery
                _userFeedback.tryEmit("Delivered to ${target.deviceName}! SHA-256 verified.")

                transferRecordDao.updateRecord(
                    initialRecord.copy(status = TransferStatus.SUCCESS)
                )
            } else {
                _courierItem.value = current.copy(
                    isTransferring = false,
                    transferError = result.errorMessage
                )
                _userFeedback.tryEmit("Delivery failed: ${result.errorMessage}. Retained in buffer.")

                transferRecordDao.updateRecord(
                    initialRecord.copy(
                        status = TransferStatus.FAILED,
                        errorMessage = result.errorMessage
                    )
                )
            }
        }
    }

    private fun handleIncomingTransfer(transfer: PeerTransferServer.ReceivedTransfer) {
        viewModelScope.launch {
            triggerHapticPulse(120)
            _userFeedback.tryEmit("Incoming transfer from ${transfer.senderName} received! SHA-256 verified.")

            val record = TransferRecord(
                id = transfer.transferId,
                fileName = transfer.fileName,
                fileSizeBytes = transfer.fileSize,
                sha256Hash = transfer.sha256Hash,
                targetDeviceName = transfer.senderName,
                targetIp = transfer.senderIp,
                direction = TransferDirection.RECEIVED,
                status = if (transfer.isVerified) TransferStatus.SUCCESS else TransferStatus.FAILED,
                localFilePath = transfer.localFilePath
            )
            transferRecordDao.insertRecord(record)
        }
    }

    fun discardCourier() {
        _courierItem.value?.filePath?.let { path ->
            File(path).delete()
        }
        _courierItem.value = null
        _userFeedback.tryEmit("Courier Buffer cleared")
    }

    fun retryFailedTransfer(record: TransferRecord) {
        val target = pairedDevices.value.find { it.ipAddress == record.targetIp }
            ?: currentTargetDevice.value
        if (target == null) {
            _userFeedback.tryEmit("Cannot find target device for retry")
            return
        }
        val file = record.localFilePath?.let { File(it) }
        if (file == null || !file.exists()) {
            _userFeedback.tryEmit("Source image file no longer available")
            return
        }

        viewModelScope.launch {
            _userFeedback.tryEmit("Retrying delivery to ${target.deviceName}...")
            val result = transferClient.sendScreenshot(
                targetIp = target.ipAddress,
                targetPort = target.port,
                file = file,
                sha256Hash = record.sha256Hash,
                senderName = myDeviceName
            ) {}

            if (result.isSuccess) {
                _userFeedback.tryEmit("Retry succeeded! Delivered to ${target.deviceName}")
                transferRecordDao.updateRecord(record.copy(status = TransferStatus.SUCCESS, errorMessage = null))
            } else {
                _userFeedback.tryEmit("Retry failed: ${result.errorMessage}")
            }
        }
    }

    fun setTargetDevice(deviceId: String) {
        viewModelScope.launch {
            pairedDeviceDao.clearCurrentTargets()
            pairedDeviceDao.setTargetDevice(deviceId)
            val dev = pairedDevices.value.find { it.id == deviceId }
            _userFeedback.tryEmit("Active target set to ${dev?.deviceName ?: "device"}")
        }
    }

    fun pairDeviceWithQr(uriString: String): Boolean {
        val payload = QrCodeGenerator.parsePairingUri(uriString) ?: return false
        viewModelScope.launch {
            val device = PairedDevice(
                id = payload.deviceId,
                deviceName = payload.deviceName,
                ipAddress = payload.ipAddress,
                port = payload.port,
                pairingPin = payload.pin,
                isCurrentTarget = true,
                isOnline = true
            )
            pairedDeviceDao.clearCurrentTargets()
            pairedDeviceDao.insertOrUpdate(device)
            _userFeedback.tryEmit("Paired with ${payload.deviceName}!")
        }
        return true
    }

    fun pairDeviceDirect(name: String, ip: String, port: Int, pin: String) {
        viewModelScope.launch {
            val device = PairedDevice(
                id = UUID.randomUUID().toString().take(8),
                deviceName = name.ifBlank { "Remote Device" },
                ipAddress = ip.trim(),
                port = if (port > 0) port else 8888,
                pairingPin = pin.trim(),
                isCurrentTarget = true,
                isOnline = true
            )
            pairedDeviceDao.clearCurrentTargets()
            pairedDeviceDao.insertOrUpdate(device)
            _userFeedback.tryEmit("Trusted device '${device.deviceName}' added!")
        }
    }

    fun removeDevice(device: PairedDevice) {
        viewModelScope.launch {
            pairedDeviceDao.deleteDevice(device)
            _userFeedback.tryEmit("Removed ${device.deviceName}")
        }
    }

    fun simulateFist() {
        gestureEngine.simulateFistCapture()
    }

    fun simulateOpenHand() {
        gestureEngine.simulateOpenHandRelease()
    }

    private fun triggerHapticPulse(durationMs: Long) {
        try {
            val app = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Haptic optional on some hardware
        }
    }

    override fun onCleared() {
        super.onCleared()
        discoveryManager.stopAdvertising()
        discoveryManager.stopDiscovery()
        transferServer.stop()
        captureManager.release()
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}
