# CrashReporter Testing Guide

## Test Structure

This library includes comprehensive tests to validate all functionality:

### Unit Tests (`src/test`)
- **DeviceInfoTest**: Tests device information data structure
- **EncryptionUtilTest**: Tests encryption utility (placeholder for structure)

### Instrumented Tests (`src/androidTest`)
- **CrashLogDaoTest**: Tests Room database operations (SQLCipher encrypted)
- **CrashLogWorkerTest**: Tests WorkManager worker functionality (file processing, data retention)
- **CrashUploadWorkerTest**: Tests upload worker functionality (server upload, retry logic)
- **CrashReporterIntegrationTest**: End-to-end integration tests (full crash reporting flow)

## Running Tests

### Run All Tests
```bash
./gradlew :mycrashlytics:test
./gradlew :mycrashlytics:connectedAndroidTest
```

### Run Specific Test Class
```bash
./gradlew :mycrashlytics:test --tests "com.github.ganeshpokale88.crashreporter.DeviceInfoTest"
./gradlew :mycrashlytics:connectedAndroidTest --tests "com.github.ganeshpokale88.crashreporter.CrashReporterIntegrationTest"
```

### Run from Android Studio
1. Right-click on test file or test method
2. Select "Run 'TestName'"

## Key Test Cases

### 1. Non-Fatal Crash to Database Flow
**Test**: `CrashReporterIntegrationTest.testNonFatalCrashProcessedToDatabase`
- Validates that non-fatal crashes are:
  - Written to encrypted file
  - Can be decrypted
  - Processed and stored in Room database
  - Include all device information (Android version, make, model)
  - Include timestamp and stack trace

### 2. Worker Processing
**Test**: `CrashLogWorkerTest.testWorkerProcessesEncryptedFile`
- Validates that worker:
  - Reads encrypted files
  - Decrypts content
  - Inserts into database
  - Deletes processed files

### 3. Database Operations
**Test**: `CrashLogDaoTest.insertCrashLogAndRetrieve`
- Validates Room database:
  - Insert operations
  - Flow-based queries
  - Data integrity

### 4. Encryption/Decryption
**Test**: `CrashReporterIntegrationTest.testEncryptionDecryption`
- Validates AES-256 encryption:
  - Encryption produces different output
  - Decryption restores original data

## Test Coverage

✅ Device information collection  
✅ Encryption/Decryption (AES-256-GCM)  
✅ File I/O operations  
✅ Room database operations (SQLCipher encrypted)  
✅ WorkManager worker processing (CrashLogWorker, CrashUploadWorker)  
✅ End-to-end crash reporting flow  
✅ Multiple crash handling  
✅ Error handling  
✅ PHI sanitization (if enabled)  
✅ Data retention cleanup  
✅ Header persistence  
✅ Network upload with retry logic  

## Notes

- Tests use in-memory Room database for isolation
- Tests clean up files after execution
- Integration tests simulate the full crash reporting flow
- All tests are designed to run independently

