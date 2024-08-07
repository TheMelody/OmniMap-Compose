# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
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

-keep class android.support.v4.** {*;}
-dontwarn android.support.v4.**
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

-keep class com.melody.map.tencent_compose.model.** {*;}
-dontwarn com.melody.map.tencent_compose.model.**
-keep class com.melody.map.tencent_compose.poperties.** {*;}
-dontwarn com.melody.map.tencent_compose.poperties.**

-keep class com.tencent.tencentmap.**{*;}
-keep class com.tencent.map.**{*;}
-keep class com.tencent.beacontmap.**{*;}
-keep class navsns.**{*;}
-dontwarn com.qq.**
-dontwarn com.tencent.**