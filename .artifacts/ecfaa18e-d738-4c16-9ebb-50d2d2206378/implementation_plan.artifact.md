# Implementation Plan - Export Lunar Events to ICS and Notes to JSONL (with Geohash)

Implement features to allow users to export data to files:
1. Lunar events for a specific month to an iCalendar (.ics) file, including location coordinates (GEO).
2. All notes to a JSON Lines (.jsonl) file, including geohash data.

These will use the Android Storage Access Framework (SAF) to let users choose the save location.

## User Review Required

> [!IMPORTANT]
> Exports will be triggered from the Calendar screen. The user will be prompted by the system to choose a location for the files.

## Proposed Changes

### core:domain
Implement the data transformation logic with geohash support.

#### [NEW] [IcsExporter.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/core/domain/src/commonMain/kotlin/com/example/moon/core/domain/util/IcsExporter.kt)
Utility to convert a list of `LunarEvent` objects and `LocationData` into a valid iCalendar string.
- Includes `GEO` property using latitude and longitude.

#### [MODIFY] [NoteRepository.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/core/domain/src/commonMain/kotlin/com/example/moon/core/domain/repository/NoteRepository.kt)
Add `exportAllNotesAsJsonl(): String` to the interface.

### core:data
Implement the repository logic for exporting notes.

#### [MODIFY] [NoteRepositoryImpl.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/core/data/src/androidMain/kotlin/com/example/moon/core/data/repository/NoteRepositoryImpl.kt)
Implement `exportAllNotesAsJsonl()` using the existing `encodeNoteEntry` logic which already supports the `geohash` field.

### feature:calendar
Update the ViewModel and UI to support export actions.

#### [MODIFY] [CalendarViewModel.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/feature/calendar/src/commonMain/kotlin/com/example/moon/feature/calendar/CalendarViewModel.kt)
Add functions:
- `getIcsExportContent(location: LocationData)`: Returns the ICS string for current month events, including geolocation.
- `getNotesExportContent()`: Returns the JSONL string for all notes (includes geohashes).

#### [MODIFY] [CalendarScreen.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/feature/calendar/src/commonMain/kotlin/com/example/moon/feature/calendar/CalendarScreen.kt)
Add an "Export" menu or multiple buttons to trigger the file save intents for ICS and JSONL.

## Verification Plan

### Manual Verification
1. Open the Calendar screen.
2. Tap the "Export Events" button -> Save as `events.ics` -> Verify content.
3. Tap the "Export Notes" button -> Save as `notes.jsonl` -> Verify content.
