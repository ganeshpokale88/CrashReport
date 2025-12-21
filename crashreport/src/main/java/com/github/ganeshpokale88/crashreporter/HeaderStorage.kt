package com.github.ganeshpokale88.crashreporter

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private const val TAG = "CrashReporter"

/**
 * Secure storage for HTTP headers using EncryptedSharedPreferences (Android Keystore-backed).
 * Headers are automatically persisted and retrieved across app restarts.
 * 
 * Security:
 * - Uses Android Keystore (hardware-backed when available)
 * - Headers are encrypted at rest
 * - Automatically cleared when app is uninstalled
 */
internal object HeaderStorage {
    
    private const val PREFS_NAME = "mycrashlytics_headers"
    private const val KEY_HEADERS = "headers"
    
    /**
     * Get encrypted SharedPreferences instance
     */
    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Save headers securely
     * @param context Application context
     * @param headers Map of header name to value
     */
    fun saveHeaders(context: Context, headers: Map<String, String>) {
        try {
            val prefs = getEncryptedPrefs(context)
            val gson = Gson()
            val json = gson.toJson(headers)
            prefs.edit().putString(KEY_HEADERS, json).apply()
        } catch (e: Exception) {
            // Log error but don't throw - header storage failure shouldn't break the app
            if (android.util.Log.isLoggable(TAG, android.util.Log.ERROR)) {
                android.util.Log.e(TAG, "Failed to save headers securely", e)
            }
        }
    }
    
    /**
     * Load persisted headers
     * @param context Application context
     * @return Map of header name to value, or empty map if none stored
     */
    fun loadHeaders(context: Context): Map<String, String> {
        return try {
            val prefs = getEncryptedPrefs(context)
            val json = prefs.getString(KEY_HEADERS, null) ?: return emptyMap()
            
            val gson = Gson()
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson<Map<String, String>>(json, type) ?: emptyMap()
        } catch (e: Exception) {
            // Log error but return empty map - header loading failure shouldn't break the app
            if (android.util.Log.isLoggable(TAG, android.util.Log.ERROR)) {
                android.util.Log.e(TAG, "Failed to load persisted headers", e)
            }
            emptyMap()
        }
    }
    
    /**
     * Clear all persisted headers (e.g., on user logout)
     * @param context Application context
     */
    fun clearHeaders(context: Context) {
        try {
            val prefs = getEncryptedPrefs(context)
            prefs.edit().remove(KEY_HEADERS).apply()
        } catch (e: Exception) {
            if (android.util.Log.isLoggable(TAG, android.util.Log.ERROR)) {
                android.util.Log.e(TAG, "Failed to clear headers", e)
            }
        }
    }
    
    /**
     * Merge provided headers with persisted headers.
     * Provided headers take precedence over persisted headers.
     * 
     * @param context Application context
     * @param providedHeaders Headers provided in config
     * @return Merged headers map
     */
    fun mergeHeaders(context: Context, providedHeaders: Map<String, String>): Map<String, String> {
        val persistedHeaders = loadHeaders(context)
        
        // Merge: provided headers override persisted headers
        return persistedHeaders.toMutableMap().apply {
            putAll(providedHeaders)
        }
    }
}

