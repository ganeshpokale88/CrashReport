# CrashReporter - Self-Hosted Android Crash Reporting Library

> **CrashReporter** is a self-contained, open-source Android crash reporting library with built-in HIPAA compliance features. Automatically captures, encrypts, and uploads crash reports to your own server. Perfect alternative to commercial crash reporting services for healthcare apps, enterprise applications, and privacy-conscious developers.

## What does CrashReporter do?
CrashReporter automatically captures both **fatal and non-fatal** crashes in Android applications, encrypts the crash data locally, stores it securely, and uploads it to your own server endpoint. It provides **enterprise-grade crash analytics** without relying on third-party cloud services.

> **Keywords:** Android crash reporting, self-hosted crash analytics, HIPAA compliant crash reporting, commercial crash reporting alternative, Android error tracking, crash reporting library, open source crash reporting, healthcare app crash reporting, secure crash reporting, Android crash logging, custom crash reporting, privacy-first crash reporting, Android exception tracking, crash analytics library, self-hosted error tracking, Android crash monitoring, secure error reporting, HIPAA crash reporting, Android crash collection, enterprise crash reporting

## What is CrashReporter?
CrashReporter is an open-source, self-hosted Android crash reporting library that provides enterprise-grade crash analytics without relying on third-party services. Unlike commercial cloud-based crash reporting solutions, CrashReporter gives you **complete data ownership**, **built-in HIPAA compliance features**, and **zero subscription costs**.

### 🎯 Best for:
*   **Healthcare apps**
*   **Enterprise applications**
*   **Privacy-conscious developers**
*   **Regulated industries**
*   Anyone who wants **full control** over their crash data

## How does CrashReporter work?

1.  **App crashes** → Library captures stack trace and device info
2.  **Data sanitized** (if PHI sanitization enabled) → Removes sensitive information
3.  **Encrypted and stored locally** → AES-256-GCM encryption with Android Keystore
4.  **Background worker processes** → Moves to encrypted SQLCipher database
5.  **Uploaded to your server** → Via TLS 1.2+ with optional certificate pinning
6.  **Auto-cleanup** → Old logs deleted based on retention policy

### 🛠️ Technology Stack
`Kotlin` • `WorkManager` • `Room Database` • `SQLCipher` • `Retrofit` • `OkHttp` • `Android Keystore` • `AES-256-GCM encryption`

> 📖 **For detailed documentation**, see [README_LONG.md](README_LONG.md)

## Features

### Core Functionality
- ✅ **Fatal & Non-Fatal Crash Reporting** - Automatic capture of both crash types
- ✅ **Automatic Upload** - Background workers upload crash logs to your server
- ✅ **Local Storage** - Encrypted crash logs stored locally before upload
- ✅ **Retry Logic** - Automatic retry on upload failures
- ✅ **WorkManager Integration** - Reliable background processing even after app crashes
- ✅ **Room Database** - Efficient local storage with SQLCipher encryption
- ✅ **Dynamic Configuration** - Update base URL and headers at runtime
- ✅ **Automatic Header Persistence** - Headers saved securely across app restarts

### Security & Compliance
- ✅ **HIPAA Compliance Features** - Built-in security for healthcare applications
- ✅ **PHI Sanitization** - Automatic redaction of sensitive data from stack traces
- ✅ **TLS 1.2+ Enforcement** - Always enabled, HTTP blocked in production
- ✅ **Encryption at Rest** - AES-256-GCM encryption for crash log files
- ✅ **Database Encryption** - SQLCipher with Android Keystore-backed keys
- ✅ **Secure Key Storage** - All keys stored using Android Keystore (hardware-backed)
- ✅ **Certificate Pinning** - Optional MITM protection with backup pins
- ✅ **Data Retention Policies** - Auto-delete old logs (default: 90 days, configurable)
- ✅ **Code Obfuscation** - R8/ProGuard rules for secure release builds
- ✅ **Logging Disabled** - All logs removed in release builds

### Developer Experience
- ✅ **Zero External Dependencies** - Self-contained, no Hilt or app-level setup required
- ✅ **Simple API** - Easy to integrate and use
- ✅ **Flexible Configuration** - Initialize with or without configuration
- ✅ **Custom Endpoints** - Configure your own API endpoint path
- ✅ **No Vendor Lock-in** - You own and control all data

