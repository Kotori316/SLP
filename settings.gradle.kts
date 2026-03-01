pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven { url = uri("https://maven.minecraftforge.net/") }
        maven { url = uri("https://maven.neoforged.net/releases") }
        maven { url = uri("https://maven.parchmentmc.org") }
        maven {
            name = "Kotori316 Plugin"
            url = uri("https://storage.googleapis.com/kotori316-maven-storage/maven/")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
    id("com.gradle.develocity") version ("4.+")
}

develocity {
    buildScan {
        if (System.getenv("CI").toBoolean()) {
            termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
            termsOfUseAgree = "yes"
        }
        publishing {
            onlyIf { false }
        }
    }
}

includeBuild("build-logic")
if (!System.getenv("DISABLE_FORGE").toBoolean()) {
    include("forge-1.21.6")
}
if (!System.getenv("DISABLE_NEO_FORGE").toBoolean()) {
    include("neoforge-1.21.9")
}
if(!System.getenv("DISABLE_EXAMPLE").toBoolean()) {
    if (!System.getenv("DISABLE_FORGE").toBoolean()) {
        // include("forge")
        // include("example:1.21.1-forge")
        // include("example:1.21.3-forge")
        // include("example:1.21.4-forge")
        // include("example:1.21.5-forge")
        // include("example:1.21.6-forge")
        include("example:1.21.9-forge")
        include("example:1.21.11-forge")
    }
    if (!System.getenv("DISABLE_NEO_FORGE").toBoolean()){
        include("neoforge")
        include("example:1.21.1-neoforge")
        include("example:1.21.3-neoforge")
        include("example:1.21.4-neoforge")
        include("example:1.21.5-neoforge")
        include("example:1.21.6-neoforge")
        include("example:1.21.9-neoforge")
        include("example:1.21.11-neoforge")
    }
}
