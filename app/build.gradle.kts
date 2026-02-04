plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.airfist.share"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.airfist.share"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // Java 17 force karna zaroori hai GitHub ke liye
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Basic UI
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Nearby Share Logic
    implementation("com.google.android.gms:play-services-nearby:19.0.0")

    // Fist Gesture Logic (Stable Version)
    implementation("com.google.mlkit:hand-detection:16.0.0")

    // Camera logic
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
}
