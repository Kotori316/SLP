# Scala language provider for Minecraft 1.20.

Branch 1.20

This mod adds Scala library to Minecraft 1.20 with Forge.
**NO COMPATIBILITY WITH 1.19 version of SLP.**

[![](http://cf.way2muchnoise.eu/versions/scalable-cats-force.svg)][curse_forge]
[![](http://cf.way2muchnoise.eu/full_scalable-cats-force_downloads.svg)][curse_forge]

[![](https://img.shields.io/modrinth/dt/scalable-cats-force?logo=modrinth&style=flat-square)][Modrinth]

## Usage

* For Player - Download Jar file from [Curse Forge][curse_forge] or [Modrinth] and move the file to your `mods` folder. This mod will
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
              it.includeModule("org.typelevel", "cats-core_3")
              it.includeModule("org.typelevel", "cats-kernel_3")
              it.includeModule("org.typelevel", "cats-free_3")
          }
      }
  }

  dependencies {
      // Change forge and minecraft version.
      minecraft 'net.minecraftforge:forge:1.20.1-47.1.43'
      compileOnly(group: 'org.scala-lang', name: 'scala-library', version: '3.3.0')
      // Add if you need this library. I use a modified version of Cats to avoid some module errors.
      compileOnly(group: 'org.typelevel', name: 'cats-core_3', version: '2.9.2-kotori')

      // The language loader. You can put the jar to the mods dir instead of declaring in `build.gradle`.
      // This file is needed as the scala library will not be loaded in dev environment due to change of classpath by Forge.
      runtimeOnly(group: "com.kotori316", name: "ScalableCatsForce".toLowerCase(), version: "3.3.0-build-2", classifier: "with-library") {
          transitive(false)
      }
  }
  ```

  * **If the Minecraft client doesn't launch with an exception to modules, change scala dependency from "implementation"
    to "compileOnly" and add slp mod in mods directory.**
  * Change library version if needed.
    * See detail pages in CurseForge or Modrinth to get which library version is included in the Jar file.
  * From 1.20 version, SLP includes Scala 3
    * **May not have binary compatibility with jars build with Scala2**.
      * Files build with Scala2 may throw error if you use ["Macthing on case classes"](https://docs.scala-lang.org/tour/pattern-matching.html#matching-on-case-classes) in your code.
      * This is due to internal change in Scala3.
      * Though SLP jar contains all classes from Scala2, this kind of error happens.
    * Compile with Scala3 will not cause these runtime errors.

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
[Modrinth]: https://modrinth.com/mod/scalable-cats-force
