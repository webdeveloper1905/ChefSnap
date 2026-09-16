# Kotlinx Serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Serialization Companion Nesneleri ve Serializer Sınıfları
-keepclassmembers class * {
    static final % Companion;
    public static final ** Companion;
}
-keepclasseswithmembers class * {
    public static final **$Companion Companion;
}
-keepnames class * implements kotlinx.serialization.KSerializer { *; }

# Proje Model ve DTO Sınıflarını Koru
-keep class com.yazilim.chefsnap.domain.model.** { *; }
-keep class com.yazilim.chefsnap.data.remote.dto.** { *; }

# Retrofit, OkHttp & RevenueCat
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepclassmembers enum * { *; }