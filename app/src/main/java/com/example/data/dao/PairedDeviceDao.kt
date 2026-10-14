package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.PairedDevice
import kotlinx.coroutines.flow.Flow

@Dao
interface PairedDeviceDao {
    @Query("SELECT * FROM paired_devices ORDER BY isCurrentTarget DESC, lastSeenTimestamp DESC")
    fun getAllDevices(): Flow<List<PairedDevice>>

    @Query("SELECT * FROM paired_devices WHERE isCurrentTarget = 1 LIMIT 1")
    fun getTargetDevice(): Flow<PairedDevice?>

    @Query("SELECT * FROM paired_devices WHERE id = :id LIMIT 1")
    suspend fun getDeviceById(id: String): PairedDevice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(device: PairedDevice)

    @Query("UPDATE paired_devices SET isCurrentTarget = 0")
    suspend fun clearCurrentTargets()

    @Query("UPDATE paired_devices SET isCurrentTarget = 1 WHERE id = :deviceId")
    suspend fun setTargetDevice(deviceId: String)

    @Query("UPDATE paired_devices SET isOnline = :isOnline, lastSeenTimestamp = :timestamp WHERE ipAddress = :ip")
    suspend fun updateDeviceStatus(ip: String, isOnline: Boolean, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteDevice(device: PairedDevice)

    @Query("DELETE FROM paired_devices WHERE id = :id")
    suspend fun deleteDeviceById(id: String)
}
