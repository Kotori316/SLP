plugins {
    id("com.kotori316.common.java")
    id("com.kotori316.common.publish")
    id("com.kotori316.common.source")
    id("com.kotori316.common.version")
    alias(libs.plugins.neoforge.moddev)
    signing
    alias(libs.plugins.publish.all)
}

evaluationDependsOn(":common")

val minecraftVersion = "26.1.2"
val neoVersion = libs.versions.neo260102.get()
version = "${project.property("modVersion")}-mc${minecraftVersion}-${libs.versions.scala.get()}"
group = "com.kotori316" // http://maven.apache.org/guides/mini/guide-naming-conventions.html
base {
    archivesName = "ScalableCatsForce-NeoForge"
}

dependencies {
    // https://mvnrepository.com/artifact/org.scala-lang/scala-library
    implementation(libs.scala, {
        jarJar(this) {
            version {
                strictly("[${libs.versions.scala.get()}, 4.0)")
                prefer(libs.versions.scala.get())
            }
        }
    })
    implementation(libs.bundles.cats) {
        isTransitive = false
        jarJar(this) {
            version {
                strictly("[2.0, ${libs.versions.cats.get()}]")
                prefer(libs.versions.cats.get())
            }
        }
    }
    compileOnly(project(":common"))

    // Test Dependencies.
    testImplementation(libs.jupiter.api)
    testImplementation(libs.jupiter.params)
    testRuntimeOnly(libs.jupiter.engine)
    testImplementation(libs.jupiter.launcher)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
}

tasks.compileJava {
    source(project(":common").sourceSets.main.get().allJava)
}

tasks.jar {
    archiveClassifier = "all"
}

neoForge {
    version = neoVersion

    unitTest {
        enable()
    }
}

val releaseDebug = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()
val publishVersion = "${project.version}-neoforge"
publishMods {
    dryRun = releaseDebug
    type = STABLE
    file = provider { tasks.jar }.flatMap { it.flatMap { t -> t.archiveFile } }
    additionalFiles = files(
        provider { tasks.sourcesJar }.flatMap { it.flatMap { t -> t.archiveFile } },
    )
    modLoaders = listOf("neoforge")
    version = publishVersion
    displayName = publishVersion
    changelog = createChangelog()

    curseforge {
        accessToken = (project.findProperty("curseforge_additional-enchanted-miner_key") ?: System.getenv("CURSE_TOKEN")
        ?: "") as String
        projectId = project.property("curseId").toString()
        minecraftVersions = listOf(minecraftVersion)
    }
    modrinth {
        accessToken = (project.findProperty("modrinthToken") ?: System.getenv("MODRINTH_TOKEN") ?: "") as String
        projectId = project.property("modrinthId").toString()
        minecraftVersions = listOf(minecraftVersion)
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
                    "Scala Loading library build with Minecraft $minecraftVersion and NeoForge $neoVersion"
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

val jksSignJar = tasks.register("jksSignJar") {
    dependsOn(tasks.jar, tasks.jarJar, tasks.sourcesJar, tasks.devJar)
    val executeCondition = project.hasProperty("jarSign.keyAlias") &&
            project.hasProperty("jarSign.keyLocation") &&
            project.hasProperty("jarSign.storePass")
    onlyIf { executeCondition }
    doLast {
        listOf(tasks.jar, tasks.sourcesJar, tasks.devJar).forEach { t ->
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

tasks.assemble {
    dependsOn(jksSignJar)
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
        For Minecraft $minecraftVersion
        
        Built with NeoForge $neoVersion
        
        This mod provides language provider, "kotori_scala".
        
        Scala: ${libs.versions.scala.get()}
        Cats: ${libs.versions.cats.get()}
        """.trimIndent()
    return t
}

ext["archivesBaseName"] = base.archivesName.get()
ext["generalDescription"] = createChangelog()
