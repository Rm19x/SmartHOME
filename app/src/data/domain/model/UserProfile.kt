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
 * Definisikan level hak akses kontrol IoT untuk membatasi eksekusi  secara nyata.
 */
enum class UserRole {
    ADMIN,  // Akses penuh tanpa batasan ke seluruh menu, otomasi, dan ruangan
    FAMILY, // Akses penuh ke ruangan tertentu yang diizinkan, tidak bisa ubah enkripsi/sistem
    GUEST   // Akses sangat terbatas, hanya bisa memantau tanpa mengeksekusi kontrol berbahaya
}

/**
 * Model data profil otentikasi dan hak akses keamanan pengguna ( 24).
 */
data class UserProfile(
    val userId: String,
    val username: String,
    val role: UserRole = UserRole.GUEST,
    val allowedRooms: List<String> = emptyList() // Daftar ruangan fisik yang diizinkan untuk dikontrol
) {
    /**
     * Memeriksa secara instan apakah pengguna memiliki wewenang administratif penuh atas sistem.
     */
    fun isAdmin(): Boolean = role == UserRole.ADMIN

    /**
     * Memeriksa validitas izin akses fisik sebelum aplikasi Android menembak perintah ke Broker MQTT.
     */
    fun isRoomControlAllowed(roomName: String): Boolean {
        if (isAdmin()) return true
        return allowedRooms.contains(roomName)
    }
}
