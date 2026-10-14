package com.example.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket

class PeerTransferClient {

    data class SendResult(
        val isSuccess: Boolean,
        val bytesSent: Long,
        val sha256: String,
        val errorMessage: String? = null
    )

    suspend fun sendScreenshot(
        targetIp: String,
        targetPort: Int,
        file: File,
        sha256Hash: String,
        senderName: String,
        onProgress: (Float) -> Unit
    ): SendResult = withContext(Dispatchers.IO) {
        val socket = Socket()
        try {
            socket.connect(InetSocketAddress(targetIp, targetPort), 7000)
            socket.soTimeout = 30000

            val outputStream = socket.getOutputStream()
            val inputStream = socket.getInputStream()
            val writer = OutputStreamWriter(outputStream, Charsets.UTF_8)
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

            // 1. Send metadata header
            val metadata = JSONObject().apply {
                put("id", System.currentTimeMillis().toString())
                put("fileName", file.name)
                put("fileSize", file.length())
                put("sha256", sha256Hash)
                put("senderName", senderName)
            }
            writer.write(metadata.toString() + "\n")
            writer.flush()

            // 2. Wait for server readiness
            val serverAck = reader.readLine()
            if (serverAck != "READY_FOR_PAYLOAD") {
                return@withContext SendResult(
                    isSuccess = false,
                    bytesSent = 0,
                    sha256 = sha256Hash,
                    errorMessage = "Server rejected payload: $serverAck"
                )
            }

            // 3. Stream payload bytes
            val totalBytes = file.length()
            var bytesSent = 0L
            val buffer = ByteArray(8192)

            FileInputStream(file).use { fis ->
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                    bytesSent += read
                    val progress = if (totalBytes > 0) bytesSent.toFloat() / totalBytes else 1f
                    onProgress(progress)
                }
                outputStream.flush()
            }

            // 4. Wait for SHA-256 verification ACK
            val verificationAck = reader.readLine()
            if (verificationAck == "VERIFIED_OK") {
                Log.d(TAG, "Server confirmed SHA-256 integrity")
                return@withContext SendResult(
                    isSuccess = true,
                    bytesSent = bytesSent,
                    sha256 = sha256Hash
                )
            } else {
                return@withContext SendResult(
                    isSuccess = false,
                    bytesSent = bytesSent,
                    sha256 = sha256Hash,
                    errorMessage = "Integrity check failed: $verificationAck"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Transmission failed", e)
            return@withContext SendResult(
                isSuccess = false,
                bytesSent = 0,
                sha256 = sha256Hash,
                errorMessage = e.localizedMessage ?: "Connection error"
            )
        } finally {
            try {
                socket.close()
            } catch (ignored: Exception) {}
        }
    }

    companion object {
        private const val TAG = "PeerTransferClient"
    }
}
