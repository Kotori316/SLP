# Adding a new Minecraft version subproject

When adding a new MC version (e.g. `forge-X.Y.Z` / `neoforge-X.Y.Z`), touch the following files:

## Files to update

1. **`gradle/libs.versions.toml`** — add the loader version string and a library alias entry.

2. **`build-logic/common/src/main/kotlin/com.kotori316.common.version.gradle.kts`** — add a `when` branch in
   `pfVersion(platform)` for each new platform.

   - **`pfVersion(platform)`** maps project name → loader version string from `libs.versions.toml`.
   - **`getMinecraftVersion(platform)`** derives the MC version via regex — no changes needed here.

   Failing to add a `pfVersion` entry causes `./gradlew checkReleaseVersion` (and `registerVersion`) to throw
   `Unknown platform: <project-name>` at configuration time.

3. **`settings.gradle.kts`** — include the new subproject and its example subproject inside the appropriate
   `DISABLE_FORGE` / `DISABLE_NEO_FORGE` guard.

4. **`<loader>-X.Y.Z/`** — create the subproject directory with `build.gradle.kts` (copy from the previous version
   and update the version references) and `src/` (copy source files from the previous version; no code changes needed
   for a version bump).

5. **`example/X.Y-<loader>/`** — create the example subproject with `build.gradle.kts`,
   `src/main/resources/META-INF/mods.toml`
   (update `versionRange` for the loader and minecraft), `src/main/resources/pack.mcmeta`, and
   `src/main/scala/…/ScalaExampleMod.scala` (copy from the previous version unchanged).

## Example addition for MC 26.3 (Forge 66.0.2 + NeoForge 26.3.0.6-beta)

**`libs.versions.toml`**:

```toml
[versions]
forge263000 = "26.3-66.0.2"
neo263000 = "26.3.0.6-beta"

[libraries]
forge263000 = { group = "net.minecraftforge", name = "forge", version.ref = "forge263000" }
neo263000 = { group = "net.neoforged", name = "neoforge", version.ref = "neo263000" }
```

**`com.kotori316.common.version.gradle.kts`**:
```kotlin
fun pfVersion(platform: String): String {
    return when (platform) {
       "forge-26.3.0" -> catalog.findVersion("forge263000").map { it.requiredVersion }.get()
       "neoforge-26.3.0" -> catalog.findVersion("neo263000").map { it.requiredVersion }.get()
        // ...
    }
}
```

**`example/26.3-forge/src/main/resources/META-INF/mods.toml`** — key version ranges to update:

```toml
[[dependencies.slp_examples]]
modId = "forge"
versionRange = "[66.0.0, 67)"
[[dependencies.slp_examples]]
modId = "minecraft"
versionRange = "[26.3.0, 26.4.0)"
```

**`example/26.3-neoforge/src/main/resources/META-INF/neoforge.mods.toml`** — key version ranges to update:

```toml
[[dependencies.slp_examples]]
modId = "neoforge"
versionRange = "[26.3.0, 26.4.0)"
[[dependencies.slp_examples]]
modId = "minecraft"
versionRange = "[26.3.0, 26.4.0)"
```
