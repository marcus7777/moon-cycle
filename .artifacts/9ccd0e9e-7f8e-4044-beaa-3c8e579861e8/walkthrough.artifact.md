# Minimalist Toggle Refinement Walkthrough

I have refined the Minimalist Toggle feature to provide a smoother and cleaner distraction-free experience.

## Changes Made

### Main Screen Enhancements
- **Truly Distraction-Free**: The **Calendar icon** in the top-right corner is now hidden when the text is toggled off. Only the moon and the subtle toggle button remain.
- **Smooth Transitions**: Integrated `AnimatedVisibility` with `fadeIn` and `fadeOut` effects for all UI elements that hide in minimalist mode (text, buttons, and calendar icon).
- **Status Bar Breathing Room**: Increased the top padding for the main phase title to `40.dp`, ensuring it sits comfortably below the status bar.
- **Accessible Toggle**: The visibility toggle button remains accessible in the top-left corner, using a subtle `0.5f` alpha to avoid being distracting.

## Verification Results

### Build
- Successfully ran `./gradlew :app:assembleDebug`.

### UI Logic
- The `isTextVisible` state appropriately controls the visibility of:
    - Location indicator
    - Moon phase title
    - Illumination percentage
    - Countdown to next event
    - "View Full Details" button
    - Calendar icon
- Animations provide a seamless transition between the full and minimalist views.
