plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    androidTarget()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core:domain"))
                implementation(project(":core:data"))
                implementation(project(":core:ui"))

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)

                implementation(libs.androidx.wear.compose.material3)
                implementation(libs.androidx.wear.compose.foundation)
                implementation(libs.androidx.wear.compose.navigation)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.core.ktx)
                implementation(libs.play.services.location)
                implementation(libs.kotlinx.coroutines.play.services)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.lifecycle.runtime.compose)
            }
        }
    }
}

android {
    namespace = "com.example.moon.wear"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.moon.wear"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            res.srcDirs("src/main/res")
        }
    }
}
