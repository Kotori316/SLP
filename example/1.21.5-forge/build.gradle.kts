plugins {
    scala
    idea
    id("net.minecraftforge.gradle") version ("[6.0.24,6.2)")
    id ("org.parchmentmc.librarian.forgegradle") version ("1.+")
}

version = "1.0.0"
group = "com.kotori316.slp.examples"

minecraft {
    mappings(
        mapOf(
           //  "channel" to "parchment",
           //  "version" to (libs.versions.parchment.get() + "-" + libs.versions.minecraft.get()),
            "channel" to "official",
            "version" to "1.21.5",
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

        // Forge 1.21.5 does not support game test server
        /*create("gameTestServer") {
            property("forge.enabledGameTestNamespaces", "slp_examples")
        }*/
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
    minecraft(libs.forge1215)
    compileOnly(libs.scala3)
    // implementation(project(":forge"))
    runtimeOnly(project(":forge")) {
        isTransitive = false
    }
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4") {
        version {
            strictly("5.0.4")
        }
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
