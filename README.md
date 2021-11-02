# Scala language provider for Minecraft 1.17.1.

Branch 1.17

This mod adds Scala library to Minecraft 1.17.1 with Forge.
**NO COMPATIBILITY WITH 1.16.5 version of SLP.**

[![](http://cf.way2muchnoise.eu/versions/scalable-cats-force.svg)][curse_forge]
[![](http://cf.way2muchnoise.eu/full_scalable-cats-force_downloads.svg)][curse_forge]

## Usage

* For Player - Download Jar file from [Curse Forge][curse_forge] and move the file to your `mods` folder.
This mod appears in mods list with name "slp".

* For Developer  
  See [this repository](https://github.com/Kotori316/SLP-example) for example.

  In your `build.gradle`, add below code in the top-level.

  ```groovy
  dependencies {
      def scala_version = getProperty("scala_version")
      def scala_major = getProperty("scala_major")
  
      // Change forge and minecraft version.
      minecraft 'net.minecraftforge:forge:1.17.1-37.0.0'
      implementation "org.scala-lang:scala-library:${scala_version}"
  }
  ```

  * **If the Minecraft client doesn't launch with an exception to modules, change scala dependency from "implementation" to "compileOnly" and add slp mod in mods directory.**
  * Properties are set in your `gradle.properties` file or just hardcoded like `def scala_version = "2.13.7"`.
  * `scala_version` should be 2.13.7 because this project contains binary of Scala 2.13.7. Make sure your version matches the version this mod provides. See [this file](https://github.com/Kotori316/SLP/blob/1.17/gradle.properties)
  * `scala_major` must be 2.13.
    * Currently, Scala3 is not supported.

### Limitations
In this section, I note some points you should care.

1. Avoid use of `Mod.EventBusSubscriber`. This will cause exception in "compileScala" task.
2. Avoid use of record, which is introduced in Java 14. This will be fixed in 2.13.7. https://github.com/scala/bug/issues/11908
3. Use gradlew to run client.
  * To avoid `java.lang.module.ResolutionException`.
  * You can still use run configuration generated by gradle task after setting "ignoreList" in VM option.
4. Use Java to write mod entry class (@Mod).
5. If you got compile error "ambiguous reference to overloaded definition", specify the return type.
  * For example, `val offsetPos = pos.relative(direction)` will cause this error because `relative` is declared both in `BlockPos` and `Vec3i`, and the return types aren't same. So, the compiler can't determine which method to call. To resolve this issue, specify the return type as follows. `val offsetPos: BlockPos = pos.relative(direction)`

## API
* [Scala](https://www.scala-lang.org/) - [GitHub](https://github.com/scala/scala) - is licenced under the [Apache License, Version 2.0](https://www.scala-lang.org/license/).
* [Cats](https://typelevel.org/cats/) - [GitHub](https://github.com/typelevel/cats) - is licenced under the [Licence](https://github.com/typelevel/cats/blob/master/COPYING).
  * SLP uses [modified version of Cats](https://github.com/Kotori316/cats) to avoid module error.

[curse_forge]: https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force
