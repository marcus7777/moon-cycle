# Implementation Plan - Rename App to Moon Cycle

Rename the application from "Moon Phace" (or the current internal "moon") to "Moon Cycle" throughout the project.

## User Review Required

> [!NOTE]
> The string "Moon Phace" was not found in the source code, only in previous implementation plans (artifacts). The current user-facing name in `strings.xml` is "moon". I will rename this and all prominent references to "Moon Cycle".

## Proposed Changes

### App Resources

#### [MODIFY] [strings.xml](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/res/values/strings.xml)
- Update `app_name` from "moon" to "Moon Cycle".

### UI Components

#### [MODIFY] [MoonDetailScreen.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/screens/MoonDetailScreen.kt)
- Update title from "Moon Details" to "Moon Cycle Details" (or keep as is if it's generic, but I'll make it consistent). Actually, "Moon Details" is fine, but I'll check for other instances.

### Project Metadata

#### [MODIFY] [settings.gradle.kts](file:///C:/Users/marcu/AndroidStudioProjects/moon/settings.gradle.kts)
- Update `rootProject.name` to "moon-cycle".

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure the project still builds.

### Manual Verification
- Verify the `app_name` in `strings.xml`.
