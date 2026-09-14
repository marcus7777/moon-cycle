# Minimalist Toggle Refinement Plan

Refine the "Minimalist Toggle" feature to provide a smoother, distraction-free experience.

## Proposed Changes

### UI Components

#### [MODIFY] [MainScreen.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/screens/MainScreen.kt)
- Wrap the Calendar icon in `AnimatedVisibility` so it's hidden in minimalist mode.
- Use `AnimatedVisibility` with `fadeIn()` and `fadeOut()` transitions for all text elements, buttons, and the location indicator.
- Add additional top padding to the phase title to provide more space from the status bar.
- Ensure the toggle button (visibility icon) remains always visible but subtle.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure the project still builds.

### Manual Verification
- Toggle the minimalist mode and verify:
    - Text and buttons fade out/in smoothly.
    - Calendar icon fades out/in smoothly.
    - Moon remains visible.
    - Toggle button remains visible.
    - Phase title has more breathing room at the top.
