import com.kotori316.plugin.cf.CallVersionCheckFunctionTask
import com.kotori316.plugin.cf.CallVersionFunctionTask

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

val releaseDebug: Boolean = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()

tasks.register("registerVersion", CallVersionFunctionTask::class) {
    functionEndpoint = CallVersionFunctionTask.readVersionFunctionEndpoint(project)
    gameVersion = getMinecraftVersion(project.name)
    platform = getPlatform(project.name)
    platformVersion = pfVersion(project.name)
    modName = project.provider { project.ext.get("archivesBaseName") as String }
    changelog = project.provider { project.ext.get("generalDescription") as String }
    homepage = "https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force"
    isDryRun = releaseDebug
}

tasks.register("checkReleaseVersion", CallVersionCheckFunctionTask::class) {
    description = "Check if the version is already registered for ${getPlatform(project.name)} ${pfVersion(project.name)}"
    gameVersion = getMinecraftVersion(project.name)
    platform = getPlatform(project.name)
    modName = project.provider { project.ext.get("archivesBaseName") as String }
    version = project.version.toString()
    failIfExists = !releaseDebug
}

fun pfVersion(platform: String): String {
    return when (platform) {
        "forge-26.1.2" -> catalog.findVersion("forge260102").map { it.requiredVersion }.get()
        "neoforge-26.1.2" -> catalog.findVersion("neo260102").map { it.requiredVersion }.get()
        "forge-26.2.0" -> catalog.findVersion("forge262000").map { it.requiredVersion }.get()
        "neoforge-26.2.0" -> catalog.findVersion("neo262000").map { it.requiredVersion }.get()
        else -> throw IllegalArgumentException("Unknown platform: $platform")
    }
}

fun getPlatform(platform: String): String {
    return when {
        platform.matches(Regex("neoforge-.*")) -> "neoforge"
        platform.matches(Regex("forge-.*")) -> "forge"
        else -> throw IllegalArgumentException("Unknown platform: $platform")
    }
}

fun getMinecraftVersion(platform: String): String {
    val versionPattern = Regex("""(?:neo)?forge-(\d+\.\d+(?:\.\d+)?)""")
    val match = versionPattern.find(platform)
        ?: throw IllegalArgumentException("Unknown platform: $platform")
    return match.groupValues[1].removeSuffix(".0")
}
