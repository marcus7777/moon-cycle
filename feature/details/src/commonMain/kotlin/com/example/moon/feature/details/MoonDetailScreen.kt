package com.example.moon.feature.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.moon.core.domain.model.LocationData
import com.example.moon.core.domain.model.MoonData
import com.example.moon.core.ui.components.MoonVisualization

import androidx.compose.ui.input.pointer.pointerInput
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
    onDownloadJsonl: () -> Unit = {},
    onUploadJsonl: ((Boolean) -> Unit) -> Unit = { _ -> },
    onDownloadICal: () -> Unit = {},
    onUploadICal: ((Boolean) -> Unit) -> Unit = { _ -> },
    onSetManualLocation: (Double, Double, String?) -> Unit = { _, _, _ -> },
    onUseDeviceLocation: () -> Unit = {},
    onRequestLocationPermission: (() -> Unit) -> Unit = { _ -> },
    onInteraction: () -> Unit = {},
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

    var importStatusMessage by remember { mutableStateOf("") }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize().nestedScroll(nestedScrollConnection),
        color = Color.Black.copy(alpha = 0.8f) // Semi-transparent as requested
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                            onInteraction()
                        }
                    }
                },
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLocationDialog = true }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(text = "Viewing From", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val locText = locationData.name ?: if (locationData.isDefault && locationData.latitude == 51.5074) "London" else if (!locationData.isDefault) "Current Location" else "Custom Location"
                            Text(text = locText, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Rounded.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp, color = Color.DarkGray)
                    }

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
                                onClick = onDownloadJsonl,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White)
                            ) {
                                Icon(Icons.Rounded.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download Data", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = {
                                    onUploadJsonl { success ->
                                        importStatusMessage = if (success) "JSONL Data fully loaded!" else "Error: Invalid JSONL backup file."
                                        showStatusDialog = true
                                    }
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
                                onClick = onDownloadICal,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White)
                            ) {
                                Icon(Icons.Rounded.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export iCal", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = {
                                    onUploadICal { success ->
                                        importStatusMessage = if (success) "iCal Data fully imported!" else "Error: Invalid iCal file."
                                        showStatusDialog = true
                                    }
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

    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            containerColor = Color(0xFF151515),
            title = { Text("Import Status", color = Color.White) },
            text = {
                Text(importStatusMessage, color = Color.White)
            },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("OK", color = Color.White)
                }
            }
        )
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
        containerColor = Color(0xFF1A1A1A),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        title = { Text("Update Location") },
        text = {
            Column {
                Text("Select a major city to view the moon from that location.")
                Spacer(modifier = Modifier.height(24.dp))
                
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
