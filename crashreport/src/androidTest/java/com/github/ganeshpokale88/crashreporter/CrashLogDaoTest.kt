package com.github.ganeshpokale88.crashreporter

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.ganeshpokale88.crashreporter.database.CrashLogDao
import com.github.ganeshpokale88.crashreporter.database.CrashLogDatabase
import com.github.ganeshpokale88.crashreporter.database.CrashLogEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented test for CrashLogDao
 */
@RunWith(AndroidJUnit4::class)
class CrashLogDaoTest {

    private lateinit var database: CrashLogDatabase
    private lateinit var dao: CrashLogDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            CrashLogDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.crashLogDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertCrashLogAndRetrieve() = runBlocking {
        val crashLog = CrashLogEntity(
            timestamp = System.currentTimeMillis(),
            stackTrace = "Test stack trace",
            androidVersion = "14",
            deviceMake = "Google",
            deviceModel = "Pixel 7",
            isFatal = false
        )

        val id = dao.insertCrashLog(crashLog)
        assertTrue(id > 0)

        val allLogs = dao.getAllCrashLogs().first()
        assertEquals(1, allLogs.size)
        assertEquals("Test stack trace", allLogs[0].stackTrace)
        assertEquals("14", allLogs[0].androidVersion)
        assertEquals("Google", allLogs[0].deviceMake)
        assertEquals("Pixel 7", allLogs[0].deviceModel)
        assertFalse(allLogs[0].isFatal)
    }

    @Test
    fun insertMultipleCrashLogs() = runBlocking {
        val crashLog1 = CrashLogEntity(
            timestamp = System.currentTimeMillis(),
            stackTrace = "Crash 1",
            androidVersion = "14",
            deviceMake = "Google",
            deviceModel = "Pixel 7",
            isFatal = false
        )

        val crashLog2 = CrashLogEntity(
            timestamp = System.currentTimeMillis() + 1000,
            stackTrace = "Crash 2",
            androidVersion = "14",
            deviceMake = "Samsung",
            deviceModel = "Galaxy S23",
            isFatal = true
        )

        dao.insertCrashLog(crashLog1)
        dao.insertCrashLog(crashLog2)

        val allLogs = dao.getAllCrashLogs().first()
        assertEquals(2, allLogs.size)
        // Should be ordered by timestamp DESC
        assertEquals("Crash 2", allLogs[0].stackTrace)
        assertEquals("Crash 1", allLogs[1].stackTrace)
    }

    @Test
    fun deleteCrashLog() = runBlocking {
        val crashLog = CrashLogEntity(
            timestamp = System.currentTimeMillis(),
            stackTrace = "Test stack trace",
            androidVersion = "14",
            deviceMake = "Google",
            deviceModel = "Pixel 7",
            isFatal = false
        )

        val id = dao.insertCrashLog(crashLog)
        dao.deleteCrashLog(id)

        val allLogs = dao.getAllCrashLogs().first()
        assertEquals(0, allLogs.size)
    }
}

