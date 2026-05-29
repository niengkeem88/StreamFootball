-keep class kotlinx.serialization.** { *; }
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**
