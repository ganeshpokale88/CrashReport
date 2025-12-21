package com.github.ganeshpokale88.crashreporter

import android.content.Context
import com.github.ganeshpokale88.crashreporter.api.CrashReportApi
import com.google.gson.GsonBuilder
import okhttp3.CertificatePinner
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Factory for creating network dependencies without Hilt
 */
internal object NetworkFactory {
    
    
    /**
     * Create CrashReportApi instance with configuration.
     * TLS 1.2+ is always enforced for HIPAA compliance.
     * HTTP connections are only allowed for localhost (development).
     * 
     * Headers are automatically merged with persisted headers (from previous sessions).
     * Provided headers take precedence over persisted headers.
     * 
     * @param context Application context (for loading persisted headers)
     * @param config Configuration containing baseUrl, headers, and sanitization settings
     */
    fun createCrashReportApi(context: Context, config: CrashReporterConfig): CrashReportApi {
        // Validate baseUrl is provided
        if (config.baseUrl.isBlank()) {
            throw IllegalArgumentException(
                "baseUrl is required. Provide baseUrl using CrashReporterConfig.Builder().baseUrl()"
            )
        }
        
        // Validate apiEndpoint is provided
        if (config.apiEndpoint.isNullOrBlank()) {
            throw IllegalArgumentException(
                "apiEndpoint is required. Provide apiEndpoint using CrashReporterConfig.Builder().apiEndpoint()"
            )
        }
        
        // Validate base URL - reject HTTP in production (except localhost)
        val baseUrl = config.baseUrl
        if (!baseUrl.startsWith("https://") && !isLocalhost(baseUrl)) {
            throw IllegalArgumentException(
                "HTTP connections are not allowed for security compliance. " +
                "Use HTTPS or localhost (http://localhost, http://10.0.2.2) for development only."
            )
        }
        
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()
        
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Change to NONE in production
        }
        
        // Merge provided headers with persisted headers (from previous sessions)
        // Provided headers take precedence over persisted headers
        val mergedHeaders = HeaderStorage.mergeHeaders(context, config.headers)
        
        // Create interceptor to add custom headers
        val headerInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
            
            // Add all merged headers (persisted + provided)
            mergedHeaders.forEach { (name, value) ->
                requestBuilder.addHeader(name, value)
            }
            
            chain.proceed(requestBuilder.build())
        }
        
        // Always enforce TLS 1.2+ for HTTPS connections
        // Allow HTTP only for localhost (development)
        val connectionSpecs = if (isLocalhost(baseUrl)) {
            // Development: Allow HTTP for localhost, but still enforce TLS for HTTPS
            listOf(
                ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
                    .build(),
                ConnectionSpec.CLEARTEXT // Allow HTTP for localhost only
            )
        } else {
            // Production: Only TLS 1.2+ allowed
            listOf(
                ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
                    .build()
            )
        }
        
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(headerInterceptor)
            .connectionSpecs(connectionSpecs)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        
        // Apply certificate pinning if configured (skip for localhost)
        if (!isLocalhost(baseUrl) && !config.certificatePins.isNullOrEmpty()) {
            val certificatePinnerBuilder = CertificatePinner.Builder()
            
            config.certificatePins.forEach { (hostname, pins) ->
                pins.forEach { pin ->
                    // Validate pin format
                    if (pin.startsWith("sha256/")) {
                        certificatePinnerBuilder.add(hostname, pin)
                    } else {
                        // Auto-add sha256/ prefix if missing
                        val formattedPin = if (pin.contains("/")) pin else "sha256/$pin"
                        certificatePinnerBuilder.add(hostname, formattedPin)
                    }
                }
            }
            
            clientBuilder.certificatePinner(certificatePinnerBuilder.build())
        }
        
        val okHttpClient = clientBuilder.build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        return retrofit.create(CrashReportApi::class.java)
    }
    
    /**
     * Check if the base URL is localhost (for development)
     */
    private fun isLocalhost(url: String): Boolean {
        return url.startsWith("http://localhost") ||
               url.startsWith("http://127.0.0.1") ||
               url.startsWith("http://192.168.1") ||
                url.startsWith("http://10.0.2.2")   // Android emulator host
    }
}

