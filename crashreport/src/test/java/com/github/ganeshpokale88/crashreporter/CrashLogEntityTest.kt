package com.github.ganeshpokale88.crashreporter.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashLogEntityTest {

    @Test
    fun `test entity creation and properties`() {
        val timestamp = System.currentTimeMillis()
        val entity = CrashLogEntity(
            timestamp = timestamp,
            isFatal = true,
            stackTrace = "java.lang.RuntimeException",
            androidVersion = "14",
            deviceMake = "Google",
            deviceModel = "Pixel 7"
        )
        
        assertEquals(timestamp, entity.timestamp)
        assertTrue(entity.isFatal)
        assertEquals("java.lang.RuntimeException", entity.stackTrace)
        assertEquals("14", entity.androidVersion)
        assertEquals("Google", entity.deviceMake)
        assertEquals("Pixel 7", entity.deviceModel)
    }
    
    @Test
    fun `test entity equality`() {
        val timestamp = 123456789L
        val entity1 = CrashLogEntity(
            id = 1,
            timestamp = timestamp,
            isFatal = true,
            stackTrace = "trace",
            androidVersion = "14",
            deviceMake = "Google",
            deviceModel = "Pixel"
        )
        
        val entity2 = CrashLogEntity(
            id = 1,
            timestamp = timestamp,
            isFatal = true,
            stackTrace = "trace",
            androidVersion = "14",
            deviceMake = "Google",
            deviceModel = "Pixel"
        )
        
        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())
    }
    
    @Test
    fun `test entity inequality`() {
        val entity1 = CrashLogEntity(
            id = 1,
            timestamp = 100L,
            isFatal = true,
            stackTrace = "trace",
            androidVersion = "14",
            deviceMake = "Google",
            deviceModel = "Pixel"
        )
        
        val entity2 = CrashLogEntity(
            id = 2,
            timestamp = 100L,
            isFatal = true,
            stackTrace = "trace",
            androidVersion = "14",
            deviceMake = "Google",
            deviceModel = "Pixel"
        )
        
        assertNotEquals(entity1, entity2)
    }
    
    @Test
    fun `test entity copy`() {
        val entity1 = CrashLogEntity(
            timestamp = 100L,
            isFatal = true,
            stackTrace = "trace",
            androidVersion = "14",
            deviceMake = "Google",
            deviceModel = "Pixel"
        )
        
        val entity2 = entity1.copy(isFatal = false)
        
        assertNotEquals(entity1, entity2)
        assertEquals(entity1.timestamp, entity2.timestamp)
        assertEquals(false, entity2.isFatal)
    }
}
