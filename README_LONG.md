# CrashReporter Library - Complete Documentation

> 📄 **This is the comprehensive documentation.** For a quick start guide, see [README.md](README.md)

A self-contained Android crash reporting library that collects, stores, and uploads crash reports to your server. The library handles both fatal and non-fatal crashes, with automatic retry mechanisms and support for dynamic configuration updates.

## Features

- ✅ **Fatal & Non-Fatal Crash Reporting** - Automatically captures both types of crashes
- ✅ **Local Storage** - Encrypted crash logs stored locally before upload
- ✅ **Automatic Upload** - Background workers upload crash logs to your server
- ✅ **Dynamic Configuration** - Update base URL and headers at runtime (e.g., after login)
- ✅ **WorkManager Integration** - Reliable background processing even after app crashes
- ✅ **No External Dependencies** - Self-contained, no Hilt or app-level configuration required
- ✅ **Room Database** - Efficient local storage of crash logs
- ✅ **Retry Logic** - Automatic retry on upload failures
- ✅ **HIPAA Compliance** - Comprehensive security features for healthcare applications
  - Stack trace sanitization to remove PHI (configurable)
  - TLS 1.2+ enforcement (always enabled, HTTP blocked in production)
  - Data retention policies (auto-delete after 90 days, configurable)
  - Secure key storage (Android Keystore - hardware-backed when available)
  - Secure header persistence (EncryptedSharedPreferences)
  - Certificate pinning (configurable, SHA-256 with backup pins)
  - Code obfuscation (R8/ProGuard)
  - Logging disabled in release builds

## Installation

Add the library to your project's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":crashreport"))
}
```

## Quick Start

### Required API Endpoint

Before using the library, ensure your server has a **POST** endpoint that accepts crash reports. The library will automatically send crash logs to: `{baseUrl}{apiEndpoint}`

**⚠️ Important:** Both `baseUrl` and `apiEndpoint` are **required**. If either is missing, the library will show debug warnings and no API calls will be made.

#### Request Format

**Endpoint:** `POST {apiEndpoint}` (configurable via `CrashReporterConfig`)  
**Content-Type:** `application/json`  
**Headers:** Custom headers you provide in configuration (e.g., Authorization, API keys)

**Request Body:** Array of crash reports

```json
[
  {
    "timeStamp": "2024-01-15T10:30:45.123Z",
    "stackTrace": "java.lang.RuntimeException: Error occurred\n\tat com.example...",
    "androidVersion": "13",
    "deviceMake": "Google",
    "deviceModel": "Pixel 6",
    "isFatal": true
  },
  {
    "timeStamp": "2024-01-15T10:35:12.456Z",
    "stackTrace": "java.lang.NullPointerException...",
    "androidVersion": "13",
    "deviceMake": "Samsung",
    "deviceModel": "Galaxy S21",
    "isFatal": false
  }
]
```

**Field Descriptions:**

| Field | Type | Description |
|-------|------|-------------|
| `timeStamp` | ISO-8601 DateTime | When the crash occurred (e.g., "2024-01-15T10:30:45.123Z") |
| `stackTrace` | String | Full stack trace of the crash/exception |
| `androidVersion` | String | Android OS version (e.g., "13", "14") |
| `deviceMake` | String | Device manufacturer (e.g., "Google", "Samsung") |
| `deviceModel` | String | Device model (e.g., "Pixel 6", "Galaxy S21") |
| `isFatal` | Boolean | `true` for fatal crashes, `false` for non-fatal exceptions |

#### Expected Response

**Success Response:** HTTP Status `200` or `201`  
**Response Body:** (Optional)

```json
{
  "message": "Crashes received successfully",
  "totalCrashes": 2
}
```

If the server returns HTTP status `200` or `201`, the library considers the upload successful and deletes the uploaded crash logs from the local database.

**Error Response:** Any status other than `200` or `201` will cause the library to retry the upload later.

#### Example Server Implementation

**FastAPI (Python):**
```python
from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
from datetime import datetime

app = FastAPI()

class CrashReport(BaseModel):
    timeStamp: datetime
    stackTrace: str
    androidVersion: str
    deviceMake: str
    deviceModel: str
    isFatal: bool

@app.post("/crashes", status_code=201)
async def upload_crashes(crashes: List[CrashReport]):
    # Process crashes (store in DB, send to monitoring service, etc.)
    for crash in crashes:
        process_crash(crash)
    
    return {
        "message": "Crashes received successfully",
        "totalCrashes": len(crashes)
    }
