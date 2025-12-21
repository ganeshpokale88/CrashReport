package com.github.ganeshpokale88.crashreporter.api.model

import com.google.gson.annotations.SerializedName

/**
 * Response model from the crash upload API
 */
data class UploadResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("totalCrashes")
    val totalCrashes: Int
)

