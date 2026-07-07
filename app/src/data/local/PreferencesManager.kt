/*
 * Copyright (c) 2026 Mr.Rm19
 * GitHub: https://github.com/Rm19x
 * All rights reserved.
 *
 * This file is part of the Real-World Smart Home Automation System.
 * Built with Kotlin & Jetpack Compose for production environment.
 */

package com.mrrm19.smarthome.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Ekstensi DataStore untuk Context (Singleton instance)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mrrm19_smarthome_prefs")

class PreferencesManager(private val context: Context) {

    private companion object {
        // Kunci DataStore Nyata untuk  16 (Tema) &  24 (Hak Akses/Profil)
        val KEY_DARK_MODE = booleanPreferencesKey("theme_dark_mode")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_ALLOWED_ROOMS = stringPreferencesKey("user_allowed_rooms")
    }

    /**
     * Aliran data (Flow) untuk memantau status Dark Mode secara real-time ( 16).
     * Secara default akan mengikuti konfigurasi sistem jika belum diatur manual.
     */
    val isDarkModeFlow: Flow<Boolean?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[KEY_DARK_MODE]
        }

    /**
     * Menyimpan preferensi tema Dark Mode pengguna secara permanen ( 16).
     */
    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DARK_MODE] = isDark
        }
    }

    /**
     * Aliran data (Flow) untuk memantau Role/Hak Akses pengguna saat ini ( 24).
     * Nilai bawaan: "GUEST" demi keamanan ketat sistem.
     */
    val userRoleFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[KEY_USER_ROLE] ?: "GUEST"
        }

    /**
     * Aliran data (Flow) untuk memantau daftar ruangan yang boleh diakses ( 24).
     * Mengembalikan string terpisah koma (Contoh: "Kamar Tidur,Dapur").
     */
    val allowedRoomsFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[KEY_ALLOWED_ROOMS] ?: ""
        }

    /**
     * Memperbarui profil dan tingkat hak akses pengguna di dalam sistem ( 24).
     * @param role Tingkatan hak akses (contoh: "ADMIN", "FAMILY", "GUEST").
     * @param allowedRooms Ruangan fisik yang diizinkan untuk dikontrol oleh user tersebut.
     */
    suspend fun saveUserAccessProfile(role: String, allowedRooms: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ROLE] = role
            preferences[KEY_ALLOWED_ROOMS] = allowedRooms.joinToString(",")
        }
    }

    /**
     * Memeriksa apakah pengguna saat ini memiliki hak akses nyata untuk mengontrol ruangan tertentu ( 24).
     * Jika role adalah "ADMIN", akses otomatis dibuka ke seluruh penjuru rumah.
     */
    fun hasAccessToRoom(currentRole: String, allowedRoomsStr: String, roomName: String): Boolean {
        if (currentRole == "ADMIN") return true
        val list = allowedRoomsStr.split(",").map { it.trim() }
        return list.contains(roomName)
    }

    /**
     * Menghapus seluruh data sesi atau preferensi saat pengguna logout/reset sistem.
     */
    suspend fun clearAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
