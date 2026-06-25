import java.io.ByteArrayOutputStream
import java.util.zip.*

plugins {
    id("com.kotori316.common.java")
    id("com.kotori316.common.publish")
    id("com.kotori316.common.version")
    alias(libs.plugins.forge.gradle)
    alias(libs.plugins.forge.jarjar)
    signing
    alias(libs.plugins.publish.all)
}

evaluationDependsOn(":common")

val minecraftVersion = "26.2"
val forgeVersion = libs.versions.forge262000.get()
version = "${project.property("modVersion")}-mc${minecraftVersion}-${libs.versions.scala.get()}"
group = "com.kotori316" // http://maven.apache.org/guides/mini/guide-naming-conventions.html
base {
    archivesName = "ScalableCatsForce"
}

minecraft {
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
}

jarJar {
    register()
}

// The official cats-kernel ships package objects in Java-reserved-word packages
// (cats.kernel.instances.{byte,char,short,int,long,float,double,boolean}). These are legal in
// Scala but Forge's securejarhandler rejects them while building the module layer at boot
// ("Invalid package name: 'byte' is not a Java identifier"), before any mod/coremod/mixin runs.
// NeoForge's fork tolerates them, but Forge does not. These packages only contain `package object`
// convenience shims (for `import cats.kernel.instances.byte.*`). No class header references them, so
// dropping them does not break class loading; a few kernel companions (Eq$, Semigroup$, SortedSetOrder,
// ...) do reference them, but only in method-body forwarders that normal implicit resolution never picks.
// So we drop them from the bundled jar to make it JPMS-valid. Instances remain available via
// cats.implicits.*, cats.syntax.*, cats.kernel.instances.all.*, and cats.kernel.instances.<Type>Instances.
tasks.named("jarJar", org.gradle.jvm.tasks.Jar::class) {
    doLast {
        stripReservedCatsPackages(archiveFile.get().asFile)
    }
}

dependencies {
    implementation(minecraft.dependency(libs.forge262000))
    compileOnly(project(":common"))
    implementation(libs.scala)
    implementation(libs.bundles.cats) { isTransitive = false }
    "jarJar"(libs.scala) {
        jarJar.configure(this) {
            setVersion(libs.versions.scala.get())
            setRange("[${libs.versions.scala.get()},4.0)")
        }
    }
    "jarJar"(libs.bundles.cats) {
        jarJar.configure(this) {
            setVersion(libs.versions.cats.get())
            setRange("[2.0, ${libs.versions.cats.get()}]")
        }
    }

    // Test Dependencies.
    testImplementation(libs.jupiter.api)
    testImplementation(libs.jupiter.params)
    testRuntimeOnly(libs.jupiter.engine)
    testImplementation(libs.jupiter.launcher)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
}

tasks.compileJava {
    source(project(":common").sourceSets.main.get().allJava)
}

val releaseDebug = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()
val publishVersion = "${project.version}-forge"
publishMods {
    dryRun = releaseDebug
    type = STABLE
    file = provider {
        tasks.named(
            "jarJar",
            org.gradle.jvm.tasks.Jar::class
        )
    }.flatMap { it.flatMap { t -> t.archiveFile } }
    additionalFiles = files(
        provider { tasks.sourcesJar }.flatMap { it.flatMap { t -> t.archiveFile } },
    )
    modLoaders = listOf("forge")
    version = publishVersion
    displayName = publishVersion
    changelog = createChangelog()

    curseforge {
        accessToken = (project.findProperty("curseforge_additional-enchanted-miner_key") ?: System.getenv("CURSE_TOKEN")
        ?: "") as String
        projectId = project.property("curseId").toString()
        minecraftVersions = listOf(minecraftVersion)
        client = true
        server = true
    }
    modrinth {
        accessToken = (project.findProperty("modrinthToken") ?: System.getenv("MODRINTH_TOKEN") ?: "") as String
        projectId = project.property("modrinthId").toString()
        minecraftVersions = listOf(minecraftVersion)
    }
}

publishing {
    publications {
        create("mavenJava", MavenPublication::class) {
            artifactId = base.archivesName.get().lowercase()
            from(components["java"])
            pom {
                name = base.archivesName.get()
                description =
                    "Scala Loading library build with Minecraft $minecraftVersion and Forge $forgeVersion"
                url = "https://github.com/Kotori316/SLP"
                packaging = "jar"
                withXml {
                    val dependencies = asElement().getElementsByTagName("dependencies")
                    for (i in 0 until dependencies.length) {
                        val dependency = dependencies.item(i)
                        dependency.parentNode.removeChild(dependency)
                    }
                }
            }
        }
    }
}

val jksSignJar = tasks.register("jksSignJar") {
    dependsOn(tasks.jar, tasks.sourcesJar, tasks.devJar)
    val executeCondition = project.hasProperty("jarSign.keyAlias") &&
            project.hasProperty("jarSign.keyLocation") &&
            project.hasProperty("jarSign.storePass")
    onlyIf { executeCondition }
    doLast {
        listOf(tasks.jar, tasks.sourcesJar, tasks.devJar).forEach { t ->
            ant.withGroovyBuilder {
                "signjar"(
                    "jar" to t.get().archiveFile.get(),
                    "alias" to project.findProperty("jarSign.keyAlias"),
                    "keystore" to project.findProperty("jarSign.keyLocation"),
                    "storepass" to project.findProperty("jarSign.storePass"),
                    "sigalg" to "Ed25519",
                    "digestalg" to "SHA-256",
                    "tsaurl" to "http://timestamp.digicert.com",
                )
            }
        }
    }
}

