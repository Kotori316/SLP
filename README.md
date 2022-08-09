# Scala language provider for Minecraft 1.19.

Branch 1.19

This mod adds Scala library to Minecraft 1.19 with Forge.
**NO COMPATIBILITY WITH 1.18 version of SLP.**

[![](http://cf.way2muchnoise.eu/versions/scalable-cats-force.svg)][curse_forge]
[![](http://cf.way2muchnoise.eu/full_scalable-cats-force_downloads.svg)][curse_forge]

## Usage

* For Player - Download Jar file from [Curse Forge][curse_forge] and move the file to your `mods` folder. This mod will
  not appear in mods list.

* For Developer  
  See [this repository](https://github.com/Kotori316/SLP-example) for example.

  In your `build.gradle`, add below code in the top-level.

  ```groovy
  repositories {
      maven {
          name = "Azure-SLP"
          url = uri("https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1")
          content {
              it.includeModule("com.kotori316", "ScalableCatsForce".toLowerCase())
              it.includeModule("org.typelevel", "cats-core_2.13")
              it.includeModule("org.typelevel", "cats-kernel_2.13")
              it.includeModule("org.typelevel", "cats-free_2.13")
          }
      }
  }

  dependencies {
      def scala_version = getProperty("scala_version")
      def scala_major = getProperty("scala_major")
      // Change forge and minecraft version.
      minecraft 'net.minecraftforge:forge:1.19-41.0.98'
      compileOnly(group: 'org.scala-lang', name: 'scala-library', version: scala_version)
      // Add if you need this library. I use a modified version of Cats to avoid some module errors.
      compileOnly(group: 'org.typelevel', name: "cats-core_${scala_major}", version: '2.8.2-kotori')

      // The language loader. You can put the jar to the mods dir instead of declaring in `build.gradle`.
      // This file is needed as the scala library will not be loaded in dev environment due to change of classpath by Forge.
      runtimeOnly(group: "com.kotori316", name: "ScalableCatsForce".toLowerCase(), version: "2.13.8-build-8", classifier: "with-library") {
          transitive(false)
      }
  }
  ```

  * **If the Minecraft client doesn't launch with an exception to modules, change scala dependency from "implementation"
    to "compileOnly" and add slp mod in mods directory.**
  * Properties are set in your `gradle.properties` file or just hardcoded like `def scala_version = "2.13.8"`.
  * `scala_version` should be 2.13.8 because this project contains binary of Scala 2.13.8. Make sure your version
    matches the version this mod provides. See [this file](https://github.com/Kotori316/SLP/blob/1.19/gradle.properties)
  * `scala_major` must be 2.13.
    * Currently, Scala3 is not supported.

Since 1.19, dependencies are included by Jar in Jar.

### Limitations

In this section, I note some points you should care.

1. Avoid use of `Mod.EventBusSubscriber` in Java code. This will cause exception in "compileScala" task.

* Use in Scala code will not throw exception.

2. If you got compile error "ambiguous reference to overloaded definition", specify the return type.

* For example, `val offsetPos = pos.relative(direction)` will cause this error because `relative` is declared both
  in `BlockPos` and `Vec3i`, and the return types aren't same. So, the compiler can't determine which method to call.
  To resolve this issue, specify the return type as follows. `val offsetPos: BlockPos = pos.relative(direction)`

## API

* [Scala](https://www.scala-lang.org/) - [GitHub](https://github.com/scala/scala) - is licenced under
  the [Apache License, Version 2.0](https://www.scala-lang.org/license/).
* [Cats](https://typelevel.org/cats/) - [GitHub](https://github.com/typelevel/cats) - is licenced under
  the [Licence](https://github.com/typelevel/cats/blob/master/COPYING).
  * SLP uses [modified version of Cats](https://github.com/Kotori316/cats) to avoid module error.

[curse_forge]: https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force
