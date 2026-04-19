plugins {
    id("com.kotori316.common.java")
    id("com.kotori316.common.publish")
    id("com.kotori316.common.version")
    alias(libs.plugins.forge.gradle)
    alias(libs.plugins.forge.jarjar)
    signing
    alias(libs.plugins.publish.all)
}

val minecraftVersion = "26.1.2"
val forgeVersion = libs.versions.forge260102.get()
version = "${project.property("modVersion")}-mc${minecraftVersion}-${libs.versions.scala3.get()}"
group = "com.kotori316" // http://maven.apache.org/guides/mini/guide-naming-conventions.html
base {
    archivesName = "ScalableCatsForce"
}

minecraft {
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
}

jarJar {
    register()
}

dependencies {
    implementation(minecraft.dependency(libs.forge260102))
    implementation(libs.scala2)
    implementation(libs.scala3) { isTransitive = false }
    implementation(libs.bundles.cats) { isTransitive = false }
    "jarJar"(libs.scala2) {
        jarJar.configure(this) {
            setVersion(libs.versions.scala2.get())
            setRange("[${libs.versions.scala2.get()},3.0)")
        }
    }
    "jarJar"(libs.scala3) {
        jarJar.configure(this) {
            setVersion(libs.versions.scala3.get())
            setRange("[${libs.versions.scala3.get()},4.0)")
        }
    }
    "jarJar"(libs.bundles.cats) {
        jarJar.configure(this) {
            setVersion(libs.versions.cats.get())
            setRange("[2.0, ${libs.versions.cats.get()}]")
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

val releaseDebug = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()
publishMods {
    dryRun = releaseDebug
    type = STABLE
    file = provider { tasks.named("jarJar", org.gradle.jvm.tasks.Jar::class) }.flatMap { it.flatMap { t -> t.archiveFile } }
    additionalFiles = files(
        provider { tasks.sourcesJar }.flatMap { it.flatMap { t -> t.archiveFile } },
    )
    modLoaders = listOf("forge")
    displayName = "${project.version}-forge"
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
                    "Scala Loading library build with Minecraft $minecraftVersion and Forge $forgeVersion"
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
    dependsOn(tasks.jar, tasks.sourcesJar, tasks.devJar)
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
        
        Built with Forge $forgeVersion
        
        This mod provides language provider, "kotori_scala".
        
        Scala3: ${libs.versions.scala3.get()}
        Scala: ${libs.versions.scala2.get()}
        Cats: ${libs.versions.cats.get()}
        """.trimIndent()
    return t
}

ext["archivesBaseName"] = base.archivesName.get()
ext["generalDescription"] = createChangelog()
