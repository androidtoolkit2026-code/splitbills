# ProGuard rules for SplitMate

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class com.splitmate.domain.model.** { *; }
-keep class com.splitmate.data.local.entity.** { *; }

# Hilt
-dontwarn com.google.errorprone.annotations.**
