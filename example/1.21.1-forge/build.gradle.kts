plugins {
    scala
    idea
    alias(libs.plugins.forge.gradle)
    alias(libs.plugins.parchment.librarian)
}

version = "1.0.0"
group = "com.kotori316.slp.examples"

minecraft {
    mappings(
        mapOf(
           //  "channel" to "parchment",
           //  "version" to (libs.versions.parchment.get() + "-" + libs.versions.minecraft.get()),
            "channel" to "official",
            "version" to libs.versions.minecraft.get(),
        )
    )
    reobf = false
    copyIdeResources = true

    runs {
        configureEach {
            workingDirectory = "./run"
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "info")
        }

        create("gameTestServer") {
            property("forge.enabledGameTestNamespaces", "slp_examples")
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

dependencies {
    minecraft(libs.forge)
    compileOnly(libs.scala3)
    // implementation(project(":forge"))
    runtimeOnly(project(":forge")) {
        isTransitive = false
    }
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
