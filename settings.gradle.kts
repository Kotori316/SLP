pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven { url = uri("https://maven.minecraftforge.net/") }
        maven { url = uri("https://maven.neoforged.net/releases") }
        maven { url = uri("https://maven.parchmentmc.org") }
        maven {
            name = "Kotori316 Plugin"
            url = uri("https://maven.kotori316.com")
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

fun wanted(envName: String): Boolean = System.getenv(envName)?.toBoolean() ?: true

includeBuild("build-logic")
include("common")
if (!System.getenv("DISABLE_FORGE").toBoolean()) {
    if (wanted("INCLUDE_FORGE_26_1_2")) include("forge-26.1.2")
    if (wanted("INCLUDE_FORGE_26_2_0")) include("forge-26.2.0")
    if (wanted("INCLUDE_FORGE_26_3_0")) include("forge-26.3.0")
}
if (!System.getenv("DISABLE_NEO_FORGE").toBoolean()) {
    if (wanted("INCLUDE_NEOFORGE_26_1_2")) include("neoforge-26.1.2")
    if (wanted("INCLUDE_NEOFORGE_26_2_0")) include("neoforge-26.2.0")
    if (wanted("INCLUDE_NEOFORGE_26_3_0")) include("neoforge-26.3.0")
}
if(!System.getenv("DISABLE_EXAMPLE").toBoolean()) {
    if (!System.getenv("DISABLE_FORGE").toBoolean()) {
        if (wanted("INCLUDE_FORGE_26_1_2")) include("example:26.1-forge")
        if (wanted("INCLUDE_FORGE_26_2_0")) include("example:26.2-forge")
        if (wanted("INCLUDE_FORGE_26_3_0")) include("example:26.3-forge")
    }
    if (!System.getenv("DISABLE_NEO_FORGE").toBoolean()){
        if (wanted("INCLUDE_NEOFORGE_26_1_2")) include("example:26.1-neoforge")
        if (wanted("INCLUDE_NEOFORGE_26_2_0")) include("example:26.2-neoforge")
        if (wanted("INCLUDE_NEOFORGE_26_3_0")) include("example:26.3-neoforge")
    }
}
