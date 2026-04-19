import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("scala")
    id("java-library")
    id("maven-publish")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withSourcesJar()
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

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

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    testImplementation(
        platform(
            "org.junit:junit-bom:${
                catalog.findVersion("jupiter").map { it.requiredVersion }.get()
            }"
        )
    )
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    mockitoAgent(catalog.findLibrary("mockito_core").get())
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
    archiveClassifier = "dev"
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

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed", "standardOut", "standardError")
        setExceptionFormat("full")
    }
    jvmArgs.add("-javaagent:${mockitoAgent.asPath}")
}

tasks.register("data") {
    doLast {
        println(
            "Java: ${System.getProperty("java.version")} JVM: ${System.getProperty("java.vm.version")}" +
                    "(${System.getProperty("java.vendor")}) Arch: ${System.getProperty("os.arch")}"
        )
    }
}
