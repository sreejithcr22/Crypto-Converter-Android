# Keep source file names + line numbers for Play crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations/signatures used by Retrofit, Gson, Room.
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Gson: models are (de)serialized via reflection.
-keep class com.codit.cryptoconverter.model.** { *; }
-keep class com.codit.cryptoconverter.data.remote.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit: keep service interfaces and their annotated methods.
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Room: entities, DAOs, database (on top of rules bundled with room-runtime).
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# DataStore preferences serializer.
-keep class androidx.datastore.** { *; }
