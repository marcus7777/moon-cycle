import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.example.moon.ui.navigation.MoonNavigation
import com.example.moon.ui.theme.MoonCycleTheme
import com.example.moon.domain.repository.LocationRepository
import com.example.moon.domain.repository.AstronomyRepository
import com.example.moon.domain.repository.NoteRepository
import com.example.moon.data.repository.AstronomyRepositoryImpl
import com.example.moon.domain.model.LocationData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.browser.document
import kotlinx.browser.window
import com.example.moon.util.MoonVectorEngine
import com.example.moon.domain.model.MoonData
import org.w3c.dom.HTMLLinkElement
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Updates the browser's favicon to the current moon phase using the shared engine.
 */
fun updateWebFavicon(moonData: MoonData, locationData: LocationData) {
    val svgString = MoonVectorEngine.generateSvg(moonData, locationData)
    // Using simple data URI for maximum compatibility in Wasm
    val dataUri = "data:image/svg+xml;utf8,${svgString.replace("#", "%23")}"
    
    val link = document.querySelector("link[rel*='icon']") as? HTMLLinkElement 
        ?: (document.createElement("link") as HTMLLinkElement).apply {
            rel = "shortcut icon"
            document.head?.append(this)
        }
    
    link.href = dataUri
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val locationRepository = object : LocationRepository {
        private val _locationFlow = MutableStateFlow(LocationRepository.DEFAULT_LOCATION)

        override fun getLocationUpdates(): Flow<LocationData> = _locationFlow.asStateFlow()
        override suspend fun getCurrentLocation(): LocationData = _locationFlow.value

        override fun setManualLocation(latitude: Double, longitude: Double, name: String?) {
            _locationFlow.value = LocationData(
                latitude = latitude,
                longitude = longitude,
                name = name,
                isDefault = false
            )
        }

        override fun clearManualLocation() {
            _locationFlow.value = LocationRepository.DEFAULT_LOCATION
        }

        override fun isFirstLaunch(): Boolean = false
        override fun setFirstLaunchCompleted() {}
    }
    val astronomyRepository = AstronomyRepositoryImpl()
    
    val noteRepository = object : NoteRepository {
        private val _notes = MutableStateFlow<Map<LocalDate, String>>(emptyMap())
        override fun getNotes(): Flow<Map<LocalDate, String>> = _notes.asStateFlow()
        override suspend fun saveNote(date: LocalDate, note: String) {
            val current = _notes.value.toMutableMap()
            current[date] = note
            _notes.value = current
        }
        override suspend fun deleteNote(date: LocalDate) {
            val current = _notes.value.toMutableMap()
            current.remove(date)
            _notes.value = current
        }
    }

    // Sync the web favicon with the live moon data
    MainScope().launch {
        locationRepository.getLocationUpdates().collect { location ->
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val moonData = astronomyRepository.getMoonData(now, location)
            updateWebFavicon(moonData, location)
        }
    }

    ComposeViewport(document.body!!) {
        MoonCycleTheme {
            MoonNavigation(locationRepository, astronomyRepository, noteRepository = noteRepository)
        }
    }
}
