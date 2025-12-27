package com.github.ganeshpokale88.crashreporter

import com.github.ganeshpokale88.crashreporter.api.CrashReportApi
import com.github.ganeshpokale88.crashreporter.database.CrashLogDao
import com.github.ganeshpokale88.crashreporter.database.CrashLogDatabase

/**
 * Registry to hold dependencies for workers.
 * This allows workers to access dependencies without Hilt injection.
 */
internal object DependencyRegistry {
    
    @Volatile
    private var crashLogDao: CrashLogDao? = null
    
    @Volatile
    private var crashReportApi: CrashReportApi? = null
    
    @Volatile
    private var config: CrashReporterConfig? = null
    
    @Volatile
    private var isInitialized: Boolean = false
    
    /**
     * Initialize the registry with dependencies
     * API and config are optional - can be set later via updateApi()
     */
    fun initialize(
        database: CrashLogDatabase,
        api: CrashReportApi? = null,
        config: CrashReporterConfig? = null
    ) {
        crashLogDao = database.crashLogDao()
        crashReportApi = api
        this.config = config
        isInitialized = true
    }
    
    /**
     * Initialize the registry with a CrashLogDao directly.
     * This is primarily for testing purposes to avoid Room database mocking issues.
     * API and config are optional - can be set later via updateApi()
     */
    @androidx.annotation.VisibleForTesting
    fun initializeForTesting(
        dao: CrashLogDao,
        api: CrashReportApi? = null,
        config: CrashReporterConfig? = null
    ) {
        crashLogDao = dao
        crashReportApi = api
        this.config = config
        isInitialized = true
    }
    
    /**
     * Update the API instance and configuration (for runtime updates)
     */
    fun updateApi(api: CrashReportApi, config: CrashReporterConfig) {
        if (!isInitialized) {
            throw IllegalStateException(
                "DependencyRegistry not initialized. Call CrashReporter.initialize() first."
            )
        }
        crashReportApi = api
        this.config = config
    }
    
    /**
     * Update configuration only (without API) - used when config is incomplete
     */
    fun updateConfig(config: CrashReporterConfig) {
        if (!isInitialized) {
            throw IllegalStateException(
                "DependencyRegistry not initialized. Call CrashReporter.initialize() first."
            )
        }
        this.config = config
        // Don't update API if config is incomplete
    }
    
    /**
     * Check if API is configured and available
     * Both baseUrl and apiEndpoint must be provided for API calls to work
     */
    fun isApiConfigured(): Boolean {
        return crashReportApi != null && config != null && 
               config?.baseUrl?.isNotBlank() == true && 
               config?.apiEndpoint?.isNotBlank() == true
    }
    
    /**
     * Get CrashLogDao
     */
    fun getCrashLogDao(): CrashLogDao {
        return crashLogDao ?: throw IllegalStateException(
            "DependencyRegistry not initialized. Call CrashReporter.initialize() first."
        )
    }
    
    /**
     * Get CrashReportApi
     * @throws IllegalStateException if API is not configured (call updateConfiguration first)
     */
    fun getCrashReportApi(): CrashReportApi {
        if (!isInitialized) {
            throw IllegalStateException(
                "DependencyRegistry not initialized. Call CrashReporter.initialize() first."
            )
        }
        return crashReportApi ?: throw IllegalStateException(
            "API not configured. Call CrashReporter.updateConfiguration() with base URL first."
        )
    }
    
    /**
     * Get configuration (may be null if not set yet)
     */
    fun getConfig(): CrashReporterConfig? {
        return config
    }
    /**
     * Reset all dependencies for testing purposes.
     */
    @androidx.annotation.VisibleForTesting
    fun reset() {
        crashLogDao = null
        crashReportApi = null
        config = null
        isInitialized = false
    }
}

