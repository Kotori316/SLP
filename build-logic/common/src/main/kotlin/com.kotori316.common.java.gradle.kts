plugins {
    id("scala")
    id("java-library")
    id("maven-publish")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
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
        name = "Azure-SLP"
        url = uri("https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1")
        content {
            includeModule("org.typelevel", "cats-core_3")
            includeModule("org.typelevel", "cats-kernel_3")
            includeModule("org.typelevel", "cats-free_3")
        }
    }
}

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
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed", "standardOut", "standardError")
        setExceptionFormat("full")
    }
}

fun inGroup(name: String, action: () -> Unit) {
    println("::group::${name}")
    action()
    println("::endgroup::")
}

tasks.register("data") {
    doLast {
        println(
            "Java: ${System.getProperty("java.version")} JVM: ${System.getProperty("java.vm.version")}" +
                    "(${System.getProperty("java.vendor")}) Arch: ${System.getProperty("os.arch")}"
        )
        println(
            """
            Scala: ${catalog.findVersion("scala3").map { it.requiredVersion }.get()}
            Build Number: ${project.property("build_number")}, ${project.property("build_number")?.javaClass}
            Version: ${project.version}, ${project.version.javaClass}
            Minecraft: ${catalog.findVersion("minecraft").map { it.requiredVersion }.get()}
            Forge: ${catalog.findVersion("forge").map { it.requiredVersion }.get()}
            NeoForge: ${catalog.findVersion("neoforge").map { it.requiredVersion }.get()}
        """.trimIndent()
        )
        print(System.lineSeparator().repeat(2))
        println("All Dependencies")
        project.configurations.forEach { con ->
            if (con.dependencies.isNotEmpty()) {
                inGroup(con.name) {
                    con.dependencies.forEach { o -> println(o) }
                }
            }
        }
        print(System.lineSeparator().repeat(2))
        inGroup("Dependencies to be included in pom") {
            project.configurations["implementation"].dependencies.forEach { dep ->
                if (!dep.name.contains("junit")) {
                    println("Group: %s, ArtifactId: %s, Version: %s".format(dep.group, dep.name, dep.version))
                } else {
                    println("JUNIT - Group: %s, ArtifactId: %s, Version: %s".format(dep.group, dep.name, dep.version))
                }
            }
        }
        inGroup("Publishing Information") {
            project.publishing.repositories.asMap.forEach { (name, value) ->
                println("$name -> $value")
            }
        }
    }
}
