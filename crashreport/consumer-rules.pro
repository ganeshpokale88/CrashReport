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

# DeviceInfo data class
-keep class com.github.ganeshpokale88.crashreporter.DeviceInfo {
    <init>(...);
    <fields>;
    <methods>;
}

# HeaderStorage
-keep class com.github.ganeshpokale88.crashreporter.HeaderStorage {
    <init>(...);
    public <methods>;
}

# ============================================================================
# Keep Library Internal Classes (Required for Functionality)
# ============================================================================

# Room database
-keep @androidx.room.Entity class com.github.ganeshpokale88.crashreporter.database.** { *; }
-keep @androidx.room.Dao interface com.github.ganeshpokale88.crashreporter.database.** { *; }
-keep @androidx.room.Database class com.github.ganeshpokale88.crashreporter.database.** { *; }

# Keep Room generated classes
-keep class com.github.ganeshpokale88.crashreporter.database.**_Impl { *; }

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

# ============================================================================
# Kotlin Data Classes
# ============================================================================

# Keep Kotlin data class generated methods (copy, componentN, etc.)
-keepclassmembers class com.github.ganeshpokale88.crashreporter.** {
    public ** component*();
    public ** copy(...);
    public ** copy$default(...);
}

# ============================================================================
# Kotlin Attributes
# ============================================================================

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# ============================================================================
# Retrofit & OkHttp
# ============================================================================

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Keep Retrofit API interfaces
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep our specific API interface
-keep interface com.github.ganeshpokale88.crashreporter.api.CrashReportApi { *; }

# Retrofit annotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }

# OkHttp platform adapters
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ============================================================================
# Gson
# ============================================================================

-keepattributes Signature
-keepattributes *Annotation*

# Gson serialization
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep classes with @SerializedName
-keep class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Gson TypeAdapters
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ============================================================================
# Security Libraries
# ============================================================================

# Android Security Crypto
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# SQLCipher
-keep class net.zetetic.database.sqlcipher.** { *; }
-dontwarn net.zetetic.database.sqlcipher.**

# Tink (used by AndroidX Security Crypto - EncryptedFile, MasterKey)
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
-keepclassmembers class * extends com.google.crypto.tink.shaded.protobuf.GeneratedMessageLite {
    <fields>;
}

# Protobuf Lite (used by Tink)
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
-dontwarn com.google.protobuf.**

# ============================================================================
# Enum Classes
# ============================================================================

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================================================
# Hilt Dependency Injection
# ============================================================================

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.hilt.**

# ============================================================================
# Coroutines
# ============================================================================

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keep class kotlinx.coroutines.flow.** { *; }

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

