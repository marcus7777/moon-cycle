plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    androidTarget()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core:domain"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(libs.androidx.compose.ui.tooling.preview)
                implementation(libs.kotlinx.datetime)
            }
        }
    }
}

android {
    namespace = "com.example.moon.core.ui"
    compileSdk = 37
    defaultConfig {
        minSdk = 24
    }
}
