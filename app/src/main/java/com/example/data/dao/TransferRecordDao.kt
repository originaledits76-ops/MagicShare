package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.TransferRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferRecordDao {
    @Query("SELECT * FROM transfer_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<TransferRecord>>

    @Query("SELECT * FROM transfer_records WHERE status = 'FAILED' ORDER BY timestamp DESC")
    fun getFailedRecords(): Flow<List<TransferRecord>>

    @Query("SELECT * FROM transfer_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: String): TransferRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TransferRecord)

    @Update
    suspend fun updateRecord(record: TransferRecord)

    @Query("DELETE FROM transfer_records WHERE id = :id")
    suspend fun deleteRecordById(id: String)

    @Query("DELETE FROM transfer_records")
    suspend fun clearAll()
}
