plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp") // KSP for Room & Moshi codegen
}

android {
    namespace = "com.growtracker.app"
    compileSdk = 35

    // Firebase config via properties or environment (for programmatic init fallback)
    val fbAppId = (project.findProperty("FIREBASE_APP_ID") as String?) ?: System.getenv("FIREBASE_APP_ID") ?: ""
    val fbApiKey = (project.findProperty("FIREBASE_API_KEY") as String?) ?: System.getenv("FIREBASE_API_KEY") ?: ""
    val fbProjectId = (project.findProperty("FIREBASE_PROJECT_ID") as String?) ?: System.getenv("FIREBASE_PROJECT_ID") ?: ""
    val fbSenderId = (project.findProperty("FIREBASE_SENDER_ID") as String?) ?: System.getenv("FIREBASE_SENDER_ID") ?: ""
    val fbStorageBucket = (project.findProperty("FIREBASE_STORAGE_BUCKET") as String?) ?: System.getenv("FIREBASE_STORAGE_BUCKET") ?: ""

    defaultConfig {
        applicationId = "com.growtracker.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
    versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Security config placeholders (override via gradle.properties or CI vars for release)
        buildConfigField("String", "PIN_HOST", "\"\"")
        buildConfigField("String", "PIN_SHA256S", "\"\"") // comma-separated pins: sha256/AAAA...,sha256/BBBB...
        buildConfigField("String", "SIGNATURE_SHA256", "\"\"") // expected app signing cert SHA-256 (hex or base64 url-safe), empty = disabled

        // Firebase programmatic init fallback (if google-services.json is not packaged)
        buildConfigField("String", "FIREBASE_APP_ID", "\"${fbAppId}\"")
        buildConfigField("String", "FIREBASE_API_KEY", "\"${fbApiKey}\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${fbProjectId}\"")
        buildConfigField("String", "FIREBASE_SENDER_ID", "\"${fbSenderId}\"")
        buildConfigField("String", "FIREBASE_STORAGE_BUCKET", "\"${fbStorageBucket}\"")
    }

    // Load optional release signing credentials from gradle.properties or environment
    val storeFileProp = project.findProperty("RELEASE_STORE_FILE") as String?
    val storePasswordProp = project.findProperty("RELEASE_STORE_PASSWORD") as String?
    val keyAliasProp = project.findProperty("RELEASE_KEY_ALIAS") as String?
    val keyPasswordProp = project.findProperty("RELEASE_KEY_PASSWORD") as String?
    val hasReleaseSigning = listOf(storeFileProp, storePasswordProp, keyAliasProp, keyPasswordProp).all { it != null }

    if (hasReleaseSigning) {
        signingConfigs.create("release") {
            storeFile = file(storeFileProp!!)
            storePassword = storePasswordProp!!
            keyAlias = keyAliasProp!!
            keyPassword = keyPasswordProp!!
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (hasReleaseSigning) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.20"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Ensure the 'baselineProfile' configuration exists even if the AGP hasn't created it yet.
// AGP 8.x typically provides it for application modules; this makes the build resilient across environments.
configurations.maybeCreate("baselineProfile")

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")
    
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    // Explicit icon artifact versions to enable AutoMirrored API if present
    implementation("androidx.compose.material:material-icons-core:1.5.0")
    implementation("androidx.compose.material:material-icons-extended:1.5.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.2")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Security Crypto (EncryptedFile / EncryptedSharedPreferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // CameraX (explicit versions; BOM artifacts not resolving in current repo context)
    val cameraXVersion = "1.3.3"
    implementation("androidx.camera:camera-core:$cameraXVersion")
    implementation("androidx.camera:camera-camera2:$cameraXVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraXVersion")
    implementation("androidx.camera:camera-view:1.3.3")
    implementation("androidx.camera:camera-extensions:1.3.3")
    
    // TensorFlow Lite (base) – GPU / Support / Metadata can be added later
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    
    // Networking & Serialization
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.moshi:moshi:1.15.1")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Play Integrity API
    implementation("com.google.android.play:integrity:1.4.0")

    // Room (for upload queue persistence)
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Baseline Profile installation in app
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")

    // Consume generated baseline profiles from :baselineprofile module
    // Use string-based configuration to avoid unresolved accessor issues in some Kotlin DSL environments
    add("baselineProfile", project(":baselineprofile"))

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    // LeakCanary in Debug for leak detection
    // LeakCanary entfernt für Test-Builds – keine Leak-Analyse im ausgelieferten Debug/Release

    // Firebase (Analytics + Crashlytics). Messaging optional for Push.
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    // Optional: enable if you want push notifications soon
    // implementation("com.google.firebase:firebase-messaging-ktx")
}

// KSP configuration (e.g., Room schema location)
ksp {
    arg("room.schemaLocation", file("schemas").path)
}

// Apply Firebase plugins only if google-services.json is present to avoid build breaks locally
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

// Optionally apply App Distribution when explicitly enabled via property
val appDistEnabled = (project.findProperty("APP_DIST_ENABLE") as String?)?.toBoolean() ?: false
if (appDistEnabled) {
    apply(plugin = "com.google.firebase.appdistribution")
}

// Configure Firebase App Distribution from properties if enabled
extensions.findByName("firebaseAppDistribution")?.let {
    val serviceCreds = project.findProperty("APP_DIST_CREDENTIALS_FILE") as String?
    val appIdProp = project.findProperty("APP_DIST_APP_ID") as String?
    val testersProp = project.findProperty("APP_DIST_TESTERS") as String? // comma-separated emails
    val groupsProp = project.findProperty("APP_DIST_GROUPS") as String?   // comma-separated group aliases
    val releaseNotesProp = project.findProperty("APP_DIST_RELEASE_NOTES") as String? ?: "BETA-Version 1.0.3"
    val artifactPathProp = project.findProperty("APP_DIST_ARTIFACT") as String? // optional override

    it.apply {
        // Cast to the App Distribution extension and assign using JavaBean-style setters (v5 plugin API)
        val ext = this as com.google.firebase.appdistribution.gradle.AppDistributionExtension
        if (serviceCreds != null) ext.serviceCredentialsFile = serviceCreds
        if (appIdProp != null) ext.appId = appIdProp
        if (!artifactPathProp.isNullOrBlank()) ext.artifactPath = artifactPathProp
        ext.releaseNotes = releaseNotesProp
        if (!testersProp.isNullOrBlank()) ext.testers = testersProp
        if (!groupsProp.isNullOrBlank()) ext.groups = groupsProp
    }
}
