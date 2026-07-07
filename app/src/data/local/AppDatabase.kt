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
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. https://github.com/Rm19x
// ==========================================

/**
 * Entitas Tabel untuk menyimpan data riwayat suhu & kelembaban ( 44).
 */
@Entity(tableName = "temperature_history")
data class TemperatureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long, // Waktu saat data dicatat (Epoch millis)
    val roomName: String, // Nama ruangan (misal: "Kamar Utama")
    val temperature: Double, // Nilai suhu asli dari sensor
    val humidity: Double // Nilai kelembaban asli dari sensor
)

/**
 * Entitas Tabel untuk menyimpan data penjadwalan/timer lampu ( 5).
 */
@Entity(tableName = "light_schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val targetTime: String, // Format waktu "HH:mm" (contoh: "18:00")
    val actionCommand: String, // Perintah nyata ("ON" atau "OFF")
    val targetTopic: String, // Topik MQTT target saklar
    val isEnabled: Boolean = true // Status jadwal aktif/tidak
)

// ==========================================
// 2. DATA ACCESS OBJECT (DAO) INTERFACE
// ==========================================

@Dao
interface HomeAutomationDao {

    // --- Kebutuhan  44 (Riwayat & Ekspor CSV) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemperatureRecord(record: TemperatureEntity)

    @Query("SELECT * FROM temperature_history ORDER BY timestamp DESC")
    fun getAllTemperatureHistory(): Flow<List<TemperatureEntity>>

    @Query("SELECT * FROM temperature_history WHERE timestamp >= :startTime ORDER BY timestamp ASC")
    suspend fun getTemperatureHistoryRange(startTime: Long): List<TemperatureEntity>

    @Query("DELETE FROM temperature_history WHERE timestamp < :limitTime")
    suspend fun clearOldHistory(limitTime: Long)


    // --- Kebutuhan  5 (Penjadwalan & Timer) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSchedule(schedule: ScheduleEntity)

    @Query("SELECT * FROM light_schedules ORDER BY targetTime ASC")
    fun getAllActiveSchedules(): Flow<List<ScheduleEntity>>

    @Query("DELETE FROM light_schedules WHERE id = :scheduleId")
    suspend fun deleteScheduleById(scheduleId: Int)

    @Query("UPDATE light_schedules SET isEnabled = :status WHERE id = :scheduleId")
    suspend fun toggleScheduleStatus(scheduleId: Int, status: Boolean)
}

// ==========================================
// 3. ABSOLUTE ROOM DATABASE CLASS
// ==========================================

@Database(
    entities = [TemperatureEntity::class, ScheduleEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun homeAutomationDao(): HomeAutomationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Mengembalikan instance Singleton nyata dari database Room.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mrrm19_smarthome_db"
                )
                // Strategi migrasi fallback untuk mencegah crash produksi jika terjadi perubahan skema data
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
