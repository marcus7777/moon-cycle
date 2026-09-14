# Implementation Plan - Landscape Scaling for Moon Cycle

This plan addresses the issue where the moon visualization is cut off in landscape mode. It ensures the graphic scales down to fit the screen height, maintains its 1:1 aspect ratio, and remains centered.

## Proposed Changes

### UI Components

#### [MODIFY] [MainScreen.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/screens/MainScreen.kt)
- Update `MoonVisualization` modifier to use `aspectRatio(1f, matchHeightConstraintsFirst = true)`. This ensures it fits the height in landscape mode.
- Change `fillMaxSize()` to `fillMaxHeight()` for better scaling in the `weight(1f)` Box.

#### [MODIFY] [MoonDetailScreen.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/screens/MoonDetailScreen.kt)
- Add `verticalScroll` to the main `Column` to ensure accessibility of all details in landscape mode.
- Ensure `MoonVisualization` maintains its aspect ratio relative to the row height.
- Adjust the `Row` height to be more flexible using `heightIn(max = 200.dp)` to prevent it from exceeding screen height on very small devices.

## Verification Plan

### Automated Tests
- Run existing unit tests to ensure no regressions in data handling.
- Build the app to verify compilation.

### Manual Verification
- Verify in the Android Studio Preview (Landscape) that the moon fits within the screen height.
- Verify that the background remains solid black.
- Verify that the realistic tilt is preserved.