```

**Spring Boot (Java):**
```java
@PostMapping("/crashes")
@ResponseStatus(HttpStatus.CREATED)
public ResponseEntity<Map<String, Object>> uploadCrashes(
    @RequestBody List<CrashReport> crashes
) {
    // Process crashes
    crashes.forEach(this::processCrash);
    
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Crashes received successfully");
    response.put("totalCrashes", crashes.size());
    return ResponseEntity.ok(response);
}
```

### 1. Basic Initialization (Without Configuration)

Initialize in your `Application` class without configuration. This is useful when base URL and headers are not available at startup (e.g., user needs to login first).

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReporter.initialize(this)
    }
}
```

**Important Notes:**
- **Crash logs will be stored locally** but won't be uploaded until configuration is provided
- **You must call `updateConfiguration()`** with `baseUrl` and `apiEndpoint` before uploads can occur
- **Both `baseUrl` and `apiEndpoint` are required** for API calls to work
- Use this approach when authentication tokens are only available after user login

**Example - Update configuration after login:**
```kotlin
// After user login
fun onLoginSuccess(jwtToken: String) {
    val config = CrashReporterConfig.Builder()
        .baseUrl("https://api.example.com")  // Required
        .apiEndpoint("/crashes")              // Required
        .addHeader("Authorization", "Bearer $jwtToken")
        .build()
    
    CrashReporter.updateConfiguration(config)
}
```

### 2. Initialize with Configuration (Recommended)

**Recommended approach:** Initialize with full configuration if you know the base URL and endpoint at startup.

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")  // Required
            .apiEndpoint("/crashes")              // Required - no default
            // Optional: Enable sanitization for HIPAA compliance
            .enableSanitization()
            // Optional: Certificate pinning with backup (recommended for production)
            .addCertificatePins(listOf(
                "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",  // Current cert
                "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="   // Backup cert
            ))
            // Optional: Add static headers (e.g., API keys)
            // .addHeader("X-API-Key", "your-api-key")
            .build()
        
        CrashReporter.initialize(this, config)
    }
}
```

**Note:** Headers can be added later using `updateConfiguration()` if they're only available after login. Headers are automatically persisted securely.

### 3. Update Configuration After Login (Recommended)

For apps where base URL and authorization tokens are available after login:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize without config - crash logs will be stored locally
        CrashReporter.initialize(this)
    }
}

// In your login/session management code
fun onLoginSuccess(jwtToken: String) {
    val config = CrashReporterConfig.Builder()
        .baseUrl("https://api.example.com")
        .apiEndpoint("/api/crashes")  // Required - no default
        .addHeader("Authorization", "Bearer $jwtToken")  // Automatically persisted!
        .addHeader("X-User-ID", getCurrentUserId())      // Automatically persisted!
        // Certificate pinning with backup (recommended for production)
        .addCertificatePins(listOf(
            "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",  // Current cert
            "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="   // Backup cert
        ))
        .build()
    
    CrashReporter.updateConfiguration(config)
    // Headers are now saved securely and will persist across app restarts!
}

// On logout - clear persisted headers
fun onLogout() {
    CrashReporter.clearHeaders(context)  // Clears all persisted headers securely
    // ... other logout logic
}
```

### 4. Update Configuration on Token Refresh

When your auth token is refreshed:

```kotlin
fun onTokenRefreshed(newToken: String) {
    val config = CrashReporterConfig.Builder()
        .baseUrl("https://api.example.com") // Same base URL
        .addHeader("Authorization", "Bearer $newToken")
        .build()
    
    CrashReporter.updateConfiguration(config)
}
```

## Configuration

### CrashReporterConfig

The configuration class allows you to set the base URL and headers for network requests.

#### Builder Pattern (Recommended)

```kotlin
val config = CrashReporterConfig.Builder()
    .baseUrl("https://api.example.com")
    .addHeader("Authorization", "Bearer token123")
    .addHeader("X-API-Key", "key456")
    .addHeader("X-App-Version", BuildConfig.VERSION_NAME)
    .build()
```

#### Helper Method (Simple)

```kotlin
// Just base URL, no headers
val config = CrashReporterConfig.create("https://api.example.com")
```

#### Multiple Headers at Once

