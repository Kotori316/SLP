plugins {
    id("com.kotori316.common.java")
    id("com.kotori316.common.publish")
    id("com.kotori316.common.version")
    alias(libs.plugins.forge.gradle)
    alias(libs.plugins.forge.jarjar)
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

jarJar {
    register()
}

dependencies {
    implementation(minecraft.dependency(libs.forge260102))
    implementation(libs.scala2)
    implementation(libs.scala3) { isTransitive = false }
    implementation(libs.bundles.cats) { isTransitive = false }
    "jarJar"(libs.scala2) {
        jarJar.configure(this) {
            setVersion(libs.versions.scala2.get())
            setRange("[${libs.versions.scala2.get()},3.0)")
        }
    }
    "jarJar"(libs.scala3) {
        jarJar.configure(this) {
            setVersion(libs.versions.scala3.get())
            setRange("[${libs.versions.scala3.get()},4.0)")
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
