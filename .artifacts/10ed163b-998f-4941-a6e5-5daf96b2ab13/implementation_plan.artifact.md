# Modular Refactor and Transparent Overlay Navigation

This plan addresses the "Unresolved reference 'alpha'" error, refactors the project into separate Gradle modules for better organization, and implements a layered navigation system to achieve "true transparency" when sliding in the calendar and note views.

## User Review Required

> [!IMPORTANT]
> **Gradle Module Restructuring**: This refactor involves moving almost all files into new modules (`:core:domain`, `:core:data`, `:feature:main`, etc.). This will significantly change the project structure and requires a full project sync.
>
> **ViewModel Splitting**: `MoonViewModel` will be split into feature-specific ViewModels (`MainViewModel`, `CalendarViewModel`, `MoonDetailViewModel`) to follow standard modularity patterns.

## Proposed Changes

### Build Configuration

#### [MODIFY] [settings.gradle.kts](file:///C:/Users/marcu/AndroidStudioProjects/moon/settings.gradle.kts)
Register new modules: `:core:domain`, `:core:data`, `:core:ui`, `:feature:main`, `:feature:calendar`, `:feature:details`, `:feature:navigation`.

#### [NEW] [core-domain/build.gradle.kts](file:///C:/Users/marcu/AndroidStudioProjects/moon/core/domain/build.gradle.kts)
Multiplatform module for models and repository interfaces.

#### [NEW] [core-data/build.gradle.kts](file:///C:/Users/marcu/AndroidStudioProjects/moon/core/data/build.gradle.kts)
Multiplatform module for repository implementations.

#### [NEW] [feature-main/build.gradle.kts](file:///C:/Users/marcu/AndroidStudioProjects/moon/feature/main/build.gradle.kts)
Multiplatform module for the Main Screen.

---

### Core Layer

#### [MOVE] Domain Models and Repositories
- Move to `:core:domain`
- [AstronomyRepository](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/commonMain/kotlin/com/example/moon/domain/repository/AstronomyRepository.kt)
- [LocationRepository](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/commonMain/kotlin/com/example/moon/domain/repository/LocationRepository.kt)
- [NoteRepository](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/commonMain/kotlin/com/example/moon/domain/repository/NoteRepository.kt)

#### [MOVE] Data Implementations
- Move to `:core:data`
- [AstronomyRepositoryImpl](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/commonMain/kotlin/com/example/moon/data/repository/AstronomyRepositoryImpl.kt)
- [LocationRepositoryImpl](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/androidMain/kotlin/com/example/moon/data/repository/LocationRepositoryImpl.kt)
- [NoteRepositoryImpl](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/androidMain/kotlin/com/example/moon/data/repository/NoteRepositoryImpl.kt)

---

### UI & Feature Layer

#### [MOVE] Shared UI Components
- Move to `:core:ui`
- [MoonVisualization](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/commonMain/kotlin/com/example/moon/ui/components/MoonVisualization.kt)

#### [REFACTOR] Feature Screens and ViewModels
Split `MoonViewModel` and move screens:
- **Main Feature**: `MainScreen` + `MainViewModel` in `:feature:main`.
- **Calendar Feature**: `CalendarScreen` + `CalendarViewModel` in `:feature:calendar`.
- **Details Feature**: `MoonDetailScreen` + `MoonDetailViewModel` in `:feature:details`.

---

### Navigation & Transparency

#### [MODIFY] [MoonNavigation.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/commonMain/kotlin/com/example/moon/ui/navigation/MoonNavigation.kt)
- **Overlay Implementation**: Replace `ListDetailPaneScaffold` with a `Box` layout that renders `MainScreen` as a persistent background.
- **Slide-in Animation**: Use `AnimatedVisibility` with `slideInVertically` to overlay `CalendarScreen` and `MoonDetailScreen` over the `MainScreen`.
- **Fix Alpha Error**: Use `Modifier.graphicsLayer { alpha = ... }` for performance and to resolve potential import issues.

## Verification Plan

### Automated Tests
- Run `./gradlew build` to ensure all modules compile and dependencies are correctly wired.

### Manual Verification
1. Launch the app.
2. Tap the calendar icon. The calendar should slide in from the bottom/side while the moon visualization remains visible through any transparent areas.
3. Tap the detail view. It should similarly overlay the main screen.
4. Verify that background updates (e.g., moon phase changes) are still reflected even when an overlay is visible.
