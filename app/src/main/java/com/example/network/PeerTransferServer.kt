package com.example.network

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.example.util.CryptoUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest

class PeerTransferServer(private val context: Context) {

    data class ReceivedTransfer(
        val transferId: String,
        val fileName: String,
        val fileSize: Long,
        val sha256Hash: String,
        val senderName: String,
        val senderIp: String,
        val localFilePath: String,
        val isVerified: Boolean
    )

    private val _incomingTransfers = MutableSharedFlow<ReceivedTransfer>(extraBufferCapacity = 10)
    val incomingTransfers: SharedFlow<ReceivedTransfer> = _incomingTransfers.asSharedFlow()

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private var assignedPort: Int = 8888

    fun getPort(): Int = assignedPort

    fun start(scope: CoroutineScope, preferredPort: Int = 8888): Int {
        stop()
        try {
            serverSocket = try {
                ServerSocket(preferredPort).also { assignedPort = preferredPort }
            } catch (e: Exception) {
                // If port is in use, let system assign any available port
                ServerSocket(0).also { assignedPort = it.localPort }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind server socket", e)
            return -1
        }

        serverJob = scope.launch(Dispatchers.IO) {
            Log.d(TAG, "Transfer server listening on port $assignedPort")
            while (isActive && serverSocket != null && !serverSocket!!.isClosed) {
                try {
                    val client = serverSocket!!.accept()
                    launch(Dispatchers.IO) {
                        handleClient(client)
                    }
                } catch (e: Exception) {
                    if (isActive) {
                        Log.e(TAG, "Error accepting connection", e)
                    }
                }
            }
        }
        return assignedPort
    }

    private fun handleClient(socket: Socket) {
        val clientIp = socket.inetAddress?.hostAddress ?: "Unknown"
        Log.d(TAG, "Client connected from $clientIp")
        try {
            socket.soTimeout = 30000
            val inputStream = socket.getInputStream()
            val outputStream = socket.getOutputStream()
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val writer = OutputStreamWriter(outputStream, Charsets.UTF_8)

            // 1. Read metadata header line
            val metadataLine = reader.readLine() ?: return
            val json = JSONObject(metadataLine)
            val transferId = json.optString("id", System.currentTimeMillis().toString())
            val fileName = json.optString("fileName", "screenshot.png")
            val fileSize = json.optLong("fileSize", 0L)
            val expectedSha256 = json.optString("sha256", "")
            val senderName = json.optString("senderName", "Peer Device")

            // 2. Acknowledge and notify ready
            writer.write("READY_FOR_PAYLOAD\n")
            writer.flush()

            // 3. Receive payload and compute SHA-256
            val storageDir = File(context.filesDir, "received_screenshots").apply { mkdirs() }
            val outputFile = File(storageDir, "${System.currentTimeMillis()}_$fileName")
            val digest = MessageDigest.getInstance("SHA-256")

            var totalBytesRead = 0L
            FileOutputStream(outputFile).use { fos ->
                val buffer = ByteArray(8192)
                var bytesRead = 0
                while (totalBytesRead < fileSize && inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val toWrite = minOf(bytesRead.toLong(), fileSize - totalBytesRead).toInt()
                    fos.write(buffer, 0, toWrite)
                    digest.update(buffer, 0, toWrite)
                    totalBytesRead += toWrite
                }
                fos.flush()
            }

            val computedSha256 = digest.digest().joinToString("") { "%02x".format(it) }
            val isVerified = computedSha256.equals(expectedSha256, ignoreCase = true)

            if (isVerified) {
                writer.write("VERIFIED_OK\n")
                writer.flush()

                val transfer = ReceivedTransfer(
                    transferId = transferId,
                    fileName = fileName,
                    fileSize = totalBytesRead,
                    sha256Hash = computedSha256,
                    senderName = senderName,
                    senderIp = clientIp,
                    localFilePath = outputFile.absolutePath,
                    isVerified = true
                )
                _incomingTransfers.tryEmit(transfer)
                Log.d(TAG, "Transfer completed & SHA-256 verified successfully: $fileName")
            } else {
                writer.write("ERROR_HASH_MISMATCH\n")
                writer.flush()
                Log.e(TAG, "Transfer corrupted: expected $expectedSha256 but got $computedSha256")
                outputFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling incoming client", e)
        } finally {
            try {
                socket.close()
            } catch (ignored: Exception) {}
        }
    }

    fun stop() {
        serverJob?.cancel()
        serverJob = null
        try {
            serverSocket?.close()
        } catch (ignored: Exception) {}
        serverSocket = null
    }

    companion object {
        private const val TAG = "PeerTransferServer"
    }
}
