plugins {
    alias(libs.plugins.idea)
    alias(libs.plugins.ghr)
}

tasks.named("wrapper", Wrapper::class) {
    distributionType = Wrapper.DistributionType.BIN
    gradleVersion = "8.7"
}

version = "${libs.versions.scala3.get()}-build-${project.property("build_number")}"

val releaseDebug = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()
githubRelease {
    owner = "Kotori316"
    repo = "SLP"
    token(project.findProperty("githubToken") as? String ?: System.getenv("REPO_TOKEN") ?: "")
    targetCommitish = project.property("branch") as String
    prerelease = project.version.toString().contains("SNAPSHOT")
    body = """\
        For Minecraft ${libs.versions.minecraft.get()}
        
        This mod provides language provider, "kotori_scala".
        
        Scala3: ${libs.versions.scala3.get()}
        Scala: ${libs.versions.scala2.get()}
        Cats: ${libs.versions.cats.get()}
        """.trimIndent()
    releaseAssets = files(
        fileTree(project(":forge").layout.buildDirectory.dir("libs")) {
            include("*.jar")
        },
        fileTree(project(":neoforge").layout.buildDirectory.dir("libs")) {
            include("*.jar")
        }
    )
    dryRun = releaseDebug
}