```kotlin
val config = CrashReporterConfig.Builder()
    .baseUrl("https://api.example.com")
    .headers(mapOf(
        "Authorization" to "Bearer token",
        "X-API-Key" to "key123",
        "X-Client-Version" to "1.0.0"
    ))
    .addHeader("X-Device-ID", getDeviceId()) // Can add more after
    .build()
```

### Configuration Properties

| Property | Required | Description | Example |
|----------|----------|-------------|---------|
| `baseUrl` | Yes | Base URL for the API endpoint (without path) | `"https://api.example.com"` |
| `apiEndpoint` | Yes | API endpoint path (no default - must be provided) | `"/api/crashes"` |
| `headers` | No | Map of headers to include in all requests | `mapOf("Authorization" to "Bearer token")` |
| `sanitizationConfig` | No | Configuration for PHI sanitization (default: null, disabled) | See sanitization section |
| `dataRetentionDays` | No | Data retention period in days (default: 90) | `90` |
| `certificatePins` | No | SSL certificate pins for MITM protection (default: null, disabled) | See certificate pinning section |

### TLS 1.2+ Enforcement (Always Enabled)

**TLS 1.2+ is always enforced** for all HTTPS connections to ensure HIPAA compliance and secure data transmission. This cannot be disabled.

- **Production:** Only HTTPS with TLS 1.2 or TLS 1.3 is allowed
- **Development:** HTTP is allowed only for localhost (`http://localhost`, `http://127.0.0.1`, `http://10.0.2.2` for Android emulator)
- **HTTP in production:** Will throw `IllegalArgumentException` to prevent insecure connections

**Example:**
```kotlin
// ✅ Production - HTTPS required
val config = CrashReporterConfig.Builder()
    .baseUrl("https://api.example.com")  // HTTPS required
    .build()

// ✅ Development - localhost allowed
val config = CrashReporterConfig.Builder()
    .baseUrl("http://10.0.2.2:8000")  // localhost allowed for development
    .build()

// ❌ Production HTTP - Will throw exception
val config = CrashReporterConfig.Builder()
    .baseUrl("http://api.example.com")  // ERROR: HTTP not allowed
    .build()
```

### Stack Trace Sanitization (HIPAA Compliance)

The library supports automatic sanitization of stack traces to remove or redact potential PHI (Protected Health Information). This is essential for HIPAA compliance.

#### Enable Default HIPAA-Compliant Sanitization

```kotlin
val config = CrashReporterConfig.Builder()
    .baseUrl("https://api.example.com")
    .enableSanitization()  // Uses default HIPAA-compliant config
    .build()
```

#### Custom Sanitization Configuration

```kotlin
val config = CrashReporterConfig.Builder()
    .baseUrl("https://api.example.com")
    .sanitizationConfig(
        patientNames = listOf("John Doe", "Jane Smith"),  // Names to redact
        customPatterns = listOf(Regex("PAT-\\d+")),  // Custom patterns
        redactEmails = true,      // Redact email addresses
        redactPhones = true,      // Redact phone numbers
        redactSSNs = true,        // Redact Social Security Numbers
        redactMRNs = true,        // Redact Medical Record Numbers
        redactUserPaths = true,   // Redact file paths with user data
        redactDates = false,      // Redact dates (default: false)
        redactCreditCards = true  // Redact credit card numbers
    )
    .build()
```

#### What Gets Redacted

By default, the sanitizer redacts:
- **SSNs**: Patterns like `123-45-6789` or `123456789`
- **Email addresses**: `user@example.com`
- **Phone numbers**: `555-123-4567`, `(555) 123-4567`, etc.
- **Medical Record Numbers**: `MRN: ABC123456`
- **File paths**: Paths containing `/user/`, `/patient/`, `/data/`, etc.
- **Credit card numbers**: `1234-5678-9012-3456`
- **Patient names**: Configurable list of names to redact

All redacted content is replaced with `[REDACTED]` to maintain stack trace structure while protecting PHI.

#### Example

```kotlin
// Stack trace before sanitization:
"Error: Patient John Doe (SSN: 123-45-6789) contact user@example.com"

// After sanitization:
"Error: Patient [REDACTED] (SSN: [REDACTED]) contact [REDACTED]"
```

### Data Retention Policies (HIPAA Compliance)

The library automatically deletes old crash logs to comply with data retention requirements. This helps minimize PHI stored on devices.

#### Default Behavior

- **Files:** Deleted immediately after writing to database
- **Database logs:** Auto-deleted after 90 days (configurable)
- **After upload:** Logs deleted from database after successful upload

