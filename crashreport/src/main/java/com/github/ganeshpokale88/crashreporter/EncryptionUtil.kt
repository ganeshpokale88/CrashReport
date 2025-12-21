package com.github.ganeshpokale88.crashreporter

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utility class for AES-256-GCM encryption/decryption.
 * 
 * Uses Android Keystore (via MasterKey) to securely store encryption keys.
 * This ensures HIPAA compliance by:
 * - Using hardware-backed security (Android Keystore via MasterKey)
 * - Storing encryption keys encrypted, not in plaintext files
 * - Using EncryptedFile with AES256-GCM encryption for the key
 */
object EncryptionUtil {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 16
    private const val KEY_FILE_NAME = "crashlytics_key.enc"

    /**
     * Get or generate encryption key using Android Keystore.
     * The key is stored encrypted using EncryptedFile (backed by Android Keystore MasterKey).
     */
    private fun getOrCreateKey(context: Context): SecretKey {
        val keyFile = File(context.filesDir, KEY_FILE_NAME)
        
        return if (keyFile.exists()) {
            // Load existing encrypted key
            try {
                loadEncryptedKey(context, keyFile)
            } catch (e: Exception) {
                android.util.Log.w(
                    "EncryptionUtil",
                    "Failed to load encrypted key, generating new one",
                    e
                )
                // If decryption fails, generate new key
                generateAndStoreKey(context, keyFile)
            }
        } else {
            // First time: generate and store key
            generateAndStoreKey(context, keyFile)
        }
    }
    
    /**
     * Load encrypted key from file using Android Keystore
     */
    private fun loadEncryptedKey(context: Context, keyFile: File): SecretKey {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        val encryptedFile = EncryptedFile.Builder(
            context,
            keyFile,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        
        val keyBytes = encryptedFile.openFileInput().use { inputStream ->
            inputStream.readBytes()
        }
        
        return SecretKeySpec(keyBytes, ALGORITHM)
    }
    
    /**
     * Generate a new encryption key and store it encrypted using Android Keystore
     */
    private fun generateAndStoreKey(context: Context, keyFile: File): SecretKey {
        // Generate new key
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(KEY_SIZE)
        val secretKey = keyGenerator.generateKey()
        
        // Store encrypted using Android Keystore-backed MasterKey
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val encryptedFile = EncryptedFile.Builder(
                context,
                keyFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            
            encryptedFile.openFileOutput().use { outputStream ->
                outputStream.write(secretKey.encoded)
                outputStream.flush()
            }
        } catch (e: Exception) {
            android.util.Log.e(
                "EncryptionUtil",
                "Failed to store encrypted key. Encryption may not persist across app reinstalls.",
                e
            )
            // Return key anyway - it will work for this session
            // but won't persist if the app is reinstalled
        }
        
        return secretKey
    }

    /**
     * Encrypt data using AES-256-GCM
     */
    fun encrypt(context: Context, data: String): String {
        val secretKey = getOrCreateKey(context)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        
        // Generate IV
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        
        // Initialize cipher for encryption
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)
        
        // Encrypt
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        // Combine IV and encrypted data
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
        
        // Return Base64 encoded string
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypt data using AES-256-GCM
     */
    fun decrypt(context: Context, encryptedData: String): String {
        val secretKey = getOrCreateKey(context)
        val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
        
        // Extract IV
        val iv = ByteArray(GCM_IV_LENGTH)
        System.arraycopy(combined, 0, iv, 0, iv.size)
        
        // Extract encrypted data
        val encryptedBytes = ByteArray(combined.size - iv.size)
        System.arraycopy(combined, iv.size, encryptedBytes, 0, encryptedBytes.size)
        
        // Initialize cipher for decryption
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)
        
        // Decrypt
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}

