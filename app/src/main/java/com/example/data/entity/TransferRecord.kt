package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransferDirection {
    SENT,
    RECEIVED
}

enum class TransferStatus {
    PENDING,
    IN_PROGRESS,
    SUCCESS,
    FAILED
}

@Entity(tableName = "transfer_records")
data class TransferRecord(
    @PrimaryKey
    val id: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val sha256Hash: String,
    val targetDeviceName: String,
    val targetIp: String,
    val direction: TransferDirection,
    val status: TransferStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val localFilePath: String? = null,
    val errorMessage: String? = null
)
