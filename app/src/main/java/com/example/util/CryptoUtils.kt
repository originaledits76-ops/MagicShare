package com.example.util

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import kotlin.random.Random

object CryptoUtils {
    fun sha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val hash = digest.digest()
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024f)
            else -> "%.2f MB".format(bytes / (1024f * 1024f))
        }
    }

    fun generatePairingPin(): String {
        val pin = Random.nextInt(100000, 999999)
        return pin.toString()
    }
}
