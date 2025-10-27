import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

dependencies {
    implementation("org.jsoup:jsoup:1.17.2")
}

// No Java sources; disable Java compile to avoid toolchain lookup in IDEs
tasks.withType<JavaCompile>().configureEach {
    enabled = false
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

application {
    mainClass.set("com.growtracker.tools.seedfinder.MainKt")
}
