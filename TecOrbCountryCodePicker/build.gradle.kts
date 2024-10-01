plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish") // Required for publishing

}

android {
    namespace = "com.tecorb.tecorbcountrycodepicker"
    compileSdkPreview = "UpsideDownCakePrivacySandbox"

    defaultConfig {
        minSdkPreview = "UpsideDownCakePrivacySandbox"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    buildFeatures{
        viewBinding = true
        buildConfig = true
        compose=true

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

}
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.libphonenumber.android)

}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                // Explicitly declare the AAR artifact
                artifact("$buildDir/outputs/aar/${project.name}-release.aar")
                groupId = "com.github.vivekkumar9798"
                artifactId = "TecOrbCountryCodePicker"
                version = "1.0.0"

                pom {
                    name.set("TecOrbCountryCodePicker")
                    description.set("A customizable Android library for selecting country codes in your app.")
                    url.set("https://github.com/vivekkumar9798/TecOrbCountryCodePicker")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("vivekkumar9798")
                            name.set("Vivek Kumar")
                            email.set("vivek2022@tecorb.co")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/vivekkumar9798/TecOrbCountryCodePicker.git")
                        developerConnection.set("scm:git:ssh://github.com:vivekkumar9798/TecOrbCountryCodePicker.git")
                        url.set("https://github.com/vivekkumar9798/TecOrbCountryCodePicker")
                    }
                }
            }
        }
    }
}

