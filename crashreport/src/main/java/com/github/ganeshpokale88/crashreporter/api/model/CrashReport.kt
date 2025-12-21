package com.github.ganeshpokale88.crashreporter.api.model

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Data class representing a crash report for API upload
 * Matches the FastAPI CrashReport model structure
 */
data class CrashReport(
    @SerializedName("timeStamp")
    val timeStamp: Date,
    
    @SerializedName("stackTrace")
    val stackTrace: String,
    
    @SerializedName("androidVersion")
    val androidVersion: String,
    
    @SerializedName("deviceMake")
    val deviceMake: String,
    
    @SerializedName("deviceModel")
    val deviceModel: String,
    
    @SerializedName("isFatal")
    val isFatal: Boolean
)

