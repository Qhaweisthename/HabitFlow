// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Google Services Gradle plugin
        classpath("com.google.gms:google-services:4.4.4")
    }
}

plugins {
    // Version catalog plugin references
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

// Apply Google Services globally (needed for Firebase)
apply(plugin = "com.google.gms.google-services")
