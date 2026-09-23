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
*   **IFS-Lunar Integration**: Specialized text and audio prompts based on **Internal Family Systems (IFS)**, synchronised with lunar phases.
    *   *New Moon (Invitation)*: "The moon rests in darkness again. I wonder—what Part within me is already awake, quietly waiting to be noticed? Can I, from Self, simply say, 'I see you. There’s no rush, no demand—just space. Would you like to walk this cycle with me?'"
    *   *Day 1 (Tending the Spark): "The faintest light returns. Is there a Part that feels hesitant to step forward, unsure if it’s safe to be seen? Can I, from Self, offer a quiet invitation: 'You don’t have to be ready. Just being here is enough. I’m listening'?"
    *   *Day 2 (Gentle Momentum)*: "The light barely visible. Which Part is eager to push forward, and which one hesitates? can you write from the one that needs to talk most?"
    *   *Day 3 (Holding Space)*: "The light is still so soft. Is there a Part that’s been overlooked, not because it’s loud, but because it’s been quiet for so long? Gently say, 'I don’t need you to prove your worth. You belong here, just as you are'?"
    *   *Day 4 (Soft Holding)*: "The moon’s glow is still tender. Is there a Part that wants to withdraw, to wait until things feel safer? Can we simply keep the space warm—no pressure, no push—just a quiet 'I’m here' to every part within?"
    *   *Day 5 (Whispers of Alignment)*: "The moon carries a little more light tonight. Is there a Part softly tugging my attention—maybe one I’ve overlooked? Can we lean in with kindness and ask, 'What do you need for me to hear you?'"
    *   *Day 6 (Bridging Inner Worlds)*: "The light is growing, and so is the pull toward action. Are any Parts starting to align around the intention, while others still linger in doubt? Can I, from Self, welcome both—inviting movement without leaving anyone behind?"
    *   *Day 7 (Tending the Spark)*: "The moon is finding its shape, and so am I. Is there a Part that’s starting to believe in this path? Can I, from Self, gently celebrate that flicker of faith—without rushing ahead—just tending the spark with care?"
    *   *Waxing Moon (Growth)*: "Which 'Manager' Parts are working hard to achieve my goals right now? Can I acknowledge their effort with Self-compassion?"
    *   *Day After Waxing (Balancing Effort)*: "As action builds, am I noticing any Parts taking on too much? Can I, from Self, pause and ask, 'Who needs relief so we can move forward together—not just pushed by the busiest Parts?'"
    *   *Mid-Waxing (Inviting Collaboration)*: "The moon grows fuller, and so does the call to act. Are there Parts that have been quiet—maybe creative or playful ones—waiting to contribute? Can I, from Self, gently invite them in, so growth doesn’t just come from effort, but from aliveness?"
    *   *Approaching Fullness (Holding Tension)*: "The light is nearly full, and so is my inner world. Are there Parts in conflict—one pushing forward, another pulling back? Can I, from Self, hold the tension without needing to fix it, letting wholeness include both?"
    *  *Deepening Light (Welcoming the Edges)*: "The fullness is near, and so is the intensity. Are there Parts I’ve been avoiding—maybe too loud, too raw, or too tender? Can I, from Self, turn toward them not to change them, but to say, 'You belong here too'?"
    *  *Near the Peak (Softening the Edges)*: "The light is almost full, and so is the pressure. Is there a Part trying to 'get it right' for everyone? Can I, from Self, gently remind them that wholeness isn’t about perfection—but about presence, even in the wobble?"
    *  *Threshold of Fullness (Listening to the Hush)*: "The moon holds its breath before the peak. In the stillness, is there a Part speaking in whispers—maybe one I’ve silenced to stay strong? Can I, from Self, lean in close and let that quiet voice be the one that guides me now?"
    *   *The Brightening (Honouring the Build): "The moon is almost full, and so is my inner landscape. Are there Parts that have carried quiet burdens to get me here? Can I, from Self, pause and say, 'I see you, and I thank you—for your strength, your silence, your steady hold'?"
    *   *The Final Approach (Surrender Before the Peak)*: "One breath from fullness. Is there a Part still trying to control how this unfolds? Can I, from Self, gently invite it to rest—not by force, but by offering, 'I’ve got us. You don’t have to hold on so tightly any more'?"
    *   *Full Moon (Release)*: "In this peak energy, am I noticing any 'Firefighter' Parts reacting to intensity? How can I bring Self-presence to soothe the flames?"
    *   *The Turn (Softening the Glow)*: "The moon begins its gentle release. Is there a Part that wants to hold on tight to this peak—afraid of what fades with the light? Can I, from Self, offer a steady hand, reminding them that letting go isn’t loss, but trust in the cycle?"
    *   *Releasing the Charge (Updating)*: "The moon is turning, and so is the energy. Are there 'Firefighter' Parts still acting from an old threat—holding a picture of danger that’s no longer true? Can I, from Self, gently offer a new image: 'Look around. See the light, feel the breath. We’re not there any more. We’re here'?"
    *   *Updating the Inner Map*: "The light continues to soften. Is there a Part still braced for a storm that has already passed—holding onto an old picture of danger? Can I, from Self, gently offer a new view: 'Look around. Feel the stillness. The threat is gone. We’re here, in this quiet, and we’re safe'?"
    *   *Curious Inquiry*: "The light is still fading. I wonder—what’s it like for the Parts who’ve been on watch? What are they noticing now, as the intensity softens? And if they’re still holding tension, what world are they seeing—one that’s still stormy, or one that’s already calm?"
    *   *Tending the Quiet*: "The moon is less full now, and the energy is turning inward. I wonder—what’s it like for the Parts who’ve been loud or active? Are they winding down on their own, or is there a part of me that’s unsure how to let go of the charge? Can I, from Self, simply ask: 'What do you need to feel safe in this stillness?'"
    *   *Listening Beneath the Surface*: "The moon continues to wane, and the inner world grows quieter. I wonder—what’s it like for the Parts who rarely speak up? Are they resting, waiting, or simply feeling unseen? Can I, from Self, gently ask: 'What have you been holding? And what would it feel like to let it be known, just a little?'"
    *   *Waning Moon (Reflection)*: "As the light fades, are there any 'Exile' Parts carrying old burdens that are ready to be seen? What does my Self-leadership look like for them tonight?"
    *   *Honouring the Hidden*: "The dark is growing, and so is the invitation to listen. Is there a Part that’s been exiled long ago, still holding a story that’s never been told? Can I, from Self, gently ask: 'What do you need for me to finally hear you—not to fix, but to witness?'"
    *   *Approaching the Dark (Tender Witnessing)*: "The moon is nearly gone, and the inner world feels hushed. Is there a Part carrying an old wound that’s been hidden, not because it wants to stay buried, but because it’s waited so long to be met with kindness? Can I, from Self, offer not solutions, but soft presence—just saying, 'I’m here. You don’t have to carry this alone any more'?"
    *   *Deepening Stillness (Compassionate Holding):* "The light is almost gone, and the silence grows. Is there a Part that’s been afraid to speak, not because it’s angry, but because it’s tender—afraid of being too much, or not enough? Can I, from Self, simply say: 'You are safe here. Your softness is not weakness. I’ve got you'?"
    *   *Threshold of the Dark (Sacred Waiting):* "We’re nearing the moon’s return to dark. Is there a Part that feels empty, as if something’s missing? Can I, from Self, gently remind it: 'This isn’t loss—it’s preparation. The void isn’t empty; it’s full of what’s waiting to be born'?"
    *   *On the Edge of Return (Whispering Gratitude):* "In this deepest quiet, I wonder—what would it feel like to thank the Parts who’ve carried the weight, even when I didn’t know their names? Can I, from Self, offer a quiet gratitude: 'Thank you for holding on. I see you now. And I’m here to hold you'?"
    *   *In the Quiet (Soft Reassurance):* "The moon is dark now, and the world feels still. Is there a Part that fears this emptiness, as if stillness means absence? Can I, from Self, gently whisper: 'This is not abandonment. This is belonging. You are not alone in the dark—I’m right here with you'?"
    *   *Just Before the New (Tending the Embers):* "The cycle is about to turn. Beneath the silence, is there a Part that’s been waiting—not demanding, just hoping to be seen? Can I, from Self, lean in close and say: 'I know you’ve been here all along. Thank you for your patience. Let’s begin again, together'?"


### 5.2. Expansion
*   **Complications**: Wear OS support for moon phase watch face complications.
*   **Extended Astronomy**: Support for planet positions and meteor shower notifications.
*   **Notifications**: Configurable alerts for upcoming Full Moons, New Moons, or other significant lunar events.