#### Configure Retention Period

```kotlin
val config = CrashReporterConfig.Builder()
    .baseUrl("https://api.example.com")
    .dataRetentionDays(90)  // Delete logs older than 90 days (default)
    .build()
```

#### Disable Automatic Deletion

```kotlin
val config = CrashReporterConfig.Builder()
    .baseUrl("https://api.example.com")
    .dataRetentionDays(0)  // Disable automatic deletion
    .build()
```

**Note:** For HIPAA compliance, it's recommended to keep automatic deletion enabled with a reasonable retention period (default: 90 days).

### Security Features

#### Secure Key Storage

All encryption keys are stored securely using Android Keystore (hardware-backed when available):
- Database encryption keys: Android Keystore
- File encryption keys: Android Keystore
- Keys are encrypted using AES256-GCM-HKDF

#### Code Obfuscation

The library uses R8/ProGuard for code obfuscation in release builds:
- Internal implementation classes are obfuscated
- Public API remains accessible
- Logging statements removed in release builds
- Optimized bytecode for better performance

#### SSL Certificate Pinning

Certificate pinning provides MITM (Man-in-the-Middle) attack protection:
- SHA-256 certificate pinning supported
- Multiple pins for certificate rotation
- Automatic hostname extraction from baseUrl
- Disabled automatically for localhost connections
- Configurable via CrashReporterConfig

**See detailed configuration below.**

### SSL Certificate Pinning (MITM Protection)

Certificate pinning protects against man-in-the-middle (MITM) attacks by verifying the server's SSL certificate. This is **highly recommended** for HIPAA compliance and production use.

#### Get Your Certificate's SHA-256 Pin

**Using OpenSSL:**
```bash
openssl s_client -servername api.example.com -connect api.example.com:443 < /dev/null | \
  openssl x509 -pubkey -noout | \
  openssl pkey -pubin -outform der | \
  openssl dgst -sha256 -binary | \
  openssl enc -base64
```

**Using Online Tools:**
- Visit: https://www.ssllabs.com/ssltest/
- Enter your domain and check certificate details

#### Enable Certificate Pinning

##### Single Certificate Pin (Basic)

Use a single pin for simple setups. **Note:** This will break when your certificate expires or rotates.

```kotlin
val config = CrashReporterConfig.Builder()
    .baseUrl("https://api.example.com")
    .apiEndpoint("/crashes")
    .addCertificatePin("sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")  // Single pin
    .build()
```

**When to use:** Development/testing environments or when you manually update the app before certificate rotation.

##### Backup Pin (Recommended for Production)

**Always use backup pins in production** to avoid app breakage during certificate rotation. The library will accept either the current or backup certificate.

```kotlin
val config = CrashReporterConfig.Builder()
    .baseUrl("https://api.example.com")
    .apiEndpoint("/crashes")
    .addCertificatePins(listOf(
        "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",  // Current certificate
        "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="   // Backup certificate (for rotation)
    ))
    .build()
```

**When to use:** Production environments where certificate rotation happens without app updates.

**How to get backup pin:**
1. Get your current certificate pin (see above)
2. Get your backup/next certificate pin from your certificate provider
3. Add both pins to the list

##### Multiple Hostnames

Pin certificates for multiple hostnames (e.g., API server and CDN):

```kotlin
val config = CrashReporterConfig.Builder()
    .baseUrl("https://api.example.com")
    .apiEndpoint("/crashes")
    .addCertificatePin("api.example.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
    .addCertificatePin("cdn.example.com", "sha256/CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC=")
    .build()
```

##### Using Map (Advanced)

For complex configurations with multiple hostnames and backup pins:

```kotlin
val config = CrashReporterConfig.Builder()
    .baseUrl("https://api.example.com")
    .apiEndpoint("/crashes")
    .certificatePins(mapOf(
        "api.example.com" to listOf(
            "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",  // Current
            "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="   // Backup
        ),
        "cdn.example.com" to listOf(
            "sha256/CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC=",  // Current
            "sha256/DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD="   // Backup
        )
    ))
    .build()
```

#### Important Notes

- **Pin Format:** Pins must be in format `sha256/XXXXXXXXXXXXXXXX` (the library will auto-add `sha256/` prefix if missing)
- **Localhost:** Certificate pinning is automatically disabled for localhost connections
- **Certificate Rotation:** 
  - **Single Pin:** Will break when certificate rotates (requires app update)
  - **Backup Pin:** Recommended for production - app continues working during certificate rotation
  - Always provide backup pins in production to avoid app breakage
