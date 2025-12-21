package com.github.ganeshpokale88.crashreporter.database

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.security.SecureRandom

/**
 * Manages the encryption key for SQLCipher database.
 * Uses Android Keystore (via MasterKey) to securely store the database passphrase.
 * 
 * This ensures HIPAA compliance by:
 * - Using hardware-backed security (Android Keystore via MasterKey)
 * - Storing encryption keys encrypted, not in plaintext files
 * - Using EncryptedFile with AES256-GCM encryption for the passphrase
 */
internal object DatabaseKeyManager {
    private const val KEY_FILE_NAME = "db_key.enc"
    
    /**
     * Get or generate the database encryption passphrase.
     * Uses Android Keystore (via MasterKey) to encrypt the SQLCipher passphrase.
     * 
     * The passphrase itself is a randomly generated 32-byte array that is:
     * 1. Generated using SecureRandom
     * 2. Stored encrypted using EncryptedFile (backed by Android Keystore MasterKey)
     * 3. Retrieved and decrypted when needed
     * 
     * @param context Application context
     * @return ByteArray containing the 32-byte passphrase for SQLCipher
     */
    fun getDatabasePassphrase(context: Context): ByteArray {
        val keyFile = File(context.filesDir, KEY_FILE_NAME)
        
        return if (keyFile.exists()) {
            // Load existing encrypted passphrase
            try {
                loadPassphrase(context, keyFile)
            } catch (e: Exception) {
                android.util.Log.w(
                    "DatabaseKeyManager",
                    "Failed to load encrypted passphrase, generating new one",
                    e
                )
                // If decryption fails, generate new passphrase
                generateAndStorePassphrase(context, keyFile)
            }
        } else {
            // First time: generate and store passphrase
            generateAndStorePassphrase(context, keyFile)
        }
    }
    
    /**
     * Load encrypted passphrase from file
     */
    private fun loadPassphrase(context: Context, keyFile: File): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        val encryptedFile = EncryptedFile.Builder(
            context,
            keyFile,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        
        return encryptedFile.openFileInput().use { inputStream ->
            inputStream.readBytes()
        }
    }
    
    /**
     * Generate a new random 32-byte passphrase and store it encrypted.
     * The passphrase is encrypted using MasterKey which uses Android Keystore.
     */
    private fun generateAndStorePassphrase(context: Context, keyFile: File): ByteArray {
        // Generate random 32-byte passphrase for SQLCipher
        // SQLCipher recommends 32 bytes for optimal security
        val passphrase = ByteArray(32)
        SecureRandom().nextBytes(passphrase)
        
        // Store encrypted using Android Keystore-backed MasterKey
        // MasterKey automatically uses Android Keystore for key management
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
                outputStream.write(passphrase)
                outputStream.flush()
            }
        } catch (e: Exception) {
            android.util.Log.e(
                "DatabaseKeyManager",
                "Failed to store encrypted passphrase. Database encryption may not persist across app reinstalls.",
                e
            )
            // Return passphrase anyway - it will work for this session
            // but won't persist if the app is reinstalled
        }
        
        return passphrase
    }
}

