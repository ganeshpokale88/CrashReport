package com.github.ganeshpokale88.crashreporter.api

import com.github.ganeshpokale88.crashreporter.api.model.CrashReport
import com.github.ganeshpokale88.crashreporter.api.model.UploadResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * API service interface for uploading crash reports to the server
 */
interface CrashReportApi {
    
    /**
     * Upload crash reports to the server
     * @param url Full URL for the endpoint (baseUrl + apiEndpoint)
     * @param crashes List of crash reports to upload
     * @return Response containing the result
     */
    @POST
    suspend fun uploadCrashes(
        @Url url: String,
        @Body crashes: List<CrashReport>
    ): Response<UploadResponse>
}

