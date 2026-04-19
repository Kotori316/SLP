import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.kotori316.common.java")
    id("com.kotori316.common.publish")
    id("com.kotori316.common.source")
    alias(libs.plugins.neoforge.moddev)
    signing
    alias(libs.plugins.publish.all)
}

val minecraftVersion = "26.1.2"
version = "${project.property("modVersion")}-mc${minecraftVersion}-${libs.versions.scala3.get()}"
group = "com.kotori316" // http://maven.apache.org/guides/mini/guide-naming-conventions.html
base {
    archivesName = "ScalableCatsForce-NeoForge"
}

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/org.scala-lang/scala-library
    implementation(libs.scala2, {
        jarJar(this) {
            version {
                strictly("[${libs.versions.scala2.get()}, 3.0)")
                prefer(libs.versions.scala2.get())
            }
        }
    })
    implementation(libs.scala3) {
        isTransitive = false
        jarJar(this) {
            version {
                strictly("[${libs.versions.scala3.get()}, 4.0)")
                prefer(libs.versions.scala3.get())
            }
        }
    }
    // https://mvnrepository.com/artifact/org.typelevel/cats-core
    implementation(libs.bundles.cats) {
        isTransitive = false
        jarJar(this) {
            version {
                strictly("[2.0, ${libs.versions.cats.get()}]")
                prefer(libs.versions.cats.get())
            }
        }
    }

    // Test Dependencies.
    testImplementation(libs.jupiter.api)
    testImplementation(libs.jupiter.params)
    testRuntimeOnly(libs.jupiter.engine)
    testImplementation(libs.jupiter.launcher)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
}

tasks.test {
    useJUnitPlatform()
}

neoForge {
    version = libs.versions.neo260102.get()

    unitTest {
        enable()
    }
}

val manifestMap = mapOf(
    "FMLModType" to "LIBRARY",
    "Automatic-Module-Name" to "kotori_scala",
    "Specification-Title" to project.name,
    "Specification-Vendor" to "Kotori316",
    "Specification-Version" to "1", // We are version 1 of ourselves
    "Implementation-Title" to project.name,
    "Implementation-Version" to project.version,
    "Implementation-Vendor" to "Kotori316",
    "Implementation-Timestamp" to ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
)

tasks.jar {
    archiveClassifier = "with-library" // The "jar" is the target of JarJar
    manifest {
        attributes(manifestMap)
    }
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    inputs.property("version", project.version)
    filesMatching("version.txt") {
        expand("version" to project.version)
    }
}

tasks.shadowJar {
    enabled = false
}

tasks.register("normalJar", Jar::class) {
    group = "build"
}

val devJar = tasks.register("devJar", Jar::class) {
    group = "build"
    archiveClassifier = "dev"
    from(sourceSets.main.get().output)
    manifest {
        attributes(manifestMap)
    }
}

artifacts {
    archives(devJar)
    archives(tasks.sourcesJar)
}

val releaseDebug = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()
val startVersion = "1.21.9"
publishMods {
    dryRun = releaseDebug
    type = STABLE
    file = provider { tasks.jar }.flatMap { it.flatMap { t -> t.archiveFile } }
    additionalFiles = files(
        provider { devJar }.flatMap { it.flatMap { t -> t.archiveFile } },
        provider { tasks.sourcesJar }.flatMap { it.flatMap { t -> t.archiveFile } },
    )
    modLoaders = listOf("neoforge")
    displayName = "${project.version}-neoforge"
    changelog = createChangelog()

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
            // from(components["java"])
            artifact(devJar)
            artifact(tasks.sourcesJar)
            pom {
                name = base.archivesName.get()
                description =
                    "Scala Loading library build with Minecraft $startVersion and NeoForge ${libs.versions.neo260102.get()}"
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
    dependsOn(tasks.jar, devJar, tasks.sourcesJar)
    val executeCondition = project.hasProperty("jarSign.keyAlias") &&
            project.hasProperty("jarSign.keyLocation") &&
            project.hasProperty("jarSign.storePass")
    onlyIf { executeCondition }
    doLast {
        listOf(tasks.jar, devJar, tasks.sourcesJar).forEach { t ->
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
}

val hasGpgSignature = project.hasProperty("signing.keyId") &&
        project.hasProperty("signing.password") &&
        project.hasProperty("signing.secretKeyRingFile")

tasks.withType(Sign::class) {
    onlyIf {
        hasGpgSignature
    }
}

fun createChangelog(): String {
    val t = """
        For Minecraft $startVersion
        
        Built with NeoForge ${libs.versions.neo260102.get()}
        
        This mod provides language provider, "kotori_scala".
        
        Scala3: ${libs.versions.scala3.get()}
        Scala: ${libs.versions.scala2.get()}
        Cats: ${libs.versions.cats.get()}
        """.trimIndent()
    return t
}

afterEvaluate {
}

ext["archivesBaseName"] = base.archivesName.get()
ext["generalDescription"] = createChangelog()
