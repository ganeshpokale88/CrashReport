package com.github.ganeshpokale88.crashreporter.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a crash log in the database
 */
@Entity(tableName = "crash_logs")
data class CrashLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val stackTrace: String,
    val androidVersion: String,
    val deviceMake: String,
    val deviceModel: String,
    val isFatal: Boolean
)

