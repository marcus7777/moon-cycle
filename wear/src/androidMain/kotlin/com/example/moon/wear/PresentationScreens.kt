package com.example.moon.wear

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.*
import com.example.moon.core.domain.model.*
import com.example.moon.core.domain.provider.MoonDataProvider
import com.example.moon.core.domain.repository.AstronomyRepository
import com.example.moon.core.domain.repository.LocationRepository
import com.example.moon.core.ui.components.MoonVisualization
import kotlinx.datetime.*
import kotlin.math.roundToInt

@Composable
fun MainScreen(
    moonDataProvider: MoonDataProvider,
    locationRepository: LocationRepository,
    onShowCalendar: () -> Unit
) {
    val moonData by moonDataProvider.getMoonDataFlow().collectAsState(initial = null)
    var locationData by remember { mutableStateOf<LocationData?>(null) }
    LaunchedEffect(locationRepository) {
        locationData = locationRepository.getCurrentLocation()
    }
    
    ScreenScaffold(
        timeText = { TimeText() }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable { onShowCalendar() },
            contentAlignment = Alignment.Center
        ) {
            if (moonData != null && locationData != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = moonData!!.phase.description,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    MoonVisualization(
                        moonData = moonData!!,
                        locationData = locationData!!,
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "${(moonData!!.illumination * 100).roundToInt()}% Illuminated",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        
                        moonData!!.nextEvent?.let { event ->
                            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                            val duration = event.dateTime.toInstant(TimeZone.currentSystemDefault()) - now.toInstant(TimeZone.currentSystemDefault())
                            val days = duration.inWholeDays
                            val hours = duration.inWholeHours % 24

                            val countdownText = when {
                                days > 0 -> "$days ${if (days == 1L) "day" else "days"} left"
                                hours > 0 -> "$hours ${if (hours == 1L) "hour" else "hours"} left"
                                else -> "Tonight"
                            }
                            
                            Text(
                                text = "${formatEventName(event)}: $countdownText",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun CalendarScreen(
    locationRepository: LocationRepository,
    astronomyRepository: AstronomyRepository,
    onBack: () -> Unit
) {
    var events by remember { mutableStateOf<List<LunarEvent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        val location = locationRepository.getCurrentLocation()
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val start = now
        val end = now.toInstant(TimeZone.currentSystemDefault()).plus(30, DateTimeUnit.DAY, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())
        
        events = astronomyRepository.getLunarEventsInRange(start, end, location)
        isLoading = false
    }

    ScreenScaffold(
        timeText = { TimeText() }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "Calendar",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(events) { event ->
                    Card(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            Text(
                                text = formatEventName(event),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = event.dateTime.date.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                if (events.isEmpty()) {
                    item {
                        Text(
                            text = "No upcoming events",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
