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

version = "${libs.versions.scala3.get()}-build-${project.property("build_number")}"
group = "com.kotori316" // http://maven.apache.org/guides/mini/guide-naming-conventions.html
base {
    archivesName = "ScalableCatsForce-NeoForge"
}

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/org.scala-lang/scala-library
    implementation(libs.scala2)
    implementation(libs.scala3) { isTransitive = false }
    // https://mvnrepository.com/artifact/org.typelevel/cats-core
    implementation(libs.bundles.cats) { isTransitive = false }

    // Jar in Jar
    // jarJar(group: "org.scala-lang", name: "scala-library", version: "[${libs.versions.scala2.get()}, 3.0)") { isTransitive = false }
    // jarJar(group: "org.scala-lang", name: "scala3-library_3", version: "[3.0, ${libs.versions.scala3.get()}]") { isTransitive = false }
    // jarJar(group: "org.typelevel", name: "cats-core_3", version: "[2.0, ${libs.versions.cats.get()}]") { isTransitive = false }
    // jarJar(group: "org.typelevel", name: "cats-kernel_3", version: "[2.0, ${libs.versions.cats.get()}]") { isTransitive = false }
    // jarJar(group: "org.typelevel", name: "cats-free_3", version: "[2.0, ${libs.versions.cats.get()}]") { isTransitive = false }

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
    version = libs.versions.neo1219.get()

    unitTest {
        enable()
    }
}

tasks.jar {
    manifest {
        attributes(
            "FMLModType" to "LIBRARY",
            "Automatic-Module-Name" to "kotori_scala",
        )
        attributes(
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

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    inputs.property("version", project.version)
    filesMatching("version.txt") {
        expand("version" to project.version)
    }
}

tasks.register("normalJar", org.gradle.jvm.tasks.Jar::class) {}

artifacts {
    archives(tasks.shadowJar)
    archives(tasks.sourcesJar)
}

tasks.register("jksSignJar") {
    dependsOn(tasks.shadowJar, tasks.jar, tasks.sourcesJar)
    val executeCondition = project.hasProperty("jarSign.keyAlias") &&
            project.hasProperty("jarSign.keyLocation") &&
            project.hasProperty("jarSign.storePass")
    onlyIf { executeCondition }
    doLast {
        listOf(tasks.shadowJar, tasks.jar, tasks.sourcesJar).forEach { t ->
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
        dependsOn(":neoforge-1.21.9:signJar")
        dependsOn(":neoforge-1.21.9:signSourcesJar")
        dependsOn(":neoforge-1.21.9:signShadowJar")
    }
}

fun createChangelog(): String {
    val t = """
        For Minecraft 1.21.9
        
        Built with forge ${libs.versions.neo1219.get()}
        
        This mod provides language provider, "kotori_scala".
        
        Scala3: ${libs.versions.scala3.get()}
        Scala: ${libs.versions.scala2.get()}
        Cats: ${libs.versions.cats.get()}
        """.trimIndent()
    return t
}

afterEvaluate {
    rootProject.tasks.named("githubRelease") { dependsOn(":neoforge-1.21.9:assemble") }
}

ext["archivesBaseName"] = base.archivesName.get()
ext["generalDescription"] = createChangelog()
