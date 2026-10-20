# Add project specific ProGuard rules here.

# Keep Data Models and API interfaces
-keep class com.battery.mantra.data.models.** { *; }
-keep interface com.battery.mantra.data.remote.** { *; }

# Gson Rules
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit Rules
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers enum * { *; }

# OkHttp Rules
-dontwarn okhttp3.**
-dontwarn okio.**

# Firebase Rules
-keepclassmembers class * extends com.google.firebase.messaging.FirebaseMessagingService {
    public <init>();
}
