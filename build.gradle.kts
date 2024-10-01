plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url= "https://jitpack.io")
    }
    dependencies {
        classpath(libs.gradle.v850)
        classpath(libs.kotlin.gradle.plugin)
    }
}

