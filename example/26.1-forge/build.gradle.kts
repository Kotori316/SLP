plugins {
    scala
    idea
    alias(libs.plugins.forge.gradle)
}

base {
    archivesName = "slp_example_26.1.2"
}

version = "1.0.0"
group = "com.kotori316.slp.examples"

java.toolchain.languageVersion = JavaLanguageVersion.of(25)

minecraft {
    runs {
        configureEach {
            workingDir = layout.projectDirectory.dir("run")
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "info")
        }

        // Forge 1.21.5 does not support game test server, so run data generator instead.
        create("data") {
            args(
                "--mod",
                "slp_examples",
                "--all",
                "--output",
                file("run/resources/"),
                "--existing",
                file("src/main/resources/")
            )
        }
    }
}

repositories {
    mavenLocal()
    maven {
        name = "Minecraft-Manually"
        url = uri("https://libraries.minecraft.net/")
        content {
            includeGroup("org.lwjgl")
            includeGroup("com.mojang")
        }
    }
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
    mavenCentral()
    maven {
        name = "Kotori316"
        url = uri("https://maven.kotori316.com")
        content {
            includeModule("org.typelevel", "cats-core_3")
            includeModule("org.typelevel", "cats-kernel_3")
            includeModule("org.typelevel", "cats-free_3")
        }
    }
}

val forgeJarJarOutput: Provider<RegularFile> = project(":forge-26.1.2").tasks
    .named("jarJar", org.gradle.jvm.tasks.Jar::class)
    .flatMap { it.archiveFile }

dependencies {
    implementation(minecraft.dependency(libs.forge260102))
    compileOnly(libs.scala3)
    // Use the jarJar fat jar at runtime so Forge can find kotori_scala with the correct version
    // and load the bundled scala/cats jars via JarJar
    runtimeOnly(files(forgeJarJarOutput))
}

sourceSets.forEach {
    val dir = layout.buildDirectory.dir("sourcesSets/${it.name}")
    it.output.setResourcesDir(dir)
    it.java.destinationDirectory = dir
    it.scala.destinationDirectory = dir
}

tasks.compileScala {
    dependsOn(tasks.processResources)
}

tasks.register("runGameTestServer")
