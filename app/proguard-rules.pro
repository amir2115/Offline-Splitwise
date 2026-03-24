-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.RoomDatabase_Impl
-dontwarn androidx.room.paging.**
-keep class kotlin.Metadata { *; }

# Retrofit needs generic signatures and runtime annotations on service interfaces.
-keepattributes Signature, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault, InnerClasses, EnclosingMethod
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep generated kotlinx.serialization serializers and companions used by Retrofit converter.
-keepclassmembers class **$$serializer { *; }
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keep,allowobfuscation class <1>
-if @kotlinx.serialization.Serializable class ** {
    static **$Companion Companion;
}
-keepclassmembers class <1> {
    static **$Companion Companion;
}

# Keep Hilt aggregated metadata in release shrinking.
-keep class dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-dontwarn dagger.hilt.internal.**
