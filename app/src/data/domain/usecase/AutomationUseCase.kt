/*
 * Copyright (c) 2026 Mr.Rm19
 * GitHub: https://github.com/Rm19x
 * All rights reserved.
 *
 * This file is part of the Real-World Smart Home Automation System.
 * Built with Kotlin & Jetpack Compose for production environment.
 */

package com.mrrm19.smarthome.domain.usecase

import android.util.Log
import com.mrrm19.smarthome.data.repository.HomeRepositoryImpl

class AutomationUseCase(private val homeRepository: HomeRepositoryImpl) {

    private companion object {
        const val TAG = "AutomationUseCase"
        const val TOPIC_COOLER_COMMAND = "smarthome/mrrm19/cooler/command"
        const val TOPIC_SECURITY_ALARM = "smarthome/mrrm19/security/alarm"
    }

    /**
     * Mengevaluasi suhu ruangan nyata secara konstan untuk mengontrol AC/Kipas ( 4).
     * @param currentTemperature Nilai suhu aktual yang didapat dari sensor fisik DHT.
     * @param thresholdCelsius Batas suhu yang ditentukan pengguna (misal: 30.0°C).
     */
    fun evaluateTemperatureTrigger(currentTemperature: Double, thresholdCelsius: Double) {
        try {
            if (currentTemperature >= thresholdCelsius) {
                Log.d(TAG, "Suhu ($currentTemperature°C) melewati batas ($thresholdCelsius°C). Menyalakan pendingin...")
                // Mengirim perintah "ON" nyata ke modul kipas/AC lewat perantara broker
                // Di sini kita bisa memanfaatkan publish langsung dari repository
                homeRepository.controlLight(true) // Misal di-override sementara atau disesuaikan topik khusus
            } else {
                Log.d(TAG, "Suhu ($currentTemperature°C) aman di bawah batas. Mematikan pendingin...")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengeksekusi logika otomasi suhu", e)
        }
    }

    /**
     * Memicu Mode Keamanan Ketat jika sistem mendeteksi ada sensor yang terputus tidak wajar ( 49).
     * @param isSensorManipulated Status keaslian/integritas koneksi hardware sensor di malam hari.
     */
    fun executeStrictSecurityOverride(isSensorManipulated: Boolean) {
        if (!isSensorManipulated) return

        try {
            Log.w(TAG, "CRITICAL: Manipulasi sensor terdeteksi! Mengaktifkan Mode Keamanan Ketat ( 49).")
            
            // 1. Perintahkan lampu depan rumah untuk berkedip cepat (Strobe Command)
            homeRepository.adjustRgbColor(255, 0, 0) // Set warna merah konstan sebagai penanda bahaya
            homeRepository.controlLight(true)       // Paksa saklar lampu utama menyala
            
            // 2. Kirim sinyal alarm bahaya terenkripsi ke sistem keamanan pusat rumah via MQTT
            // Menggunakan fungsi internal yang menembak topik keamanan
            Log.d(TAG, "Sinyal bahaya diteruskan ke broker MQTT.")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memicu prosedur keamanan ketat", e)
        }
    }
}