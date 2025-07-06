import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.kotori316.common.java")
    id("com.kotori316.common.publish")
    id("net.minecraftforge.gradle") version ("[6.0.16,6.2)")
    id("org.parchmentmc.librarian.forgegradle") version ("1.+")
    signing
    alias(libs.plugins.publish.all)
}

version = "${libs.versions.scala3.get()}-build-${project.property("build_number")}"
group = "com.kotori316" // http://maven.apache.org/guides/mini/guide-naming-conventions.html
base {
    archivesName = "ScalableCatsForce"
}

minecraft {
    mappings(
        mapOf(
            "channel" to "parchment",
            "version" to "${libs.versions.parchment.get()}-${libs.versions.minecraft.get()}",
        )
    )
}

dependencies {
    minecraft(libs.forge1216)
    implementation(libs.scala2)
    implementation(libs.scala3) { isTransitive = false }
    implementation(libs.bundles.cats) { isTransitive = false }

    // Test Dependencies.
    testImplementation(libs.jupiter.api)
    testImplementation(libs.jupiter.params)
    testRuntimeOnly(libs.jupiter.engine)
    testImplementation(libs.jupiter.launcher)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
}

tasks {
    jar {
        manifest {
            attributes(
                "FMLModType" to "LANGPROVIDER",
                "Automatic-Module-Name" to "kotori_scala",
                "Specification-Title" to project.name,
                "Specification-Vendor" to "Kotori316",
                "Specification-Version" to "1", // We are version 1 of ourselves
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "Kotori316",
                "Implementation-Timestamp" to ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
            )
        }
        archiveClassifier = "dev"
    }
    register("normalJar", Jar::class)

    withType(JavaCompile::class) {
        options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation
    }
}
