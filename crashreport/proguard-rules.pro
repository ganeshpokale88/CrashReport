# CrashReporter Library - ProGuard/R8 Rules for Secure Code Obfuscation

# ============================================================================
# Keep public API classes
# ============================================================================

# Main entry point - must be kept public
-keep class com.github.ganeshpokale88.crashreporter.CrashReporter {
    public *;
}

-keep class com.github.ganeshpokale88.crashreporter.CrashReporter$Companion {
    public *;
}

# Configuration class - public API
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
    public <methods>;
}

# Stack trace sanitizer - public utility
-keep class com.github.ganeshpokale88.crashreporter.StackTraceSanitizer {
    public <methods>;
}

-keep class com.github.ganeshpokale88.crashreporter.StackTraceSanitizer$SanitizationConfig {
    public <init>(...);
    public <fields>;
}

-keep class com.github.ganeshpokale88.crashreporter.StackTraceSanitizer$Companion {
    public <methods>;
}

# ============================================================================
# Room Database
# ============================================================================

# Keep Room entities
-keep @androidx.room.Entity class com.github.ganeshpokale88.crashreporter.database.** {
    *;
}

# Keep Room DAOs
-keep @androidx.room.Dao interface com.github.ganeshpokale88.crashreporter.database.** {
    *;
}

# Keep Room database
-keep @androidx.room.Database class com.github.ganeshpokale88.crashreporter.database.** {
    *;
}

# Keep Room generated classes
-keep class com.github.ganeshpokale88.crashreporter.database.**_Impl {
    *;
}

-keep class com.github.ganeshpokale88.crashreporter.database.**_RoomDatabase {
    *;
}

# Room reflection
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <methods>;
}

# ============================================================================
# WorkManager
# ============================================================================

# Keep WorkManager workers
-keep class com.github.ganeshpokale88.crashreporter.worker.** extends androidx.work.CoroutineWorker {
    <init>(...);
}

# Keep WorkerFactory
-keep class com.github.ganeshpokale88.crashreporter.CrashReporterWorkerFactory {
    <init>();
    public <methods>;
}

# WorkManager reflection
-keepclassmembers class * extends androidx.work.Worker {
    <init>(...);
}

-keepclassmembers class * extends androidx.work.CoroutineWorker {
    <init>(...);
}

# ============================================================================
# Retrofit & Gson
# ============================================================================

# Keep Retrofit API interfaces
-keep interface com.github.ganeshpokale88.crashreporter.api.** {
    *;
}

# Keep API models (used for serialization)
-keep class com.github.ganeshpokale88.crashreporter.api.model.** {
    <fields>;
    <init>(...);
}

# Gson annotations
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Gson serialization
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ============================================================================
# Hilt Dependency Injection
# ============================================================================

# Keep Hilt generated classes
-keep class com.github.ganeshpokale88.crashreporter.**_HiltModules { *; }
-keep class com.github.ganeshpokale88.crashreporter.**_HiltModules$* { *; }
-keep class com.github.ganeshpokale88.crashreporter.**_HiltModules$*$* { *; }

# Hilt annotations
-keepattributes *Annotation*
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# ============================================================================
# Android Security Crypto (MasterKey, EncryptedFile)
# ============================================================================

# Keep security crypto classes
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# Keep Android Keystore classes
-keep class android.security.keystore.** { *; }
-dontwarn android.security.keystore.**

# ============================================================================
# SQLCipher
# ============================================================================

# Keep SQLCipher classes
-keep class net.zetetic.database.sqlcipher.** { *; }
-dontwarn net.zetetic.database.sqlcipher.**

# SQLCipher native library
-keep class net.zetetic.database.sqlcipher.SupportOpenHelperFactory {
    <init>(...);
}

# ============================================================================
# Coroutines & Flow
# ============================================================================

# Keep coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep Flow
-keep class kotlinx.coroutines.flow.** { *; }

# ============================================================================
# Kotlin
# ============================================================================

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Kotlin data classes used in public API
-keepclassmembers class com.github.ganeshpokale88.crashreporter.** {
    <methods>;
}

# Keep companion objects
-keepclassmembers class * {
    public static ** Companion;
}

# ============================================================================
# Reflection & Serialization
# ============================================================================

# Keep classes accessed via reflection
-keep class com.github.ganeshpokale88.crashreporter.DependencyRegistry {
    *;
}

-keep class com.github.ganeshpokale88.crashreporter.UploadWorkerScheduler {
    *;
}

# ============================================================================
# Security: Obfuscate Internal Implementation
# ============================================================================

# Obfuscate internal implementation classes (not public API)
-assumenosideeffects class com.github.ganeshpokale88.crashreporter.EncryptionUtil {
    private <methods>;
}

-assumenosideeffects class com.github.ganeshpokale88.crashreporter.database.DatabaseKeyManager {
    private <methods>;
}

-assumenosideeffects class com.github.ganeshpokale88.crashreporter.NetworkFactory {
    private <methods>;
}

-assumenosideeffects class com.github.ganeshpokale88.crashreporter.CrashLogProcessor {
    private <methods>;
}

# ============================================================================
# Stack Traces: Keep readable for debugging
# ============================================================================

# Keep source file names for stack traces (but obfuscate)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================================
# General Android
# ============================================================================

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ============================================================================
# Warnings & Optimization
# ============================================================================

# Suppress warnings for libraries
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# ============================================================================
# Remove Logging in Release Builds
# ============================================================================

# Remove all Log statements in release builds for security and performance
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static boolean isLoggable(java.lang.String, int);
}

# Remove printStackTrace() calls that write to stderr (logging)
# Note: printStackTrace(PrintWriter) is kept as it's used for crash reporting
-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
    public void printStackTrace(java.io.PrintStream);
}

# ============================================================================
# Consumer Rules (for library users)
# ============================================================================

# These rules will be applied to apps using this library
# Keep public API accessible to consumers
