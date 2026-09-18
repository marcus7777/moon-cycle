package com.example.moon.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.moon.domain.manager.WallpaperManager
import com.example.moon.domain.repository.AstronomyRepository
import com.example.moon.domain.repository.LocationRepository
import com.example.moon.domain.repository.NoteRepository
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.compose.runtime.mutableStateListOf
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
data class MoonCalendar(val initialPage: Int = 0) : NavKey

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MoonNavigation(
    locationRepository: LocationRepository,
    astronomyRepository: AstronomyRepository,
    wallpaperManager: WallpaperManager? = null,
    noteRepository: NoteRepository? = null,
    onDownloadFile: (content: String, mimeType: String, fileName: String) -> Unit = { _, _, _ -> },
    onUploadFile: (mimeType: String, onRead: (String) -> Unit) -> Unit = { _, _ -> },
    onRequestLocationPermission: (() -> Unit) -> Unit = { _ -> }
) {
    val backStack = remember { mutableStateListOf<NavKey>(MoonHome) }
    
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(
                horizontalPartitionSpacerSize = 0.dp,
                maxHorizontalPartitions = 1 // Force single pane so the moon screen is centered
            )
    }
    
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)
    
    val viewModel: MoonViewModel = viewModel {
        MoonViewModel(locationRepository, astronomyRepository, wallpaperManager, noteRepository)
    }
    val moonData by viewModel.moonData.collectAsState()
    val locationData by viewModel.locationData.collectAsState()
    val isTextVisible by viewModel.isTextVisible.collectAsState()
    val isWallpaperScheduled by viewModel.isWallpaperScheduled.collectAsState()
    val showSwipeHint by viewModel.showSwipeHint.collectAsState()

    val entries: List<NavEntry<NavKey>> = backStack.map { key ->
        when (key) {
            is MoonHome -> {
                NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.listPane()
                ) {
                    MainScreen(
                        moonData = moonData,
                        locationData = locationData,
                        isTextVisible = isTextVisible,
                        showSwipeHint = showSwipeHint,
                        onDismissSwipeHint = { viewModel.dismissSwipeHint() },
                        onToggleTextVisibility = { viewModel.toggleTextVisibility() },
                        onInteraction = { viewModel.showTextWithTimer() },
                        onShowDetails = {
                            backStack.add(MoonDetails)
                        },
                        onShowCalendar = { initialPage ->
                            backStack.add(MoonCalendar(initialPage = initialPage))
                        },
                        onSetManualLocation = { lat, lng, name -> viewModel.setManualLocation(lat, lng, name) },
                        onUseDeviceLocation = { viewModel.useDeviceLocation() },
                        onRequestLocationPermission = onRequestLocationPermission
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
                        onBack = { backStack.removeLastOrNull() },
                        isWallpaperScheduled = isWallpaperScheduled,
                        onToggleWallpaperSchedule = { viewModel.toggleWallpaperSchedule(it) },
                        onUpdateWallpaperNow = { viewModel.updateWallpaperNow() },
                        onDownloadJsonl = { 
                            onDownloadFile(viewModel.exportNotesJsonl(), "application/jsonl", "moon_notes.jsonl") 
                        },
                        onUploadJsonl = { callback ->
                            onUploadFile("application/jsonl") { text ->
                                viewModel.importNotesJsonl(text, callback)
                            }
                        },
                        onDownloadICal = { 
                            onDownloadFile(viewModel.exportNotesICal(), "text/calendar", "lunar_notes.ics") 
                        },
                        onUploadICal = { callback ->
                            onUploadFile("text/calendar") { text ->
                                viewModel.importNotesICal(text, callback)
                            }
                        }
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
                        moonData = moonData,
                        noteRepository = noteRepository,
                        initialPage = key.initialPage,
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
