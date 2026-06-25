plugins {
    scala
    idea
    alias(libs.plugins.neoforge.moddev)
}

base {
    archivesName = "slp_example_26.2"
}

version = "1.0.0"
group = "com.kotori316.slp.examples"
java.toolchain.languageVersion = JavaLanguageVersion.of(25)

neoForge {
    version = libs.versions.neo262000.get()

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
    // Cats is provided at runtime by the SLP jar (JarInJar). Only needed for compilation here.
    compileOnly(libs.bundles.cats) { isTransitive = false }
    runtimeOnly(project(":neoforge-26.2.0")) {
        isTransitive = false
    }
}

// compileScala emits the source set's resources (e.g. META-INF/neoforge.mods.toml) into its own
// output dir in addition to processResources, so the source set output carries two identical copies.
// Drop the duplicate when assembling the jar (the copies are byte-identical, so EXCLUDE is lossless).
tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
