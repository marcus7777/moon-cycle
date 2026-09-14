package com.example.moon.ui.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moon.domain.repository.AstronomyRepository
import com.example.moon.domain.repository.LocationRepository
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.moon.ui.MoonViewModel
import com.example.moon.ui.screens.CalendarScreen
import com.example.moon.ui.screens.MainScreen
import com.example.moon.ui.screens.MoonDetailScreen
import kotlinx.serialization.Serializable

@Serializable
object MoonHome : NavKey

@Serializable
object MoonDetails : NavKey

@Serializable
object MoonCalendar : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MoonNavigation(
    locationRepository: LocationRepository,
    astronomyRepository: AstronomyRepository
) {
    val backStack = rememberNavBackStack(MoonHome)
    
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)
    
    val viewModel: MoonViewModel = viewModel {
        MoonViewModel(locationRepository, astronomyRepository)
    }
    val moonData by viewModel.moonData.collectAsState()
    val locationData by viewModel.locationData.collectAsState()
    val isTextVisible by viewModel.isTextVisible.collectAsState()

    val entries: List<NavEntry<NavKey>> = backStack.map { key ->
        when (key) {
            is MoonHome -> {
                NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.listPane(
                        detailPlaceholder = {
                            MoonDetailScreen(
                                moonData = moonData,
                                locationData = locationData,
                                onBack = { /* No-op in placeholder */ }
                            )
                        }
                    )
                ) {
                    MainScreen(
                        moonData = moonData,
                        locationData = locationData,
                        isTextVisible = isTextVisible,
                        onToggleTextVisibility = { viewModel.toggleTextVisibility() },
                        onInteraction = { viewModel.showTextWithTimer() },
                        onShowDetails = {
                            backStack.add(MoonDetails)
                        },
                        onShowCalendar = {
                            backStack.add(MoonCalendar)
                        },
                        onSetManualLocation = { lat, lng, name -> viewModel.setManualLocation(lat, lng, name) },
                        onUseDeviceLocation = { viewModel.useDeviceLocation() }
                    )
                }
            }
            is MoonDetails -> {
                NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.detailPane()
                ) {
                    MoonDetailScreen(
                        moonData = moonData,
                        locationData = locationData,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
            is MoonCalendar -> {
                NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.listPane()
                ) {
                    CalendarScreen(
                        locationData = locationData,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
            else -> error("Unknown key: $key")
        }
    }

    NavDisplay(
        entries = entries,
        sceneStrategies = listOf(listDetailStrategy),
        onBack = { backStack.removeLastOrNull() }
    )
}