tasks.assemble {
    dependsOn(jksSignJar)
}

signing {
    sign(publishing.publications)
}

val hasGpgSignature = project.hasProperty("signing.keyId") &&
        project.hasProperty("signing.password") &&
        project.hasProperty("signing.secretKeyRingFile")

tasks.withType(Sign::class) {
    onlyIf {
        hasGpgSignature
    }
}

fun createChangelog(): String {
    val t = """
        For Minecraft $minecraftVersion
        
        Built with Forge $forgeVersion
        
        This mod provides language provider, "kotori_scala".
        
        Scala: ${libs.versions.scala.get()}
        Cats: ${libs.versions.cats.get()}
        """.trimIndent()
    return t
}

ext["archivesBaseName"] = base.archivesName.get()
ext["generalDescription"] = createChangelog()

/**
 * Package directories named after Java reserved words that the official cats-kernel ships and
 * that Forge's module layer cannot accept.
 */
val reservedCatsPackages = listOf("byte", "char", "short", "int", "long", "float", "double", "boolean")
    .map { "cats/kernel/instances/$it/" }

/**
 * Rewrites the JarInJar fat jar in place, replacing the nested `cats-kernel_3-*.jar` with a copy
 * that has the reserved-word package directories removed. Compression method is preserved per
 * entry so the JarInJar locator keeps reading the nested jars unchanged.
 */
fun stripReservedCatsPackages(fatJar: File) {
    val tmp = File(fatJar.parentFile, "${fatJar.name}.stripping")
    var rewrote = false
    ZipFile(fatJar).use { zf ->
        ZipOutputStream(tmp.outputStream().buffered()).use { zos ->
            for (entry in zf.entries()) {
                val data = if (entry.name.matches(Regex("META-INF/jarjar/cats-kernel_3-.*\\.jar"))) {
                    rewrote = true
                    cleanNestedJar(zf.getInputStream(entry).readBytes())
                } else {
                    zf.getInputStream(entry).readBytes()
                }
                copyZipEntry(zos, entry.name, data, entry.method, entry.time)
            }
        }
    }
    if (!rewrote) {
        tmp.delete()
        throw GradleException("stripReservedCatsPackages: no nested cats-kernel jar found in ${fatJar.name}")
    }
    fatJar.delete()
    tmp.renameTo(fatJar)
    logger.lifecycle("Stripped reserved-word cats-kernel packages from ${fatJar.name}")
}

/**
 * Returns the bytes of [jarBytes] with all [reservedCatsPackages] entries removed.
 *
 * Read straight from memory with [ZipInputStream]: the cats jars are entirely `DEFLATED`, and even
 * `STORED` entries always carry their `size`/`crc` in the local header (data descriptors are only
 * allowed for `DEFLATED`), so streaming is reliable here. ([java.util.jar.JarInputStream] is
 * intentionally not used because it swallows `META-INF/MANIFEST.MF` from the entry iteration.)
 */
fun cleanNestedJar(jarBytes: ByteArray): ByteArray {
    val out = ByteArrayOutputStream(jarBytes.size)
    ZipInputStream(jarBytes.inputStream()).use { zis ->
        ZipOutputStream(out).use { zos ->
            var entry = zis.nextEntry
            while (entry != null) {
                val e = entry
                if (reservedCatsPackages.none { e.name.startsWith(it) }) {
                    copyZipEntry(zos, e.name, zis.readBytes(), e.method, e.time)
                }
                entry = zis.nextEntry
            }
        }
    }
    return out.toByteArray()
}

/**
 * Writes [data] to [zos] under [name], preserving the original compression [method] and [time].
 *
 * Built from a name-only [ZipEntry] (whose `size`/`crc`/`compressedSize` start at `-1`, i.e.
 * "unknown") rather than the `ZipEntry(ZipEntry)` copy constructor, because [data] is re-compressed
 * here and those size fields must be recomputed. The copy constructor would carry over the source's
 * `size`/`crc`/`compressedSize`, and for a `DEFLATED` entry that makes [ZipOutputStream] take the
 * "sizes already known" path and validate them in `closeEntry()` against the freshly deflated
 * output, throwing `ZipException: invalid entry compressed size`. They cannot be reset
 * either: `setSize(-1)` and `setCrc(-1)` throw `IllegalArgumentException` (only `setCompressedSize(-1)`
 * is allowed). A fresh entry sidesteps all of that; for `STORED` we must supply the sizes ourselves.
 * (`extra` fields / entry comments are dropped, which is irrelevant to class loading.)
 */
fun copyZipEntry(zos: ZipOutputStream, name: String, data: ByteArray, method: Int, time: Long) {
    val out = ZipEntry(name)
    out.method = method
    out.time = time
    if (method == ZipEntry.STORED) {
        out.size = data.size.toLong()
        out.compressedSize = data.size.toLong()
        out.crc = CRC32().apply { update(data) }.value
    }
    zos.putNextEntry(out)
    zos.write(data)
    zos.closeEntry()
}

