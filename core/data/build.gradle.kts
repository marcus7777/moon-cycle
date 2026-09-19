plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core:domain"))
                implementation(libs.kastro)
                implementation(libs.androidx.datastore.preferences.core)
                implementation(libs.kotlinx.serialization.core)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.play.services.location)
                implementation(libs.kotlinx.coroutines.play.services)
                implementation(libs.androidx.datastore.preferences.impl)
            }
        }
    }
}

android {
    namespace = "com.example.moon.core.data"
    compileSdk = 37
    defaultConfig {
        minSdk = 24
    }
}
