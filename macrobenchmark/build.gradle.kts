plugins {
    id("com.android.test")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.growtracker.macrobenchmark"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Benchmark against the release variant for realistic performance
    targetProjectPath = ":app"

    buildTypes {
        // Define a release build type for the test APK. Since the app's release is minified,
        // enable minify here too to satisfy the obfuscation check. Keep it debuggable for profiling.
        create("release") {
            isMinifyEnabled = true
            isDebuggable = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.test:runner:1.6.2")
    implementation("androidx.test:rules:1.6.1")
    implementation("androidx.test.ext:junit:1.2.1")
    implementation("androidx.test.uiautomator:uiautomator:2.3.0")
    implementation("androidx.benchmark:benchmark-macro-junit4:1.3.1")
}