- **Production:** Highly recommended for all production deployments with backup pins
- **Automatic Hostname:** If you don't specify a hostname, the library automatically extracts it from `baseUrl`

#### What Happens if Pin Mismatch?

If the server's certificate doesn't match the pinned certificate, the connection will fail with a `CertificatePinnerException`. This protects against:
- MITM attacks
- Compromised certificate authorities
- Fake certificates

## Usage

### Report Non-Fatal Crash

```kotlin
try {
    // Your code that might throw
    riskyOperation()
} catch (e: Exception) {
    CrashReporter.getInstance().reportNonFatalCrash(e)
    // Handle error in your app
}
```

### Fatal Crashes

Fatal crashes are automatically captured by the library - no additional code needed!

## Network Configuration

### Base URL Requirements

- **Production:** Must use `https://` (HTTP is not allowed)
- **Development:** `http://` allowed only for localhost
- No trailing slash
- Should not include the endpoint path (use `apiEndpoint` for that)
- Examples:
  - Production: `"https://api.example.com"` ✅
  - Development: `"http://localhost:8000"` ✅
  - Emulator: `"http://10.0.2.2:8000"` ✅ (to access host machine)
  - Production HTTP: `"http://api.example.com"` ❌ (will throw exception)
  - With endpoint: `"https://api.example.com/crashes"` ❌ (use `apiEndpoint` instead)

### API Endpoint Configuration

**⚠️ Required:** The API endpoint path must be provided. There is no default value.

```kotlin
val config = CrashReporterConfig.Builder()
    .baseUrl("https://api.example.com")
    .apiEndpoint("/api/crashes")  // Required - no default
    .build()
```

**Examples:**
- `"/crashes"`
- `"/api/crashes"`
- `"/v1/reports"`
- `"/crash-reports/upload"`

**Note:** 
- The endpoint should start with `/`. The library will automatically prepend `/` if you forget it.
- If `apiEndpoint` is missing, debug warnings will be shown and no API calls will be made.

**Full URL Construction:**
The library constructs the full URL as: `baseUrl + apiEndpoint`
- Base URL: `"https://api.example.com"`
- Endpoint: `"/crashes"`
- Full URL: `"https://api.example.com/crashes"` ✅

### Security

- **TLS 1.2+ is always enforced** for all HTTPS connections
- HTTP connections are automatically rejected in production
- Only localhost HTTP is allowed for development purposes
- This ensures HIPAA compliance for encryption in transit

### Headers

All headers specified in configuration are added to every network request. **Headers are automatically persisted securely** across app restarts using Android Keystore-backed encryption.

#### Automatic Header Persistence

**✅ Developer-Friendly:** Headers are automatically saved and restored - no need to set them in multiple places!

- **Set once after login:** Headers are automatically saved securely
- **Persist across restarts:** Headers are automatically loaded on next app launch
- **Secure storage:** Headers are encrypted using Android Keystore (hardware-backed when available)
- **Automatic merge:** New headers override old ones, but persisted headers remain if not overridden

**Example - Set headers after login (they persist automatically):**
```kotlin
// After user login
fun onLoginSuccess(jwtToken: String) {
    val config = CrashReporterConfig.Builder()
        .baseUrl("https://api.example.com")
        .apiEndpoint("/crashes")
        .addHeader("Authorization", "Bearer $jwtToken")  // Saved automatically
        .addHeader("X-User-ID", getCurrentUserId())      // Saved automatically
        .build()
    
    CrashReporter.updateConfiguration(config)
    // Headers are now saved securely and will be available on next app launch!
}
```

**On next app launch:**
- Headers are automatically loaded from secure storage
- No need to set them again unless they change
- API calls work immediately without re-authentication

#### Clear Headers on Logout

When user logs out, clear persisted headers:

```kotlin
fun onLogout() {
    CrashReporter.clearHeaders(context)  // Clears all persisted headers
    // ... other logout logic
}
```

#### Common Header Use Cases

- **Authorization**: `"Bearer <jwt_token>"` - Automatically persists across restarts
- **API Keys**: `"X-API-Key"` - Set once, persists automatically
- **User Identification**: `"X-User-ID"` - Update when user changes
- **App Version**: `"X-App-Version"` - Can be set once during initialization

#### Security

