package com.github.ganeshpokale88.crashreporter.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.ganeshpokale88.crashreporter.DependencyRegistry
import com.github.ganeshpokale88.crashreporter.api.model.CrashReport
import com.github.ganeshpokale88.crashreporter.database.CrashLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

private const val TAG = "CrashReporter"

/**
 * WorkManager worker to upload crash logs from database to server
 * and clean up successfully uploaded logs
 */
class CrashUploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val crashLogDao by lazy { DependencyRegistry.getCrashLogDao() }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val config = DependencyRegistry.getConfig()
            
            // Check if baseUrl is missing
            if (config?.baseUrl.isNullOrBlank()) {
                if (android.util.Log.isLoggable(TAG, android.util.Log.WARN)) {
                    android.util.Log.w(TAG, 
                        "⚠️ CrashReporter: baseUrl is missing. API calls will not be made. " +
                        "Please configure baseUrl using CrashReporterConfig.Builder().baseUrl()")
                }
                return@withContext Result.success() // Return success so we don't retry immediately
            }
            
            // Check if apiEndpoint is missing
            if (config?.apiEndpoint.isNullOrBlank()) {
                if (android.util.Log.isLoggable(TAG, android.util.Log.WARN)) {
                    android.util.Log.w(TAG, 
                        "⚠️ CrashReporter: apiEndpoint is missing. API calls will not be made. " +
                        "Please configure apiEndpoint using CrashReporterConfig.Builder().apiEndpoint()")
                }
                return@withContext Result.success() // Return success so we don't retry immediately
            }
            
            // Check if API is configured - if not, skip upload (will retry later when configured)
            if (!DependencyRegistry.isApiConfigured()) {
                if (android.util.Log.isLoggable(TAG, android.util.Log.DEBUG)) {
                    android.util.Log.d(TAG, 
                        "API not fully configured yet, skipping upload. Crash logs remain in database.")
                }
                return@withContext Result.success() // Return success so we don't retry immediately
            }
            
            // Fetch all crash logs from database
            val crashLogs = crashLogDao.getAllCrashLogsList()

            if (crashLogs.isEmpty()) {
                // No crash logs to upload
                return@withContext Result.success()
            }

            // Get API instance and config (we know it's configured from check above)
            val crashReportApi = DependencyRegistry.getCrashReportApi()
            val finalConfig = DependencyRegistry.getConfig()
                ?: throw IllegalStateException("Config is null but API is configured")

            // Construct full URL from baseUrl + apiEndpoint
            val baseUrl = finalConfig.baseUrl.trimEnd('/') // Remove trailing slash if present
            val apiEndpoint = finalConfig.apiEndpoint!!.let { endpoint ->
                // Ensure endpoint starts with "/"
                if (endpoint.startsWith("/")) endpoint else "/$endpoint"
            }
            val fullUrl = "$baseUrl$apiEndpoint"

            // Convert CrashLogEntity to CrashReport API model
            val crashReports = crashLogs.map { entity ->
                convertToApiModel(entity)
            }

            // Upload to server
            val response = crashReportApi.uploadCrashes(fullUrl, crashReports)

            // Check if upload was successful (200 or 201)
            if (response.isSuccessful && (response.code() == 200 || response.code() == 201)) {
                // Delete successfully uploaded crash logs from database
                val idsToDelete = crashLogs.map { it.id }
                crashLogDao.deleteCrashLogsByIds(idsToDelete)

                Result.success()
            } else {
                // Upload failed, retry later
                Result.retry()
            }
        } catch (e: Exception) {
            // Log error and retry
            e.printStackTrace()
            Result.retry()
        }
    }

    /**
     * Convert CrashLogEntity to CrashReport API model
     */
    private fun convertToApiModel(entity: CrashLogEntity): CrashReport {
        return CrashReport(
            timeStamp = Date(entity.timestamp),
            stackTrace = entity.stackTrace,
            androidVersion = entity.androidVersion,
            deviceMake = entity.deviceMake,
            deviceModel = entity.deviceModel,
            isFatal = entity.isFatal
        )
    }
}

