package com.example.moon

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.moon.data.manager.AndroidWallpaperManager
import com.example.moon.data.repository.AstronomyRepositoryImpl
import com.example.moon.data.repository.LocationRepositoryImpl
import com.example.moon.data.repository.NoteRepositoryImpl
import com.example.moon.ui.navigation.MoonNavigation
import com.example.moon.ui.theme.MoonCycleTheme
import com.example.moon.util.IconManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class MainActivity : ComponentActivity() {
    private var isAppReady by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        
        // Keep splash screen on until we have moon data
        splashScreen.setKeepOnScreenCondition { !isAppReady }

        // Customize the exit animation for a seamless handover
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Create a fade out animation for the splash icon
            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenView.iconView,
                View.ALPHA,
                1f,
                0f
            )
            fadeOut.interpolator = AnticipateInterpolator()
            fadeOut.duration = 500L

            fadeOut.doOnEnd { 
                splashScreenView.remove() 
            }

            fadeOut.start()
        }

        super.onCreate(savedInstanceState)
        
        val locationRepository = LocationRepositoryImpl(
            this,
            LocationServices.getFusedLocationProviderClient(this)
        )
        val astronomyRepository = AstronomyRepositoryImpl()
        val wallpaperManager = AndroidWallpaperManager(this)
        val noteRepository = NoteRepositoryImpl(this)
        
        // Update icon and mark app as ready
        MainScope().launch {
            try {
                // 1. Get initial location quickly with timeout/fallback to unblock splash screen
                val initialLocation = locationRepository.getCurrentLocation()
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val initialData = astronomyRepository.getMoonData(now, initialLocation)
                IconManager.updateIconForPhase(this@MainActivity, initialData.phase)
            } catch (e: Exception) {
                // Fallback to avoid getting stuck
            } finally {
                isAppReady = true
            }

            // 2. Start observing for subsequent updates (e.g. location changes)
            try {
                locationRepository.getLocationUpdates().collect { location ->
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val updatedData = astronomyRepository.getMoonData(now, location)
                    IconManager.updateIconForPhase(this@MainActivity, updatedData.phase)
                }
            } catch (e: Exception) {
                // Ignore collection errors
            }
        }
        
        enableEdgeToEdge()
        setContent {
            MoonCycleTheme {
                MoonNavigation(locationRepository, astronomyRepository, wallpaperManager, noteRepository)
            }
        }
    }
}
