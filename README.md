# Jena for Android

This project aims to make the [Apache Jena](http://jena.apache.org/) Framework usable on Android. While Jena is written in pure Java, it can't be used as is due to some package conflict issues.

## Background
The main issue is that Jena contains code that refers to classes from the javax.xml namespace that aren't part of the Dalvik Virtual machine. The classes are actually provided by [xerces](https://xerces.apache.org/) but the dalvik compiler refuses to build anything in core java namespaces as it could, in theory, be added to future Android versions causing conflicts. You could use the `--core-library` option of the dex tool, but according to the dex documentation:
   
> the path you are on will ultimately lead to pain, suffering, grief, and lamentation.
   
It's also not very portable as everyone has to change the arguments to the dexer.

A similar issue is caused by a version conflict in the Apache [httpclient](https://hc.apache.org/httpcomponents-client-ga/index.html) library because Android ships a ancient version of it but Jena currently requires version 4.2. The [httpclient website](https://hc.apache.org/httpcomponents-client-4.3.x/android-port.html) has more information about it.

The solution is to relocate all elements from conflicting namespaces to a different package and change all code referencing those elements accordingly. Fortunately, there are tools for doing that automatically. We use the [Maven Shade](https://Maven.apache.org/plugins/Maven-shade-plugin/) plugin for that, specifically the [relocation feature](https://Maven.apache.org/plugins/Maven-shade-plugin/examples/class-relocation.html).

## Usage

Create modified Jena packages:

```bash
$ cd jena-android
$ mvn install
```

This will install modified Jena packages that run on Android into your local Maven repository. At the moment it creates packages for the following modules:

 * jena-core
 * jena-arq
 * jena-iri
 * jena-tdb
 * jena-spatial
 * jena-text

External dependencies that rely on things from conflicting namespaces are shaded into a single jar in the module that depends on them, and the dependencies on the original libraries are removed.
All other dependencies are **not** included but are kept as dependency in the modified pom file.
E.g. the jar of jena-android-iri contains xerces and xml-apis as they use or define things in javax.xml, but not slf4j, which is kept as a dependency.
The internal dependencies of the Jena modules are overwritten, so jena-android-arq contains jena-arq but depends on jena-android-core instead of jena-core directly.

Now, in your Android project, add a dependency to the library. E.g. if you use gradle add the following to your build.gradle:

```groovy
repositories {
    ...
    MavenLocal()
}

dependencies {
    ...
    compile 'mobi.seus.jena:jena-android-arq:2.12.1'
}
```

If you want to use it in a team you should deploy it to your Maven repository or add the jars as unmanaged dependencies.

If you need unmanaged dependencies you can also copy all jars including their dependencies into one directory the following way:

 1. Install the modified libraries into your local Maven repository as explained above.
 2.
```bash
$ cd jena-android-jars
$ mvn dependency:copy-dependencies
```

This will copy all modified Jena jars as well as their transitive dependencies into the target/dependencies directory.

Jena and its dependencies reference a lot of methods, so you will reach the 64k method limit for Android's dex files quite soon. To work around this limitation, you either have to:

  1. Run Proguard on every build (even on development builds).
  2. Use the new multi dex class loader that is available since Android 5.0. See https://github.com/casidiablo/multidex for details.