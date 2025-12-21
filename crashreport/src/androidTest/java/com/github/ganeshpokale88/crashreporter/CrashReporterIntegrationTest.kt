package com.github.ganeshpokale88.crashreporter

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.github.ganeshpokale88.crashreporter.database.CrashLogDao
import com.github.ganeshpokale88.crashreporter.database.CrashLogDatabase
import com.github.ganeshpokale88.crashreporter.worker.CrashLogWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.io.File

/**
 * Integration test for CrashReporter - tests the full flow from crash reporting to database storage
 */
@RunWith(AndroidJUnit4::class)
class CrashReporterIntegrationTest {

    private lateinit var context: Context
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
    fun testNonFatalCrashWritesToFile() = runBlocking {
        // Create a test exception
        val exception = RuntimeException("Test non-fatal crash")
        
        // Report non-fatal crash
        crashlytics.reportNonFatalCrash(exception)
        
        // Wait a bit for file to be written
        Thread.sleep(100)
        
        // Verify encrypted file was created
        val crashFiles = crashLogsDir.listFiles { file ->
            file.name.endsWith(".enc")
        }
        
        assertNotNull("Crash log file should be created", crashFiles)
        assertTrue("At least one crash log file should exist", crashFiles!!.isNotEmpty())
        
        // Verify file is encrypted (not plain text)
        val file = crashFiles[0]
        val content = file.readText()
        assertFalse("File should be encrypted", content.contains("Test non-fatal crash"))
    }

    @Test
    fun testNonFatalCrashProcessedToDatabase() = runTest {
        // Create a test exception
        val exception = RuntimeException("Test non-fatal crash for DB")
        
        // Report non-fatal crash
        crashlytics.reportNonFatalCrash(exception)
        
        // Wait for file to be written
        Thread.sleep(100)
        
        // Find the crash log file
        val crashFiles = crashLogsDir.listFiles { file ->
            file.name.endsWith(".enc")
        }
        
        assertNotNull("Crash log file should exist", crashFiles)
        assertTrue("At least one crash log file should exist", crashFiles!!.isNotEmpty())
        
        val file = crashFiles[0]
        val encryptedContent = file.readText()
        
        // Decrypt and verify content
        val decryptedContent = EncryptionUtil.decrypt(context, encryptedContent)
        assertTrue("Decrypted content should contain stack trace", 
            decryptedContent.contains("Test non-fatal crash for DB"))
        
        // Manually process the file (simulating what the worker does)
        val parts = decryptedContent.split("|", limit = 6)
        assertEquals("Should have 6 parts", 6, parts.size)
        
        val timestamp = parts[0].toLong()
        val isFatal = parts[1].toBoolean()
        val androidVersion = parts[2]
        val deviceMake = parts[3]
        val deviceModel = parts[4]
        val stackTrace = parts[5]
        
        // Insert into database
        val crashLog = com.github.ganeshpokale88.crashreporter.database.CrashLogEntity(
            timestamp = timestamp,
            stackTrace = stackTrace,
            androidVersion = androidVersion,
            deviceMake = deviceMake,
            deviceModel = deviceModel,
            isFatal = isFatal
        )
        
        dao.insertCrashLog(crashLog)
        
        // Verify it's in the database
        val allLogs = dao.getAllCrashLogs().first()
        assertEquals("Should have one crash log", 1, allLogs.size)
        
        val savedLog = allLogs[0]
        assertTrue("Stack trace should match", savedLog.stackTrace.contains("Test non-fatal crash for DB"))
        assertEquals("Android version should match", android.os.Build.VERSION.RELEASE, savedLog.androidVersion)
        assertEquals("Device make should match", android.os.Build.MANUFACTURER, savedLog.deviceMake)
        assertEquals("Device model should match", android.os.Build.MODEL, savedLog.deviceModel)
        assertFalse("Should be non-fatal", savedLog.isFatal)
    }

    @Test
    fun testDeviceInfoCollection() = runBlocking {
        val deviceInfo = DeviceInfo.collect(context)
        
        assertEquals("Android version should match", android.os.Build.VERSION.RELEASE, deviceInfo.androidVersion)
        assertEquals("Device make should match", android.os.Build.MANUFACTURER, deviceInfo.deviceMake)
        assertEquals("Device model should match", android.os.Build.MODEL, deviceInfo.deviceModel)
    }

    @Test
    fun testEncryptionDecryption() = runBlocking {
        val testData = "Test crash data|false|14|Google|Pixel 7|Stack trace here"
        
        // Encrypt
        val encrypted = EncryptionUtil.encrypt(context, testData)
        assertNotNull("Encrypted data should not be null", encrypted)
        assertNotEquals("Encrypted data should be different from original", testData, encrypted)
        
        // Decrypt
        val decrypted = EncryptionUtil.decrypt(context, encrypted)
        assertEquals("Decrypted data should match original", testData, decrypted)
    }

    @Test
    fun testMultipleNonFatalCrashes() = runTest {
        // Report multiple crashes
        crashlytics.reportNonFatalCrash(RuntimeException("Crash 1"))
        Thread.sleep(50)
        crashlytics.reportNonFatalCrash(IllegalStateException("Crash 2"))
        Thread.sleep(50)
        crashlytics.reportNonFatalCrash(NullPointerException("Crash 3"))
        Thread.sleep(100)
        
        // Verify multiple files were created
        val crashFiles = crashLogsDir.listFiles { file ->
            file.name.endsWith(".enc")
        }
        
        assertNotNull("Crash log files should exist", crashFiles)
        assertTrue("Should have at least 3 crash log files", crashFiles!!.size >= 3)
    }
}

