package com.example.moon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moon.domain.model.LocationData
import com.example.moon.domain.model.MoonData
import com.example.moon.ui.components.MoonVisualization

import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset

@Composable
fun MoonDetailScreen(
    moonData: MoonData?,
    onBack: () -> Unit,
    isWallpaperScheduled: Boolean = false,
    onToggleWallpaperSchedule: (Boolean) -> Unit = {},
    onUpdateWallpaperNow: () -> Unit = {},
    onExportJsonl: () -> String = { "" },
    onImportJsonl: (String, (Boolean) -> Unit) -> Unit = { _, _ -> },
    onExportICal: () -> String = { "" },
    onImportICal: (String, (Boolean) -> Unit) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    locationData: LocationData = LocationData(latitude = 51.5074, longitude = -0.1278)
) {
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 40) {
                    onBack()
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }
        }
    }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var isICalMode by remember { mutableStateOf(false) }
    var importInputText by remember { mutableStateOf("") }
    var importStatusMessage by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxSize().nestedScroll(nestedScrollConnection),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = "Moon Details",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (moonData != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(max = 200.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MoonVisualization(
                        moonData = moonData,
                        locationData = locationData,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    )
                    
                    Spacer(modifier = Modifier.width(24.dp))
                    
                    Column {
                        Text(
                            text = moonData.phase.description,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Illumination: ${(moonData.illumination * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    DetailItem("Phase", moonData.phase.description)
                    DetailItem("Illumination", "${(moonData.illumination * 100).toInt()}%")
                    DetailItem("Age", "${formatOneDecimal(moonData.age)} days")
                    
                    moonData.riseTime?.let {
                        val timeStr = "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
                        DetailItem("Moonrise", timeStr)
                    }
                    moonData.setTime?.let {
                        val timeStr = "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
                        DetailItem("Moonset", timeStr)
                    }
                    
                    moonData.altitude?.let {
                        DetailItem("Altitude", "${formatOneDecimal(it)}°")
                    }
                    moonData.azimuth?.let {
                        DetailItem("Azimuth", "${formatOneDecimal(it)}°")
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Backup / JSONL Data space
                    Text(
                        text = "Data Backup & Recovery (JSONL)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    isICalMode = false
                                    exportText = onExportJsonl()
                                    showExportDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White)
                            ) {
                                Icon(Icons.Rounded.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download Data", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = {
                                    isICalMode = false
                                    importInputText = ""
                                    importStatusMessage = ""
                                    showImportDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White)
                            ) {
                                Icon(Icons.Rounded.Upload, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upload Data", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Calendar Sync iCal space
                    Text(
                        text = "Calendar Sync (iCal / .ics)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    isICalMode = true
                                    exportText = onExportICal()
                                    showExportDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White)
                            ) {
                                Icon(Icons.Rounded.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export iCal", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = {
                                    isICalMode = true
                                    importInputText = ""
                                    importStatusMessage = ""
                                    showImportDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White)
                            ) {
                                Icon(Icons.Rounded.Upload, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import iCal", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Wallpaper Settings
                    Text(
                        text = "Wallpaper Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Dynamic Wallpaper", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                    Text(
                                        "Automatically update your background to match the moon phase.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Switch(
                                    checked = isWallpaperScheduled,
                                    onCheckedChange = onToggleWallpaperSchedule
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = onUpdateWallpaperNow,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Rounded.Wallpaper, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Update Wallpaper Now")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            } else {
                Text(
                    text = "No data available",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = Color(0xFF151515),
            title = { Text(if (isICalMode) "Your iCal Export Data" else "Your JSONL Export Data", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = exportText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White, unfocusedBorderColor = Color.Gray)
                )
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = Color(0xFF151515),
            title = { Text(if (isICalMode) "Upload / Paste iCal Data" else "Upload / Paste JSONL Data", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = { importInputText = it },
                        placeholder = { Text(if (isICalMode) "Paste iCal text here starting with BEGIN:VCALENDAR..." else "Paste JSONL lines here...", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White, unfocusedBorderColor = Color.Gray)
                    )
                    if (importStatusMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(importStatusMessage, color = if (importStatusMessage.contains("Success")) Color.Green else Color.Red, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isICalMode) {
                            onImportICal(importInputText) { success ->
                                importStatusMessage = if (success) "Success! iCal Data fully imported." else "Error: Invalid iCal backup format."
                            }
                        } else {
                            onImportJsonl(importInputText) { success ->
                                importStatusMessage = if (success) "Success! Data fully loaded." else "Error: Invalid JSONL backup format."
                            }
                        }
                    }
                ) {
                    Text("Import Now", color = Color.Green)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

private fun formatOneDecimal(v: Double): String {
    val rounded = kotlin.math.round(v * 10) / 10.0
    val s = rounded.toString()
    return if (s.contains('.')) s else "$s.0"
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = Color.White)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp, color = Color.DarkGray)
    }
}
