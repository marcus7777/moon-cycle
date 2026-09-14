package com.example.moon.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview
import com.example.moon.domain.model.LocationData
import com.example.moon.domain.model.MoonData
import com.example.moon.domain.model.MoonPhase
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs

/**
 * A component that renders the moon's visual phase with a 3D spherical effect.
 * It handles hemispheric orientation by flipping the moon horizontally for the Southern Hemisphere.
 */
@Composable
fun MoonVisualization(
    moonData: MoonData,
    locationData: LocationData,
    modifier: Modifier = Modifier
) {
    val isSouthernHemisphere = locationData.latitude < 0
    val tiltAngle = moonData.parallacticAngle?.toFloat() ?: 0f

    Canvas(modifier = modifier.aspectRatio(1f)) {
        // Use 80% of the available space to provide a safe margin and prevent edge-bleeding
        val radius = (size.minDimension / 2f) * 0.8f
        val center = Offset(size.width / 2f, size.height / 2f)

        withTransform({
            // Application of the parallactic angle (tilt)
            rotate(tiltAngle, pivot = center)
            
            if (isSouthernHemisphere) {
                scale(scaleX = -1f, scaleY = 1f, pivot = center)
            }
        }) {
            drawMoon3D(radius, center, moonData.illumination, moonData.phase)
        }
    }
}

private fun DrawScope.drawMoon3D(
    radius: Float,
    center: Offset,
    illumination: Double,
    phase: MoonPhase
) {
    val illuminationFloat = illumination.toFloat()
    
    // 1. Draw the base sphere (the dark side/unlit part)
    val darkBrush = Brush.radialGradient(
        0.0f to Color(0xFF2C2C2C),
        0.7f to Color(0xFF151515),
        1.0f to Color(0xFF080808),
        center = center,
        radius = radius
    )
    drawCircle(brush = darkBrush, radius = radius, center = center)

    // 2. Define the light colors for the 3D surface
    val lightMain = Color(0xFFFFF9C4)
    val lightHighlight = Color(0xFFFFFFFF)
    val lightShadow = Color(0xFFFBC02D)
    
    // Determine lighting direction based on phase
    val isWaxing = phase == MoonPhase.WAXING_CRESCENT || 
                   phase == MoonPhase.FIRST_QUARTER || 
                   phase == MoonPhase.WAXING_GIBBOUS ||
                   (phase == MoonPhase.NEW && illuminationFloat > 0)

    val lightCenter = if (isWaxing) {
        Offset(center.x + radius * 0.4f, center.y - radius * 0.1f)
    } else {
        Offset(center.x - radius * 0.4f, center.y - radius * 0.1f)
    }

    val lightBrush = Brush.radialGradient(
        0.0f to lightHighlight,
        0.5f to lightMain,
        1.0f to lightShadow,
        center = lightCenter,
        radius = radius * 1.6f
    )

    // 3. Draw the illuminated part based on phase
    when (phase) {
        MoonPhase.NEW -> { /* Already drawn as base sphere */ }
        MoonPhase.FULL -> {
            drawCircle(brush = lightBrush, radius = radius, center = center)
        }
        else -> {
            if (isWaxing) {
                drawArc(
                    brush = lightBrush,
                    startAngle = -90f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
                
                val innerWidth = (abs(illuminationFloat - 0.5f) * 2f) * radius
                if (illuminationFloat < 0.5f) {
                    drawOval(
                        brush = darkBrush,
                        topLeft = Offset(center.x - innerWidth, center.y - radius),
                        size = Size(innerWidth * 2, radius * 2)
                    )
                } else {
                    drawOval(
                        brush = lightBrush,
                        topLeft = Offset(center.x - innerWidth, center.y - radius),
                        size = Size(innerWidth * 2, radius * 2)
                    )
                }
            } else {
                drawArc(
                    brush = lightBrush,
                    startAngle = 90f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )

                val innerWidth = (abs(illuminationFloat - 0.5f) * 2f) * radius
                if (illuminationFloat < 0.5f) {
                    drawOval(
                        brush = darkBrush,
                        topLeft = Offset(center.x - innerWidth, center.y - radius),
                        size = Size(innerWidth * 2, radius * 2)
                    )
                } else {
                    drawOval(
                        brush = lightBrush,
                        topLeft = Offset(center.x - innerWidth, center.y - radius),
                        size = Size(innerWidth * 2, radius * 2)
                    )
                }
            }
        }
    }

    // 4. Add crater texture
    drawCraters(radius, center, isWaxing, illuminationFloat, phase)
    
    // 5. Final pass: A rim light
    drawCircle(
        brush = Brush.radialGradient(
            0.92f to Color.Transparent,
            1.0f to Color.White.copy(alpha = 0.15f),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawCraters(
    radius: Float,
    center: Offset,
    isWaxing: Boolean,
    illumination: Float,
    phase: MoonPhase
) {
    val craterShadow = Color.Black.copy(alpha = 0.12f)
    val craterHighlight = Color.White.copy(alpha = 0.08f)

    // Expanded seeds for more detailed surface
    val craterSeeds = listOf(
        Pair(0.2f, 0.3f), Pair(-0.4f, 0.1f), Pair(0.1f, -0.5f),
        Pair(-0.2f, -0.3f), Pair(0.5f, 0.4f), Pair(-0.6f, -0.2f),
        Pair(0.3f, -0.1f), Pair(0.0f, 0.6f), Pair(-0.3f, 0.5f),
        Pair(0.4f, -0.4f), Pair(0.6f, 0.1f), Pair(-0.1f, -0.7f),
        Pair(0.0f, 0.0f), Pair(-0.5f, -0.5f), Pair(0.7f, 0.3f)
    )

    craterSeeds.forEach { (dx, dy) ->
        val x = center.x + dx * radius
        val y = center.y + dy * radius
        
        // Logical "terminator" distance (distance to the light/dark edge)
        // This is where craters should look most prominent
        val litEdgeX = if (isWaxing) {
            (0.5f - illumination) * 2f
        } else {
            (illumination - 0.5f) * 2f
        }

        val isLit = if (phase == MoonPhase.FULL) true 
                    else if (phase == MoonPhase.NEW) false
                    else if (isWaxing) dx > litEdgeX 
                    else dx < litEdgeX

        if (isLit) {
            // Distance to the terminator line improves crater prominence
            val distToEdge = abs(dx - litEdgeX)
            val terminatorMultiplier = (1f - distToEdge).coerceIn(0.5f, 1.5f)
            
            val cRadius = radius * (0.04f + abs(dx * dy) * 0.08f)
            val shadowOffset = 1.5f * terminatorMultiplier
            
            drawCircle(color = craterShadow, radius = cRadius, center = Offset(x + shadowOffset, y + shadowOffset))
            drawCircle(color = craterHighlight, radius = cRadius * 0.8f, center = Offset(x - shadowOffset/2, y - shadowOffset/2))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MoonVisualization3DPreview() {
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
