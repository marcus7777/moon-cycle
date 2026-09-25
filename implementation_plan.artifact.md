# Move Web Prompts to JSON & Sync Remote Prompts to Android / Wear OS

This plan extracts the IFS lunar prompts into a standalone `prompts.json` file in the web version (`web/prompts.json`) hosted at `https://moon-is.web.app/prompts.json`. It also adds a remote prompt checker mechanism to the Android phone app (`:app`) and Wear OS app (`:wear`) so that updated prompts are checked on startup, cached locally, and propagated throughout the apps.

## Proposed Changes

### Web Application (`web/`)

#### [NEW] [prompts.json](file:///C:/Users/marcu/AndroidStudioProjects/moon/web/prompts.json)
- Extract the 32 IFS lunar prompts into a formatted JSON file with versioning and title/prompt attributes.

#### [MODIFY] [app.js](file:///C:/Users/marcu/AndroidStudioProjects/moon/web/app.js)
- Replace the hardcoded `IFS_PROMPTS` array with `loadPrompts()` which fetches `./prompts.json`.
- Trigger `loadPrompts()` during application initialization (`init()`).

#### [MODIFY] [sw.js](file:///C:/Users/marcu/AndroidStudioProjects/moon/web/sw.js)
- Add `'./prompts.json'` to the Service Worker's precached `ASSETS` array for offline web functionality.

---

### Core Domain Module (`:core:domain`)

#### [MODIFY] [IfsPrompt.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/core/domain/src/commonMain/kotlin/com/example/moon/core/domain/model/IfsPrompt.kt)
- Update `IfsPromptProvider` object to maintain dynamic state using a `StateFlow<List<IfsPrompt>>`.
- Expose `updatePrompts()` to update the active prompt list at runtime.

#### [NEW] [PromptRepository.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/core/domain/src/commonMain/kotlin/com/example/moon/core/domain/repository/PromptRepository.kt)
- Define `PromptRepository` interface with `loadCachedPrompts()` and `checkAndUpdatePrompts(): Boolean`.

---

### Core Data Module (`:core:data`)

#### [NEW] [PromptRepositoryImpl.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/core/data/src/androidMain/kotlin/com/example/moon/core/data/repository/PromptRepositoryImpl.kt)
- Implement `PromptRepository` using `SharedPreferences` for local disk caching and `HttpURLConnection` on `Dispatchers.IO` to fetch from `https://moon-is.web.app/prompts.json`.
- Parse fetched JSON, save to disk, and update `IfsPromptProvider`.

---

### Feature Calendar Module (`:feature:calendar`)

#### [MODIFY] [CalendarScreen.kt](file:///C:/Users/marcu/AndroidStudioProjects/moon/feature/calendar/src/commonMain/kotlin/com/example/moon/feature/calendar/CalendarScreen.kt)
- Collect `IfsPromptProvider.promptsFlow` in Compose so prompts automatically recompose when updated prompts are loaded.

---

### App & Wear OS Modules (`:app` & `:wear`)

#### [MODIFY] [MainActivity.kt (Phone)](file:///C:/Users/marcu/AndroidStudioProjects/moon/app/src/androidMain/kotlin/com/example/moon/MainActivity.kt)
- Instantiate `PromptRepositoryImpl`, load cached prompts on boot, and launch a background coroutine to fetch remote prompt updates.

#### [MODIFY] [MainActivity.kt (Wear OS)](file:///C:/Users/marcu/AndroidStudioProjects/moon/wear/src/main/java/com/example/moon/wear/MainActivity.kt)
- Instantiate `PromptRepositoryImpl`, load cached prompts on boot, and launch a background coroutine to fetch remote prompt updates.

## Verification Plan

### Automated Build & Compilation
- Run `./gradlew assembleDebug` to verify all Kotlin modules compile cleanly.
- Run Gradle unit tests to ensure no regressions.

### Manual Verification
- Verify `web/prompts.json` loads correctly in `web/app.js`.
- Verify Android phone app and Wear OS app fetch and load prompts correctly.
