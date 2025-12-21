package com.github.ganeshpokale88.crashreporter

import android.content.Context
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.github.ganeshpokale88.crashreporter.database.CrashLogDatabase
import com.github.ganeshpokale88.crashreporter.worker.CrashLogWorker
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Main entry point for the CrashReporter library.
 * This library provides crash reporting functionality with WorkManager, Hilt, Coroutines, and Flow support.
 */
class CrashReporter private constructor(private val applicationContext: Context) {

    private val originalHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    private val crashLogsDir: File = File(applicationContext.filesDir, "crash_logs")

    init {
        // Create crash logs directory if it doesn't exist
        if (!crashLogsDir.exists()) {
            crashLogsDir.mkdirs()
        }

        // Set up fatal crash handler
        setupFatalCrashHandler()
        
        // Note: scheduleCrashLogProcessing() is called after WorkManager initialization
        // in the initialize() method, not here in init block
    }

    /**
     * Set up handler for fatal crashes
     */
    private fun setupFatalCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            try {
                // Collect device info
                val deviceInfo = DeviceInfo.collect(applicationContext)

                // Get stack trace
                var stackTrace = getStackTrace(exception)
                
                // Sanitize stack trace if sanitization is enabled
                stackTrace = sanitizeStackTrace(stackTrace)

                // Save crash log to encrypted file (synchronous, will complete before crash)
                saveCrashLogToFile(
                    stackTrace = stackTrace,
                    deviceInfo = deviceInfo,
                    isFatal = true
                )

                // Schedule WorkManager worker
                // For fatal crashes, we schedule the worker immediately
                // If app crashes before completion, worker will run on next app launch
                scheduleCrashLogProcessing()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // Call original handler to let the system handle the crash
                originalHandler?.uncaughtException(thread, exception)
            }
        }
    }

    /**
     * Report a non-fatal crash/exception
     */
    fun reportNonFatalCrash(exception: Throwable) {
        try {
            // Collect device info
            val deviceInfo = DeviceInfo.collect(applicationContext)

            // Get stack trace
            var stackTrace = getStackTrace(exception)
            
            // Sanitize stack trace if sanitization is enabled
            stackTrace = sanitizeStackTrace(stackTrace)

            // Save crash log to encrypted file
            saveCrashLogToFile(
                stackTrace = stackTrace,
                deviceInfo = deviceInfo,
                isFatal = false
            )

            // Start WorkManager to process the crash log
            scheduleCrashLogProcessing()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get stack trace as string
     */
    private fun getStackTrace(exception: Throwable): String {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        exception.printStackTrace(printWriter)
        return stringWriter.toString()
    }
    
    /**
     * Sanitize stack trace to remove potential PHI if sanitization is enabled in configuration.
     * 
     * @param stackTrace Original stack trace string
     * @return Sanitized stack trace (or original if sanitization is disabled)
     */
    private fun sanitizeStackTrace(stackTrace: String): String {
        return try {
            val config = DependencyRegistry.getConfig()
            val sanitizationConfig = config?.sanitizationConfig
            
            if (sanitizationConfig != null) {
                StackTraceSanitizer.sanitize(stackTrace, sanitizationConfig)
            } else {
                stackTrace  // No sanitization configured, return original
            }
        } catch (e: Exception) {
            // If sanitization fails, log error but return original stack trace
            // This ensures crash reporting still works even if sanitization has issues
            android.util.Log.e(TAG, "Failed to sanitize stack trace", e)
            stackTrace
        }
    }

    /**
     * Save crash log to encrypted file
     */
    private fun saveCrashLogToFile(
        stackTrace: String,
        deviceInfo: DeviceInfo,
        isFatal: Boolean
    ) {
        try {
            val timestamp = System.currentTimeMillis()

            // Format: timestamp|isFatal|androidVersion|deviceMake|deviceModel|stackTrace
            val crashData = buildString {
                append(timestamp)
                append("|")
                append(isFatal)
                append("|")
                append(deviceInfo.androidVersion)
                append("|")
                append(deviceInfo.deviceMake)
                append("|")
                append(deviceInfo.deviceModel)
                append("|")
                append(stackTrace)
            }

            // Encrypt the crash data
            val encryptedData = EncryptionUtil.encrypt(applicationContext, crashData)

            // Save to file synchronously and ensure it's written to disk
            val fileName = "crash_${timestamp}.enc"
            val file = File(crashLogsDir, fileName)
            // Use FileOutputStream with explicit flush to ensure data is written to disk
            file.outputStream().use { outputStream ->
                outputStream.write(encryptedData.toByteArray())
                outputStream.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Schedule CrashLogWorker to process encrypted crash log files and store them in the database
     * This is called both when crashes occur and on app initialization to catch any files
     * from previous fatal crashes
     */
    private fun scheduleCrashLogProcessing() {
        try {
            // Constraints: No network needed, can run immediately
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<CrashLogWorker>()
                .setConstraints(constraints)
                .build()

            // Use REPLACE to ensure work is scheduled even if there's pending work
            // This ensures fatal crashes are always processed
            val workManager = try {
                WorkManager.getInstance(applicationContext)
            } catch (e: IllegalStateException) {
                // WorkManager not initialized yet - this shouldn't happen if called after initialize()
                // but we'll catch it gracefully
                android.util.Log.w(TAG, "WorkManager not initialized, skipping worker scheduling", e)
                return
            }
            
            workManager.enqueueUniqueWork(
                "crash_log_processing",
                ExistingWorkPolicy.REPLACE, // Replace pending work, ensures latest crash is processed
                workRequest
            )
        } catch (e: Exception) {
            // Log error but don't throw - processing can be retried later
            // If this fails during fatal crash, initialize() will schedule it on next app launch
            android.util.Log.e(TAG, "Failed to schedule crash log processing", e)
        }
    }

    companion object {
        private const val TAG = "CrashReporter"
        
        @Volatile
        private var INSTANCE: CrashReporter? = null

        /**
         * Initialize CrashReporter (should be called in Application class).
         * This sets up WorkManager configuration and database.
         * 
         * Note: Network configuration (base URL and headers) is optional and can be set later
         * using updateConfiguration() after login when base URL and authorization tokens are available.
         * Crash logs will be stored locally and uploaded once configuration is provided.
         * 
         * @param context Application context
         * @param config Optional configuration for base URL and headers.
         *               If not provided, crash logs will be stored locally but won't be uploaded
         *               until updateConfiguration() is called.
         */
        fun initialize(context: Context, config: CrashReporterConfig? = null): CrashReporter {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CrashReporter(context.applicationContext).also { instance ->
                    INSTANCE = instance
                    
                    // Load SQLCipher native library before any database operations
                    // This must be done early to ensure the library is available
                    loadSQLCipherNativeLibrary()
                    
                    // Set up WorkManager configuration FIRST (before any WorkManager usage)
                    setupWorkManager(context.applicationContext)
                    
                    // Initialize dependency registry
                    val database = CrashLogDatabase.create(context.applicationContext)
                    
                    // Validate and create API only if config is provided and complete
                    val api = config?.let { cfg ->
                        // Validate configuration - show warnings in debug mode
                        if (cfg.baseUrl.isBlank()) {
                            if (android.util.Log.isLoggable(TAG, android.util.Log.WARN)) {
                                android.util.Log.w(TAG, 
                                    "⚠️ CrashReporter: baseUrl is missing. API calls will not be made. " +
                                    "Please configure baseUrl using CrashReporterConfig.Builder().baseUrl()")
                            }
                            null
                        } else if (cfg.apiEndpoint.isNullOrBlank()) {
                            if (android.util.Log.isLoggable(TAG, android.util.Log.WARN)) {
                                android.util.Log.w(TAG, 
                                    "⚠️ CrashReporter: apiEndpoint is missing. API calls will not be made. " +
                                    "Please configure apiEndpoint using CrashReporterConfig.Builder().apiEndpoint()")
                            }
                            null
                        } else {
                            try {
                                // Save headers securely for future sessions (if provided)
                                // Even if no headers provided, persisted headers from previous session will be used
                                if (cfg.headers.isNotEmpty()) {
                                    HeaderStorage.saveHeaders(context.applicationContext, cfg.headers)
                                }
                                
                                NetworkFactory.createCrashReportApi(context.applicationContext, cfg)
                            } catch (e: Exception) {
                                if (android.util.Log.isLoggable(TAG, android.util.Log.ERROR)) {
                                    android.util.Log.e(TAG, "Failed to create API", e)
                                }
                                null
                            }
                        }
                    }
                    DependencyRegistry.initialize(database, api, config)
                    
                    // Now schedule processing for any existing crash log files (from previous fatal crashes)
                    // This is safe now because WorkManager is initialized
                    instance.scheduleCrashLogProcessing()
                }
            }
        }
        
        /**
         * Update configuration at runtime (e.g., after user login when JWT token becomes available).
         * This recreates the network client with new base URL and headers.
         * 
         * Use this method when:
         * - User logs in and you get a JWT token
         * - Base URL needs to change based on environment/user preferences
         * - Headers need to be updated dynamically
         * 
         * Example usage after login:
         * ```kotlin
         * // After successful login
         * val newConfig = CrashReporterConfig.Builder()
         *     .baseUrl("https://api.example.com")
         *     .addHeader("Authorization", "Bearer $jwtToken")
         *     .build()
         * CrashReporter.updateConfiguration(newConfig)
         * ```
         * 
         * @param config New configuration with updated base URL and/or headers
         * @throws IllegalStateException if CrashReporter is not initialized
         */
        fun updateConfiguration(config: CrashReporterConfig) {
            val instance = INSTANCE ?: throw IllegalStateException(
                "CrashReporter not initialized. Call CrashReporter.initialize() first."
            )
            
            // Validate configuration - show warnings in debug mode
            if (config.baseUrl.isBlank()) {
                if (android.util.Log.isLoggable(TAG, android.util.Log.WARN)) {
                    android.util.Log.w(TAG, 
                        "⚠️ CrashReporter: baseUrl is missing. API calls will not be made. " +
                        "Please configure baseUrl using CrashReporterConfig.Builder().baseUrl()")
                }
            }
            
            if (config.apiEndpoint.isNullOrBlank()) {
                if (android.util.Log.isLoggable(TAG, android.util.Log.WARN)) {
                    android.util.Log.w(TAG, 
                        "⚠️ CrashReporter: apiEndpoint is missing. API calls will not be made. " +
                        "Please configure apiEndpoint using CrashReporterConfig.Builder().apiEndpoint()")
                }
            }
            
            // Only create API if both baseUrl and apiEndpoint are provided
            val newApi = if (config.baseUrl.isNotBlank() && config.apiEndpoint?.isNotBlank() == true) {
                // Save headers securely for future sessions
                if (config.headers.isNotEmpty()) {
                    HeaderStorage.saveHeaders(instance.applicationContext, config.headers)
                }
                
                NetworkFactory.createCrashReportApi(instance.applicationContext, config)
            } else {
                null
            }
            
            // Update dependency registry (API may be null if config is incomplete)
            if (newApi != null) {
                DependencyRegistry.updateApi(newApi, config)
                // Schedule upload worker to process any pending crash logs now that API is configured
                UploadWorkerScheduler.scheduleUploadWorker(instance.applicationContext)
            } else {
                // Update config only (without API) so warnings can be shown
                DependencyRegistry.updateConfig(config)
            }
        }
        
        /**
         * Load SQLCipher native library explicitly
         * This ensures the library is available before database operations
         */
        private fun loadSQLCipherNativeLibrary() {
            try {
                System.loadLibrary("sqlcipher")
            } catch (e: UnsatisfiedLinkError) {
                android.util.Log.e(
                    TAG,
                    "Failed to load SQLCipher native library. Database encryption may not work.",
                    e
                )
                throw IllegalStateException(
                    "SQLCipher native library failed to load. Ensure sqlcipher-android dependency is correctly included.",
                    e
                )
            }
        }
        
        /**
         * Set up WorkManager configuration with custom factory
         */
        private fun setupWorkManager(context: Context) {
            try {
                val configuration = Configuration.Builder()
                    .setWorkerFactory(CrashReporterWorkerFactory())
                    .build()
                
                // Initialize WorkManager with our configuration
                // Note: This will fail if WorkManager is already initialized.
                // Apps should disable auto-initialization in AndroidManifest.xml
                WorkManager.initialize(context, configuration)
            } catch (e: IllegalStateException) {
                // WorkManager already initialized - this is expected if app has auto-init disabled
                // but initialized it themselves, or if auto-init ran before we got here.
                // We'll log a warning but continue - workers may still work if the factory is compatible
                android.util.Log.w(
                    TAG,
                    "WorkManager already initialized. " +
                    "Ensure WorkManager auto-initialization is disabled in AndroidManifest.xml " +
                    "and call CrashReporter.initialize() before any WorkManager usage.",
                    e
                )
            }
        }

        /**
         * Get the singleton instance
         */
        fun getInstance(): CrashReporter {
            return INSTANCE ?: throw IllegalStateException(
                "CrashReporter not initialized. Call CrashReporter.initialize(context) first."
            )
        }
        
        /**
         * Clear all persisted headers (e.g., on user logout).
         * This removes securely stored headers like Authorization tokens.
         * 
         * After calling this, you should call updateConfiguration() with new headers
         * if the user logs in again.
         * 
         * Example usage on logout:
         * ```kotlin
         * fun onLogout() {
         *     CrashReporter.clearHeaders(context)
         *     // ... other logout logic
         * }
         * ```
         * 
         * @param context Application context
         */
        fun clearHeaders(context: Context) {
            val appContext = context.applicationContext
            HeaderStorage.clearHeaders(appContext)
            
            // Also clear headers from current config if instance exists
            INSTANCE?.let {
                val currentConfig = DependencyRegistry.getConfig()
                if (currentConfig != null) {
                    // Create new config without headers
                    val builder = CrashReporterConfig.Builder()
                        .baseUrl(currentConfig.baseUrl)
                        .apiEndpoint(currentConfig.apiEndpoint ?: "")
                        .dataRetentionDays(currentConfig.dataRetentionDays)
                    
                    // Add sanitization config if present
                    currentConfig.sanitizationConfig?.let { sanitizationConfig ->
                        builder.sanitizationConfig(
                            patientNames = sanitizationConfig.patientNames,
                            customPatterns = sanitizationConfig.customPatterns,
                            redactEmails = sanitizationConfig.redactEmails,
                            redactPhones = sanitizationConfig.redactPhones,
                            redactSSNs = sanitizationConfig.redactSSNs,
                            redactMRNs = sanitizationConfig.redactMRNs,
                            redactUserPaths = sanitizationConfig.redactUserPaths,
                            redactDates = sanitizationConfig.redactDates,
                            redactCreditCards = sanitizationConfig.redactCreditCards
                        )
                    }
                    
                    // Add certificate pins if any
                    currentConfig.certificatePins?.forEach { (hostname, pins) ->
                        pins.forEach { pin ->
                            builder.addCertificatePin(hostname, pin)
                        }
                    }
                    
                    val configWithoutHeaders = builder.build()
                    
                    // Update with config without headers
                    updateConfiguration(configWithoutHeaders)
                }
            }
        }
        /**
         * Reset the singleton instance for testing purposes.
         */
        @androidx.annotation.VisibleForTesting
        fun reset() {
            synchronized(this) {
                INSTANCE = null
            }
        }
    }
}
