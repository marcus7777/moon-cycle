package com.example.moon.feature.main

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.moon.core.domain.model.LocationData
import com.example.moon.core.domain.model.MoonData
import com.example.moon.core.domain.model.formatEventName
import com.example.moon.core.ui.components.MoonVisualization
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant

@Composable
fun MainScreen(
    moonData: MoonData?,
    locationData: LocationData,
    isTextVisible: Boolean,
    onToggleTextVisibility: () -> Unit,
    onInteraction: () -> Unit,
    onShowDetails: () -> Unit,
    onShowCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var moonVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(moonData) {
        if (moonData != null) moonVisible = true
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .statusBarsPadding()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial)
                            onInteraction()
                        }
                    }
                }
        ) {
            if (moonData != null) {
                AnimatedVisibility(
                    visible = moonVisible,
                    enter = fadeIn(animationSpec = tween(1000)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { 
                                onInteraction()
                                onShowCalendar() 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        MoonVisualization(
                            moonData = moonData,
                            locationData = locationData,
                            modifier = Modifier
                                .fillMaxHeight(0.85f)
                                .aspectRatio(1f)
                                .padding(16.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isTextVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(64.dp)
                            .clickable(onClick = onToggleTextVisibility)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (moonData != null) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 40.dp)
                                    .clickable { onShowDetails() },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = moonData.phase.description,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Column(
                                modifier = Modifier.padding(bottom = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                moonData.nextEvent?.let { event ->
                                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                    val duration = event.dateTime.toInstant(TimeZone.currentSystemDefault()) - now.toInstant(TimeZone.currentSystemDefault())
                                    val days = duration.inWholeDays
                                    val hours = duration.inWholeHours % 24

                                    val countdownText = when {
                                        days > 0 -> "$days ${if (days == 1L) "day" else "days"} until ${formatEventName(event)}"
                                        hours > 0 -> "$hours ${if (hours == 1L) "hour" else "hours"} until ${formatEventName(event)}"
                                        else -> "${formatEventName(event)} is tonight"
                                    }

                                    Text(
                                        text = countdownText,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (moonData == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}
