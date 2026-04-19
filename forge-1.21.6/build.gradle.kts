import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.kotori316.common.java")
    id("com.kotori316.common.publish")
    alias(libs.plugins.forge.gradle)
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
            "version" to libs.versions.parchment1216.get(),
        )
    )
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
}

dependencies {
    implementation(minecraft.dependency(libs.forge1216))
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
val startVersion = "1.21.6"
publishMods {
    dryRun = releaseDebug
    type = STABLE
    file = provider { tasks.shadowJar }.flatMap { it.flatMap { t -> t.archiveFile } }
    additionalFiles = files(
        provider { tasks.jar }.flatMap { it.flatMap { t -> t.archiveFile } },
        provider { tasks.sourcesJar }.flatMap { it.flatMap { t -> t.archiveFile } },
    )
    modLoaders = listOf("forge")
    displayName = "${project.version}-forge"
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
            from(components["java"])
            pom {
                name = base.archivesName.get()
                description =
                    "Scala Loading library build with Minecraft $startVersion and Forge ${libs.versions.forge.get()}"
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
    dependsOn(tasks.shadowJar, tasks.jar, tasks.sourcesJar)
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
    val t = """
        For Minecraft $startVersion
        
        Built with forge ${libs.versions.forge1216.get()}
        
        This mod provides language provider, "kotori_scala".
        
        Scala3: ${libs.versions.scala3.get()}
        Scala: ${libs.versions.scala2.get()}
        Cats: ${libs.versions.cats.get()}
        """.trimIndent()
    return t
}

afterEvaluate {
    rootProject.tasks.named("githubRelease") { dependsOn(":forge-1.21.6:assemble") }
}

ext["archivesBaseName"] = base.archivesName.get()
ext["generalDescription"] = createChangelog()
