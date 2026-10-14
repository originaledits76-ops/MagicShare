package com.example.data.model

import android.graphics.Bitmap
import com.example.data.entity.PairedDevice

data class CourierItem(
    val id: String,
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val sha256Hash: String,
    val width: Int,
    val height: Int,
    val capturedAt: Long = System.currentTimeMillis(),
    val bitmap: Bitmap? = null,
    val isTransferring: Boolean = false,
    val transferProgress: Float = 0f,
    val transferError: String? = null
)
