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
                implementation(project(":core:ui"))
                implementation(project(":core:data"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.material)
                implementation(libs.androidx.compose.material.icons.core)
                implementation(libs.androidx.compose.material.icons.extended)
                implementation(compose.ui)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.kotlinx.datetime)
            }
        }
    }
}

android {
    namespace = "com.example.moon.feature.calendar"
    compileSdk = 37
    defaultConfig {
        minSdk = 24
    }
}
