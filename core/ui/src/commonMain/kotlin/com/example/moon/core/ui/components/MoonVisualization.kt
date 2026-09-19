package com.example.moon.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.moon.core.domain.model.LocationData
import com.example.moon.core.domain.model.MoonData
import com.example.moon.core.domain.model.MoonPhase
import com.example.moon.core.ui.util.MoonVectorEngine
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * A component that renders the moon's visual phase using the unified MoonVectorEngine.
 */
@Composable
fun MoonVisualization(
    moonData: MoonData,
    locationData: LocationData,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.aspectRatio(1f)) {
        MoonVectorEngine.drawMoon(this, moonData, locationData)
    }
}

@Preview
@Composable
fun MoonVisualizationPreview() {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val mockMoonData = MoonData(
        phase = MoonPhase.WAXING_GIBBOUS,
        illumination = 0.75,
        age = 10.5,
        riseTime = now,
        setTime = now
    )
    val mockLocation = LocationData(latitude = 37.7749, longitude = -122.4194)
    
    MoonVisualization(
        moonData = mockMoonData,
        locationData = mockLocation,
        modifier = Modifier.fillMaxSize()
    )
}
