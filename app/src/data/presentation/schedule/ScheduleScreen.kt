/*
 * Copyright (c) 2026 Mr.Rm19
 * GitHub: https://github.com/Rm19x
 * All rights reserved.
 *
 * This file is part of the Real-World Smart Home Automation System.
 * Built with Kotlin & Jetpack Compose for production environment.
 */

package com.mrrm19.smarthome.presentation.schedule

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrrm19.smarthome.data.local.ScheduleEntity
import com.mrrm19.smarthome.presentation.dashboard.DashboardViewModel
import java.util.Calendar

/**
 * Komponen UI Manajemen Penjadwalan Waktu Otomatis Saklar ( 5).
 * Membaca data langsung dari tabel database Room secara asinkron.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    activeSchedules: List<ScheduleEntity>,
    onAddSchedule: (time: String, action: String) -> Unit,
    onDeleteSchedule: (id: Int) -> Unit,
    onToggleSchedule: (id: Int, isEnabled: Boolean) -> Unit
) {
    val context = LocalContext.current
    var selectedActionState by remember { mutableStateOf("ON") }

    // Mengambil kalender bawaan internal HP untuk inisialisasi awal TimePicker
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    // Membangun Dialog Jam Asli Android OS
    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            // Jalankan callback untuk diteruskan ke usecase & disimpan ke Room DB nyata
            onAddSchedule(formattedTime, selectedActionState)
        }, hour, minute, true
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { timePickerDialog.show() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Jadwal Baru")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Otomasi Waktu Lampu ( 5)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tentukan aksi perintah hardware terjadwal",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector Perintah untuk target jadwal berikutnya sebelum memicu picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Aksi Terjadwal Berikutnya:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row {
                    FilterChip(
                        selected = selectedActionState == "ON",
                        onClick = { selectedActionState = "ON" },
                        label = { Text("Nyalakan (ON)") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = selectedActionState == "OFF",
                        onClick = { selectedActionState = "OFF" },
                        label = { Text("Matikan (OFF)") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            if (activeSchedules.isEmpty()) {
                Box(modifier = Modifier.fillGrid(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Belum ada jadwal operasional terdaftar.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                }
            } else {
                // Tampilkan baris jadwal dari database lokal Room
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(activeSchedules, key = { it.id }) { schedule ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                              ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, contentDescription = "Waktu", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = schedule.targetTime,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Perintah: Kirim Sinyal [${schedule.actionCommand}]",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Switch Toggle Aktif/Nonaktifkan Sementara fungsi eksekusi jadwal
                                    Switch(
                                        checked = schedule.isEnabled,
                                        onCheckedChange = { onToggleSchedule(schedule.id, it) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Tombol hapus baris jadwal permanen dari Room DB
                                    IconButton(onClick = { onDeleteSchedule(schedule.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus Jadwal",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Fungsi pembantu ekstensi Modifier untuk mengisi ruang grid kosong
private fun Modifier.fillGrid(): Modifier = this.fillMaxWidth().fillMaxHeight(0.7f)
