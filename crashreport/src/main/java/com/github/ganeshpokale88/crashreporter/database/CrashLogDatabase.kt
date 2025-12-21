package com.github.ganeshpokale88.crashreporter.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/**
 * Room database for storing crash logs.
 * 
 * This database is encrypted using SQLCipher for HIPAA compliance.
 * Uses sqlcipher-android:4.6.1 which supports 16KB page size alignment for Android 15+ compatibility.
 * The encryption key is securely managed using Android Keystore via DatabaseKeyManager.
 */
@Database(
    entities = [CrashLogEntity::class],
    version = 2, // Incremented from 1 to 2 due to adding encryption
    exportSchema = false
)
abstract class CrashLogDatabase : RoomDatabase() {
    abstract fun crashLogDao(): CrashLogDao

    companion object {
        private const val DATABASE_NAME = "crash_log_database"
        
        init {
            // Load SQLCipher native library explicitly
            // This ensures the native library is loaded before database operations
            try {
                System.loadLibrary("sqlcipher")
            } catch (e: UnsatisfiedLinkError) {
                // If loading fails, try alternative library names
                try {
                    System.loadLibrary("sqlcipher_android")
                } catch (e2: UnsatisfiedLinkError) {
                    android.util.Log.e(
                        "CrashLogDatabase",
                        "Failed to load SQLCipher native library. Database encryption may not work.",
                        e2
                    )
                }
            }
        }

        /**
         * Create an encrypted Room database using SQLCipher.
         * 
         * The database encryption passphrase is securely managed via DatabaseKeyManager,
         * which uses Android Keystore to encrypt the passphrase itself.
         * 
         * @param context Application context
         * @return Encrypted CrashLogDatabase instance
         */
        fun create(context: Context): CrashLogDatabase {
            // Get secure passphrase from DatabaseKeyManager
            // This passphrase is itself encrypted and stored securely using Android Keystore
            val passphrase = DatabaseKeyManager.getDatabasePassphrase(context.applicationContext)
            
            // Create SQLCipher SupportOpenHelperFactory with the passphrase
            val factory = SupportOpenHelperFactory(passphrase)
            
            return Room.databaseBuilder(
                context.applicationContext,
                CrashLogDatabase::class.java,
                DATABASE_NAME
            )
                .openHelperFactory(factory) // Use SQLCipher for encryption
                .fallbackToDestructiveMigration() // For development: drop and recreate on version change
                // In production, implement proper migration strategy if upgrading from unencrypted DB
                .build()
        }
    }
}