## Why Choose CrashReporter Over Commercial Solutions?

**CrashReporter** is a self-hosted alternative to commercial cloud-based crash reporting services. Unlike third-party solutions, CrashReporter gives you complete control over your crash data, eliminates vendor lock-in, and provides built-in HIPAA compliance features that many commercial services lack.

### 🏆 Key Advantages

| Feature | CrashReporter | Commercial Services |
|---------|---------------|---------------------|
| **Data Ownership** | ✅ You own all data | ❌ Service provider owns data |
| **Data Location** | ✅ Your server | ❌ Third-party servers |
| **HIPAA Compliance** | ✅ Built-in features | ⚠️ Limited support |
| **PHI Sanitization** | ✅ Automatic | ❌ Not available |
| **Cost** | ✅ Free (self-hosted) | 💰 Subscription fees |
| **Vendor Lock-in** | ✅ None | ❌ Platform dependency |
| **Privacy** | ✅ Full control | ⚠️ Third-party privacy policies |
| **Customization** | ✅ Full control | ⚠️ Limited options |
| **Offline Support** | ✅ Works offline | ✅ Works offline |
| **SDK Size** | ✅ Lightweight | ⚠️ Larger SDKs |

### 🎯 Perfect For

- **Healthcare Applications** - Built-in HIPAA compliance features
- **Enterprise Apps** - Full data ownership and control
- **Privacy-Conscious Apps** - No third-party data sharing
- **Regulated Industries** - Compliance-ready security features
- **Cost-Sensitive Projects** - Free, self-hosted solution
- **Custom Requirements** - Full control over data and infrastructure

### 💡 Benefits

1. **Complete Data Ownership** - Your data stays on your servers, not third-party platforms
2. **No Vendor Lock-in** - Switch servers anytime without losing functionality
3. **HIPAA-Ready** - Built-in PHI sanitization, encryption, and security features
4. **Cost Effective** - No per-user fees, no subscription costs
5. **Privacy First** - No data shared with third-party services or cloud providers
6. **Full Control** - Customize endpoints, headers, retention policies, and more
7. **Transparent** - Open source, inspect and verify all security measures
8. **Lightweight** - No heavy SDK dependencies, minimal app size impact

## Installation

### Gradle Setup

Add CrashReporter to your Android project:

```kotlin
dependencies {
    implementation(project(":crashreport"))
}
```

### Requirements

- **Minimum SDK:** Android API 21 (Android 5.0 Lollipop)
- **Target SDK:** Android API 34+ (recommended)
- **Kotlin:** 1.9.0 or higher
- **Java:** 11 or higher

### Dependencies Included

CrashReporter includes all necessary dependencies:
- WorkManager (background processing)
- Room Database (local storage)
- SQLCipher (database encryption)
- Retrofit & OkHttp (networking)
- Android Security Crypto (key storage)
- Kotlin Coroutines (async operations)

## Quick Start

### 1. AndroidManifest Setup

Disable WorkManager auto-initialization:

```xml
<application>
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

### 2. Initialize in Application

**Recommended - With Configuration:**
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")  // Required
            .apiEndpoint("/crashes")              // Required
            .enableSanitization()                 // Optional: HIPAA compliance
            .build()
        
        CrashReporter.initialize(this, config)
    }
}
```

**Alternative - Without Configuration:**
```kotlin
// Initialize without config (update later after login)
CrashReporter.initialize(this)

// Later, after login:
val config = CrashReporterConfig.Builder()
    .baseUrl("https://api.example.com")
    .apiEndpoint("/crashes")
    .addHeader("Authorization", "Bearer $jwtToken")  // Auto-persisted!
    .build()
CrashReporter.updateConfiguration(config)
```

### 3. Report Crashes

**Non-Fatal:**
```kotlin
try {
    // Your code
} catch (e: Exception) {
    CrashReporter.getInstance().reportNonFatalCrash(e)
}
```

**Fatal:** Automatically captured - no code needed!

## Configuration Guide

Configure CrashReporter to work with your server. The library supports flexible configuration options for different use cases including healthcare apps, enterprise applications, and privacy-focused projects.

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `baseUrl` | Base URL (without path) | `"https://api.example.com"` |
| `apiEndpoint` | Endpoint path | `"/crashes"` |

