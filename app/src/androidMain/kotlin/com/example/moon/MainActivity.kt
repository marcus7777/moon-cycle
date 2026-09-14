package com.example.moon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.moon.data.repository.AstronomyRepositoryImpl
import com.example.moon.data.repository.LocationRepositoryImpl
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
        splashScreen.setKeepOnScreenCondition { !isAppReady }
        
        super.onCreate(savedInstanceState)
        
        val locationRepository = LocationRepositoryImpl(
            this,
            LocationServices.getFusedLocationProviderClient(this)
        )
        val astronomyRepository = AstronomyRepositoryImpl()
        
        // Update icon and mark app as ready
        MainScope().launch {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val data = astronomyRepository.getMoonData(now, locationRepository.getCurrentLocation())
            IconManager.updateIconForPhase(this@MainActivity, data.phase)
            isAppReady = true
        }
        
        enableEdgeToEdge()
        setContent {
            MoonCycleTheme {
                MoonNavigation(locationRepository, astronomyRepository)
            }
        }
    }
}
