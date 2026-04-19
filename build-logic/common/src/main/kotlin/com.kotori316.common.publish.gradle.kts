import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar

plugins {
    id("maven-publish")
    id("com.kotori316.plugin.cf")
    id("com.gradleup.shadow")
}

val releaseDebug: Boolean = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()

tasks.shadowJar {
    archiveClassifier = "with-library"
    dependencies {
        include(dependency("org.scala-lang:.*"))
        include(dependency("org.scala-lang.modules:.*"))
        include(dependency("org.typelevel:.*"))
    }
}

// configure the maven publication
publishing {
    repositories {
        if (!releaseDebug) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/Kotori316/SLP")
                credentials {
                    username = project.findProperty("gpr.user") as? String ?: System.getenv("GITHUB_ACTOR") ?: ""
                    password = project.findProperty("githubToken") as? String ?: System.getenv("REPO_TOKEN") ?: ""
                }
            }
        }

        val u = project.findProperty("maven_username") as? String ?: System.getenv("MAVEN_USERNAME") ?: ""
        val p = project.findProperty("maven_password") as? String ?: System.getenv("MAVEN_PASSWORD") ?: ""
        if (u != "" && p != "" && !releaseDebug) {
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
        if (u != "" && p != "") {
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
