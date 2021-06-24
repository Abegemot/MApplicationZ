# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Kotlin serialization looks up the generated serializer classes through a function on companion
# objects. The companions are looked up reflectively so we need to explicitly keep these functions.
#-keepclasseswithmembers class **.*$Companion {
#    kotlinx.serialization.KSerializer serializer(...);
#}
# If a companion has the serializer function, keep the companion field on the original type so that
# the reflective lookup succeeds.
#-if class **.*$Companion {
#  kotlinx.serialization.KSerializer serializer(...);
#}
#-keepclassmembers class <1>.<2> {
#  <1>.<2>$Companion Companion;
#}

#-dontobfuscate

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Change here com.yourcompany.yourpackage
#-keep,includedescriptorclasses class com.begemot.knewscommon**$$serializer { *; } # <-- change package name to your app's
#-keepclassmembers class com.begemot.knewscommon** { # <-- change package name to your app's
#    *** Companion;
#}
#-keepclasseswithmembers class com.begemot.knewscommon** { # <-- change package name to your app's
#    kotlinx.serialization.KSerializer serializer(...);
#}

#-keep,includedescriptorclasses class com.begemot.myapplicationz**$$serializer { *; } # <-- change package name to your app's
#-keepclassmembers class com.begemot.myapplicationz** { # <-- change package name to your app's
#    *** Companion;
#}
#-keepclasseswithmembers class com.begemot.myapplicationz** { # <-- change package name to your app's
#    kotlinx.serialization.KSerializer serializer(...);
#}

-keep,includedescriptorclasses class com.begemot.**$$serializer { *; } # <-- change package name to your app's
-keepclassmembers class com.begemot.** { # <-- change package name to your app's
    *** Companion;
}
-keepclasseswithmembers class com.begemot.** { # <-- change package name to your app's
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class io.ktor.** { *; }