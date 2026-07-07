/*
 * Copyright (c) 2026 Mr.Rm19
 * GitHub: https://github.com/Rm19x
 * All rights reserved.
 *
 * This file is part of the Real-World Smart Home Automation System.
 * Built with Kotlin & Jetpack Compose for production environment.
 */

package com.mrrm19.smarthome.data.repository

import com.mrrm19.smarthome.data.local.HomeAutomationDao
import com.mrrm19.smarthome.data.local.ScheduleEntity
import com.mrrm19.smarthome.data.local.TemperatureEntity
import com.mrrm19.smarthome.data.remote.MqttClientManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

class HomeRepositoryImpl(
    private val mqttManager: MqttClientManager,
    private val homeDao: HomeAutomationDao
) {

    // --- Manajemen Real-time MQTT ( 1, 2, 3, 10, 11, 13, 32) ---
    
    val mqttMessageEvents: SharedFlow<Pair<String, String>> = mqttManager.messageEvents
    val mqttConnectionStatus: SharedFlow<Boolean> = mqttManager.connectionStatus

    fun connectToBroker() = mqttManager.connect()
    
    fun disconnectFromBroker() = mqttManager.disconnect()

    fun controlLight(turnOn: Boolean) = mqttManager.publishLightCommand(turnOn)

    fun adjustDimmer(brightness: Int) = mqttManager.publishDimmerValue(brightness)

    fun adjustRgbColor(r: Int, g: Int, b: Int) = mqttManager.publishRgbColor(r, g, b)


    // --- Manajemen Database Lokal Sensor ( 44) ---

    suspend fun saveTemperatureRecord(roomName: String, temp: Double, humid: Double) {
        val entity = TemperatureEntity(
            timestamp = System.currentTimeMillis(),
            roomName = roomName,
            temperature = temp,
            humidity = humid
        )
        homeDao.insertTemperatureRecord(entity)
    }

    fun getAllTemperatureLogs(): Flow<List<TemperatureEntity>> = homeDao.getAllTemperatureHistory()

    suspend fun getTemperatureLogsForExport(startTime: Long): List<TemperatureEntity> {
        return homeDao.getTemperatureHistoryRange(startTime)
    }


    // --- Manajemen Database Penjadwalan & Timer ( 5) ---

    fun getActiveSchedules(): Flow<List<ScheduleEntity>> = homeDao.getAllActiveSchedules()

    suspend fun addNewSchedule(time: String, command: String, topic: String) {
        val schedule = ScheduleEntity(
            targetTime = time,
            actionCommand = command,
            targetTopic = topic
        )
        homeDao.insertOrUpdateSchedule(schedule)
    }

    suspend fun removeSchedule(scheduleId: Int) = homeDao.deleteScheduleById(scheduleId)

    suspend fun toggleSchedule(scheduleId: Int, isEnabled: Boolean) {
        homeDao.toggleScheduleStatus(scheduleId, isEnabled)
    }
}
