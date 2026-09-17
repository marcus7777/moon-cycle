package com.example.moon.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.moon.domain.model.LocationData
import com.example.moon.domain.model.MoonData
import com.example.moon.domain.model.MoonPhase
import kotlin.math.abs

/**
 * The single source of truth for generating moon visuals.
 * Can produce SVG strings or render directly to a Compose Canvas.
 */
object MoonVectorEngine {

    /**
     * Common 3D Draw logic for Compose-based surfaces (UI, Wallpaper).
     */
    fun drawMoon(
        drawScope: DrawScope,
        moonData: MoonData,
        locationData: LocationData
    ) = with(drawScope) {
        val isSouthernHemisphere = locationData.latitude < 0
        val tiltAngle = moonData.parallacticAngle?.toFloat() ?: 0f
        
        // Size factor adjusted to 0.5f to ensure 1 radius of padding around the moon (D = W/2)
        val radius = (size.minDimension / 2f) * 0.5f
        val center = Offset(size.width / 2f, size.height / 2f)

        withTransform({
            rotate(tiltAngle, pivot = center)
            if (isSouthernHemisphere) {
                scale(scaleX = -1f, scaleY = 1f, pivot = center)
            }
        }) {
            val illuminationFloat = moonData.illumination.toFloat()
            val phase = moonData.phase

            // 1. Dark side
            val darkBrush = Brush.radialGradient(
                0.0f to Color(0xFF2C2C2C),
                0.7f to Color(0xFF151515),
                1.0f to Color(0xFF080808),
                center = center,
                radius = radius
            )
            drawCircle(brush = darkBrush, radius = radius, center = center)

            // 2. Lighting setup
            val lightMain = Color(0xFFFFF9C4)
            val lightHighlight = Color(0xFFFFFFFF)
            val lightShadow = Color(0xFFFBC02D)
            
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

            // 3. Draw Illuminated part
            when (phase) {
                MoonPhase.NEW -> { }
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
                            drawOval(brush = darkBrush, topLeft = Offset(center.x - innerWidth, center.y - radius), size = Size(innerWidth * 2, radius * 2))
                        } else {
                            drawOval(brush = lightBrush, topLeft = Offset(center.x - innerWidth, center.y - radius), size = Size(innerWidth * 2, radius * 2))
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
                            drawOval(brush = darkBrush, topLeft = Offset(center.x - innerWidth, center.y - radius), size = Size(innerWidth * 2, radius * 2))
                        } else {
                            drawOval(brush = lightBrush, topLeft = Offset(center.x - innerWidth, center.y - radius), size = Size(innerWidth * 2, radius * 2))
                        }
                    }
                }
            }

            // 4. Rim light pass
            drawCircle(
                brush = Brush.radialGradient(0.92f to Color.Transparent, 1.0f to Color.White.copy(alpha = 0.15f), center = center, radius = radius),
                radius = radius, center = center
            )
        }
    }

    /**
     * Generates a standard SVG XML string of the moon.
     */
    fun generateSvg(
        moonData: MoonData, 
        locationData: LocationData,
        width: Int = 108,
        height: Int = 108
    ): String {
        val radius = 30f
        val centerX = 54f
        val centerY = 54f
        val illumination = moonData.illumination.toFloat()
        val phase = moonData.phase
        val isSouthern = locationData.latitude < 0
        val tilt = moonData.parallacticAngle?.toFloat() ?: 0f

        val isWaxing = phase == MoonPhase.WAXING_CRESCENT || 
                       phase == MoonPhase.FIRST_QUARTER || 
                       phase == MoonPhase.WAXING_GIBBOUS

        // Scale factor for the horizontal flip if in Southern Hemisphere
        val scaleX = if (isSouthern) -1 else 1
        val rotation = tilt

        // Simplified SVG logic for 3D effect using <defs> for gradients
        return """
            <svg width="$width" height="$height" viewBox="0 0 108 108" xmlns="http://www.w3.org/2000/svg">
                <defs>
                    <radialGradient id="darkSide" cx="50%" cy="50%" r="50%">
                        <stop offset="0%" stop-color="#2C2C2C" />
                        <stop offset="70%" stop-color="#151515" />
                        <stop offset="100%" stop-color="#080808" />
                    </radialGradient>
                    <radialGradient id="lightSide" cx="${if (isWaxing) "70%" else "30%"}" cy="40%" r="80%">
                        <stop offset="0%" stop-color="#FFFFFF" />
                        <stop offset="50%" stop-color="#FFF9C4" />
                        <stop offset="100%" stop-color="#FBC02D" />
                    </radialGradient>
                </defs>
                <g transform="rotate($rotation $centerX $centerY) scale($scaleX 1) translate(${if (isSouthern) -108 else 0} 0)">
                    <!-- Base Sphere -->
                    <circle cx="$centerX" cy="$centerY" r="$radius" fill="url(#darkSide)" />
                    
                    <!-- Illuminated Part -->
                    ${getSvgIlluminationPath(centerX, centerY, radius, illumination, isWaxing, phase)}
                </g>
            </svg>
        """.trimIndent()
    }

    private fun getSvgIlluminationPath(
        cx: Float, 
        cy: Float, 
        r: Float, 
        illumination: Float, 
        isWaxing: Boolean,
        phase: MoonPhase
    ): String {
        if (phase == MoonPhase.NEW) return ""
        if (phase == MoonPhase.FULL) return "<circle cx=\"$cx\" cy=\"$cy\" r=\"$r\" fill=\"url(#lightSide)\" />"

        val sweep = if (isWaxing) 1 else 0
        val innerWidth = abs(illumination - 0.5f) * 2f * r
        
        // Primary semi-circle
        val baseArc = "M $cx ${cy - r} A $r $r 0 0 $sweep $cx ${cy + r}"
        
        // Inner arc to form crescent or gibbous
        val innerSweep = if (illumination < 0.5f) sweep else (1 - sweep)
        val innerArc = "A $innerWidth $r 0 0 $innerSweep $cx ${cy - r} Z"

        return "<path d=\"$baseArc $innerArc\" fill=\"url(#lightSide)\" />"
    }
}
