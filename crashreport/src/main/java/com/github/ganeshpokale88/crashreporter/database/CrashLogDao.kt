package com.github.ganeshpokale88.crashreporter.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for crash logs
 */
@Dao
interface CrashLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrashLog(crashLog: CrashLogEntity): Long

    @Query("SELECT * FROM crash_logs ORDER BY timestamp DESC")
    fun getAllCrashLogs(): Flow<List<CrashLogEntity>>

    @Query("SELECT * FROM crash_logs ORDER BY timestamp ASC")
    suspend fun getAllCrashLogsList(): List<CrashLogEntity>

    @Query("SELECT * FROM crash_logs WHERE id = :id")
    suspend fun getCrashLogById(id: Long): CrashLogEntity?

    @Query("DELETE FROM crash_logs WHERE id = :id")
    suspend fun deleteCrashLog(id: Long)

    @Query("DELETE FROM crash_logs WHERE id IN (:ids)")
    suspend fun deleteCrashLogsByIds(ids: List<Long>)
    
    /**
     * Delete crash logs older than the specified timestamp.
     * Used for data retention policy enforcement.
     * 
     * @param olderThanTimestamp Timestamp in milliseconds. Logs with timestamp less than this will be deleted.
     * @return Number of deleted rows
     */
    @Query("DELETE FROM crash_logs WHERE timestamp < :olderThanTimestamp")
    suspend fun deleteCrashLogsOlderThan(olderThanTimestamp: Long): Int
}

