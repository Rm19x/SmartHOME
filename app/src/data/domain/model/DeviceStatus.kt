/*
 * Copyright (c) 2026 Mr.Rm19
 * GitHub: https://github.com/Rm19x
 * All rights reserved.
 *
 * This file is part of the Real-World Smart Home Automation System.
 * Built with Kotlin & Jetpack Compose for production environment.
 */

package com.mrrm19.smarthome.domain.model

/**
 * Model data nyata untuk merepresentasikan status penuh dari satu ruangan pintar.
 * Menggabungkan kontrol lampu (saklar, dimmer, warna) dan pemantauan sensor lingkungan.
 */
data class DeviceStatus(
    val isLightOn: Boolean = false,         //  1: Status Saklar Utama (ON/OFF)
    val brightnessDimmer: Int = 100,        //  10: Nilai Kecerahan Lampu (0 - 100)
    val rgbColor: RgbColor = RgbColor(),    //  11: Struktur Warna Lampu RGB
    val temperature: Double = 0.0,          //  2: Angka Suhu Nyata dari Sensor DHT
    val humidity: Double = 0.0              //  3: Angka Kelembaban Nyata dari Sensor DHT
)

/**
 * Representasi data warna berbasis komponen Red, Green, Blue untuk lampu pintar.
 */
data class RgbColor(
    val red: Int = 255,
    val green: Int = 255,
    val blue: Int = 255
) {
    /**
     * Mengonversi data RGB menjadi format string koma standar untuk dikirim ke payload MQTT fisik.
     * Contoh output: "255,120,0"
     */
    fun toMqttPayload(): String = "$red,$green,$blue"
}
