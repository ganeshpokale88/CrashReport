package com.github.ganeshpokale88.crashreporter

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.github.ganeshpokale88.crashreporter.worker.CrashUploadWorker

/**
 * Utility class to schedule crash log upload worker
 */
object UploadWorkerScheduler {
    
    private const val UPLOAD_WORK_NAME = "crash_upload_work"

    /**
     * Schedule the crash upload worker to upload crash logs from database to server
     */
    fun scheduleUploadWorker(context: Context) {
        try {
            // Constraints: Network is required for upload
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val uploadWorkRequest = OneTimeWorkRequestBuilder<CrashUploadWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniqueWork(
                    UPLOAD_WORK_NAME,
                    ExistingWorkPolicy.REPLACE, // Replace any pending work with new work
                    uploadWorkRequest
                )
        } catch (e: Exception) {
            // Log error but don't throw - upload can be retried later
            e.printStackTrace()
        }
    }
}

