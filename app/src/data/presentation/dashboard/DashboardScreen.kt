/*
 * Copyright (c) 2026 Mr.Rm19
 * GitHub: https://github.com/Rm19x
 * All rights reserved.
 *
 * This file is part of the Real-World Smart Home Automation System.
 * Built with Kotlin & Jetpack Compose for production environment.
 */

package com.mrrm19.smarthome.presentation.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tampilan Antarmuka Panel Kendali Utama Smart Home ( 1, 2, 3, 10, 11, 43, 45, 46)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val systemHealth by viewModel.systemHealth.collectAsState()
    val isConnected by viewModel.isMqttConnected.collectAsState()

    //  43: Transisi warna latar belakang kontainer secara halus (Fade In/Out Effect)
    val animatedCardBackground by animateColorAsState(
        targetValue = if (uiState.isLightOn) {
            Color(uiState.rgbColor.red, uiState.rgbColor.green, uiState.rgbColor.blue).copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 800) // Durasi pemudaran warna 800ms
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Mr.Rm19", fontWeight = FontWeight.Bold) },
                actions = {
                    // Indikator Status Koneksi Hardware Nyata ( 13)
                    Badge(
                        containerColor = if (isConnected) Color.Green else Color.Red,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = if (isConnected) "ONLINE" else "OFFLINE",
                            color = Color.Black,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedThan(16.dp)
        ) {
            
            // --- SECTION 1: MONITORING SENSOR LINGKUNGAN ( 2 & 3) ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Suhu Card
                Card(modifier = Modifier.weight(1fr), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Thermostat, contentDescription = "Suhu", tint = Color.Red)
                        Text("Suhu Ruangan", fontSize = 12.sp, color = Color.Gray)
                        Text("${uiState.temperature}°C", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
                // Kelembaban Card
                Card(modifier = Modifier.weight(1fr), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.WaterDrop, contentDescription = "Kelembaban", tint = Color.Blue)
                        Text("Kelembaban", fontSize = 12.sp, color = Color.Gray)
                        Text("${uiState.humidity}%", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- SECTION 2: UTILITY SAKLAR & DIMMER UTAMA ( 1 & 10) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = animatedCardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = "Lampu", modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lampu Utama", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        }
                        // Tombol Saklar Utama ( 1)
                        Switch(
                            checked = uiState.isLightOn,
                            onCheckedChange = { viewModel.toggleLight(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pengatur Slider Dimmer Kecerahan ( 10)
                    Text("Tingkat Kecerahan Dimmer: ${uiState.brightnessDimmer}%", fontSize = 14.sp)
                    Slider(
                        value = uiState.brightnessDimmer.toFloat(),
                        onValueChange = { viewModel.setDimmerValue(it.toInt()) },
                        valueRange = 0f..100f,
                        enabled = uiState.isLightOn
                    )
                }
            }

            // --- SECTION 3: RGB SCENE & SCENARIO MODE ( 11 & 45) ---
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Warna Lampu RGB & Mode Skenario", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Warna RGB Cepat ( 11)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(onClick = { viewModel.setRgbColor(255, 0, 0) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Merah") }
                        Button(onClick = { viewModel.setRgbColor(0, 255, 0) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) { Text("Hijau") }
                        Button(onClick = { viewModel.setRgbColor(0, 0, 255) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)) { Text("Biru") }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Tombol Aktivasi Mode Bioskop ( 45)
                    Button(
                        onClick = { viewModel.activateCinemaMode() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = "Cinema Mode")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Aktifkan Cinema Mode ( 45)")
                    }
                }
            }

            // --- SECTION 4: METRIK RESOURCE INTERNAL ANDROID HP ( 46) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = "Health", tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kesehatan Sistem HP ( 46)", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Penggunaan RAM Aplikasi: ${systemHealth["ram_used_percentage"] ?: 0}%", fontSize = 13.sp)
                    Text("Sisa Kapasitas RAM: ${systemHealth["ram_available_gb"] ?: "0 GB"} / ${systemHealth["ram_total_gb"] ?: "0 GB"}", fontSize = 13.sp)
                    Text("Estimasi Load CPU Prosedur: ${systemHealth["cpu_usage_percentage"] ?: 0}%", fontSize = 13.sp)
                }
            }

            // Footer Penanda Kepemilikan Berkas
            Text(
                text = "Developer Infrastructure: Mr.Rm19 | github.com/Rm19x",
                color = Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            )
        }
    }
}
