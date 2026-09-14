# Implementation Plan - Refactor to Kotlin Multiplatform (KMP)

Refactor the Moon Cycle project into a Kotlin Multiplatform structure to support Android and Web (Wasm) targets.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/marcu/AndroidStudioProjects/moon/gradle/libs.versions.toml)
- Add `compose-multiplatform` plugin version (1.12.0).
- Add `kotlin-multiplatform` plugin.
- Add `compose-multiplatform` to plugins.

#### [MODIFY] [build.gradle.kts (root)](file:///C:/Users/marcu/AndroidStudioProjects/moon/build.gradle.kts)
- Apply `kotlin("multiplatform")` and `org.jetbrains.compose` plugins as `apply false`.

#### [MODIFY] [build.gradle.kts (app)](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/build.gradle.kts)
- Apply `kotlin("multiplatform")` and `org.jetbrains.compose` plugins.
- Configure `androidTarget()`.
- Configure `wasmJs()`.
- Define source sets (`commonMain`, `androidMain`, `wasmJsMain`).
- Move dependencies to appropriate source sets.

### UI & Logic Migration

#### [NEW] [LocationProvider.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/commonMain/kotlin/com/example/moon/data/location/LocationProvider.kt)
- Define `expect class LocationProvider`.

#### [NEW] [LocationProvider.android.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/androidMain/kotlin/com/example/moon/data/location/LocationProvider.kt)
- Implement `actual class LocationProvider` using `FusedLocationProviderClient`.

#### [NEW] [LocationProvider.wasmJs.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/wasmJsMain/kotlin/com/example/moon/data/location/LocationProvider.kt)
- Implement `actual class LocationProvider` (stub or navigator.geolocation).

#### [MOVE] Source code to `commonMain`
- Move domain, data (AstronomyRepository), and UI (Screens, Components, ViewModels) to `commonMain`.
- Update imports if necessary (though Compose Multiplatform uses `androidx.compose` package names).

### Target Support

#### [NEW] [index.html](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/wasmJsMain/resources/index.html)
- Basic HTML entry point for Wasm target.

#### [NEW] [main.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/wasmJsMain/kotlin/main.kt)
- Entry point for Compose Wasm.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify Android build.
- Run `./gradlew :app:compileKotlinWasmJs` to verify Wasm compilation.

### Manual Verification
- Verify that the app still runs on Android and the UI is intact.
