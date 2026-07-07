/*
 * Copyright (c) 2026 Mr.Rm19
 * GitHub: https://github.com/Rm19x
 * All rights reserved.
 *
 * This file is part of the Real-World Smart Home Automation System.
 * Built with Kotlin & Jetpack Compose for production environment.
 */

package com.mrrm19.smarthome.presentation.auth

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.mrrm19.smarthome.domain.usecase.BiometricAuthUseCase

/**
 * Komponen UI Gerbang Masuk Smart Home Utama ( 24 & 50).
 * Mengamankan dashboard menggunakan enkripsi sensor biometrik asli bawaan sistem Android.
 * * @param biometricUseCase Instansiasi logika otentikasi biometrik yang sah.
 * @param onAuthSuccess Callback ketika user berhasil memindai sidik jari/wajah dan mendapatkan hak akses.
 */
@Composable
fun LoginScreen(
    biometricUseCase: BiometricAuthUseCase,
    onAuthSuccess: (role: String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    
    var authErrorMessage by remember { mutableStateOf("") }
    var isHardwareAvailable by remember { mutableStateOf(true) }

    // Jalankan pengecekan ketersediaan hardware sensor fisik saat layar pertama kali dimuat
    LaunchedEffect(Unit) {
        isHardwareAvailable = biometricUseCase.isBiometricHardwareAvailable()
        if (!isHardwareAvailable) {
            authErrorMessage = "Sensor Biometrik tidak terdeteksi atau belum dikonfigurasi di HP ini."
        } else {
            // Otomatis memicu dialog sidik jari saat aplikasi dibuka (Seamless Security Experience)
            activity?.let {
                biometricUseCase.promptBiometricAuthentication(
                    activity = it,
                    onSuccess = {
                        // Secara bawaan mengarahkan ke Role ADMIN untuk pemilik perangkat utama ( 24)
                        onAuthSuccess("ADMIN")
                    },
                    onError = { errorCode, errString ->
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                            authErrorMessage = "Gagal Verifikasi: $errString"
                        }
                    }
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Identitas Pembuat & Keamanan Berkas Sistem
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Security System Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "MR.RM19 SMART HOME",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.5.sp
            )
            
            Text(
                text = "Secured Infrastructure Ecosystem",
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Kotak Kontrol Otentikasi Utama
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Diperlukan Autentikasi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Tombol Trigger Sensor Sidik Jari Lingkaran
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                if (isHardwareAvailable) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (isHardwareAvailable && activity != null) {
                                    authErrorMessage = ""
                                    biometricUseCase.promptBiometricAuthentication(
                                        activity = activity,
                                        onSuccess = { onAuthSuccess("ADMIN") },
                                        onError = { _, errString -> authErrorMessage = errString.toString() }
                                    )
                                } else {
                                    Toast.makeText(context, "Hardware Keamanan Bermasalah", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(70.dp)
                        ) {
                            Icon(
                                imageVector = if (isHardwareAvailable) Icons.Default.Fingerprint else Icons.Default.Lock,
                                contentDescription = "Scan Biometric Sensor",
                                tint = if (isHardwareAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Ketuk ikon sidik jari di atas jika prompt pemindaian sistem tersembunyi",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Area Tampilan Log Error Sistem Secara Dinamis
            Spacer(modifier = Modifier.height(24.dp))
            AnimatedVisibility(
                visible = authErrorMessage.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = authErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Teks Hak Cipta Statis Footer Layar
        Text(
            text = "© 2026 Mr.Rm19 | github.com/Rm19x",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
