# App ProGuard Rules
# The library's consumer-rules.pro is automatically applied

# ============================================================================
# Keep App-Specific Classes
# ============================================================================

# Keep app's main classes
-keep class com.example.crashreportingdemo.** { *; }

# ============================================================================
# Hilt Application
# ============================================================================

-keep class * extends android.app.Application
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }

# ============================================================================
# Stack Traces
# ============================================================================

# Keep source file names for stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================================
# Retrofit & OkHttp (Additional app-level rules)
# ============================================================================

-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# ============================================================================
# Gson
# ============================================================================

-keepattributes Signature
-keepattributes *Annotation*

# ============================================================================
# Kotlin
# ============================================================================

-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations

# ============================================================================
# Enum Classes
# ============================================================================

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================================================
# Warnings Suppression
# ============================================================================

-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn javax.annotation.**
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ============================================================================
# Optimization Settings
# ============================================================================

# Enable optimization (required for -assumenosideeffects to work)
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# ============================================================================
# Remove Logging in Release Builds
# ============================================================================

# Remove all Log statements in release builds for security and performance
# This MUST be in the app's proguard file for R8 to strip logs
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
    public static boolean isLoggable(java.lang.String, int);
}

# Remove println statements
-assumenosideeffects class java.io.PrintStream {
    public void println(...);
    public void print(...);
}

# Remove printStackTrace() calls
-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
    public void printStackTrace(java.io.PrintStream);
}

