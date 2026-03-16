# Add project specific ProGuard rules here.

# Keep Hilt
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel *;
}

# Keep Room entities
-keep class com.airesume.builder.data.database.** { *; }

# Keep Retrofit models
-keep class com.airesume.builder.network.aiService.** { *; }

# Keep Gson models (for TypeConverters)
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Retrofit
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations
-keepattributes EnclosingMethod
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
