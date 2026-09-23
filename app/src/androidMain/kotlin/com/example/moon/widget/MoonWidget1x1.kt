package com.example.moon.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.moon.MainActivity
import kotlin.math.roundToInt

class MoonWidget1x1 : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetData = WidgetHelper.loadWidgetData(context)

        provideContent {
            val moonBitmap = WidgetHelper.rememberMoonBitmap(context, widgetData)

            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    if (moonBitmap != null) {
                        Image(
                            provider = ImageProvider(moonBitmap),
                            contentDescription = "Current Moon Phase (1x1)",
                            modifier = GlanceModifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        widgetData.moonData?.let { data ->
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxSize()
                                    .padding(bottom = 2.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Text(
                                    text = "${(data.illumination * 100).roundToInt()}%",
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurface,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "...",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

class MoonWidget1x1Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MoonWidget1x1()
}