### Optional Properties

| Property | Description | Default |
|----------|-------------|---------|
| `headers` | HTTP headers (auto-persisted) | `emptyMap()` |
| `sanitizationConfig` | PHI sanitization config | `null` (disabled) |
| `dataRetentionDays` | Auto-delete after N days | `90` |
| `certificatePins` | SSL certificate pins | `null` (disabled) |

### Common Examples

**With Authentication (Headers Auto-Persist):**
```kotlin
val config = CrashReporterConfig.Builder()
    .baseUrl("https://api.example.com")
    .apiEndpoint("/crashes")
    .addHeader("Authorization", "Bearer $token")  // Saved automatically!
    .addHeader("X-User-ID", userId)
    .build()
```

**HIPAA-Compliant Setup:**
```kotlin
val config = CrashReporterConfig.Builder()
    .baseUrl("https://api.example.com")
    .apiEndpoint("/crashes")
    .enableSanitization()  // Redacts PHI from stack traces
    .dataRetentionDays(90)
    .addCertificatePins(listOf(
        "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",  // Current
        "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="   // Backup
    ))
    .build()
```

**Custom Sanitization:**
```kotlin
.sanitizationConfig(
    patientNames = listOf("John Doe", "Jane Smith"),
    redactEmails = true,
    redactPhones = true,
    redactSSNs = true
)
```

## Header Persistence

Headers are **automatically saved securely** and persist across app restarts:

```kotlin
// Set once after login
CrashReporter.updateConfiguration(config)
// Headers are now saved and will work on next app launch!

// Clear on logout
CrashReporter.clearHeaders(context)
```

## Server Requirements

Your server needs a **POST** endpoint that accepts:

**Request:**
- **URL:** `{baseUrl}{apiEndpoint}`
- **Method:** `POST`
- **Content-Type:** `application/json`
- **Body:** Array of crash reports

```json
[
  {
    "timeStamp": "2024-01-15T10:30:45.123Z",
    "stackTrace": "java.lang.RuntimeException...",
    "androidVersion": "13",
    "deviceMake": "Google",
    "deviceModel": "Pixel 6",
    "isFatal": true
  }
]
```

**Response:** HTTP `200` or `201` for success

## Certificate Pinning

**Get SHA-256 Pin:**
```bash
openssl s_client -servername api.example.com -connect api.example.com:443 < /dev/null | \
  openssl x509 -pubkey -noout | \
  openssl pkey -pubin -outform der | \
  openssl dgst -sha256 -binary | \
  openssl enc -base64
```

**Enable:**
```kotlin
.addCertificatePins(listOf(
    "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",  // Current
    "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="   // Backup
))
```

## Security Features

CrashReporter implements multiple layers of security to protect crash data:

- **TLS 1.2+** - Always enforced for HTTPS connections, HTTP blocked in production
- **Encrypted Storage** - AES-256-GCM encryption for crash log files, Android Keystore-backed keys
- **Database Encryption** - SQLCipher encryption for local database storage
- **PHI Sanitization** - Automatic redaction of sensitive data (emails, SSNs, phone numbers, patient names)
- **Data Retention** - Auto-delete old logs (default: 90 days, configurable)
- **Code Obfuscation** - R8/ProGuard rules for secure release builds
- **Certificate Pinning** - Optional SHA-256 certificate pinning for MITM protection
- **Secure Key Storage** - All encryption keys stored using Android Keystore (hardware-backed when available)
- **No Plain Text Storage** - All sensitive data encrypted, no plain text storage anywhere
- **Logging Disabled** - All debug logs removed in release builds for security

**Security Standards:** CrashReporter follows industry best practices for secure data handling, encryption, and key management. Suitable for applications requiring HIPAA, SOC 2, or other compliance standards.

## Frequently Asked Questions (FAQ)

### Is CrashReporter HIPAA compliant?
CrashReporter includes HIPAA-compliant security features like PHI sanitization, encryption at rest, TLS 1.2+ enforcement, and data retention policies. However, full HIPAA compliance also requires proper server-side implementation, which is your responsibility.

