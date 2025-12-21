package com.example.crashreportingdemo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.ganeshpokale88.crashreporter.CrashReporter
import com.github.ganeshpokale88.crashreporter.CrashReporterConfig

/**
 * Complete usage example demonstrating all CrashReporter features
 * 
 * This activity shows:
 * 1. Reporting non-fatal crashes
 * 2. Triggering fatal crashes (automatically captured)
 * 3. Updating configuration (e.g., after login)
 * 4. Clearing headers (e.g., on logout)
 * 5. Testing different crash scenarios
 */
class MainActivity : AppCompatActivity() {
    
    private val crashlytics = CrashReporter.getInstance()
    private var isLoggedIn = false  // Simulate login state
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupButtons()
    }
    
    private fun setupButtons() {
        // ============================================
        // Button 1: Report Non-Fatal Crash
        // ============================================
        findViewById<Button>(R.id.btnNonFatalCrash).setOnClickListener {
            reportNonFatalCrash()
        }

        // ============================================
        // Button 2: Trigger Fatal Crash
        // ============================================
        findViewById<Button>(R.id.btnFatalCrash).setOnClickListener {
            triggerFatalCrash()
        }
        
        // ============================================
        // Button 3: Simulate Login (Update Configuration)
        // ============================================
        findViewById<Button>(R.id.btnSimulateLogin).setOnClickListener {
            simulateLogin()
        }
        
        // ============================================
        // Button 4: Simulate Logout (Clear Headers)
        // ============================================
        findViewById<Button>(R.id.btnSimulateLogout).setOnClickListener {
            simulateLogout()
        }
        
        // ============================================
        // Button 5: Test NullPointerException
        // ============================================
        findViewById<Button>(R.id.btnTestNullPointer).setOnClickListener {
            testNullPointerException()
        }
        
        // ============================================
        // Button 6: Test ArrayIndexOutOfBounds
        // ============================================
        findViewById<Button>(R.id.btnTestArrayIndex).setOnClickListener {
            testArrayIndexOutOfBounds()
        }
        
        // ============================================
        // Button 7: Test with PHI Data (Sanitization)
        // ============================================
        findViewById<Button>(R.id.btnTestPHI).setOnClickListener {
            testWithPHIData()
        }
    }
    
    /**
     * EXAMPLE 1: Report Non-Fatal Crash
     * 
     * Non-fatal crashes are caught, reported, and the app continues running.
     */
    private fun reportNonFatalCrash() {
        try {
            // Simulate a non-fatal exception
            throw RuntimeException("This is a test non-fatal crash at ${System.currentTimeMillis()}")
        } catch (e: Exception) {
            // Report the crash - library handles sanitization, encryption, and upload
            crashlytics.reportNonFatalCrash(e)
            
            Toast.makeText(
                this,
                "✅ Non-fatal crash reported!\n" +
                "Check logs: Crash captured, sanitized, encrypted, and queued for upload.",
                Toast.LENGTH_LONG
            ).show()
            
            Log.d("MainActivity", "Non-fatal crash reported: ${e.message}")
        }
    }
    
    /**
     * EXAMPLE 2: Trigger Fatal Crash
     * 
     * Fatal crashes are automatically captured by the library's uncaught exception handler.
     * The app will crash, but the crash data is saved and uploaded on next app launch.
     */
    private fun triggerFatalCrash() {
        Toast.makeText(
            this,
            "⚠️ App will crash now!\n" +
            "Crash will be captured automatically.\n" +
            "Restart app to see it uploaded.",
            Toast.LENGTH_LONG
        ).show()
        
        // Give user time to read the message
        findViewById<Button>(R.id.btnFatalCrash).postDelayed({
            // This will trigger the fatal crash handler
            throw RuntimeException("This is a test fatal crash at ${System.currentTimeMillis()}")
        }, 2000)
    }
    
    /**
     * EXAMPLE 3: Update Configuration (Simulate Login)
     * 
     * After user login, update configuration with authentication headers.
     * Headers are automatically persisted securely for future sessions.
     */
    private fun simulateLogin() {
        if (isLoggedIn) {
            Toast.makeText(this, "Already logged in!", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Simulate getting JWT token after login
        val jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."  // Mock token
        val userId = "user_12345"
        
        val config = CrashReporterConfig.Builder()
            .baseUrl(BASE_URL)  // Your API base URL
            .apiEndpoint("/crashes")
            
            // Add authentication headers (automatically persisted!)
            .addHeader("Authorization", "Bearer $jwtToken")
            .addHeader("X-User-ID", userId)
            .addHeader("X-Client-Version", "1.0.0")
            
            // Optional: Re-enable sanitization if needed
            .enableSanitization()
            
            .build()
        
        // Update configuration - headers are saved securely
        CrashReporter.updateConfiguration(config)
        isLoggedIn = true
        
        Toast.makeText(
            this,
            "✅ Login successful!\n" +
            "Headers saved securely.\n" +
            "Will persist across app restarts.",
            Toast.LENGTH_LONG
        ).show()
        
        Log.d("MainActivity", "Configuration updated with auth headers")
    }
    
    /**
     * EXAMPLE 4: Clear Headers (Simulate Logout)
     * 
     * Clear persisted headers when user logs out.
     * This removes authentication tokens from secure storage.
     */
    private fun simulateLogout() {
        if (!isLoggedIn) {
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Clear all persisted headers
        CrashReporter.clearHeaders(this)
        isLoggedIn = false
        
        Toast.makeText(
            this,
            "✅ Logout successful!\n" +
            "All persisted headers cleared.",
            Toast.LENGTH_LONG
        ).show()
        
        Log.d("MainActivity", "Headers cleared on logout")
    }
    
    /**
     * EXAMPLE 5: Test NullPointerException
     * 
     * Common crash scenario - null pointer access.
     */
    private fun testNullPointerException() {
        try {
            val data: String? = null
            val length = data!!.length  // Force NPE
        } catch (e: NullPointerException) {
            crashlytics.reportNonFatalCrash(e)
            Toast.makeText(
                this,
                "✅ NullPointerException reported!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * EXAMPLE 6: Test ArrayIndexOutOfBoundsException
     * 
     * Common crash scenario - array bounds violation.
     */
    private fun testArrayIndexOutOfBounds() {
        try {
            val array = arrayOf(1, 2, 3)
            val value = array[10]  // Out of bounds
        } catch (e: ArrayIndexOutOfBoundsException) {
            crashlytics.reportNonFatalCrash(e)
            Toast.makeText(
                this,
                "✅ ArrayIndexOutOfBoundsException reported!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * EXAMPLE 7: Test with PHI Data (Sanitization)
     * 
     * This demonstrates how sanitization removes PHI from stack traces.
     * Check the uploaded crash report - sensitive data should be redacted.
     */
    private fun testWithPHIData() {
        try {
            // Simulate code that might contain PHI in stack trace
            val patientSSN = "123-45-6789"
            val patientEmail = "patient@example.com"
            val patientPhone = "555-123-4567"
            val patientName = "John Doe"
            
            // Create a stack trace that might contain PHI
            val message = "Error processing patient: $patientName, SSN: $patientSSN, " +
                    "Email: $patientEmail, Phone: $patientPhone"
            
            throw RuntimeException(message)
        } catch (e: Exception) {
            crashlytics.reportNonFatalCrash(e)
            Toast.makeText(
                this,
                "✅ Crash with PHI reported!\n" +
                "PHI will be sanitized before upload.\n" +
                "Check server logs to see redacted data.",
                Toast.LENGTH_LONG
            ).show()
            
            Log.d("MainActivity", "Crash with PHI reported - sanitization should remove sensitive data")
        }
    }
}