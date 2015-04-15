# Apache Jena for Android

This project aims to make the [Apache Jena](http://jena.apache.org/) Framework usable on Android. While Jena is written in pure Java, it can't be used on Android as is, due to multiple package conflict issues. To overcome these issues, we have created an Android Port enabling developers to use Apache Jena in Android apps. In contrast to other ports we don't just publish binaries, but make the Maven build files of the port available. We also stay as close as possible to the original (i.e. no source code changes).
This should make it easier to stay in sync with upcoming Jena releases.

The Android Port was created at the [Junior Professorship in Software Engineering of Ubiquitous Systems (SEUS)](http://seus.inf.tu-dresden.de/) at the Faculty of Computer Science at the TU Dresden.

## Background
The main issue is that Jena contains code that refers to classes from the javax.xml namespace that aren't part of the Dalvik Virtual machine. The classes are actually provided by [xerces](https://xerces.apache.org/) but the dalvik compiler refuses to build anything in core java namespaces as it could, in theory, be added to future Android versions causing conflicts. You could use the `--core-library` option of the dex tool, but according to the dex documentation:
   
> the path you are on will ultimately lead to pain, suffering, grief, and lamentation.
   
It's also not very portable as everyone has to change the arguments to the dexer.

A similar issue is caused by a version conflict in the Apache [httpclient](https://hc.apache.org/httpcomponents-client-ga/index.html) library because Android ships a ancient version of it but Jena currently requires version 4.2. The [httpclient website](https://hc.apache.org/httpcomponents-client-4.3.x/android-port.html) has more information about it.

The solution is to relocate all elements from conflicting namespaces to a different package and change all code referencing those elements accordingly. Fortunately, there are tools for doing that automatically. We use the [Maven Shade](https://Maven.apache.org/plugins/Maven-shade-plugin/) plugin for that, specifically the [relocation feature](https://Maven.apache.org/plugins/Maven-shade-plugin/examples/class-relocation.html).

External dependencies that rely on things from conflicting namespaces are shaded into modified jars (xerces and httpclient at the moment). The modules that depend on them are rewritten so they depend on the modified version while the dependencies on the original libraries are removed.
Internal dependencies of the Jena modules are overwritten as well, so jena-android-arq (containing the shaded jena-arq) depends on jena-android-core instead of jena-core directly.
All other dependencies are **not** included but are kept as dependency in the modified pom file.

## Usage

Create modified Jena packages:

```bash
$ cd jena-android
$ mvn install
```

This will install modified Jena packages that run on Android into your local Maven repository. At the moment it creates packages for the following Jena modules:

 * jena-core
 * jena-arq
 * jena-iri
 * jena-tdb
 * jena-spatial
 * jena-text

The modified modules are called jena-android-core, jena-android-arq etc.

### Managed Dependencies
If you use a build tool with dependency management such as Gradle, Maven or SBT, you can now just add a dependency to one of the libraries in your Android project.
E.g. if you use gradle add the following to your build.gradle:

```groovy
repositories {
    ...
    MavenLocal()
}

dependencies {
    ...
    compile 'mobi.seus.jena:jena-android-arq:2.13.0'
}

// we have to exclude a few files here that appear in multiple libraries
packagingOptions {
    exclude 'META-INF/LICENSE'
    exclude 'META-INF/NOTICE'
    exclude 'META-INF/DEPENDENCIES'
    exclude 'META-INF/LICENSE.txt'
    exclude 'META-INF/NOTICE.txt'
}
```

If your want to pull in all Jena libraries you should add a dependency on jena-android-jars.

Currently the artifacts are not deployed to a public repository. If you want to use them in a team you should deploy them to your own Maven repository or add the jars as unmanaged dependencies.

### Unmanaged Dependencies

If you don't use a tool for dependency management, you can also create jars including their dependencies with the following command:

```bash
$ cd jena-android
$ mvn package dependency:copy-dependencies
```
For each module this will create a file called `target/module-name.jar` and copy all transitive dependencies into the `target/dependencies/` directory of that module.

Now copy the module jar and the dependency jar into the libs folder of your Android project.
 
E.g. for jena-android-arq you have to copy `jena-android-arq/target/jena-android-arq-2.12.1.jar` and the content from `jena-android-arq/target/dependencies/` to your Android Project. Some of the modules like jena-android-text don't create their own jar. In that case it's enough to just copy the dependencies.

### How to deal with the Dalvik 64K Method Limit

Jena and its dependencies reference a lot of methods, so you will probably reach the 64K method limit for Android's dex files quite soon. 
You will get an error message like the following during compilation:

> Unable to execute dex: method ID not in [0, 0xffff]: 65536
> Conversion to Dalvik format failed: Unable to execute dex: method ID not in [0, 0xffff]: 65536

To work around this limitation, you either have to:

  1. Run Proguard on every build (even on development builds).
  2. Use the new multi dex class loader that is available since Android 5.0. See https://developer.android.com/tools/building/multidex.html for details.
  
Have a look at the example project to see how it can be done.
