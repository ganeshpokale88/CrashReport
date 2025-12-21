package com.github.ganeshpokale88.crashreporter

import org.junit.Test
import org.junit.Assert.*

/**
 * Test for DeviceInfo collection
 */
class DeviceInfoTest {

    @Test
    fun testDeviceInfoStructure() {
        val deviceInfo = DeviceInfo(
            androidVersion = "14",
            deviceMake = "Google",
            deviceModel = "Pixel 7"
        )

        assertEquals("14", deviceInfo.androidVersion)
        assertEquals("Google", deviceInfo.deviceMake)
        assertEquals("Pixel 7", deviceInfo.deviceModel)
    }
    
    @Test
    fun testDeviceInfoEquality() {
        val device1 = DeviceInfo(
            androidVersion = "14",
            deviceMake = "Google",
            deviceModel = "Pixel 7"
        )
        
        val device2 = DeviceInfo(
            androidVersion = "14",
            deviceMake = "Google",
            deviceModel = "Pixel 7"
        )
        
        assertEquals(device1, device2)
        assertEquals(device1.hashCode(), device2.hashCode())
    }
    
    @Test
    fun testDeviceInfoToString() {
        val deviceInfo = DeviceInfo(
            androidVersion = "14",
            deviceMake = "Google",
            deviceModel = "Pixel 7"
        )
        
        assertTrue(deviceInfo.toString().contains("Pixel 7"))
        assertTrue(deviceInfo.toString().contains("Google"))
    }
}
