# CrashReporter Test Verification

## Current Architecture

The library uses **WorkManager** for reliable background processing of crash logs. WorkManager ensures crash logs are processed even after app crashes or restarts.

### Key Components

1. **WorkManager Integration** - Used for background processing
   - `CrashLogWorker` - Processes encrypted crash log files and stores them in the database
   - `CrashUploadWorker` - Uploads crash logs from database to server
   - WorkManager is initialized by the library during `CrashReporter.initialize()`

2. **Crash Flow**:
   - Crash occurs → Encrypted file created → WorkManager scheduled → Worker processes file → Database updated → File deleted

3. **Upload Flow**:
   - Crash logs in database → WorkManager scheduled → Worker uploads to server → Logs deleted after successful upload

## 🧪 How to Test

### Test 1: Non-Fatal Crash (Primary Test)
1. Run the app on a device/emulator
2. Click the "Trigger Non-Fatal Crash" button
3. Check logcat for:
   - ✅ File creation: `crash_*.enc` file should be created in `filesDir/crash_logs/`
   - ✅ WorkManager scheduling: `CrashLogWorker` should be scheduled
   - ✅ Worker processing: Worker should process the file and store in database
   - ✅ Database storage: Crash log should be in Room database
   - ✅ File deletion: Encrypted file should be deleted after processing
   - ✅ Upload scheduling: `CrashUploadWorker` should be scheduled after database storage

### Test 2: Fatal Crash Test
1. Trigger a fatal crash (e.g., throw uncaught exception)
2. App should crash
3. On next app launch:
   - ✅ Encrypted file should be found from previous crash
   - ✅ WorkManager should process the file
   - ✅ Data should be stored in database

### Test 3: Upload Test
1. Ensure `baseUrl` and `apiEndpoint` are configured
2. Trigger a crash
3. Wait for upload worker to run (requires network)
4. Check:
   - ✅ Crash log uploaded to server
   - ✅ Log deleted from database after successful upload

### Test 4: Check Database
You can verify the database contains crash logs by:
- Using Room's database inspector in Android Studio
- Or adding a query method to retrieve logs

## 🔍 Key Components

### CrashReporter.kt
- `scheduleCrashLogProcessing()` - Schedules `CrashLogWorker` via WorkManager
- `setupWorkManager()` - Initializes WorkManager with custom configuration
- WorkManager is initialized during `CrashReporter.initialize()`

### CrashLogWorker.kt
- Processes encrypted crash log files
- Decrypts and stores in Room database
- Deletes files after processing
- Performs data retention cleanup

### CrashUploadWorker.kt
- Uploads crash logs from database to server
- Uses configured `baseUrl` and `apiEndpoint`
- Deletes logs after successful upload
- Retries on failure

## ✅ Expected Behavior

1. **Non-fatal crash reported** → File created with encryption ✅
2. **WorkManager scheduled** → `CrashLogWorker` enqueued ✅
3. **Worker processes file** → Data stored in database ✅
4. **File deleted** → Encrypted file removed after processing ✅
5. **Upload worker scheduled** → `CrashUploadWorker` enqueued (if API configured) ✅
6. **Upload to server** → Crash logs uploaded (if network available) ✅

## ⚠️ Important Notes

- **WorkManager Auto-Init**: Must be disabled in AndroidManifest.xml (see README.md)
- **Initialization Order**: `CrashReporter.initialize()` must be called before any WorkManager usage
- **Network Required**: Upload worker requires network connectivity
- **API Configuration**: Both `baseUrl` and `apiEndpoint` must be configured for uploads to work

## 🎯 Result

The library uses WorkManager for reliable, background processing of crash logs. This ensures crash logs are processed even after app crashes or restarts, providing robust crash reporting functionality.

