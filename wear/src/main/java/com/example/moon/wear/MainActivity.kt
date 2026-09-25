package com.example.moon.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.moon.core.data.provider.MoonDataProviderImpl
import com.example.moon.core.data.repository.LocationRepositoryImpl
import com.example.moon.core.data.repository.AstronomyRepositoryImpl
import com.example.moon.core.data.repository.PromptRepositoryImpl
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val locationRepository = LocationRepositoryImpl(
            this,
            LocationServices.getFusedLocationProviderClient(this)
        )
        val astronomyRepository = AstronomyRepositoryImpl()
        val moonDataProvider = MoonDataProviderImpl(locationRepository, astronomyRepository)
        val promptRepository = PromptRepositoryImpl(this)
        promptRepository.loadCachedPrompts()
        
        MainScope().launch {
            promptRepository.checkAndUpdatePrompts()
        }
        
        setContent {
            WearApp(
                locationRepository = locationRepository,
                astronomyRepository = astronomyRepository,
                moonDataProvider = moonDataProvider
            )
        }
    }
}
