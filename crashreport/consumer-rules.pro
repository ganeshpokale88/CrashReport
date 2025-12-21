# Consumer ProGuard Rules for CrashReporter Library
# These rules are automatically applied to apps that use this library

# ============================================================================
# Keep Public API
# ============================================================================

# Main entry point
-keep class com.github.ganeshpokale88.crashreporter.CrashReporter {
    public *;
}

-keep class com.github.ganeshpokale88.crashreporter.CrashReporter$Companion {
    public static <methods>;
}

# Configuration
-keep class com.github.ganeshpokale88.crashreporter.CrashReporterConfig {
    public <init>(...);
    public <fields>;
    public <methods>;
}

-keep class com.github.ganeshpokale88.crashreporter.CrashReporterConfig$Builder {
    public <init>();
    public <methods>;
}

-keep class com.github.ganeshpokale88.crashreporter.CrashReporterConfig$Companion {
    public static <methods>;
}

# Stack trace sanitizer (public utility)
-keep class com.github.ganeshpokale88.crashreporter.StackTraceSanitizer {
    public static <methods>;
}

-keep class com.github.ganeshpokale88.crashreporter.StackTraceSanitizer$SanitizationConfig {
    public <init>(...);
    public <fields>;
}

-keep class com.github.ganeshpokale88.crashreporter.StackTraceSanitizer$Companion {
    public static <methods>;
}

# ============================================================================
# Keep Library Internal Classes (Required for Functionality)
# ============================================================================

# Room database
-keep @androidx.room.Entity class com.github.ganeshpokale88.crashreporter.database.** { *; }
-keep @androidx.room.Dao interface com.github.ganeshpokale88.crashreporter.database.** { *; }
-keep @androidx.room.Database class com.github.ganeshpokale88.crashreporter.database.** { *; }

# WorkManager workers
-keep class com.github.ganeshpokale88.crashreporter.worker.** extends androidx.work.CoroutineWorker {
    <init>(...);
}

-keep class com.github.ganeshpokale88.crashreporter.CrashReporterWorkerFactory {
    <init>();
    public <methods>;
}

# Retrofit API models
-keep class com.github.ganeshpokale88.crashreporter.api.model.** {
    <fields>;
    <init>(...);
}

# Gson annotations
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ============================================================================
# Kotlin
# ============================================================================

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses

# ============================================================================
# Security Libraries
# ============================================================================

# Android Security Crypto
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# SQLCipher
-keep class net.zetetic.database.sqlcipher.** { *; }
-dontwarn net.zetetic.database.sqlcipher.**
