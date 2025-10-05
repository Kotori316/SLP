plugins {
    id("com.kotori316.common.java")
    id("com.kotori316.common.publish")
    id("com.kotori316.common.source")
    alias(libs.plugins.neoforge.moddev)
    signing
    alias(libs.plugins.publish.all)
}

version = "${libs.versions.scala3.get()}-build-${project.property("build_number")}"
group = "com.kotori316" // http://maven.apache.org/guides/mini/guide-naming-conventions.html
base {
    archivesName = "ScalableCatsForce-NeoForge"
}

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/org.scala-lang/scala-library
    implementation(libs.scala2)
    implementation(libs.scala3) { isTransitive = false }
    // https://mvnrepository.com/artifact/org.typelevel/cats-core
    implementation(libs.bundles.cats) { isTransitive = false }

    // Jar in Jar
    // jarJar(group: "org.scala-lang", name: "scala-library", version: "[${libs.versions.scala2.get()}, 3.0)") { isTransitive = false }
    // jarJar(group: "org.scala-lang", name: "scala3-library_3", version: "[3.0, ${libs.versions.scala3.get()}]") { isTransitive = false }
    // jarJar(group: "org.typelevel", name: "cats-core_3", version: "[2.0, ${libs.versions.cats.get()}]") { isTransitive = false }
    // jarJar(group: "org.typelevel", name: "cats-kernel_3", version: "[2.0, ${libs.versions.cats.get()}]") { isTransitive = false }
    // jarJar(group: "org.typelevel", name: "cats-free_3", version: "[2.0, ${libs.versions.cats.get()}]") { isTransitive = false }

    // Test Dependencies.
    testImplementation(libs.jupiter.api)
    testImplementation(libs.jupiter.params)
    testRuntimeOnly(libs.jupiter.engine)
    testImplementation(libs.jupiter.launcher)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
}

tasks.test {
    useJUnitPlatform()
}

neoForge {
    version = libs.versions.neo1219.get()

    unitTest {
        enable()
    }
}
