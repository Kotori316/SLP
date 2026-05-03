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
    "Implementation-Vendor" to "Kotori316",
    "Implementation-Timestamp" to ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
)

tasks.jar {
    manifest {
        attributes(manifestMap)
        // Use provider so the version is resolved at task execution time (after the project version is set)
        attributes(mapOf("Implementation-Version" to provider { project.version }))
    }
}

val devJar by tasks.registering(Jar::class) {
    archiveClassifier = "dev"
    manifest {
        attributes(manifestMap)
        attributes(mapOf("Implementation-Version" to provider { project.version }))
    }
    from(sourceSets.main.get().output)
}
tasks.assemble {
    dependsOn(devJar)
}

val devJarElements = configurations.create("devJarElements") {
    isCanBeResolved = false
    isCanBeConsumed = true
    outgoing.artifact(devJar)
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}
(components["java"] as AdhocComponentWithVariants).addVariantsFromConfiguration(devJarElements) {
    mapToMavenScope("runtime")
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

tasks.register("checkBinaryContent") {
    group = "verification"
    description = "Checks if the built JAR contains nested Scala JARs (JarInJar)."
    dependsOn(tasks.jar)
    tasks.findByName("jarJar")?.let { dependsOn(it) }

    doLast {
        var anyCheckPassed = false

        val jarJarTask = tasks.findByName("jarJar") as? AbstractArchiveTask
        val filesToCheck: List<Pair<File, String>> = listOfNotNull(
            jarJarTask?.let { it.archiveFile.get().asFile to "jarJar" },
            tasks.jar.get().archiveFile.get().asFile to "jar",
        )

        for ((file, label) in filesToCheck) {
            if (!file.exists()) {
                println("$label: ${file.name} does not exist, skipping.")
                continue
            }
            val scalaJars = project.zipTree(file).matching {
                include("META-INF/jarjar/scala*.jar")
            }.files

            if (scalaJars.isEmpty()) {
                println("$label: no scala*.jar found in META-INF/jars/ of ${file.name}, skipping.")
                continue
            }

            scalaJars.forEach { scalaJar ->
                var count = 0
                project.zipTree(scalaJar).visit {
                    val path = relativePath.pathString
                    if (!isDirectory && path.startsWith("scala/") && (path.endsWith(".class") || path.endsWith(".tasty"))) {
                        count++
                    }
                }
                if (count < 5) {
                    throw GradleException("${scalaJar.name}: found $count scala/.class/.tasty file(s) inside META-INF/jarjar/, expected at least 5")
                }
                println("Verified ${scalaJar.name} ($label): Found $count files in 'scala/' directory.")
            }
            anyCheckPassed = true
        }

        if (!anyCheckPassed) {
            throw GradleException("No Scala JarInJar content found: no scala*.jar in META-INF/jars/ of any checked JAR")
        }
        println("All binary checks passed successfully!")
    }
}

tasks.register("data") {
    doLast {
        println(
            "Java: ${System.getProperty("java.version")} JVM: ${System.getProperty("java.vm.version")}" +
                    "(${System.getProperty("java.vendor")}) Arch: ${System.getProperty("os.arch")}"
        )
    }
}
