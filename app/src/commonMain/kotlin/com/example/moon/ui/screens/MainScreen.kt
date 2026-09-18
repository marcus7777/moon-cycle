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
    onSetManualLocation: (Double, Double, String?) -> Unit,
    onUseDeviceLocation: () -> Unit,
    onRequestLocationPermission: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var hasSwipedInThisGesture by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    
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
            // ... (keep existing moon visualization)
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
                                // Location Badge - Clickable
                                Surface(
                                    color = if (locationData.isDefault) Color.DarkGray else Color.White.copy(alpha = 0.1f),
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier
                                        .padding(bottom = 16.dp)
                                        .clickable { showLocationDialog = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.LocationOn,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        val locText = locationData.name ?: if (locationData.isDefault) "London" else "Custom Location"
                                        Text(text = locText, style = MaterialTheme.typography.labelSmall, color = Color.White)
                                    }
                                }

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

    if (showLocationDialog) {
        LocationPickerDialog(
            locationData = locationData,
            onDismiss = { showLocationDialog = false },
            onSetManual = { lat, lng, name ->
                onSetManualLocation(lat, lng, name)
                showLocationDialog = false
            },
            onUseDevice = {
                onRequestLocationPermission {
                    onUseDeviceLocation()
                    showLocationDialog = false
                }
            }
        )
    }
}

@Composable
fun LocationPickerDialog(
    locationData: LocationData,
    onDismiss: () -> Unit,
    onSetManual: (Double, Double, String?) -> Unit,
    onUseDevice: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val selectedCityName = if (locationData.isDefault && locationData.name == "London") {
        "Select a City"
    } else if (!locationData.isDefault && locationData.name != null) {
        locationData.name
    } else if (!locationData.isDefault) {
         "Current Location"
    } else {
        "Select a City"
    }

    val cities = listOf(
        City("London", 51.5074, -0.1278),
        City("New York", 40.7128, -74.0060),
        City("Tokyo", 35.6762, 139.6503),
        City("Sydney", -33.8688, 151.2093),
        City("Berlin", 52.5200, 13.4050),
        City("Dubai", 25.2048, 55.2708),
        City("Los Angeles", 34.0522, -118.2437),
        City("Paris", 48.8566, 2.3522),
        City("Mumbai", 19.0760, 72.8777),
        City("São Paulo", -23.5505, -46.6333),
        City("Cairo", 30.0444, 31.2357),
        City("Cape Town", -33.9249, 18.4241),
        City("Moscow", 55.7558, 37.6173),
        City("Beijing", 39.9042, 116.4074),
        City("Singapore", 1.3521, 103.8198),
        City("Bangkok", 13.7563, 100.5018),
        City("Mexico City", 19.4326, -99.1332),
        City("Seoul", 37.5665, 126.9780),
        City("Toronto", 43.6532, -79.3832),
        City("Madrid", 40.4168, -3.7038)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A), // Dark mode background
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        title = { Text("Update Location") },
        text = {
            Column {
                Text("Select a major city to view the moon from that location.")
                Spacer(modifier = Modifier.height(24.dp))
                
                // City Dropdown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), MaterialTheme.shapes.small)
                        .clickable { expanded = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = selectedCityName ?: "Select a City", color = if (selectedCityName == "Select a City") Color.Gray else Color.White)
                        Icon(
                            imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .background(Color(0xFF2C2C2C))
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Use My Current Location", color = Color.White)
                                }
                            },
                            onClick = {
                                expanded = false
                                onUseDevice()
                            }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city.name, color = Color.White) },
                                onClick = {
                                    expanded = false
                                    onSetManual(city.lat, city.lng, city.name)
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
            ) {
                Text("Cancel")
            }
        }
    )
}

data class City(val name: String, val lat: Double, val lng: Double)
