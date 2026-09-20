package com.example.moon.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.moon.core.domain.model.LocationData
import com.example.moon.core.domain.model.MoonData
import com.example.moon.core.ui.util.MoonVectorEngine

object MoonBitmapRenderer {

    fun renderMoon(
        context: Context,
        moonData: MoonData,
        location: LocationData,
        width: Int,
        height: Int,
        backgroundColor: Int = Color.BLACK
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(backgroundColor)

        val drawScope = CanvasDrawScope()
        val composeCanvas = androidx.compose.ui.graphics.Canvas(canvas)
        val size = Size(width.toFloat(), height.toFloat())
        
        drawScope.draw(
            density = Density(context),
            layoutDirection = LayoutDirection.Ltr,
            canvas = composeCanvas,
            size = size
        ) {
            MoonVectorEngine.drawMoon(this, moonData, location)
        }
        
        return bitmap
    }
}
