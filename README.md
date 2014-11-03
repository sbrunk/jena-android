# Jena for Android

This project aims to make the [Apache Jena](http://jena.apache.org/) Framework usable on Android. While Jena is written in pure Java, it can't be used as is due to some package conflict issues.

## Background
The main issue is that Jena contains code that refers to classes from the javax.xml namespace that aren't part of the Dalvik Virtual machine. The classes are actually provided by [xerces](https://xerces.apache.org/) but the dalvik compiler refuses to build anything in core java namespaces as it could, in theory, be added to future Android versions causing conflicts. You could use the `--core-library` option of the dex tool, but according to the dex documentation:
   
> the path you are on will ultimately lead to pain, suffering, grief, and lamentation.
   
It's also not very portable as everyone has to change the arguments to the dexer.

A similar issue is caused by a version conflict in the Apache [httpclient](https://hc.apache.org/httpcomponents-client-ga/index.html) library because Android ships a ancient version of it but Jena currently requires version 4.2. The [httpclient website](https://hc.apache.org/httpcomponents-client-4.3.x/android-port.html) has more information about it.

The solution is to relocate all elements from conflicting namespaces to a different package and change all code referencing those elements accordingly. Fortunately, there are tools for doing that automatically. We use the [Maven Shade](https://maven.apache.org/plugins/maven-shade-plugin/) plugin for that, specifically the [relocation feature](https://maven.apache.org/plugins/maven-shade-plugin/examples/class-relocation.html).

## Usage

Create modified Jena packages:

```bash
$ cd jena-android-parent
$ mvn install
```

This will install modified Jena packages that run on Android into your local maven repository. Right now, it creates packages for the jena-core and jena-arq modules. All dependencies that rely on things from javax.xml (e.g. jena-iri and the xerces xml library) are shaded into those jars and the dependencies to the original libraries are removed. All other dependencies are **not** included but are kept as dependency in the modified pom file.

Now, in your Android project, add a dependency to the library. E.g. if you use gradle add the following to your build.gradle:

```groovy
repositories {
    ...
    mavenLocal()
}

dependencies {
    ...
    compile 'mobi.seus.jena:jena-arq-android:2.12.1'
}
```

If you want to use it in a team you should deploy it to your maven repository or add the jars as unmanaged dependencies.

TODO howto unmanaged dependencies

Jena and its dependencies reference a lot of methods, so you will reach the 64k method limit for Android's dex files quite soon. To work around this limitation, you either have to:

  1. Run Proguard on every build (even on development builds).
  2. Use the new multi dex class loader that is available since Android 5.0. See https://github.com/casidiablo/multidex for details.