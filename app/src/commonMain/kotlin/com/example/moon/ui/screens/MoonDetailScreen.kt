package com.example.moon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moon.domain.model.LocationData
import com.example.moon.domain.model.MoonData
import com.example.moon.ui.components.MoonVisualization

@Composable
fun MoonDetailScreen(
    moonData: MoonData?,
    onBack: () -> Unit,
    isWallpaperScheduled: Boolean = false,
    onToggleWallpaperSchedule: (Boolean) -> Unit = {},
    onUpdateWallpaperNow: () -> Unit = {},
    modifier: Modifier = Modifier,
    locationData: LocationData = LocationData(latitude = 51.5074, longitude = -0.1278)
) {
    Surface(
        modifier = modifier.fillMaxSize(),
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
