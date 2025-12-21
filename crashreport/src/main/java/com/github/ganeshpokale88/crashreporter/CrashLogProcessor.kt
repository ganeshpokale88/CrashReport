package com.github.ganeshpokale88.crashreporter

import android.content.Context
import com.github.ganeshpokale88.crashreporter.database.CrashLogDao
import com.github.ganeshpokale88.crashreporter.database.CrashLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * Utility class to process crash logs from encrypted files to Room database
 * Can be used directly without WorkManager as a fallback
 */
object CrashLogProcessor {
    
    /**
     * Process all encrypted crash log files and store them in the database
     * This can be called directly if WorkManager is not available
     */
    fun processCrashLogs(
        context: Context,
        crashLogDao: CrashLogDao,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) {
        scope.launch {
            try {
                val crashLogsDir = File(context.filesDir, "crash_logs")
                if (!crashLogsDir.exists()) {
                    return@launch
                }

                val crashFiles = crashLogsDir.listFiles { file: File ->
                    file.name.endsWith(".enc")
                } ?: emptyArray()

                var hasNewCrashLogs = false

                for (file in crashFiles) {
                    try {
                        // Read encrypted content
                        val encryptedContent = file.readText()
                        
                        // Decrypt content
                        val decryptedContent = EncryptionUtil.decrypt(context, encryptedContent)
                        
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
                            hasNewCrashLogs = true

                            // Delete processed file
                            file.delete()
                        }
                    } catch (e: Exception) {
                        // Log error but continue processing other files
                        e.printStackTrace()
                    }
                }

                // Schedule upload worker if new crash logs were added
                if (hasNewCrashLogs) {
                    UploadWorkerScheduler.scheduleUploadWorker(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

