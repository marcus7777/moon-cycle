# Moon Cycle: Application Specification

## 1. Overview
Moon Cycle is a modern, high-precision lunar tracking and journaling application for Android. It combines scientific astronomical data with a minimalist, interactive user interface. The app provides real-time moon phase visualization, a true lunar cycle calendar, and a private daily reflection journal.

---

## 2. Core Features

### 2.1. Dynamic Moon Visualization
*   **Vector Engine**: High-fidelity rendering of the moon using the unified `MoonVectorEngine`.
*   **Accuracy**: Calculations account for phase, illumination, age, and parallactic angle based on the user's specific latitude and longitude.
*   **Interactivity**: The primary moon visualization on the `MainScreen` acts as the gateway to the rest of the application.

### 2.2. Intelligent Navigation (Tap-First Model)
*   **Minimalist Gestures**: Navigation is driven by responsive taps rather than complex swiping.
*   **Main Screen → Calendar**: Tapping the central moon visualization opens the calendar overlay.
*   **Main Screen → Details**: Tapping the phase description or event countdown opens the deep-dive details and settings page.
*   **Idle Timeout**: The app automatically returns to the "Zen" moon screen after 1 minute of inactivity (typing or touch resets this timer).

### 2.3. True Lunar Calendar & Journaling
*   **Cycle-Based Calendar**: Unlike Gregorian calendars, months are defined by the actual synodic period (New Moon to New Moon).
*   **Grid Visualization**: Displays moon phases for each day within the current lunar cycle, with indicators for events and existing journal entries.
*   **Dual-Tap Journaling**:
    *   **Single Tap**: Selects a date to view its specific lunar events and a summary of your daily note.
    *   **Double Tap (Tap Selected)**: Opens a full-screen, distraction-free editing environment for the selected day's note.
*   **Landscape Optimization**: A side-by-side layout in landscape mode provides maximum visibility for both the calendar grid and the journaling summary/settings link.

### 2.4. System Integration
*   **Dynamic Launcher Icons**: The app icon changes its appearance on the home screen to match the current phase of the moon and the user's hemisphere (Northern vs. Southern).
*   **Dynamic Wallpaper**: A background worker periodically updates the device's system wallpaper to show the current moon against a stark black background.
*   **Home Screen Widget**: Built with Jetpack Glance, providing a live, high-precision moon phase visualization directly on the home screen. Tapping the widget launches the app.
*   **Location Awareness**: Supports both real-time GPS location and a manual "City" override for astronomical calculations.

### 2.5. Data Portability
*   **Backup & Recovery**: Users can export and import their daily notes using the standard JSONL format.
*   **Calendar Sync**: Export/Import capabilities for iCal (.ics) format to integrate lunar events and notes with external calendar apps.

---

## 3. Technical Architecture

### 3.1. Project Structure (Multi-Module)
*   `:app`: Android entry point, `WorkManager` implementation, `Glance` widget logic, and `IconManager`.
*   `:core:domain`: Pure Kotlin module containing repository interfaces and data models (`MoonData`, `LunarEvent`).
*   `:core:data`: Implementations for `AstronomyRepository`, `LocationRepository`, and `NoteRepository` (SQLite/Room).
*   `:core:ui`: Shared Jetpack Compose components and the unified `MoonVectorEngine`.
*   `:feature:*`: Independent modules for `Main`, `Calendar`, `Details`, and `Navigation`.

### 3.2. Rendering Pipeline
*   **Shared Renderer**: `MoonBitmapRenderer` allows the app, wallpaper worker, and widget to share identical high-quality drawing logic.

### 3.3. State Management
*   **MVI/MVVM**: Features use `StateFlow` to expose immutable UI state from ViewModels.
*   **Navigation3**: Utilizes the modern `androidx.navigation3` for state-driven overlay management.

---

## 4. Visual Design Language
*   **Primary Color**: Stark Black (`#000000`).
*   **Accent Color**: Soft Lunar White and Muted Grays.
*   **Typography**: Clean, sans-serif Material 3 typography with an emphasis on legibility and hierarchy.
*   **Translucency**: Overlays utilize semi-transparent backgrounds to keep the central moon visualization always present as a "hero" element.

---

## 5. Future Roadmap

### 5.1. Enhanced Journaling
*   **Audio Recording**: Ability to record and attach voice reflections directly to daily entries.
*   **Audio Prompts**: Guided spoken prompts to facilitate meditative journaling sessions.
*   **IFS-Lunar Integration**: Specialized text and audio prompts based on **Internal Family Systems (IFS)**, synchronized with lunar phases.
    *   *New Moon (Intention)*: "As we enter the dark phase, which Part of me is most active in setting intentions? How can I lead from 'Self' to ensure all Parts feel heard in this new cycle?"
    *   *Waxing Moon (Growth)*: "Which 'Manager' Parts are working hard to achieve my goals right now? Can I acknowledge their effort with Self-compassion?"
    *   *Full Moon (Release)*: "In this peak energy, am I noticing any 'Firefighter' Parts reacting to intensity? How can I bring Self-presence to soothe the flames?"
    *   *Waning Moon (Reflection)*: "As the light fades, are there any 'Exile' Parts carrying old burdens that are ready to be seen? What does my Self-leadership look like for them tonight?"

### 5.2. Expansion
*   **Complications**: Wear OS support for moon phase watch face complications.
*   **Extended Astronomy**: Support for planet positions and meteor shower notifications.
*   **Notifications**: Configurable alerts for upcoming Full Moons, New Moons, or other significant lunar events.
