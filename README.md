# Scala language provider for Minecraft 26.1.x.

Branch 26.1

This mod adds a Scala library to Minecraft 26.1.2 with Forge and NeoForge.
**NO COMPATIBILITY WITH 1.x version of SLP.**

[![](http://cf.way2muchnoise.eu/versions/scalable-cats-force.svg)][curse_forge]
[![](http://cf.way2muchnoise.eu/full_scalable-cats-force_downloads.svg)][curse_forge]

[![](https://img.shields.io/modrinth/dt/scalable-cats-force?logo=modrinth&style=flat-square)][Modrinth]

## Usage

* For Player – Download a Jar file from [Curse Forge][curse_forge] or [Modrinth] and move the file to your `mods` folder. This mod will
  not appear in the mod list.

* For Developer  
  See the example directory.

  In your `build.gradle`, add below code in the top-level.

  ```groovy
  repositories {
      maven {
          name = "kotori316"
          url = uri("https://maven.kotori316.com")
          content {
              includeModule("com.kotori316", "ScalableCatsForce".toLowerCase())
          }
      }
  }

  dependencies {
      // Add Forge or NeoForge dependency as the platform requires
  
      // Scala, no need to add Scala2 dependency since 3.8.3
      implementation('org.scala-lang:scala3-library_3:3.8.4')
      // Add if you need this library.
      // the runtime copy is bundled in the SLP jar.
      implementation('org.typelevel:cats-kernel_3:2.13.0')

      // The language loader. You can put the jar to the mods dir instead of declaring in `build.gradle.kts`.
      runtimeOnly("com.kotori316:scalablecatsforce:4.0.5-mc26.1.2-3.8.4:dev") {
          transitive(false)
      }
  }
  ```

  * **If the Minecraft client doesn't launch with an exception to modules, change scala dependency from "implementation"
    to "compileOnly" and add slp mod in the mods directory.**
  * Change the library version if needed.
    * See detail pages in CurseForge or Modrinth to get which library version is included in the Jar file.
  * From 26.1.2 version, SLP includes Scala 3.8.4

### Limitations

In this section, I note some points you should care.

1. Avoid use of `Mod.EventBusSubscriber` in Java code. This will cause exception in "compileScala" task.

* Use in Scala code will not throw an exception.

2. If you got the compile error "ambiguous reference to overloaded definition", specify the return type.

* For example, `val offsetPos = pos.relative(direction)` will cause this error because `relative` is declared both
  in `BlockPos` and `Vec3i`, and the return types are different. So, the compiler can't determine which method to call.
  To resolve this issue, specify the return type as follows. `val offsetPos: BlockPos = pos.relative(direction)`

## API

* [Scala](https://www.scala-lang.org/) - [GitHub](https://github.com/scala/scala) - is licenced under
  the [Apache License, Version 2.0](https://www.scala-lang.org/license/).
* [Cats](https://typelevel.org/cats/) - [GitHub](https://github.com/typelevel/cats) - is licenced under
  the [License](https://github.com/typelevel/cats/blob/master/COPYING).
  * SLP bundles the official Cats. NeoForge uses the jars as-is. The Forge jar removes the
    Java-reserved-word packages (e.g. `cats.kernel.instances.byte`) from `cats-kernel`, because Forge's
    module system rejects them while building the module layer at boot.

[curse_forge]: https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force
[Modrinth]: https://modrinth.com/mod/scalable-cats-force