### How does CrashReporter compare to commercial crash reporting services?
Unlike commercial cloud-based services, CrashReporter gives you complete data ownership, no vendor lock-in, built-in PHI sanitization, and works with your own server. See the comparison table above for details.

### Can I use CrashReporter instead of commercial services?
Yes! CrashReporter is a perfect alternative to commercial crash reporting platforms that provides similar functionality while giving you full control over your data and infrastructure. No subscription fees, no data sharing with third parties.

### Is CrashReporter free?
Yes, CrashReporter is completely free and open source. You only need to host your own server endpoint to receive crash reports.

### Does CrashReporter work offline?
Yes, crash logs are stored locally in encrypted format and automatically uploaded when the device comes online.

### Can I customize the API endpoint?
Yes, you can configure any endpoint path using the `apiEndpoint` parameter in `CrashReporterConfig`.

### How secure is CrashReporter?
CrashReporter uses AES-256-GCM encryption, Android Keystore for key storage, SQLCipher for database encryption, TLS 1.2+ for network communication, and optional certificate pinning for MITM protection.

### Does CrashReporter sanitize PHI?
Yes, CrashReporter includes automatic PHI sanitization that redacts emails, phone numbers, SSNs, patient names, and other sensitive data from stack traces.

### What Android versions does CrashReporter support?
CrashReporter supports Android API level 21 (Android 5.0) and above, covering the vast majority of active Android devices.

### How do I integrate CrashReporter into my Android app?
Integration is simple: add the dependency, initialize in your Application class, and optionally configure base URL and endpoint. See the Quick Start section above for detailed steps.

### Can CrashReporter handle both fatal and non-fatal crashes?
Yes, CrashReporter automatically captures fatal crashes and provides an API to report non-fatal exceptions. Both types are encrypted, stored locally, and uploaded to your server.

### Does CrashReporter require internet connection?
No, CrashReporter works offline. Crash logs are stored locally and automatically uploaded when network connectivity is available.

### How do I get started with CrashReporter?
1. Add the library dependency to your project
2. Configure AndroidManifest.xml (disable WorkManager auto-init)
3. Initialize in your Application class
4. Set up your server endpoint to receive crash reports
5. Optionally configure sanitization, certificate pinning, and other features

### What encryption does CrashReporter use?
CrashReporter uses AES-256-GCM for file encryption, SQLCipher for database encryption, and Android Keystore for secure key storage. All encryption is hardware-backed when available.

### Can I use CrashReporter for production apps?
Yes, CrashReporter is production-ready with features like code obfuscation, logging disabled in release builds, and comprehensive error handling.

### How much does CrashReporter increase app size?
CrashReporter is lightweight and adds minimal size to your app. The library uses efficient dependencies and doesn't include heavy SDKs.

### Does CrashReporter support certificate pinning?
Yes, CrashReporter supports SHA-256 certificate pinning with backup pins for certificate rotation. This provides additional protection against MITM attacks.

### What data does CrashReporter collect?
CrashReporter collects crash stack traces, device information (Android version, device make/model), timestamps, and crash type (fatal/non-fatal). All data can be sanitized to remove PHI before upload.

### How do I update CrashReporter configuration at runtime?
Use `CrashReporter.updateConfiguration()` to update base URL, endpoint, headers, or other settings at runtime. This is useful when authentication tokens become available after user login.

## Troubleshooting

**Crash logs not uploading?**
- Check `baseUrl` and `apiEndpoint` are set
- Verify server endpoint is accessible
- Check logs for warnings
- Ensure network connectivity

**Headers not persisting?**
- Headers are saved when `updateConfiguration()` is called
- Use `clearHeaders()` to remove them
- Headers are automatically loaded on app restart

**Configuration not working?**
- Both `baseUrl` and `apiEndpoint` are required
- Check for debug warnings in logcat
- Verify configuration is set before crashes occur

## Use Cases & Examples

### Healthcare Applications
CrashReporter is ideal for healthcare apps requiring HIPAA compliance. Built-in PHI sanitization automatically redacts sensitive patient information from crash reports. Perfect for telemedicine apps, EHR systems, patient portals, and medical device applications.

**Example Use Case:** A telemedicine app uses CrashReporter to track crashes while ensuring patient data (SSNs, emails, phone numbers) in stack traces are automatically redacted before upload to their HIPAA-compliant server.