- **Encrypted Storage:** Headers stored using `EncryptedSharedPreferences` (Android Keystore-backed)
- **Hardware Security:** Uses hardware-backed keys when available (on supported devices)
- **Automatic Cleanup:** Headers are automatically deleted when app is uninstalled
- **No Plain Text:** Headers are never stored in plain text

## How It Works

1. **Crash Occurs** → Stack trace sanitized (if enabled) → Encrypted crash log saved to local file
2. **Worker Scheduled** → `CrashLogWorker` processes files and stores in Room database
   - File deleted immediately after database write
   - Old logs cleaned up based on retention policy
3. **Upload Worker** → `CrashUploadWorker` uploads logs to server via TLS 1.2+ (when configured)
4. **Cleanup** → Successfully uploaded logs are deleted from database
5. **Retention** → Logs older than retention period automatically deleted

### Flow Diagram

```
Crash Occurs
    ↓
Sanitize Stack Trace (if enabled)
    ↓
Encrypt with Android Keystore Key
    ↓
Save to Encrypted File
    ↓
Worker Processes File → Store in Encrypted Database → Delete File
    ↓
Retention Check → Delete Old Logs (>90 days)
    ↓
Upload via TLS 1.2+ (when configured)
    ↓
Delete from Database (after successful upload)
```

## AndroidManifest Setup

You need to disable WorkManager auto-initialization:

```xml
<application>
    <!-- Disable WorkManager auto-initialization -->
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
</application>
```

## Important Notes

1. **No Default URL**: The library doesn't require a default URL. Initialize without config and update later.

2. **Configuration Updates**: Configuration can be updated anytime using `updateConfiguration()`. The network client is recreated with new settings.

3. **Pending Logs**: Crash logs stored before configuration will be uploaded once configuration is provided.

4. **Thread Safety**: All configuration updates are thread-safe.

5. **Retry Logic**: Failed uploads are automatically retried by WorkManager.

6. **Local Storage**: Crash logs are stored locally in encrypted format, so they persist even if upload fails.

## Troubleshooting

### Crash logs not uploading

- Ensure `updateConfiguration()` has been called with a valid base URL
- Check network connectivity
- Verify server endpoint is accessible
- Check logs for upload errors

### Workers not executing

- Ensure WorkManager auto-initialization is disabled in manifest
- Verify `CrashReporter.initialize()` is called in Application.onCreate()
- Check that app has INTERNET permission

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

**Free to Use**: This library is free to use, modify, and distribute in both commercial and non-commercial projects.

**Attribution**: When using this library, please include attribution to the original developer in your application's credits, about page, or documentation.

### Quick License Summary

- ✅ **Free to use** in commercial and non-commercial projects
- ✅ **Free to modify** to suit your needs
- ✅ **Free to distribute** in your applications
- ✅ **Attribution required** - Please credit the original developer
- ❌ **No warranty** - Software is provided "as is"

For full license terms, see [LICENSE](LICENSE) file.

## Important Security & Compliance Notice

### HIPAA Compliance Status

⚠️ **This library is NOT fully HIPAA compliant yet.** While it includes many security features (encryption, sanitization, secure key storage), it is missing critical requirements:
- Audit logging
- User consent mechanisms
- Certificate pinning
- Breach detection

**You MUST verify compliance** before using in HIPAA-regulated environments. See [HIPAA_CHECKLIST.md](mycrashlytics/HIPAA_CHECKLIST.md) for current status.

### Security Benefits

This library provides **better security than third-party crash reporting services** because:
- ✅ **Your own server** - Crash data goes to YOUR server, not a third party
- ✅ **No data sharing** - You control where crash reports are stored
- ✅ **Full control** - You can implement additional security measures on your server
- ✅ **No vendor lock-in** - You own the data and infrastructure

### Verification Required

**Before using in production, especially for healthcare/HIPAA applications:**
1. ✅ Review the [HIPAA Compliance Checklist](mycrashlytics/HIPAA_CHECKLIST.md)
2. ✅ Verify your server implementation meets HIPAA requirements (your responsibility)
3. ✅ Implement missing library compliance features if needed
4. ✅ Consult with legal/compliance experts
5. ✅ Test thoroughly in your environment

**Important:** This library only handles **client-side** security and compliance. **Server-side compliance** (BAA, server encryption, access controls, audit logging, etc.) is **your responsibility** and must be implemented separately on your server.

**The library developer assumes NO liability** for HIPAA violations or security breaches. Use at your own risk.

