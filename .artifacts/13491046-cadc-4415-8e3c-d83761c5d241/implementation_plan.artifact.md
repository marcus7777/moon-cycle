# Implementation Plan - Lunar Event Calendar

Implement a monthly lunar calendar screen that displays moon phases for each day and highlights key lunar events.

## User Review Required

> [!IMPORTANT]
> The calendar will focus on Moon Phases (New, Full, Quarters) and Perigee/Apogee if supported by the library. Eclipses might require more complex calculations or a different library; I will attempt to include them if `suncalc` supports it.

## Proposed Changes

### Domain Layer

#### [NEW] [LunarEvent.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/domain/model/LunarEvent.kt)
Define a data class to represent lunar events.

#### [MODIFY] [AstronomyRepository.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/domain/repository/AstronomyRepository.kt)
Add `getLunarEvents(year: Int, month: Int, location: LocationData): List<LunarEvent>` and `getMonthlyMoonData(year: Int, month: Int, location: LocationData): Map<LocalDate, MoonData>`.

### Data Layer

#### [MODIFY] [AstronomyRepositoryImpl.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/data/repository/AstronomyRepositoryImpl.kt)
Implement the new methods using the `suncalc` library.

### UI Layer

#### [MODIFY] [MoonViewModel.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/MoonViewModel.kt)
Add state for monthly lunar data and events.

#### [NEW] [LunarCalendarScreen.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/screens/LunarCalendarScreen.kt)
Implement the calendar UI.

#### [MODIFY] [MoonNavigation.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/navigation/MoonNavigation.kt)
Add the `LunarCalendar` route.

#### [MODIFY] [MainScreen.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/main/java/com/example/moon/ui/screens/MainScreen.kt)
Add a button or navigation link to the Lunar Calendar.

## Verification Plan

### Automated Tests
- Unit tests for `AstronomyRepositoryImpl` to verify event calculation for a known month.

### Manual Verification
- Navigate to the Lunar Calendar screen.
- Verify the calendar shows the current month.
- Verify moon phases are displayed for each day.
- Verify Full/New moon events are marked.
