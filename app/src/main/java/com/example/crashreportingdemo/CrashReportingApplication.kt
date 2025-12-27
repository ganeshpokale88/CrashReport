package com.example.crashreportingdemo

import android.app.Application
import android.util.Log
import com.github.ganeshpokale88.crashreporter.CrashReporter
import com.github.ganeshpokale88.crashreporter.CrashReporterConfig
import com.github.ganeshpokale88.crashreporter.StackTraceSanitizer

/**
 * Complete usage example for CrashReporter library
 * 
 * This demonstrates:
 * 1. Basic initialization with configuration
 * 2. Initialization without config (set later after login)
 * 3. HIPAA-compliant configuration with sanitization
 * 4. Certificate pinning setup
 * 5. Custom headers
 */

//const val BASE_URL = "http://192.168.1.10:8000"
const val BASE_URL = "http://192.168.1.2:8000"
class CrashReportingApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // ============================================
        // EXAMPLE 1: Basic Initialization (Recommended)
        // ============================================
        // Initialize with full configuration including base URL and endpoint
        initializeWithConfig()
        
        // ============================================
        // EXAMPLE 2: Initialize Without Config (Alternative)
        // ============================================
        // Use this if base URL/headers are not available at app startup
        // (e.g., user needs to login first)
        // Uncomment to use:
        // initializeWithoutConfig()
        
        // ============================================
        // EXAMPLE 3: HIPAA-Compliant Configuration
        // ============================================
        // Uncomment to use HIPAA-compliant setup:
        // initializeHipaaCompliant()
    }
    
    /**
     * EXAMPLE 1: Basic Initialization with Configuration
     * 
     * Use this when you know the base URL and endpoint at app startup.
     * Headers can be added later using updateConfiguration().
     */
    private fun initializeWithConfig() {
        val config = CrashReporterConfig.Builder()
            // Required: Base URL (without endpoint path)
            .baseUrl(BASE_URL)  // Development: localhost allowed
            // .baseUrl("https://api.example.com")  // Production: use HTTPS
            
            // Required: API endpoint path
            .apiEndpoint("/crashes")
            // .apiEndpoint("/api/crashes")  // Alternative endpoint
            
            // Optional: Enable PHI sanitization (recommended for HIPAA)
            .enableSanitization()  // Uses default HIPAA-compliant config
            
            // Optional: Custom sanitization config
            // .sanitizationConfig(
            //     patientNames = listOf("John Doe", "Jane Smith"),
            //     redactEmails = true,
            //     redactPhones = true,
            //     redactSSNs = true
            // )
            
            // Optional: Data retention (default: 90 days)
            .dataRetentionDays(90)
            
            // Optional: Add headers (e.g., API keys that don't change)
            // .addHeader("X-API-Key", "your-api-key")
            // .addHeader("X-App-Version", BuildConfig.VERSION_NAME)
            
            // Optional: Certificate pinning (highly recommended for production)
            // .addCertificatePins(listOf(
            //     "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",  // Current cert
            //     "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="   // Backup cert
            // ))
            
            .build()

        CrashReporter.initialize(this, config)
        Log.d("CrashReportingApp", "CrashReporter initialized with configuration")
    }
    
    /**
     * EXAMPLE 2: Initialize Without Configuration
     * 
     * Use this when base URL/headers are not available at startup.
     * Configuration will be set later (e.g., after user login).
     * 
     * Crash logs will be stored locally and uploaded once configuration is provided.
     */
    private fun initializeWithoutConfig() {
        // Initialize without config - crash logs stored locally
        CrashReporter.initialize(this)
        Log.d("CrashReportingApp", "CrashReporter initialized without configuration")
        Log.d("CrashReportingApp", "Call CrashReporter.updateConfiguration() after login")
        
        // Later, after user login, update configuration:
        // val config = CrashReporterConfig.Builder()
        //     .baseUrl("https://api.example.com")
        //     .apiEndpoint("/crashes")
        //     .addHeader("Authorization", "Bearer $jwtToken")
        //     .build()
        // CrashReporter.updateConfiguration(config)
    }
    
    /**
     * EXAMPLE 3: HIPAA-Compliant Configuration
     * 
     * Complete setup with all HIPAA compliance features enabled.
     */
    private fun initializeHipaaCompliant() {
        // Create custom sanitization config
        val sanitizationConfig = StackTraceSanitizer.SanitizationConfig(
            patientNames = listOf("John Doe", "Jane Smith"),  // Add known patient names
            customPatterns = listOf(
                Regex("MRN-\\d+"),  // Custom pattern for medical record numbers
                Regex("PATIENT-\\d+")  // Custom pattern for patient IDs
            ),
            redactEmails = true,
            redactPhones = true,
            redactSSNs = true,
            redactMRNs = true,
            redactUserPaths = true,
            redactDates = false,  // Usually keep dates for debugging
            redactCreditCards = true
        )
        
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")  // Production: HTTPS required
            .apiEndpoint("/api/crashes")
            
            // HIPAA: Enable sanitization with custom config
            .enableSanitization(sanitizationConfig)
            
            // HIPAA: Data retention (90 days recommended)
            .dataRetentionDays(90)
            
            // HIPAA: Certificate pinning (MITM protection)
            .addCertificatePins(listOf(
                "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",  // Current
                "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="   // Backup
            ))
            
            // Optional: Add static headers
            // .addHeader("X-API-Key", "your-api-key")
            
            .build()

        CrashReporter.initialize(this, config)
        Log.d("CrashReportingApp", "CrashReporter initialized with HIPAA-compliant configuration")
    }
}

