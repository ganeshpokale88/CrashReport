# CrashReporter Verification Guide

## Current Status

The library has been set up with:
- ✅ WorkManager integration for background processing
- ✅ Room database with SQLCipher encryption for crash log storage
- ✅ AES-256-GCM encryption for crash log files
- ✅ Android Keystore for secure key storage
- ✅ Device information collection
- ✅ Fatal and non-fatal crash tracking
- ✅ PHI sanitization (configurable)
- ✅ TLS 1.2+ enforcement
- ✅ Data retention policies (auto-delete after 90 days)
- ✅ Secure header persistence (EncryptedSharedPreferences)
- ✅ Certificate pinning (configurable)
- ✅ Code obfuscation (R8/ProGuard)

## WorkManager Setup Verification

### 1. Verify AndroidManifest.xml
Ensure WorkManager auto-initialization is disabled:
```xml
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    android:exported="false"
    tools:node="merge">
    <meta-data
        android:name="androidx.work.WorkManagerInitializer"
        android:value="androidx.startup"
        tools:node="remove" />
</provider>
```

### 2. Verify Application Class
- Calls `CrashReporter.initialize()` in `onCreate()`
- WorkManager is initialized automatically by the library
- No manual WorkManager setup required

### 4. Test Non-Fatal Crash Flow

1. **Trigger Non-Fatal Crash:**
   ```kotlin
   try {
       throw RuntimeException("Test crash")
   } catch (e: Exception) {
       CrashReporter.getInstance().reportNonFatalCrash(e)
   }
   ```

2. **Verify File Creation:**
   - Check `context.filesDir/crash_logs/` for `.enc` files
   - Files should be encrypted (not readable as plain text)

3. **Verify Database Storage:**
   - Use Database Inspector in Android Studio
   - Check `crash_logs` table
   - Verify crash log with stack trace, device info, timestamp

4. **Verify WorkManager Processing:**
   - Check Logcat for WorkManager execution
   - Files should be deleted after processing
   - Data should appear in Room database

## WorkManager Workers

The library uses two WorkManager workers:

1. **CrashLogWorker** - Processes encrypted crash log files
   - Reads encrypted files from `filesDir/crash_logs/`
   - Decrypts and stores in Room database
   - Deletes files after processing
   - Performs data retention cleanup

2. **CrashUploadWorker** - Uploads crash logs to server
   - Reads crash logs from database
   - Uploads to configured `baseUrl + apiEndpoint`
   - Deletes logs after successful upload
   - Retries on failure

## Troubleshooting

### Issue: "WorkManager not initialized"
**Possible Causes:**
1. `CrashReporter.initialize()` not called before crash occurs
2. WorkManager auto-initialization not disabled in manifest
3. WorkManager initialized elsewhere before library initialization

**Solution:**
- Ensure `CrashReporter.initialize()` is called in Application.onCreate()
- Verify WorkManager auto-initialization is disabled in AndroidManifest.xml
- Check initialization order - library should initialize WorkManager first

### Issue: Crash logs not appearing in database
**Check:**
1. Files are being created in `crash_logs/` directory
2. WorkManager worker is executing (check Logcat for `CrashLogWorker`)
3. Database is accessible (check Room database inspector)
4. Worker constraints are met (network not required for processing)

### Issue: Crash logs not uploading
**Check:**
1. `baseUrl` and `apiEndpoint` are configured
2. Network connectivity is available
3. Server endpoint is accessible
4. Headers are properly configured (if required)
5. Certificate pinning is correct (if enabled)

## Running Tests

```bash
# Unit tests
./gradlew :crashreport:test

# Instrumented tests (requires device/emulator)
./gradlew :crashreport:connectedAndroidTest

# Specific test
./gradlew :crashreport:connectedAndroidTest --tests "CrashReporterIntegrationTest.testNonFatalCrashProcessedToDatabase"
```

## Expected Behavior

1. **Non-Fatal Crash:**
   - Exception caught
   - Encrypted file created
   - WorkManager scheduled (or direct processing)
   - Data stored in Room database
   - File deleted after processing

2. **Fatal Crash:**
   - Uncaught exception handler triggered
   - Encrypted file created
   - WorkManager scheduled (if app doesn't crash immediately)
   - App crashes (normal behavior)
   - On next app launch: WorkManager processes file, data stored in Room database

3. **Upload:**
   - Crash logs in database
   - WorkManager upload worker scheduled
   - Network connectivity checked
   - Upload to server (if `baseUrl` and `apiEndpoint` configured)
   - Logs deleted after successful upload

