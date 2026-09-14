package com.example.moon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moon.domain.model.EventType
import com.example.moon.domain.model.LocationData
import com.example.moon.domain.model.LunarEvent
import com.example.moon.domain.model.MoonData
import com.example.moon.domain.model.MoonPhase
import com.example.moon.domain.model.formatEventName
import com.example.moon.ui.CalendarViewModel
import com.example.moon.ui.components.MoonVisualization
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.toInstant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    locationData: LocationData,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: CalendarViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var selectedDate by remember { mutableStateOf(today) }
    
    LaunchedEffect(uiState.selectedYear, uiState.selectedMonth, locationData) {
        viewModel.loadEvents(uiState.selectedYear, uiState.selectedMonth, locationData)
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = "Lunar Calendar",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
            
            MonthHeader(
                year = uiState.selectedYear,
                month = uiState.selectedMonth,
                onPreviousMonth = {
                    viewModel.previousMonth(locationData)
                },
                onNextMonth = {
                    viewModel.nextMonth(locationData)
                }
            )
            
            CalendarGrid(
                year = uiState.selectedYear,
                month = uiState.selectedMonth,
                events = uiState.events,
                dailyMoonData = uiState.dailyMoonData,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                locationData = locationData
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.2f))
            
            EventList(
                selectedDate = selectedDate,
                events = uiState.events,
                locationData = locationData,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MonthHeader(
    year: Int,
    month: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthName = Month(month).name.lowercase().replaceFirstChar { it.uppercase() }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Previous Month", tint = Color.White)
        }
        
        Text(
            text = "$monthName $year",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next Month", tint = Color.White)
        }
    }
}

@Composable
fun CalendarGrid(
    year: Int,
    month: Int,
    events: List<LunarEvent>,
    dailyMoonData: Map<LocalDate, MoonData>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    locationData: LocationData
) {
    val firstOfMonth = LocalDate(year, month, 1)
    val daysInMonth = if (month == 12) {
        LocalDate(year + 1, 1, 1).toEpochDays() - firstOfMonth.toEpochDays()
    } else {
        LocalDate(year, month + 1, 1).toEpochDays() - firstOfMonth.toEpochDays()
    }
    
    val firstDayOfWeek = (firstOfMonth.dayOfWeek.ordinal + 1) % 7 // 0 = Sunday
    
    val totalCells = (daysInMonth + firstDayOfWeek).toInt()
    val rows = (totalCells + 6) / 7

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        // Day names
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    Box(modifier = Modifier.weight(1f)) {
                        if (index in firstDayOfWeek until totalCells) {
                            val day = (index - firstDayOfWeek + 1).toInt()
                            val date = LocalDate(year, month, day)
                            val isSelected = date == selectedDate
                            val dayEvents = events.filter { it.dateTime.date == date }
                            val moonData = dailyMoonData[date]
                            
                            DayCell(
                                day = day,
                                isSelected = isSelected,
                                events = dayEvents,
                                moonData = moonData,
                                onDateSelected = { onDateSelected(date) },
                                locationData = locationData
                            )
                        } else {
                            Box(modifier = Modifier.aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    isSelected: Boolean,
    events: List<LunarEvent>,
    moonData: MoonData?,
    onDateSelected: () -> Unit,
    locationData: LocationData
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onDateSelected() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) Color.Black else Color.White,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            
            if (moonData != null) {
                Box(modifier = Modifier.size(16.dp)) {
                    MoonVisualization(
                        moonData = moonData,
                        locationData = locationData,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else if (events.isNotEmpty()) {
                // Fallback for events if daily data missing (shouldn't happen with updated VM)
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.Black else Color.Gray)
                )
            }
        }
    }
}

@Composable
fun EventList(
    selectedDate: LocalDate,
    events: List<LunarEvent>,
    locationData: LocationData,
    modifier: Modifier = Modifier
) {
    val dayEvents = events.filter { it.dateTime.date == selectedDate }
    
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            val monthName = Month(selectedDate.monthNumber).name.lowercase().replaceFirstChar { it.uppercase() }
            Text(
                text = "Events for $monthName ${selectedDate.dayOfMonth}, ${selectedDate.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        if (dayEvents.isEmpty()) {
            item {
                Text(
                    text = "No major lunar events",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            items(dayEvents) { event ->
                EventItem(event = event, locationData = locationData)
            }
        }
    }
}

@Composable
fun EventItem(event: LunarEvent, locationData: LocationData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (event.type != EventType.PERIGEE && event.type != EventType.APOGEE) {
                 Box(modifier = Modifier.size(40.dp)) {
                    MoonVisualization(
                        moonData = MoonData(
                            phase = mapEventToPhase(event.type),
                            illumination = if (event.type == EventType.FULL_MOON) 1.0 else if (event.type == EventType.NEW_MOON) 0.0 else 0.5,
                            age = 0.0,
                            riseTime = null,
                            setTime = null
                        ),
                        locationData = locationData,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            } else {
                Icon(
                    imageVector = if (event.type == EventType.PERIGEE) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column {
                Text(
                    text = formatEventName(event),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                val timeStr = "${event.dateTime.hour.toString().padStart(2, '0')}:${event.dateTime.minute.toString().padStart(2, '0')}"
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
        }
    }
}

fun mapEventToPhase(type: EventType): MoonPhase {
    return when (type) {
        EventType.NEW_MOON -> MoonPhase.NEW
        EventType.FIRST_QUARTER -> MoonPhase.FIRST_QUARTER
        EventType.FULL_MOON -> MoonPhase.FULL
        EventType.LAST_QUARTER -> MoonPhase.LAST_QUARTER
        else -> MoonPhase.NEW
    }
}
