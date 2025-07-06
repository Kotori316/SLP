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
            "version" to "1.21.6-2025.06.29-1.21.6",
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

val releaseDebug = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()
publishMods {
    dryRun = releaseDebug
    type = STABLE
    file = tasks.shadowJar.flatMap { it.archiveFile }
    additionalFiles = files(
        tasks.jar.flatMap { it.archiveFile },
        tasks.sourcesJar.flatMap { it.archiveFile },
        tasks.jarJar.flatMap { it.archiveFile },
    )
    modLoaders = listOf("forge")
    displayName = "${project.version}-forge"
    changelog = createChangelog()

    val startVersion = "1.21.6"
    val endVersion = project.property("target_latest_minecraft_version").toString()
    curseforge {
        accessToken = (project.findProperty("curseforge_additional-enchanted-miner_key") ?: System.getenv("CURSE_TOKEN")
        ?: "") as String
        projectId = "320926"
        minecraftVersionRange {
            start = startVersion
            end = endVersion
        }
    }
    modrinth {
        accessToken = (project.findProperty("modrinthToken") ?: System.getenv("MODRINTH_TOKEN") ?: "") as String
        projectId = "zr0QMQMo"
        minecraftVersionRange {
            start = startVersion
            end = endVersion
            includeSnapshots = false
        }
    }
}

publishing {
    publications {
        create("mavenJava", MavenPublication::class) {
            artifactId = base.archivesName.get().lowercase()
            from(components["java"])
            pom {
                name = base.archivesName.get()
                description =
                    "Scala Loading library build with Minecraft ${libs.versions.minecraft.get()} and Forge ${libs.versions.forge.get()}"
                url = "https://github.com/Kotori316/SLP"
                packaging = "jar"
                withXml {
                    val dependencies = asElement().getElementsByTagName("dependencies")
                    for (i in 0 until dependencies.length) {
                        val dependency = dependencies.item(i)
                        dependency.parentNode.removeChild(dependency)
                    }
                }
            }
        }
    }
}

tasks.register("jksSignJar") {
    dependsOn(tasks.shadowJar, tasks["reobfJar"], tasks.sourcesJar)
    val executeCondition = project.hasProperty("jarSign.keyAlias") &&
            project.hasProperty("jarSign.keyLocation") &&
            project.hasProperty("jarSign.storePass")
    onlyIf { executeCondition }
    doLast {
        listOf(tasks.jar, tasks.shadowJar, tasks.sourcesJar).forEach { t ->
            ant.withGroovyBuilder {
                "signjar"(
                    "jar" to t.get().archiveFile.get(),
                    "alias" to project.findProperty("jarSign.keyAlias"),
                    "keystore" to project.findProperty("jarSign.keyLocation"),
                    "storepass" to project.findProperty("jarSign.storePass"),
                    "sigalg" to "Ed25519",
                    "digestalg" to "SHA-256",
                    "tsaurl" to "http://timestamp.digicert.com",
                )
            }
        }
    }
}

tasks.named("assemble") {
    dependsOn("jksSignJar")
}

signing {
    sign(publishing.publications)
    sign(tasks.jar.get(), tasks.shadowJar.get(), tasks.sourcesJar.get())
}

val hasGpgSignature = project.hasProperty("signing.keyId") &&
        project.hasProperty("signing.password") &&
        project.hasProperty("signing.secretKeyRingFile")

tasks.withType(Sign::class) {
    onlyIf {
        hasGpgSignature
    }
}

tasks.withType(AbstractPublishToMaven::class) {
    if (hasGpgSignature) {
        dependsOn(":forge-1.21.6:signJar")
        dependsOn(":forge-1.21.6:signSourcesJar")
        dependsOn(":forge-1.21.6:signShadowJar")
    }
}

fun createChangelog(): String {
    val t = """\
        For Minecraft ${libs.versions.minecraft.get()}
        
        Built with forge ${libs.versions.forge.get()}
        
        This mod provides language provider, "kotori_scala".
        
        Scala3: ${libs.versions.scala3.get()}
        Scala: ${libs.versions.scala2.get()}
        Cats: ${libs.versions.cats.get()}
        """
    return t
}
