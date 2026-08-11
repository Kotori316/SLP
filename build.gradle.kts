plugins {
    alias(libs.plugins.idea)
    alias(libs.plugins.ghr)
}

tasks.named("wrapper", Wrapper::class) {
    distributionType = Wrapper.DistributionType.BIN
    gradleVersion = "9.7.0"
}

version = "${project.property("modVersion")}"

val releaseDebug = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()
githubRelease {
    owner = "Kotori316"
    repo = "SLP"
    token(project.findProperty("githubToken") as? String ?: System.getenv("REPO_TOKEN") ?: "")
    targetCommitish = project.property("branch") as String
    prerelease = project.version.toString().contains("SNAPSHOT")
    body = """
        For Minecraft
        
        This mod provides language provider, "kotori_scala".
        
        Scala: ${libs.versions.scala.get()}
        Cats: ${libs.versions.cats.get()}
        """.trimIndent()
    releaseAssets = files(
        *listOfNotNull(
            findProject(":forge")?.let {
                fileTree(it.layout.buildDirectory.dir("libs")) {
                    include("*.jar")
                }
            },
            findProject(":neoforge")?.let {
                fileTree(it.layout.buildDirectory.dir("libs")) {
                    include("*.jar")
                }
            },
        ).toTypedArray()
    )
    dryRun = releaseDebug
}
