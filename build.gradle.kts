import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

val localProperties = Properties()

val localPropertiesFile =
    rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use {
        localProperties.load(it)
    }
}

val groqApiKey =
    localProperties.getProperty("GROQ_API_KEY") ?: ""

android {
    namespace = "com.example.keeptrack"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.keeptrack"

        minSdk = 24
        targetSdk = 37

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GROQ_API_KEY",
            "\"$groqApiKey\""
        )
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


}

dependencies {

    // =========================================================
    // COMPOSE
    // =========================================================

    implementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.graphics
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    implementation(
        libs.androidx.compose.material3
    )

    implementation(
        libs.androidx.compose.material.icons.extended
    )

    implementation(
        libs.androidx.activity.compose
    )


    // =========================================================
    // CORE
    // =========================================================

    implementation(
        libs.androidx.core.ktx
    )


    // =========================================================
    // LIFECYCLE / VIEWMODEL
    // =========================================================

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    implementation(
        libs.androidx.lifecycle.viewmodel.compose
    )


    // =========================================================
    // NAVIGATION
    // =========================================================

    implementation(
        libs.androidx.navigation.compose
    )


    // =========================================================
    // ROOM
    // =========================================================

    implementation(
        libs.androidx.room.runtime
    )

    implementation(
        libs.androidx.room.ktx
    )

    ksp(
        libs.androidx.room.compiler
    )


    // =========================================================
    // ML KIT OCR
    // =========================================================

    implementation(
        "com.google.mlkit:text-recognition:16.0.1"
    )

    // =========================================================
// GROK AI API
// =========================================================

    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")


    // =========================================================
    // TESTING
    // =========================================================

    testImplementation(
        libs.junit
    )

    androidTestImplementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )


    // =========================================================
    // DEBUG
    // =========================================================

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )
}