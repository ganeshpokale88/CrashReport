package com.github.ganeshpokale88.crashreporter

import org.junit.Test
import org.junit.Assert.*

/**
 * Test for DeviceInfo collection
 */
class DeviceInfoTest {

    @Test
    fun testDeviceInfoStructure() {
        // This is a unit test that verifies DeviceInfo data class structure
        val deviceInfo = DeviceInfo(
            androidVersion = "14",
            deviceMake = "Google",
            deviceModel = "Pixel 7"
        )

        assertEquals("14", deviceInfo.androidVersion)
        assertEquals("Google", deviceInfo.deviceMake)
        assertEquals("Pixel 7", deviceInfo.deviceModel)
    }
}