### Enterprise Applications
Enterprise apps benefit from complete data ownership, no vendor lock-in, and full control over crash data storage and processing. Ideal for B2B applications, internal tools, and enterprise mobile solutions.

**Example Use Case:** An enterprise app needs crash reporting but cannot use third-party services due to data residency requirements. CrashReporter allows them to host crash data on their own infrastructure in their required geographic region.

### Privacy-Conscious Apps
Apps that prioritize user privacy can use CrashReporter to ensure crash data never leaves their infrastructure or gets shared with third parties. Perfect for messaging apps, password managers, and privacy-focused applications.

**Example Use Case:** A privacy-focused messaging app uses CrashReporter to track crashes without sharing any data with external analytics providers, maintaining their privacy-first brand promise.

### Regulated Industries
Financial, healthcare, and other regulated industries can use CrashReporter to meet compliance requirements while maintaining full control. Suitable for banking apps, insurance applications, and government software.

**Example Use Case:** A banking app requires crash reporting that meets financial regulations. CrashReporter provides encrypted storage, data retention policies, and full audit trail capabilities.

### Open Source Projects
Open source projects can use CrashReporter without vendor lock-in or subscription costs. Perfect for community-driven applications and non-profit projects.

### Cost-Sensitive Projects
Projects with budget constraints can use CrashReporter as a free, self-hosted alternative to paid crash reporting services. Only requires hosting your own server endpoint.

## Related Projects & Alternatives

Looking for crash reporting solutions? Here are common approaches:

- **Commercial Cloud Services** - Third-party crash reporting platforms (vendor lock-in, data on provider servers, subscription fees, limited HIPAA features)
- **Self-Hosted Solutions** - Open-source alternatives that give you full control
- **CrashReporter** - Self-hosted, open-source solution with HIPAA compliance features, complete data ownership, and no vendor lock-in

**Why choose CrashReporter?** Unlike commercial cloud services, CrashReporter gives you full control, built-in HIPAA features, zero subscription costs, and complete privacy. All data stays on your infrastructure.

**Note:** This library is not affiliated with, endorsed by, or associated with any commercial crash reporting service providers. All product names mentioned are trademarks of their respective owners.

## Contributing

Contributions are welcome! This is an open-source project designed to give developers a privacy-first, self-hosted alternative to commercial crash reporting services.

## License

MIT License - See [LICENSE](LICENSE) file for details.

**⚠️ Important:** This library provides security features but **does not guarantee HIPAA compliance**. Server-side compliance is your responsibility. Verify all security measures before use in production.

---

## Technical Specifications

- **Language:** Kotlin
- **Minimum Android Version:** API 21 (Android 5.0)
- **Architecture:** MVVM-friendly, works with any architecture
- **Threading:** Coroutines-based, non-blocking operations
- **Storage:** Room Database with SQLCipher encryption
- **Networking:** Retrofit with OkHttp, TLS 1.2+ required
- **Encryption:** AES-256-GCM for files, SQLCipher for database
- **Key Storage:** Android Keystore (hardware-backed when available)
- **Background Processing:** WorkManager for reliable task execution
- **Code Obfuscation:** R8/ProGuard support for release builds

## Search Terms & Discoverability

**Common Search Queries:**
- "Android crash reporting library"
- "Self-hosted crash analytics"
- "HIPAA compliant crash reporting Android"
- "Open source crash reporting"
- "Android error tracking library"
- "Privacy-first crash reporting"
- "Enterprise crash reporting Android"
- "Secure crash reporting library"
- "Custom crash reporting Android"
- "Android crash logging library"

**Tags:** `android` `crash-reporting` `error-tracking` `hipaa-compliance` `self-hosted` `open-source` `commercial-alternative` `privacy` `security` `encryption` `crash-analytics` `android-library` `kotlin` `workmanager` `room-database` `sqlcipher` `aes-256` `tls` `certificate-pinning` `phi-sanitization` `enterprise` `healthcare` `compliance`

---

**Legal Disclaimer:** Product names and trademarks mentioned in this README are the property of their respective owners. This library is not affiliated with, endorsed by, or sponsored by any commercial crash reporting service provider. Comparisons are made for informational purposes only.
