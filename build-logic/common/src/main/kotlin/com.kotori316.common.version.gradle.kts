import com.kotori316.plugin.cf.CallVersionCheckFunctionTask
import com.kotori316.plugin.cf.CallVersionFunctionTask
import org.gradle.api.artifacts.VersionCatalogsExtension
import kotlin.IllegalArgumentException

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
    gameVersion = getMinecraftVersion(project.name)
    platform = getPlatform(project.name)
    modName = project.provider { project.ext.get("archivesBaseName") as String }
    version = project.version.toString()
    failIfExists = !releaseDebug
}

fun pfVersion(platform: String): String {
    return when (platform) {
        "forge" -> catalog.findVersion("forge").map { it.requiredVersion }.get()
        "forge-1.21.6" -> catalog.findVersion("forge1216").map { it.requiredVersion }.get()
        "neoforge" -> catalog.findVersion("neoforge").map { it.requiredVersion }.get()
        "neoforge-1.21.9" -> catalog.findVersion("neo1219").map { it.requiredVersion }.get()
        else -> throw IllegalArgumentException("Unknown platform: $platform")
    }
}

fun getPlatform(platform: String): String {
    return when (platform) {
        "forge" -> "forge"
        "forge-1.21.6" -> "forge"
        "neoforge" -> "neoforge"
        "neoforge-1.21.9" -> "neoforge"
        else -> throw IllegalArgumentException("Unknown platform: $platform")
    }
}

fun getMinecraftVersion(platform: String): String {
    return when (platform) {
        "forge" -> catalog.findVersion("minecraft").map { it.requiredVersion }.get()
        "forge-1.21.6" -> "1.21.6"
        "neoforge" -> catalog.findVersion("minecraft").map { it.requiredVersion }.get()
        "neoforge-1.21.9" -> "1.21.9"
        else -> throw IllegalArgumentException("Unknown platform: $platform")
    }
}
