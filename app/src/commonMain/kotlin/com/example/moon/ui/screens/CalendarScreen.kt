package com.example.moon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moon.domain.model.*
import com.example.moon.domain.repository.NoteRepository
import com.example.moon.ui.CalendarViewModel
import com.example.moon.ui.components.MoonVisualization
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.toInstant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    locationData: LocationData,
    moonData: MoonData? = null,
    noteRepository: NoteRepository? = null,
    initialPage: Int = 0,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: CalendarViewModel = viewModel {
        CalendarViewModel(noteRepository)
    }
    val uiState by viewModel.uiState.collectAsState()
    
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var selectedDate by remember { mutableStateOf(today) }
    
    LaunchedEffect(uiState.selectedYear, uiState.selectedMonth, locationData) {
        viewModel.loadEvents(uiState.selectedYear, uiState.selectedMonth, locationData)
    }
    
    val pagerState = rememberPagerState(initialPage = initialPage) { 2 }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .pointerInput(pagerState.currentPage) {
                    var hasSwiped = false
                    detectHorizontalDragGestures(
                        onDragStart = { hasSwiped = false },
                        onHorizontalDrag = { change, dragAmount ->
                            if (!hasSwiped) {
                                // On Calendar view (0), swipe left (finger right-to-left, dragAmount < -20) returns to Moon
                                if (pagerState.currentPage == 0 && dragAmount < -20) {
                                    hasSwiped = true
                                    onBack()
                                }
                                // On Daily Note view (1), swipe right (finger left-to-right, dragAmount > 20) returns to Moon
                                else if (pagerState.currentPage == 1 && dragAmount > 20) {
                                    hasSwiped = true
                                    onBack()
                                }
                            }
                            change.consume()
                        }
                    )
                }
        ) {
            // Background Moon Visualization so it shines through the text
            if (moonData != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    MoonVisualization(
                        moonData = moonData,
                        locationData = locationData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(32.dp)
                            .alpha(0.35f) // Transparent so moon is beautifully visible through words
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        // Full Screen Calendar View
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val isLandscape = maxWidth > maxHeight
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding()
                            ) {
                                // Elegant header bar for full screen view
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = onBack) {
                                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go Back", tint = Color.White)
                                    }
                                    
                                    TextButton(
                                        onClick = {
                                            scope.launch { pagerState.animateScrollToPage(1) }
                                        }
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.EditNote, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Swipe Left for Daily Note", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }

                                if (isLandscape) {
                                    Row(modifier = Modifier.fillMaxSize()) {
                                        EventList(
                                            selectedDate = selectedDate,
                                            events = uiState.events,
                                            notes = uiState.notes,
                                            onSaveNote = { date, note -> viewModel.saveNote(date, note) },
                                            locationData = locationData,
                                            showTextField = false,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        VerticalDivider(color = Color.White.copy(alpha = 0.1f))

                                        Column(modifier = Modifier.weight(1.2f)) {
                                            MonthHeader(
                                                year = uiState.selectedYear,
                                                month = uiState.selectedMonth,
                                                onPreviousMonth = { viewModel.previousMonth(locationData) },
                                                onNextMonth = { viewModel.nextMonth(locationData) }
                                            )
                                            
                                            CalendarGrid(
                                                year = uiState.selectedYear,
                                                month = uiState.selectedMonth,
                                                events = uiState.events,
                                                dailyMoonData = uiState.dailyMoonData,
                                                notes = uiState.notes,
                                                selectedDate = selectedDate,
                                                onDateSelected = { 
                                                    selectedDate = it
                                                    scope.launch { pagerState.animateScrollToPage(1) }
                                                },
                                                locationData = locationData
                                            )
                                        }
                                    }
                                } else {
                                    MonthHeader(
                                        year = uiState.selectedYear,
                                        month = uiState.selectedMonth,
                                        onPreviousMonth = { viewModel.previousMonth(locationData) },
                                        onNextMonth = { viewModel.nextMonth(locationData) }
                                    )
                                    
                                    CalendarGrid(
                                        year = uiState.selectedYear,
                                        month = uiState.selectedMonth,
                                        events = uiState.events,
                                        dailyMoonData = uiState.dailyMoonData,
                                        notes = uiState.notes,
                                        selectedDate = selectedDate,
                                        onDateSelected = { 
                                            selectedDate = it
                                            scope.launch { pagerState.animateScrollToPage(1) }
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
                    1 -> {
                        // Daily Note focused space
                        val currentNote = uiState.notes[selectedDate] ?: ""
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val monthName = selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
                                Column {
                                    Text(
                                        text = if (selectedDate == today) "Today's Note" else "Daily Note",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "$monthName ${selectedDate.dayOfMonth}, ${selectedDate.year}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                                
                                IconButton(
                                    onClick = {
                                        scope.launch { pagerState.animateScrollToPage(0) }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack, // Changed to back since it's on page 1 going to 0
                                        contentDescription = "Back to Calendar",
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            TextField(
                                value = currentNote,
                                onValueChange = { viewModel.saveNote(selectedDate, it, locationData) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                placeholder = {
                                    Text(
                                        text = "Type your daily thoughts and reflections here...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White.copy(alpha = 0.3f)
                                    )
                                },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
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
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth, modifier = Modifier.size(32.dp)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Previous Month", tint = Color.White)
        }
        
        Text(
            text = "$monthName $year",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        IconButton(onClick = onNextMonth, modifier = Modifier.size(32.dp)) {
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
    notes: Map<LocalDate, String>,
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
        
        Spacer(modifier = Modifier.height(4.dp))
        
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
                                hasNote = notes.containsKey(date),
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
            .padding(2.dp)
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
        item {
            val monthName = Month(selectedDate.monthNumber).name.lowercase().replaceFirstChar { it.uppercase() }
            Text(
                text = "Details for $monthName ${selectedDate.dayOfMonth}, ${selectedDate.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
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
