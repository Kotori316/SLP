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
