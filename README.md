# Scala language provider for Minecraft 1.16.3.

Branch 1.16

This mod adds Scala library to Minecraft 1.16.3 with Forge.
**NO COMPATIBILITY WITH 1.15.2 version of SLP.**

### Usage

* For Player - Download Jar file from [Curse Forge](https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force) and move the file to your `mods` folder.
This mod doesn't appear in mods list.
This library provides `kotori_scala` language loader.  
[![](http://cf.way2muchnoise.eu/versions/scalable-cats-force.svg)](https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force)
[![](http://cf.way2muchnoise.eu/full_scalable-cats-force_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force)

* For Developer - In your `build.gradle`, add below code in the top-level.

  ```groovy
  dependencies {
      def scala_version = getProperty("scala_version")
      def scala_major = getProperty("scala_major")
  
      // Change forge and minecraft version.
      minecraft 'net.minecraftforge:forge:1.16.3-34.1.33'
      implementation "org.scala-lang:scala-library:${scala_version}"
  
  }
  ```

  * Properties are set in your `gradle.properties` file or just hardcoded like `def scala_version = "2.13.3"`.
  * `scala_version` should be 2.13.3 because this project contains binary of Scala 2.13.3. Make sure your version matches the version this mod provides. See [this file](https://github.com/Kotori316/SLP/blob/1.16/gradle.properties)
  * `scala_major` must be 2.13.

  If you want to write Mod entry class in Scala, add this jar to dependency.
  * Set `modloader` in your `mods.toml` file to "kotori_scala". (`modLoader="kotori_scala"`) Loader version is like `loaderVersion="[2.13.3-build-1,2.14)"`.
  * See [`ScalaModObject.scala`](https://github.com/Kotori316/SLP/blob/1.16/src/main/scala/com/kotori316/scala_lib/example/ScalaModObject.scala) and [`mods.toml`](https://github.com/Kotori316/SLP/blob/1.16/src/main/resources/META-INF/mods.toml) in this project.
  * You can get mod's event bus via `FMLJavaModLoadingContext#get()`, as same the normal java project. `@Mod.EventBusSubscriber`(both FORGE and MOD) for the top-level scala object works fine. Of course, the annotation also works for java codes.

  Then you can change mod entry class to Scala Object.

### API
* [Scala](https://www.scala-lang.org/) - [GitHub](https://github.com/scala/scala) - is licenced under the [Apache License, Version 2.0](https://www.scala-lang.org/license/).
* [Cats](https://typelevel.org/cats/) - [GitHub](https://github.com/typelevel/cats) - is licenced under the [Licence](https://github.com/typelevel/cats/blob/master/COPYING).
