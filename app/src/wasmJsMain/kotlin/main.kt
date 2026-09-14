import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.example.moon.ui.navigation.MoonNavigation
import com.example.moon.ui.theme.MoonCycleTheme
import com.example.moon.domain.repository.LocationRepository
import com.example.moon.domain.repository.AstronomyRepository
import com.example.moon.data.repository.AstronomyRepositoryImpl
import com.example.moon.domain.model.LocationData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val locationRepository = object : LocationRepository {
        override fun getLocationUpdates(): Flow<LocationData> = flowOf(LocationRepository.DEFAULT_LOCATION)
        override suspend fun getCurrentLocation(): LocationData = LocationRepository.DEFAULT_LOCATION
    }
    val astronomyRepository = AstronomyRepositoryImpl()

    ComposeViewport(document.body!!) {
        MoonCycleTheme {
            MoonNavigation(locationRepository, astronomyRepository)
        }
    }
}
