// This file is located at C:/Users/aatif/AndroidStudioProjects/HabitFlow/app/build.gradle.kts

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.habitflow"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.habitflow"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // --- FIX #1: CORRECT JAVA VERSION ---
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // This is needed to use Kotlin with the correct Java version
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Required AndroidX libraries for a basic app
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase BOM and Authentication
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-auth-ktx")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Lifecycle / ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Material UI
    implementation("com.google.android.material:material:1.12.0")

    // --- FIX #2: CORRECT NAVIGATION DEPENDENCY ---
    // Replaced 'libs.androidx.navigation.fragment' with the standard declaration
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // --- FIX #3: REMOVED CONFLICTING FILAMENT DEPENDENCY ---
    // implementation(libs.filament.android) // This line was removed

    // Test (optional)
    testImplementation("junit:junit:4.13.2")
}
