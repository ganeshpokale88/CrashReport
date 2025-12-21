package com.github.ganeshpokale88.crashreporter.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.ganeshpokale88.crashreporter.DependencyRegistry
import com.github.ganeshpokale88.crashreporter.EncryptionUtil
import com.github.ganeshpokale88.crashreporter.UploadWorkerScheduler
import com.github.ganeshpokale88.crashreporter.database.CrashLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * WorkManager worker to process encrypted crash logs from files and store them in Room database
 */
class CrashLogWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val crashLogDao by lazy { DependencyRegistry.getCrashLogDao() }
    val TAG = "CrashLogWorker"
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val crashLogsDir = File(applicationContext.filesDir, "crash_logs")
            if (!crashLogsDir.exists()) {
                return@withContext Result.success()
            }

            val crashFiles = crashLogsDir.listFiles { file: File ->
                file.name.endsWith(".enc")
            } ?: emptyArray()

            var hasProcessedLogs = false

            for (file in crashFiles) {
                try {
                    // Read encrypted content
                    val encryptedContent = file.readText()
                    
                    // Decrypt content
                    val decryptedContent = EncryptionUtil.decrypt(applicationContext, encryptedContent)
                    
                    // Parse the content (format: timestamp|isFatal|androidVersion|deviceMake|deviceModel|stackTrace)
                    val parts = decryptedContent.split("|", limit = 6)
                    if (parts.size == 6) {
                        val timestamp = parts[0].toLong()
                        val isFatal = parts[1].toBoolean()
                        val androidVersion = parts[2]
                        val deviceMake = parts[3]
                        val deviceModel = parts[4]
                        val stackTrace = parts[5]

                        // Insert into database
                        val crashLog = CrashLogEntity(
                            timestamp = timestamp,
                            stackTrace = stackTrace,
                            androidVersion = androidVersion,
                            deviceMake = deviceMake,
                            deviceModel = deviceModel,
                            isFatal = isFatal
                        )
                        crashLogDao.insertCrashLog(crashLog)
                        hasProcessedLogs = true

                        // Delete processed file
                        file.delete()
                    }
                } catch (e: Exception) {
                    // Log error but continue processing other files
                    e.printStackTrace()
                }
            }
            Log.d(TAG, "doWork: hasProcessedLogs:$hasProcessedLogs")
            
            // Perform data retention cleanup - delete old logs from database
            cleanupOldCrashLogs()
            
            // Schedule upload worker if new crash logs were processed and inserted into database
            if (hasProcessedLogs) {
                UploadWorkerScheduler.scheduleUploadWorker(applicationContext)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    /**
     * Clean up old crash logs from database based on data retention policy.
     * Deletes logs older than the configured retention period.
     */
    private suspend fun cleanupOldCrashLogs() {
        try {
            val config = DependencyRegistry.getConfig()
            val retentionDays = config?.dataRetentionDays ?: 90L
            
            // If retention is disabled (0 or negative), skip cleanup
            if (retentionDays <= 0) {
                return
            }
            
            // Calculate cutoff timestamp (current time - retention period)
            val cutoffTimestamp = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000)
            
            // Delete logs older than cutoff timestamp
            val deletedCount = crashLogDao.deleteCrashLogsOlderThan(cutoffTimestamp)
            
            if (deletedCount > 0) {
                Log.d(TAG, "Deleted $deletedCount old crash log(s) older than $retentionDays days")
            }
        } catch (e: Exception) {
            // Log error but don't fail the worker - cleanup can be retried next time
            Log.e(TAG, "Failed to cleanup old crash logs", e)
        }
    }
}

