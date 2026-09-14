# Implementation Plan: Scale Moon Visualization to Fit Screen Bounds

Ensure the moon visualization in the `MainScreen` scales appropriately across different device sizes and doesn't bleed off the edges.

## Proposed Changes

### [UI Components]

#### [MODIFY] [MoonVisualization.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/components/MoonVisualization.kt)
- Add internal padding to the moon drawing logic to ensure a safety margin even if the modifier doesn't provide enough padding.
- Ensure the `Canvas` always respects the aspect ratio of 1:1.

#### [MODIFY] [MainScreen.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/screens/MainScreen.kt)
- Replace hardcoded `Modifier.size(300.dp)` with a more flexible constraint.
- Use `Modifier.weight(1f)` within the `Column` and `Modifier.aspectRatio(1f)` to allow the moon to occupy available space while remaining square and centered.
- Apply appropriate padding.

## Verification Plan

### Automated Tests
- Build the project to ensure no regressions: `./gradlew :app:assembleDebug`

### Manual Verification
- Verify the `MoonVisualizationPreview` in Android Studio to see how it scales with `Modifier.fillMaxSize()`.
- Check different screen sizes (phone, tablet) to ensure the moon is centered and fully visible.
- Verify the adaptive layout (ListDetail) on tablets/large screens.
