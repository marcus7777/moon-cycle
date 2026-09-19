package com.example.moon.feature.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moon.core.domain.manager.WallpaperManager
import com.example.moon.core.domain.repository.AstronomyRepository
import com.example.moon.core.domain.repository.LocationRepository
import com.example.moon.core.domain.repository.NoteRepository
import com.example.moon.core.domain.provider.MoonDataProvider
import com.example.moon.feature.main.MainScreen
import com.example.moon.feature.main.MainViewModel
import com.example.moon.feature.calendar.CalendarScreen
import com.example.moon.feature.details.MoonDetailScreen
import com.example.moon.feature.details.MoonDetailViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MoonNavigation(
    locationRepository: LocationRepository,
    astronomyRepository: AstronomyRepository,
    moonDataProvider: MoonDataProvider,
    wallpaperManager: WallpaperManager? = null,
    noteRepository: NoteRepository? = null,
    onDownloadFile: (content: String, mimeType: String, fileName: String) -> Unit = { _, _, _ -> },
    onUploadFile: (mimeType: String, onRead: (String) -> Unit) -> Unit = { _, _ -> },
    onRequestLocationPermission: (() -> Unit) -> Unit = { _ -> }
) {
    var currentOverlay by remember { mutableStateOf<Overlay?>(null) }
    var navigationAlpha by remember { mutableStateOf(1f) }
    var idleJob by remember { mutableStateOf<Job?>(null) }
    var fadeJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    fun resetNavigationIdleTimer() {
        fadeJob?.cancel()
        navigationAlpha = 1f
        idleJob?.cancel()
        idleJob = scope.launch {
            delay(60000)
            val duration = 10000L
            val steps = 100
            val delayPerStep = duration / steps
            for (i in steps downTo 0) {
                navigationAlpha = i / steps.toFloat()
                delay(delayPerStep)
            }
            currentOverlay = null
            navigationAlpha = 1f
        }
    }

    val mainViewModel: MainViewModel = viewModel {
        MainViewModel(locationRepository, astronomyRepository, moonDataProvider)
    }
    
    val detailsViewModel: MoonDetailViewModel = viewModel {
        MoonDetailViewModel(locationRepository, astronomyRepository, noteRepository, wallpaperManager)
    }

    val moonData by mainViewModel.moonData.collectAsState()
    val locationData by mainViewModel.locationData.collectAsState()
    val isTextVisible by mainViewModel.isTextVisible.collectAsState()
    val showSwipeHint by mainViewModel.showSwipeHint.collectAsState()
    val isWallpaperScheduled by detailsViewModel.isWallpaperScheduled.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        MainScreen(
            moonData = moonData,
            locationData = locationData,
            isTextVisible = isTextVisible,
            showSwipeHint = showSwipeHint,
            onDismissSwipeHint = { mainViewModel.dismissSwipeHint() },
            onToggleTextVisibility = { mainViewModel.toggleTextVisibility() },
            onInteraction = { mainViewModel.showTextWithTimer() },
            onShowDetails = {
                currentOverlay = Overlay.Details
                resetNavigationIdleTimer()
            },
            onShowCalendar = { initialPage ->
                currentOverlay = Overlay.Calendar(initialPage)
                resetNavigationIdleTimer()
            }
        )

        AnimatedVisibility(
            visible = currentOverlay is Overlay.Calendar,
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            val calendarOverlay = currentOverlay as? Overlay.Calendar
            if (calendarOverlay != null) {
                CalendarScreen(
                    modifier = Modifier.graphicsLayer { alpha = navigationAlpha },
                    locationData = locationData,
                    moonData = moonData,
                    noteRepository = noteRepository,
                    initialPage = calendarOverlay.initialPage,
                    onInteraction = { resetNavigationIdleTimer() },
                    onBack = { currentOverlay = null }
                )
            }
        }

        AnimatedVisibility(
            visible = currentOverlay is Overlay.Details,
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            MoonDetailScreen(
                modifier = Modifier.graphicsLayer { alpha = navigationAlpha },
                moonData = moonData,
                locationData = locationData,
                onBack = { currentOverlay = null },
                isWallpaperScheduled = isWallpaperScheduled,
                onToggleWallpaperSchedule = { detailsViewModel.toggleWallpaperSchedule(it) },
                onUpdateWallpaperNow = { detailsViewModel.updateWallpaperNow() },
                onDownloadJsonl = { 
                    onDownloadFile(detailsViewModel.exportNotesJsonl(), "application/jsonl", "moon_notes.jsonl") 
                },
                onUploadJsonl = { callback ->
                    onUploadFile("application/jsonl") { text ->
                        detailsViewModel.importNotesJsonl(text, callback)
                    }
                },
                onDownloadICal = { 
                    onDownloadFile(detailsViewModel.exportNotesICal(), "text/calendar", "lunar_notes.ics") 
                },
                onUploadICal = { callback ->
                    onUploadFile("text/calendar") { text ->
                        detailsViewModel.importNotesICal(text, callback)
                    }
                },
                onSetManualLocation = { lat, lng, name -> detailsViewModel.setManualLocation(lat, lng, name) },
                onUseDeviceLocation = { detailsViewModel.useDeviceLocation() },
                onRequestLocationPermission = onRequestLocationPermission,
                onInteraction = { resetNavigationIdleTimer() }
            )
        }
    }
}

sealed class Overlay {
    data class Calendar(val initialPage: Int) : Overlay()
    object Details : Overlay()
}
