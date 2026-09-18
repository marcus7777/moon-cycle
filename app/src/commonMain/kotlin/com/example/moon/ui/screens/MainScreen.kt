package com.example.moon.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.moon.domain.model.LocationData
import com.example.moon.domain.model.MoonData
import com.example.moon.domain.model.formatEventName
import com.example.moon.ui.components.MoonVisualization
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    moonData: MoonData?,
    locationData: LocationData,
    isTextVisible: Boolean,
    showSwipeHint: Boolean,
    onDismissSwipeHint: () -> Unit,
    onToggleTextVisibility: () -> Unit,
    onInteraction: () -> Unit,
    onShowDetails: () -> Unit,
    onShowCalendar: (initialPage: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var hasSwipedInThisGesture by remember { mutableStateOf(false) }
    
    // Smooth fade in for the first moon render to match splash
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
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { hasSwipedInThisGesture = false },
                        onDrag = { change, dragAmount ->
                            if (!hasSwipedInThisGesture) {
                                // Swipe left to get Daily Note
                                if (dragAmount.x < -20) {
                                    hasSwipedInThisGesture = true
                                    if (showSwipeHint) onDismissSwipeHint()
                                    onShowCalendar(1)
                                }
                                // Swipe right for Calendar
                                else if (dragAmount.x > 20) {
                                    hasSwipedInThisGesture = true
                                    if (showSwipeHint) onDismissSwipeHint()
                                    onShowCalendar(0)
                                }
                                // Vertical swipe (up) for Details
                                else if (dragAmount.y < -20) {
                                    hasSwipedInThisGesture = true
                                    onShowDetails()
                                }
                            }
                            change.consume()
                        }
                    )
                }
        ) {
            // Background Moon - Persistent
            if (moonData != null) {
                AnimatedVisibility(
                    visible = moonVisible,
                    enter = fadeIn(animationSpec = tween(1000)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        MoonVisualization(
                            moonData = moonData,
                            locationData = locationData,
                            modifier = Modifier
                                .fillMaxHeight(0.85f) // Scale to 85% of screen height
                                .aspectRatio(1f)      // Keep it square
                                .padding(16.dp)
                        )
                    }
                }
            }

            // UI Overlay
            AnimatedVisibility(
                visible = isTextVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Hidden toggle area (still allow clicking top corner if user knows, but no icon)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(64.dp)
                            .clickable(onClick = onToggleTextVisibility)
                    )

                    // Text Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (moonData != null) {
                            Column(
                                modifier = Modifier.padding(top = 40.dp),
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

            // Swipe Hint
            AnimatedVisibility(
                visible = showSwipeHint && isTextVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Swipe left for calendar",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
