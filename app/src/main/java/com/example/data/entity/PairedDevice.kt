package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paired_devices")
data class PairedDevice(
    @PrimaryKey
    val id: String, // Device unique UUID or MAC/Fingerprint
    val deviceName: String,
    val ipAddress: String,
    val port: Int,
    val pairingPin: String,
    val isCurrentTarget: Boolean = false,
    val isOnline: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val trustedSince: Long = System.currentTimeMillis()
)
