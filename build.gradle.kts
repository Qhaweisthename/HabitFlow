// Root build.gradle.kts

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // 🔔 Google Services Gradle plugin
        classpath("com.google.gms:google-services:4.4.2")
    }
}

plugins {
    // Keep using your version catalog plugins for modules
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
apply(plugin = "com.google.gms.google-services")
