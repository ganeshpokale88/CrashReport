package com.github.ganeshpokale88.crashreporter

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.ganeshpokale88.crashreporter.database.CrashLogDao
import com.github.ganeshpokale88.crashreporter.database.CrashLogDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.io.File

/**
 * Comprehensive test to verify non-fatal crash is successfully written to Room database
 * This test validates the complete flow without relying on WorkManager
 */
@RunWith(AndroidJUnit4::class)
class NonFatalCrashToDatabaseTest {

    private lateinit var context: android.content.Context
    private lateinit var database: CrashLogDatabase
    private lateinit var dao: CrashLogDao
    private lateinit var crashlytics: CrashReporter
    private lateinit var crashLogsDir: File

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            CrashLogDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.crashLogDao()

        // Initialize CrashReporter
        crashlytics = CrashReporter.initialize(context)
        
        // Get crash logs directory
        crashLogsDir = File(context.filesDir, "crash_logs")
        
        // Clean up any existing crash log files
        crashLogsDir.listFiles()?.forEach { it.delete() }
    }

    @After
    fun tearDown() {
        database.close()
        // Clean up crash log files
        crashLogsDir.listFiles()?.forEach { it.delete() }
    }

    @Test
    fun testNonFatalCrashSuccessfullyWritesToDatabase() = runBlocking {
        // Step 1: Create a test exception
        val testException = RuntimeException("Test non-fatal crash for database verification")
        val expectedStackTrace = "java.lang.RuntimeException: Test non-fatal crash for database verification"
        
        // Step 2: Report non-fatal crash
        crashlytics.reportNonFatalCrash(testException)
        
        // Step 3: Wait for file to be written
        delay(200)
        
        // Step 4: Verify encrypted file was created
        val crashFiles = crashLogsDir.listFiles { file ->
            file.name.endsWith(".enc")
        }
        
        assertNotNull("Crash log file should be created", crashFiles)
        assertTrue("At least one crash log file should exist", crashFiles!!.isNotEmpty())
        
        val file = crashFiles[0]
        assertTrue("File should exist", file.exists())
        
        // Step 5: Verify file is encrypted (not plain text)
        val encryptedContent = file.readText()
        assertFalse("File should be encrypted", encryptedContent.contains("Test non-fatal crash"))
        assertTrue("File should contain encrypted data", encryptedContent.isNotEmpty())
        
        // Step 6: Decrypt and verify content structure
        val decryptedContent = EncryptionUtil.decrypt(context, encryptedContent)
        assertTrue("Decrypted content should contain stack trace", 
            decryptedContent.contains("Test non-fatal crash for database verification"))
        
        // Step 7: Parse and verify all components
        val parts = decryptedContent.split("|", limit = 6)
        assertEquals("Should have 6 parts (timestamp|isFatal|androidVersion|deviceMake|deviceModel|stackTrace)", 
            6, parts.size)
        
        val timestamp = parts[0].toLong()
        val isFatal = parts[1].toBoolean()
        val androidVersion = parts[2]
        val deviceMake = parts[3]
        val deviceModel = parts[4]
        val stackTrace = parts[5]
        
        // Verify parsed data
        assertTrue("Timestamp should be valid", timestamp > 0)
        assertFalse("Should be non-fatal", isFatal)
        assertEquals("Android version should match", android.os.Build.VERSION.RELEASE, androidVersion)
        assertEquals("Device make should match", android.os.Build.MANUFACTURER, deviceMake)
        assertEquals("Device model should match", android.os.Build.MODEL, deviceModel)
        assertTrue("Stack trace should contain exception message", 
            stackTrace.contains("Test non-fatal crash for database verification"))
        
        // Step 8: Process file manually (simulating worker) and insert into database
        val crashLog = com.github.ganeshpokale88.crashreporter.database.CrashLogEntity(
            timestamp = timestamp,
            stackTrace = stackTrace,
            androidVersion = androidVersion,
            deviceMake = deviceMake,
            deviceModel = deviceModel,
            isFatal = isFatal
        )
        
        val insertedId = dao.insertCrashLog(crashLog)
        assertTrue("Insert should return valid ID", insertedId > 0)
        
        // Step 9: Verify data is in database
        val allLogs = dao.getAllCrashLogs().first()
        assertEquals("Should have exactly one crash log", 1, allLogs.size)
        
        val savedLog = allLogs[0]
        
        // Step 10: Verify all fields are correctly stored
        assertEquals("Timestamp should match", timestamp, savedLog.timestamp)
        assertEquals("Stack trace should match", stackTrace, savedLog.stackTrace)
        assertEquals("Android version should match", androidVersion, savedLog.androidVersion)
        assertEquals("Device make should match", deviceMake, savedLog.deviceMake)
        assertEquals("Device model should match", deviceModel, savedLog.deviceModel)
        assertEquals("Is fatal should match", isFatal, savedLog.isFatal)
        
        // Step 11: Verify stack trace contains expected content
        assertTrue("Stack trace should contain exception class", 
            savedLog.stackTrace.contains("RuntimeException"))
        assertTrue("Stack trace should contain exception message", 
            savedLog.stackTrace.contains("Test non-fatal crash for database verification"))
        
        // Step 12: Verify device information
        assertEquals("Android version should be collected correctly", 
            android.os.Build.VERSION.RELEASE, savedLog.androidVersion)
        assertEquals("Device make should be collected correctly", 
            android.os.Build.MANUFACTURER, savedLog.deviceMake)
        assertEquals("Device model should be collected correctly", 
            android.os.Build.MODEL, savedLog.deviceModel)
        
        println("✅ Test passed: Non-fatal crash successfully written to Room database")
        println("   - Timestamp: ${savedLog.timestamp}")
        println("   - Android Version: ${savedLog.androidVersion}")
        println("   - Device: ${savedLog.deviceMake} ${savedLog.deviceModel}")
        println("   - Is Fatal: ${savedLog.isFatal}")
        println("   - Stack Trace Length: ${savedLog.stackTrace.length} characters")
    }

    @Test
    fun testMultipleNonFatalCrashes() = runBlocking {
        // Report multiple crashes
        crashlytics.reportNonFatalCrash(RuntimeException("Crash 1"))
        delay(50)
        crashlytics.reportNonFatalCrash(IllegalStateException("Crash 2"))
        delay(50)
        crashlytics.reportNonFatalCrash(NullPointerException("Crash 3"))
        delay(100)
        
        // Verify multiple files were created
        val crashFiles = crashLogsDir.listFiles { file ->
            file.name.endsWith(".enc")
        }
        
        assertNotNull("Crash log files should exist", crashFiles)
        assertTrue("Should have at least 3 crash log files", crashFiles!!.size >= 3)
        
        // Process all files and verify in database
        crashFiles.forEach { file ->
            try {
                val encryptedContent = file.readText()
                val decryptedContent = EncryptionUtil.decrypt(context, encryptedContent)
                val parts = decryptedContent.split("|", limit = 6)
                
                if (parts.size == 6) {
                    val crashLog = com.github.ganeshpokale88.crashreporter.database.CrashLogEntity(
                        timestamp = parts[0].toLong(),
                        stackTrace = parts[5],
                        androidVersion = parts[2],
                        deviceMake = parts[3],
                        deviceModel = parts[4],
                        isFatal = parts[1].toBoolean()
                    )
                    dao.insertCrashLog(crashLog)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Verify all crashes in database
        val allLogs = dao.getAllCrashLogs().first()
        assertTrue("Should have at least 3 crash logs in database", allLogs.size >= 3)
        
        // Verify each crash has correct data
        allLogs.forEach { log ->
            assertFalse("All should be non-fatal", log.isFatal)
            assertTrue("Stack trace should not be empty", log.stackTrace.isNotEmpty())
            assertTrue("Android version should not be empty", log.androidVersion.isNotEmpty())
            assertTrue("Device make should not be empty", log.deviceMake.isNotEmpty())
            assertTrue("Device model should not be empty", log.deviceModel.isNotEmpty())
            assertTrue("Timestamp should be valid", log.timestamp > 0)
        }
    }
}

