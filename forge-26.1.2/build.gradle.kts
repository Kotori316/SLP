plugins {
    id("com.kotori316.common.java")
    id("com.kotori316.common.publish")
    id("com.kotori316.common.version")
    alias(libs.plugins.forge.gradle)
    signing
    alias(libs.plugins.publish.all)
}

val minecraftVersion = "26.1.2"
version = "${project.property("modVersion")}-mc${minecraftVersion}-${libs.versions.scala3.get()}"
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

dependencies {
    implementation(minecraft.dependency(libs.forge260102))
    implementation(libs.scala2)
    implementation(libs.scala3) { isTransitive = false }
    implementation(libs.bundles.cats) { isTransitive = false }

    // Test Dependencies.
    testImplementation(libs.jupiter.api)
    testImplementation(libs.jupiter.params)
    testRuntimeOnly(libs.jupiter.engine)
    testImplementation(libs.jupiter.launcher)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
}
