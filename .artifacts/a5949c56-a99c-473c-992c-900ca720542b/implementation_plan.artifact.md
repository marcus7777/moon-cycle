# Wear OS Port Implementation Plan

This plan outlines the steps to add a Wear OS port to the Moon Cycle project. We will create a new `:wear` module, leverage existing core libraries, and implement a Wear OS optimized UI using Compose for Wear OS (Material 3).

## User Review Required

> [!IMPORTANT]
> The Wear OS app will be a standalone module within this project, sharing the `:core:domain`, `:core:data`, and `:core:ui` logic.
> Since Wear OS has different UI constraints (circular displays, limited space), we will implement a simplified version of the Main Screen and Calendar specifically for the watch.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/marcu/AndroidStudioProjects/moon/gradle/libs.versions.toml)
Add Wear OS specific dependencies.

#### [MODIFY] [settings.gradle.kts](file:///C:/Users/marcu/AndroidStudioProjects/moon/settings.gradle.kts)
Include the new `:wear` module.

#### [NEW] [wear/build.gradle.kts](file:///C:/Users/marcu/AndroidStudioProjects/moon/wear/build.gradle.kts)
Configure the new Wear OS application module.

### Core Logic & Resources

#### [NEW] [wear/src/main/AndroidManifest.xml](file:///C:/Users/marcu/AndroidStudioProjects/moon/wear/src/main/AndroidManifest.xml)
Standard Wear OS manifest with required permissions and `uses-feature android.hardware.type.watch`.

#### [NEW] [wear/src/main/res/values/strings.xml](file:///C:/Users/marcu/AndroidStudioProjects/moon/wear/src/main/res/values/strings.xml)
Wear-specific string resources if needed.

### Wear UI Implementation

#### [NEW] [wear/src/main/kotlin/com/example/moon/wear/MainActivity.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/wear/src/main/kotlin/com/example/moon/wear/MainActivity.kt)
Entry point for the Wear OS app, setting up the Compose content.

#### [NEW] [wear/src/main/kotlin/com/example/moon/wear/WearApp.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/wear/src/main/kotlin/com/example/moon/wear/WearApp.kt)
Main Wear OS navigation and theme setup using `AppScaffold` and `ScreenScaffold`.

#### [NEW] [wear/src/main/kotlin/com/example/moon/wear/presentation/MainScreen.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/wear/presentation/MainScreen.kt)
A simplified, glanceable view of the current moon phase, illumination, and next event.

#### [NEW] [wear/src/main/kotlin/com/example/moon/wear/presentation/CalendarScreen.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/wear/presentation/CalendarScreen.kt)
A `ScalingLazyColumn` or `TransformingLazyColumn` based calendar list view optimized for watch faces.

## Verification Plan

### Automated Tests
- Build the project using `./gradlew :wear:assembleDebug`.
- Run unit tests for any new shared view models if added (most will be reused).

### Manual Verification
- Deploy to a Wear OS emulator or device.
- Verify that the moon visualization renders correctly on a circular display.
- Verify that navigation between the main moon view and the calendar works as expected.
- Verify that location data is correctly retrieved and used for calculations.
