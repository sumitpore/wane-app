# ── OpenGL ES / Water Animation Engine ──
# Keep native methods (JNI bridge for OpenGL calls)
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Keep OpenGL renderer classes (referenced via reflection for shader compilation)
-keep class com.wane.app.animation.** { *; }

# Keep GLES classes used by the animation engine
-keep class android.opengl.** { *; }

# ── Compose ──
# Compose compiler generates classes that must not be renamed
-dontwarn androidx.compose.**

# ── Room ──
# Annotation-driven code generation; R8 may strip "unused" constructors
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}

# ── DataStore / Serialization ──
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
}

# Keep kotlinx.serialization generated serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.wane.app.**$$serializer { *; }
-keepclassmembers class com.wane.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.wane.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Kotlin ──
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ── Android Services ──
# Services declared in manifest are auto-kept by AAPT,
# but keep inner classes used by AccessibilityService event handling
-keep class com.wane.app.service.** { *; }

# ── Debug: keep source file names for stack traces ──
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
