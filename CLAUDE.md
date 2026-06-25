# Adding a new Minecraft version subproject

When adding a new MC version (e.g. `forge-X.Y.Z` / `neoforge-X.Y.Z`), update *
*`build-logic/common/src/main/kotlin/com.kotori316.common.version.gradle.kts`** in addition to the usual build files.

The file contains two functions that need updating:

- **`pfVersion(platform)`** — maps project name → loader version string from `libs.versions.toml`. Add a `when` branch
  for each new platform.
- **`getMinecraftVersion(platform)`** — derives the MC version from the project name via regex, stripping a trailing
  `.0` (so `"forge-26.2.0"` → `"26.2"`). No changes needed here for new versions.

Failing to add a `pfVersion` entry causes `./gradlew checkReleaseVersion` (and `registerVersion`) to throw
`Unknown platform: <project-name>` at configuration time.

Example addition for MC 26.2:

```kotlin
fun pfVersion(platform: String): String {
    return when (platform) {
        "forge-26.2.0" -> catalog.findVersion("forge262000").map { it.requiredVersion }.get()
        "neoforge-26.2.0" -> catalog.findVersion("neo262000").map { it.requiredVersion }.get()
        // ...
    }
}
```
