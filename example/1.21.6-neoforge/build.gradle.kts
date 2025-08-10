plugins {
    scala
    idea
    id("net.neoforged.moddev") version ("2.0.107")
}

version = "1.0.0"
group = "com.kotori316.slp.examples"
java.toolchain.languageVersion = JavaLanguageVersion.of(21)

neoForge {
    version = libs.versions.neo1216.get()

    runs {
        create("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", "slp_examples,minecraft")
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.INFO
        }
    }

    mods {
        create("slp_examples") {
            sourceSet(sourceSets.main.get())
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.scala3)
    runtimeOnly(project(":neoforge")) {
        isTransitive = false
    }
}
