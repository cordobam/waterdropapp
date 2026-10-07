# Trazas de crash legibles (sin esto, R8 ofusca los nombres de archivo/línea)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Retrofit: la interface del service se usa por reflexión ---
-keepattributes Signature
-keepattributes *Annotation*
-keep interface com.example.waterdropapp.data.remote.api.WeatherApi { *; }

# --- Gson: DTOs deserializados por reflectión (Open-Meteo) ---
-keep class com.example.waterdropapp.data.remote.dto.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- Firebase Firestore: POJOs deserializados por reflectión ---
# doc.toObject(Publicacion::class.java), Chat, Oferta, Vivero, UsuarioMarket, Mensaje
-keep class com.example.waterdropapp.data.firebase.model.** { *; }
