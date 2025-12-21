package com.github.ganeshpokale88.crashreporter

import android.content.Context
import android.os.Build

/**
 * Data class to hold device information
 */
data class DeviceInfo(
    val androidVersion: String,
    val deviceMake: String,
    val deviceModel: String
) {
    companion object {
        fun collect(context: Context): DeviceInfo {
            return DeviceInfo(
                androidVersion = Build.VERSION.RELEASE,
                deviceMake = Build.MANUFACTURER,
                deviceModel = Build.MODEL
            )
        }
    }
}

