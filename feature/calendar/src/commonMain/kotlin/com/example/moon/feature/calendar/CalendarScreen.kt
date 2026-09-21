package com.example.moon.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moon.core.domain.model.*
import com.example.moon.core.domain.repository.NoteRepository
import com.example.moon.core.ui.components.MoonVisualization
import kotlinx.datetime.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    locationData: LocationData,
    modifier: Modifier = Modifier,
    noteRepository: NoteRepository? = null,
    onInteraction: () -> Unit = {},
    onBack: () -> Unit,
    onShowDetails: () -> Unit = {}
) {
    val viewModel: CalendarViewModel = viewModel {
        CalendarViewModel(noteRepository)
    }
    val uiState by viewModel.uiState.collectAsState()
    
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var selectedDate by remember { mutableStateOf(today) }
    var isEditing by remember { mutableStateOf(false) }
    
    LaunchedEffect(locationData) {
        viewModel.loadEvents(today, locationData)
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                            onInteraction()
                        }
                    }
                }
        ) {
            if (isEditing) {
                // ... (Editing UI remains same)
            } else {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val isLandscape = maxWidth > maxHeight
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .background(Color.Transparent)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        if (isLandscape) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                EventList(
                                    selectedDate = selectedDate,
                                    events = uiState.events,
                                    notes = uiState.notes,
                                    onSaveNote = { date, note -> viewModel.saveNote(date, note) },
                                    locationData = locationData,
                                    showTextField = false,
                                    onShowDetails = onShowDetails,
                                    modifier = Modifier.weight(1.5f)
                                )
                                
                                VerticalDivider(color = Color.White.copy(alpha = 0.1f))

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    CycleHeader(
                                        uiState = uiState,
                                        onPreviousCycle = { viewModel.previousMonth(locationData) },
                                        onNextCycle = { viewModel.nextMonth(locationData) }
                                    )
                                    
                                    LunarCalendarGrid(
                                        uiState = uiState,
                                        selectedDate = selectedDate,
                                        onDateSelected = { 
                                            if (it == selectedDate) {
                                                isEditing = true
                                            } else {
                                                selectedDate = it
                                            }
                                        },
                                        locationData = locationData
                                    )
                                }
                            }
                        } else {
                            CycleHeader(
                                uiState = uiState,
                                onPreviousCycle = { viewModel.previousMonth(locationData) },
                                onNextCycle = { viewModel.nextMonth(locationData) }
                            )
                            
                            LunarCalendarGrid(
                                uiState = uiState,
                                selectedDate = selectedDate,
                                onDateSelected = { 
                                    if (it == selectedDate) {
                                        isEditing = true
                                    } else {
                                        selectedDate = it
                                    }
                                },
                                locationData = locationData
                            )
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.2f))
                            
                            EventList(
                                selectedDate = selectedDate,
                                events = uiState.events,
                                notes = uiState.notes,
                                onSaveNote = { date, note -> viewModel.saveNote(date, note) },
                                locationData = locationData,
                                showTextField = false,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Back button to moon
            if (!isEditing) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun CycleHeader(
    uiState: CalendarUiState,
    onPreviousCycle: () -> Unit,
    onNextCycle: () -> Unit
) {
    val start = uiState.cycleStart?.dateTime?.date
    val end = uiState.cycleEnd?.dateTime?.date
    
    val title = if (start != null && end != null) {
        val startMonth = start.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        val endMonth = end.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        if (startMonth == endMonth) "$startMonth ${start.year}" else "$startMonth - $endMonth ${start.year}"
    } else "Lunar Cycle"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousCycle, modifier = Modifier.size(32.dp)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Previous Cycle", tint = Color.White)
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (start != null) {
                Text(
                    text = "Starts ${start.dayOfMonth} ${start.month.name.lowercase().take(3)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        IconButton(onClick = onNextCycle, modifier = Modifier.size(32.dp)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next Cycle", tint = Color.White)
        }
    }
}

@Composable
fun LunarCalendarGrid(
    uiState: CalendarUiState,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    locationData: LocationData
) {
    val start = uiState.cycleStart?.dateTime?.date ?: return
    val end = uiState.cycleEnd?.dateTime?.date ?: return
    
    val days = mutableListOf<LocalDate>()
    var curr = start
    while (curr <= end) {
        days.add(curr)
        curr = curr.plus(1, DateTimeUnit.DAY)
    }

    val columns = 7
    val rows = (days.size + columns - 1) / columns

    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    Box(modifier = Modifier.weight(1f)) {
                        if (index < days.size) {
                            val date = days[index]
                            val isSelected = date == selectedDate
                            val dayEvents = uiState.events.filter { it.dateTime.date == date }
                            val moonData = uiState.dailyMoonData[date]
                            
                            DayCell(
                                day = date.dayOfMonth,
                                isSelected = isSelected,
                                events = dayEvents,
                                moonData = moonData,
                                hasNote = uiState.notes.containsKey(date),
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
    hasNote: Boolean,
    onDateSelected: () -> Unit,
    locationData: LocationData
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onDateSelected() },
        contentAlignment = Alignment.Center
    ) {
        if (hasNote) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color.Yellow)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color.Black else Color.White,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            
            if (moonData != null) {
                Box(modifier = Modifier.size(20.dp)) {
                    MoonVisualization(
                        moonData = moonData,
                        locationData = locationData,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else if (events.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(3.dp)
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
    notes: Map<LocalDate, String>,
    onSaveNote: (LocalDate, String) -> Unit,
    locationData: LocationData,
    showTextField: Boolean = true,
    onShowDetails: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dayEvents = events.filter { it.dateTime.date == selectedDate }
    val currentNote = notes[selectedDate] ?: ""
    
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showTextField) {
            item {
                OutlinedTextField(
                    value = currentNote,
                    onValueChange = { onSaveNote(selectedDate, it) },
                    label = { Text("Daily Note", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray
                    )
                )
            }
        } else {
            if (currentNote.isNotEmpty()) {
                item {
                    Text(
                        text = currentNote,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
            
            item {
                TextButton(
                    onClick = onShowDetails,
                    modifier = Modifier.padding(top = 8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
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
                    val moonData = event.moonData ?: MoonData(
                        phase = mapEventToPhase(event.type),
                        illumination = if (event.type == EventType.FULL_MOON) 1.0 else if (event.type == EventType.NEW_MOON) 0.0 else 0.5,
                        age = 0.0,
                        riseTime = null,
                        setTime = null
                    )
                    
                    MoonVisualization(
                        moonData = moonData,
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
