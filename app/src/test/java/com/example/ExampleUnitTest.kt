package com.example

import com.example.util.CryptoUtils
import com.example.util.QrCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun sha256_computesCorrectDeterministicHash() {
        val testData = "FistShot Test Payload".toByteArray(Charsets.UTF_8)
        val hash = CryptoUtils.sha256(testData)
        assertNotNull(hash)
        assertEquals(64, hash.length) // SHA-256 is 64 hex characters

        // Repeat calculation must be strictly deterministic
        val hash2 = CryptoUtils.sha256(testData)
        assertEquals(hash, hash2)
    }

    @Test
    fun qrCodeGenerator_createsValidMatrixAndFinders() {
        val uri = "fistshot://pair?id=abc123&name=Tablet&ip=192.168.1.10&port=8888&pin=492812"
        val matrix = QrCodeGenerator.generateMatrix(uri)
        assertEquals(25, matrix.size)

        // Top-left finder pattern corners must be dark
        assertTrue(matrix.isDark(0, 0))
        assertTrue(matrix.isDark(6, 0))
        assertTrue(matrix.isDark(0, 6))
        assertTrue(matrix.isDark(6, 6))

        // Center of finder pattern must be dark
        assertTrue(matrix.isDark(3, 3))
    }

    @Test
    fun pairingUri_roundTripEncoding() {
        val uri = QrCodeGenerator.createPairingUri(
            deviceId = "dev_1234",
            deviceName = "Pixel Tablet",
            ipAddress = "192.168.1.42",
            port = 8888,
            pin = "543210"
        )
        assertTrue(uri.startsWith("fistshot://pair?"))
        assertTrue(uri.contains("id=dev_1234"))
        assertTrue(uri.contains("ip=192.168.1.42"))
        assertTrue(uri.contains("port=8888"))
        assertTrue(uri.contains("pin=543210"))
    }
}
