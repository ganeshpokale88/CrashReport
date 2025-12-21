package com.github.ganeshpokale88.crashreporter

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.github.ganeshpokale88.crashreporter.worker.CrashLogWorker
import com.github.ganeshpokale88.crashreporter.worker.CrashUploadWorker

/**
 * Custom WorkerFactory for CrashReporter workers.
 * This factory creates workers without requiring Hilt injection.
 */
class CrashReporterWorkerFactory : WorkerFactory() {
    
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            CrashLogWorker::class.java.name -> {
                CrashLogWorker(appContext, workerParameters)
            }
            CrashUploadWorker::class.java.name -> {
                CrashUploadWorker(appContext, workerParameters)
            }
            else -> null // Return null to let WorkManager use default factory
        }
    }
}

