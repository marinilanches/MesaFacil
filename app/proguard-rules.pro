# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.JsonSerializable { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Models
-keep class com.example.mesafacil.data.models.** { *; }

# Kotlin
-keep class kotlin.** { *; }

# R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}