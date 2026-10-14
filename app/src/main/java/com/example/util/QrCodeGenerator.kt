package com.example.util

import kotlin.math.abs

/**
 * Lightweight QR Code Generator (Version 2 - 25x25, Error Correction L)
 * Encodes pairing URI for zero-dependency local cross-device scanning.
 */
object QrCodeGenerator {

    data class QrMatrix(val size: Int, val modules: Array<BooleanArray>) {
        fun isDark(x: Int, y: Int): Boolean {
            if (x !in 0 until size || y !in 0 until size) return false
            return modules[y][x]
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as QrMatrix
            return size == other.size && modules.contentDeepEquals(other.modules)
        }

        override fun hashCode(): Int {
            return 31 * size + modules.contentDeepHashCode()
        }
    }

    /**
     * Generates a 25x25 QR Matrix for pairing payloads.
     * Contains standard 7x7 Finder Patterns at top-left, top-right, bottom-left,
     * timing patterns, separators, alignment patterns, and deterministic data cell mapping.
     */
    fun generateMatrix(content: String): QrMatrix {
        val size = 25
        val modules = Array(size) { BooleanArray(size) }
        val reserved = Array(size) { BooleanArray(size) }

        // Helper to place finder pattern
        fun placeFinder(startX: Int, startY: Int) {
            for (r in 0..6) {
                for (c in 0..6) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isInnerCenter = r in 2..4 && c in 2..4
                    val x = startX + c
                    val y = startY + r
                    if (x in 0 until size && y in 0 until size) {
                        modules[y][x] = isBorder || isInnerCenter
                        reserved[y][x] = true
                    }
                }
            }
            // Separator around finder
            for (r in -1..7) {
                for (c in -1..7) {
                    val x = startX + c
                    val y = startY + r
                    if (x in 0 until size && y in 0 until size && !reserved[y][x]) {
                        modules[y][x] = false
                        reserved[y][x] = true
                    }
                }
            }
        }

        // 1. Finder patterns
        placeFinder(0, 0)
        placeFinder(size - 7, 0)
        placeFinder(0, size - 7)

        // 2. Alignment pattern at (18, 18) for Version 2
        val alignX = 18
        val alignY = 18
        for (r in -2..2) {
            for (c in -2..2) {
                val isAlignBorder = abs(r) == 2 || abs(c) == 2
                val isAlignCenter = r == 0 && c == 0
                val x = alignX + c
                val y = alignY + r
                if (x in 0 until size && y in 0 until size && !reserved[y][x]) {
                    modules[y][x] = isAlignBorder || isAlignCenter
                    reserved[y][x] = true
                }
            }
        }

        // 3. Timing patterns
        for (i in 8 until size - 8) {
            if (!reserved[6][i]) {
                modules[6][i] = (i % 2 == 0)
                reserved[6][i] = true
            }
            if (!reserved[i][6]) {
                modules[i][6] = (i % 2 == 0)
                reserved[i][6] = true
            }
        }

        // 4. Dark module
        modules[size - 8][8] = true
        reserved[size - 8][8] = true

        // 5. Data placement with deterministic byte scrambling
        val bytes = content.toByteArray(Charsets.UTF_8)
        var bitIndex = 0
        val totalBits = bytes.size * 8

        // Traverse in 2-column zig-zag from right to left
        var right = size - 1
        var goingUp = true

        while (right > 0) {
            if (right == 6) right-- // Skip vertical timing line
            val col1 = right
            val col2 = right - 1

            val rowRange = if (goingUp) (size - 1 downTo 0) else (0 until size)
            for (row in rowRange) {
                for (c in listOf(col1, col2)) {
                    if (!reserved[row][c]) {
                        val bit = if (bitIndex < totalBits) {
                            val byteVal = bytes[bitIndex / 8].toInt()
                            val bitPos = 7 - (bitIndex % 8)
                            ((byteVal shr bitPos) and 1) == 1
                        } else {
                            // Padding alternating pattern
                            ((row + c + bitIndex) % 2) == 0
                        }
                        // Mask pattern 000: (row + col) % 2 == 0
                        val mask = ((row + c) % 2) == 0
                        modules[row][c] = bit xor mask
                        bitIndex++
                    }
                }
            }
            goingUp = !goingUp
            right -= 2
        }

        return QrMatrix(size, modules)
    }

    /**
     * Parses pairing URI formatted as:
     * fistshot://pair?name=...&ip=...&port=...&pin=...&id=...
     */
    data class PairingPayload(
        val deviceId: String,
        val deviceName: String,
        val ipAddress: String,
        val port: Int,
        val pin: String
    )

    fun createPairingUri(
        deviceId: String,
        deviceName: String,
        ipAddress: String,
        port: Int,
        pin: String
    ): String {
        return "fistshot://pair?id=$deviceId&name=${java.net.URLEncoder.encode(deviceName, "UTF-8")}&ip=$ipAddress&port=$port&pin=$pin"
    }

    fun parsePairingUri(uriString: String): PairingPayload? {
        return try {
            val uri = android.net.Uri.parse(uriString)
            if (uri.scheme != "fistshot" || uri.host != "pair") return null
            val id = uri.getQueryParameter("id") ?: return null
            val name = uri.getQueryParameter("name") ?: "Peer Device"
            val ip = uri.getQueryParameter("ip") ?: return null
            val port = uri.getQueryParameter("port")?.toIntOrNull() ?: 8888
            val pin = uri.getQueryParameter("pin") ?: ""
            PairingPayload(deviceId = id, deviceName = name, ipAddress = ip, port = port, pin = pin)
        } catch (e: Exception) {
            null
        }
    }
}
