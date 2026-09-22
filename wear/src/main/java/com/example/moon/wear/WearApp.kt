package com.example.moon.wear

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.moon.core.domain.provider.MoonDataProvider
import com.example.moon.core.domain.repository.AstronomyRepository
import com.example.moon.core.domain.repository.LocationRepository

@Composable
fun WearApp(
    locationRepository: LocationRepository,
    astronomyRepository: AstronomyRepository,
    moonDataProvider: MoonDataProvider
) {
    MaterialTheme {
        val navController = rememberSwipeDismissableNavController()
        
        AppScaffold {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "main"
            ) {
                composable("main") {
                    MainScreen(
                        moonDataProvider = moonDataProvider,
                        locationRepository = locationRepository,
                        onShowCalendar = { navController.navigate("calendar") }
                    )
                }
                composable("calendar") {
                    CalendarScreen(
                        locationRepository = locationRepository,
                        astronomyRepository = astronomyRepository,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
