# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in $ANDROID_HOME/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontobfuscate
-dontoptimize

-dontwarn com.github.jsonldjava.utils.**
-dontwarn com.hp.hpl.jena.sparql.mgt.ContextMBean
-dontwarn org.apache.html.dom.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.thrift.server.**
-dontwarn org.apache.xerces.**
-dontwarn com.fasterxml.jackson.databind.ext.**
-dontwarn com.github.jsonldjava.core.**
-dontwarn com.hp.hpl.jena.rdf.model.AnonId
-dontwarn com.hp.hpl.jena.sparql.mgt.ARQMgt_X
-dontwarn mobi.seus.org.apache.http.impl.auth.**
-dontwarn mobi.seus.org.apache.http.impl.client.cache.**
-dontwarn org.apache.thrift.transport.**
-dontwarn org.apache.xml.serialize.**
-dontwarn org.w3c.dom.html.HTMLDOMImplementation

# TDB
-dontwarn com.hp.hpl.jena.tdb.sys.ProcessUtils

# keep classes that are used but only instantiated reflectively
-keep public class org.apache.xerces.impl.dv.dtd.DTDDVFactoryImpl

# logback
-keep class ch.qos.** { *; }
-keep class org.slf4j.** { *; }
-keepattributes *Annotation*
-dontwarn ch.qos.logback.core.net.*

#-ignorewarnings