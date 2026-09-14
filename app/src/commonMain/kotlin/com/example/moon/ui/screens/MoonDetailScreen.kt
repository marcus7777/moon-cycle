package com.example.moon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
                    DetailItem("Age", "${"%.1f".format(moonData.age)} days")
                    
                    moonData.riseTime?.let {
                        val timeStr = "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
                        DetailItem("Moonrise", timeStr)
                    }
                    moonData.setTime?.let {
                        val timeStr = "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
                        DetailItem("Moonset", timeStr)
                    }
                    
                    moonData.altitude?.let {
                        DetailItem("Altitude", "${"%.1f".format(it)}°")
                    }
                    moonData.azimuth?.let {
                        DetailItem("Azimuth", "${"%.1f".format(it)}°")
                    }
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

@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = Color.White)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp, color = Color.DarkGray)
    }
}
