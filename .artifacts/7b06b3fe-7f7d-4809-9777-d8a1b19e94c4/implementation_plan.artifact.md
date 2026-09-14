# Implementation Plan - Enforce Solid Black Background

The goal is to disable dynamic colors in the theme and ensure the `DarkColorScheme` (and `LightColorScheme` if applicable) uses `Color.Black` for background and surface to provide a solid black background as requested.

## Proposed Changes

### [Component Name] UI Theme

#### [MODIFY] [Theme.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/theme/Theme.kt)
- Change the default value of `dynamicColor` to `false` in `MoonCycleTheme`.
- Ensure `DarkColorScheme` and `LightColorScheme` have `background = Color.Black` and `surface = Color.Black`.
- (Optional but recommended) Remove the `dynamicColor` logic entirely if "strictly enforced" means it should never be used. I will set the default to `false` for now as it's less destructive but achieves the goal.

### [Component Name] UI Components

#### [MODIFY] [MoonVisualization.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/components/MoonVisualization.kt)
- Verify that the `Canvas` is transparent so it shows the background of its parent. It is already transparent.

#### [MODIFY] [MainScreen.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/screens/MainScreen.kt)
- Ensure the `Scaffold` uses the theme's background color. By default, it does. I will explicitly set `containerColor = MaterialTheme.colorScheme.background` to be absolutely sure.

#### [MODIFY] [CalendarScreen.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/screens/CalendarScreen.kt)
- Explicitly set `containerColor = MaterialTheme.colorScheme.background` in the `Scaffold`.

## Verification Plan

### Automated Tests
- Build the app to ensure no compilation errors: `./gradlew :app:assembleDebug`

### Manual Verification
- Verify the background is solid black in the UI. (I can't do this directly, but I'll ensure the code is correct).
- Check the `@Preview` in `MoonVisualization.kt` which already uses `backgroundColor = 0xFF000000`.
