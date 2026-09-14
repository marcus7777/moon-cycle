# Implementation Plan - Navigation 3 and Adaptive Main UI

Setting up Jetpack Navigation 3 and Compose Material Adaptive layout for the Moon Cycle app.

## Proposed Changes

### [Component Name] UI and Navigation

#### [NEW] [MoonNavigation.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/navigation/MoonNavigation.kt)
- Define `NavKey` serializable objects for routes: `MoonList`, `MoonDetail`, `Settings`.
- Implement `MoonNavigation` composable using `NavDisplay` and `rememberNavBackStack`.
- Integrate `ListDetailSceneStrategy` for adaptive layout.

#### [NEW] [MoonVisualization.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/components/MoonVisualization.kt)
- Implement a custom moon rendering component.
- Add hemispheric logic: flip the moon horizontally if the user is in the Southern Hemisphere (latitude < 0).

#### [NEW] [MainScreen.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/screens/MainScreen.kt)
- Create the adaptive layout structure.
- List pane: Navigation/Phase selection.
- Detail pane: `MoonVisualization`.
- Extra pane: Detailed moon data.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/MainActivity.kt)
- Initialize `MoonNavigation` as the content.
- Ensure `enableEdgeToEdge()` is called.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify compilation.
- (Optional) Implement a unit test for Hemispheric rendering logic.

### Manual Verification
- Verify that the app starts and shows the moon.
- Check adaptive layout behavior (simulated or real devices).
- Verify moon orientation for different latitudes.
