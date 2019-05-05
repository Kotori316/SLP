# Scala language provider for Minecraft 1.13.2

This mod adds Scala library to Minecraft 1.13.2 with Forge.

### Usage

* For Developer - In your `build.gradle`, add below code in top level.

  ```groovy
  dependencies {
      def scala_version = getProperty("scala_version")
      def scala_major = getProperty("scala_major")
  
      minecraft 'net.minecraftforge:forge:1.13.2-25.0.191'
      implementation "org.scala-lang:scala-library:${scala_version}"
  
  }
  ```

  * Properties are set in your `gradle.properties` file or just hardcoded.
  * `scala_version` should be 2.12.8 because this project contains binary of Scala 2.12.8. 
  * `scala_major` must be 2.12.

  If you want to write Mod entry class in Scala, add this jar to dependency.
  And.
  * Set `modloader` in your `mods.toml` file to "kotori_scala". (`modLoader="kotori_scala"`) Loader version is like `loaderVersion="[0,)"`.
  * See [`ScalaMC.scala`](https://github.com/Kotori316/SLP/blob/master/src/main/scala/com/kotori316/scala_lib/ScalaMC.scala) and [`mods.toml`](https://github.com/Kotori316/SLP/blob/master/src/main/resources/META-INF/mods.toml) in this project.
  
  Then you can change mod entry class to Scala Object.

* For Player - Download Jar file from GitHub Release or Curse Forge and move the file to your `mods` folder.