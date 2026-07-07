/*
 * Copyright (c) 2026 Mr.Rm19
 * GitHub: https://github.com/Rm19x
 * All rights reserved.
 *
 * This file is part of the Real-World Smart Home Automation System.
 * Built with Kotlin & Jetpack Compose for production environment.
 */

package com.mrrm19.smarthome.data.repository

import com.mrrm19.smarthome.data.local.PreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class UserRepositoryImpl(
    private val preferencesManager: PreferencesManager
) {

    // --- Manajemen Tema Tampilan ( 16) ---

    val isDarkModeEnabled: Flow<Boolean?> = preferencesManager.isDarkModeFlow

    suspend fun updateThemePreference(isDark: Boolean) {
        preferencesManager.setDarkMode(isDark)
    }


    // --- Manajemen Profil & Hak Akses Pengguna ( 24) ---

    val currentUserRole: Flow<String> = preferencesManager.userRoleFlow
    val allowedRooms: Flow<String> = preferencesManager.allowedRoomsFlow

    /**
     * Memperbarui profil keamanan pengguna saat ini ke dalam memori lokal persisten.
     */
    suspend fun updateUserProfile(role: String, rooms: List<String>) {
        preferencesManager.saveUserAccessProfile(role, rooms)
    }

    /**
     * Logika pemeriksaan izin akses fisik ruangan nyata sebelum mengeksekusi aksi IoT.
     */
    suspend fun validateRoomAccessPermission(roomName: String): Boolean {
        val currentRole = currentUserRole.first()
        val allowedRoomsStr = allowedRooms.first()
        return preferencesManager.hasAccessToRoom(currentRole, allowedRoomsStr, roomName)
    }

    /**
     * Membersihkan seluruh berkas data user saat proses pembersihan enkripsi aplikasi.
     */
    suspend fun resetUserPreferences() {
        preferencesManager.clearAllPreferences()
    }
}
