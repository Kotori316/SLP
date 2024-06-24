import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.kotori316.plugin.cf.CallVersionCheckFunctionTask
import com.kotori316.plugin.cf.CallVersionFunctionTask

plugins {
    id("maven-publish")
    id("com.kotori316.plugin.cf")
    id("com.github.johnrengelman.shadow")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

val mc: String = catalog.findVersion("minecraft").map { it.requiredVersion }.get()
val releaseDebug: Boolean = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()

tasks.named("shadowJar", ShadowJar::class) {
    val groupNames = listOf("org.scala-lang", "org.scala-lang.modules", "org.typelevel")
    archiveClassifier = "with-library"
    dependencies {
        groupNames.forEach { name ->
            include(dependency("${name}:"))
        }
    }
}

fun pfVersion(platform: String): String {
    return when (platform) {
        "forge" -> catalog.findVersion("forge").map { it.requiredVersion }.get()
        "neoforge" -> catalog.findVersion("neoforge").map { it.requiredVersion }.get()
        else -> throw IllegalArgumentException("Unknown platform: $platform")
    }
}

// configure the maven publication
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Kotori316/SLP")
            credentials {
                username = project.findProperty("gpr.user") as? String ?: System.getenv("GITHUB_ACTOR") ?: ""
                password = project.findProperty("githubToken") as? String ?: System.getenv("REPO_TOKEN") ?: ""
            }
        }
        val u = project.findProperty("maven_username") as? String ?: System.getenv("MAVEN_USERNAME") ?: ""
        val p = project.findProperty("maven_password") as? String ?: System.getenv("MAVEN_PASSWORD") ?: ""
        if (u != "" && p != "") {
            maven {
                name = "kotori316-maven"
                // For users: Use https://maven.kotori316.com to get artifacts
                url = uri("https://maven2.kotori316.com/production/maven")
                credentials {
                    username = u
                    password = p
                }
            }
        }
        if (u != "" && p != "" && System.getenv("CI") == null) {
            maven {
                name = "MavenTestGCP"
                url = uri("https://maven2.kotori316.com/test/maven")
                credentials {
                    username = u
                    password = p
                }
            }
        }
    }
}

tasks.register("registerVersion", CallVersionFunctionTask::class) {
    functionEndpoint = CallVersionFunctionTask.readVersionFunctionEndpoint(project)
    gameVersion = mc
    platform = project.name
    platformVersion = pfVersion(project.name)
    modName = project.provider { project.ext.get("archivesBaseName") as String }
    changelog = project.provider { project.ext.get("generalDescription") as String }
    homepage = "https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force"
    isDryRun = releaseDebug
}

tasks.register("checkReleaseVersion", CallVersionCheckFunctionTask::class) {
    gameVersion = mc
    platform = project.name
    modName = project.provider { project.ext.get("archivesBaseName") as String }
    version = project.version.toString()
    failIfExists = !releaseDebug
}
