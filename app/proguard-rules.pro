# Retrofit + Gson
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

-keep class com.lasante.tvkiosk.data.remote.** { *; }
-keep class com.lasante.tvkiosk.data.** { *; }

# Filament / SceneView
-keep class com.google.android.filament.** { *; }
-keep class io.github.sceneview.** { *; }
-dontwarn com.google.android.filament.**

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ZXing
-keep class com.google.zxing.** { *; }

# Coil
-dontwarn coil.**

# Compose (release tooling)
-dontwarn androidx.compose.**
