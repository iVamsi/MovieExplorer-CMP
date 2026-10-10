# Kotlin metadata
-keepattributes *Annotation*, InnerClasses, EnclosingMethod

# kotlinx.serialization generated serializers
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class **$$serializer {
    *;
}
-keepclasseswithmembers class **.*$* {
    synthetic <fields>;
}

# Model / API types used with Kotlin Serialization (keep package minimally)
-keep,includedescriptorclasses class com.movieapp.core.model.** { *; }

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.movieapp.**$$serializer { *; }
-keepclassmembers class com.movieapp.** { *** Companion; }
-keepclasseswithmembers class com.movieapp.** { kotlinx.serialization.KSerializer serializer(...); }
-assumenosideeffects class android.util.Log { public static int d(...); public static int v(...); public static int i(...); }
