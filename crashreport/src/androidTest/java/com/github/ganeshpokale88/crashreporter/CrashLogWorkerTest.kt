package com.github.ganeshpokale88.crashreporter

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkerParameters
import com.github.ganeshpokale88.crashreporter.database.CrashLogDao
import com.github.ganeshpokale88.crashreporter.database.CrashLogDatabase
import com.github.ganeshpokale88.crashreporter.worker.CrashLogWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.mockito.Mockito.mock
import java.io.File

/**
 * Test for CrashLogWorker
 */
@RunWith(AndroidJUnit4::class)
class CrashLogWorkerTest {

    private lateinit var context: Context
    private lateinit var database: CrashLogDatabase
    private lateinit var dao: CrashLogDao
    private lateinit var crashLogsDir: File

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        database = Room.inMemoryDatabaseBuilder(
            context,
            CrashLogDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.crashLogDao()

        crashLogsDir = File(context.filesDir, "crash_logs")
        if (!crashLogsDir.exists()) {
            crashLogsDir.mkdirs()
        }
        
        // Initialize DependencyRegistry
        DependencyRegistry.initialize(database)
        
        // Clean up existing files
        crashLogsDir.listFiles()?.forEach { it.delete() }
    }

    @After
    fun tearDown() {
        database.close()
        crashLogsDir.listFiles()?.forEach { it.delete() }
    }

    @Test
    fun testWorkerProcessesEncryptedFile() = runBlocking {
        // Create a test crash log file
        val timestamp = System.currentTimeMillis()
        val crashData = "$timestamp|false|14|Google|Pixel 7|Test stack trace"
        val encryptedData = EncryptionUtil.encrypt(context, crashData)
        
        val fileName = "crash_${timestamp}.enc"
        val file = File(crashLogsDir, fileName)
        file.writeText(encryptedData)
        
        // Create worker
        val workerParams = mock(WorkerParameters::class.java)
        val worker = CrashLogWorker(context, workerParams)
        
        // Execute worker
        val result = worker.doWork()
        
        // Verify result
        assertEquals("Worker should succeed", androidx.work.ListenableWorker.Result.success(), result)
        
        // Verify file was deleted
        assertFalse("Crash log file should be deleted after processing", file.exists())
        
        // Verify data was inserted into database
        val allLogs = dao.getAllCrashLogs().first()
        assertEquals("Should have one crash log", 1, allLogs.size)
        
        val savedLog = allLogs[0]
        assertEquals("Stack trace should match", "Test stack trace", savedLog.stackTrace)
        assertEquals("Android version should match", "14", savedLog.androidVersion)
        assertEquals("Device make should match", "Google", savedLog.deviceMake)
        assertEquals("Device model should match", "Pixel 7", savedLog.deviceModel)
        assertFalse("Should be non-fatal", savedLog.isFatal)
    }

    @Test
    fun testWorkerHandlesMultipleFiles() = runBlocking {
        // Create multiple crash log files
        val timestamps = listOf(
            System.currentTimeMillis(),
            System.currentTimeMillis() + 1000,
            System.currentTimeMillis() + 2000
        )
        
        timestamps.forEachIndexed { index, timestamp ->
            val crashData = "$timestamp|false|14|Google|Pixel 7|Crash $index"
            val encryptedData = EncryptionUtil.encrypt(context, crashData)
            val fileName = "crash_${timestamp}.enc"
            val file = File(crashLogsDir, fileName)
            file.writeText(encryptedData)
        }
        
        // Create worker
        val workerParams = mock(WorkerParameters::class.java)
        val worker = CrashLogWorker(context, workerParams)
        
        // Execute worker
        val result = worker.doWork()
        
        // Verify result
        assertEquals("Worker should succeed", androidx.work.ListenableWorker.Result.success(), result)
        
        // Verify all files were processed
        val remainingFiles = crashLogsDir.listFiles { file ->
            file.name.endsWith(".enc")
        }
        assertEquals("All files should be processed", 0, remainingFiles?.size ?: 0)
        
        // Verify all data was inserted
        val allLogs = dao.getAllCrashLogs().first()
        assertEquals("Should have 3 crash logs", 3, allLogs.size)
    }

    @Test
    fun testWorkerHandlesEmptyDirectory() = runBlocking {
        // Ensure directory is empty
        crashLogsDir.listFiles()?.forEach { it.delete() }
        
        // Create worker
        val workerParams = mock(WorkerParameters::class.java)
        val worker = CrashLogWorker(context, workerParams)
        
        // Execute worker
        val result = worker.doWork()
        
        // Verify result
        assertEquals("Worker should succeed with empty directory", 
            androidx.work.ListenableWorker.Result.success(), result)
        
        // Verify no data was inserted
        val allLogs = dao.getAllCrashLogs().first()
        assertEquals("Should have no crash logs", 0, allLogs.size)
    }
}

