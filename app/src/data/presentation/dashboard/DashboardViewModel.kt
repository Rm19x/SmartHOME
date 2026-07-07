/*
 * Copyright (c) 2026 Mr.Rm19
 * GitHub: https://github.com/Rm19x
 * All rights reserved.
 *
 * This file is part of the Real-World Smart Home Automation System.
 * Built with Kotlin & Jetpack Compose for production environment.
 */

package com.mrrm19.smarthome.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mrrm19.smarthome.data.repository.HomeRepositoryImpl
import com.mrrm19.smarthome.data.remote.SystemHealthMonitor
import com.mrrm19.smarthome.domain.model.DeviceStatus
import com.mrrm19.smarthome.domain.model.RgbColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    application: Application,
    private val homeRepository: HomeRepositoryImpl
) : AndroidViewModel(application) {

    private val healthMonitor = SystemHealthMonitor(application.applicationContext)

    // State Utama untuk menampung status perangkat dan sensor secara real-time
    private val _uiState = MutableStateFlow(DeviceStatus())
    val uiState: StateFlow<DeviceStatus> = _uiState.asStateFlow()

    // State untuk memantau kesehatan hardware HP ( 46)
    private val _systemHealth = MutableStateFlow<Map<String, Any>>(emptyMap())
    val systemHealth: StateFlow<Map<String, Any>> = _systemHealth.asStateFlow()

    // State Koneksi Broker MQTT
    private val _isMqttConnected = MutableStateFlow(false)
    val isMqttConnected: StateFlow<Boolean> = _isMqttConnected.asStateFlow()

    init {
        // Hubungkan ke broker MQTT nyata saat dashboard aktif
        homeRepository.connectToBroker()
        observeMqttSignals()
        startSystemHealthTracking()
    }

    /**
     * Berlangganan ke sinyal data masuk asli dari Broker MQTT ( 2 & 3)
     */
    private fun observeMqttSignals() {
        viewModelScope.launch {
            homeRepository.mqttConnectionStatus.collect { connected ->
                _isMqttConnected.value = connected
            }
        }

        viewModelScope.launch {
            homeRepository.mqttMessageEvents.collect { (topic, payload) ->
                when (topic) {
                    "smarthome/mrrm19/light/status" -> {
                        _uiState.update { it.copy(isLightOn = payload == "ON") }
                    }
                    "smarthome/mrrm19/light/dimmer" -> {
                        _uiState.update { it.copy(brightnessDimmer = payload.toIntOrNull() ?: 100) }
                    }
                    "smarthome/mrrm19/sensor/temperature" -> {
                        _uiState.update { it.copy(temperature = payload.toDoubleOrNull() ?: 0.0) }
                    }
                    "smarthome/mrrm19/sensor/humidity" -> {
                        _uiState.update { it.copy(humidity = payload.toDoubleOrNull() ?: 0.0) }
                    }
                }
            }
        }
    }

    /**
     * Melacak performa RAM & CPU HP asli secara berkala ( 46)
     */
    private fun startSystemHealthTracking() {
        viewModelScope.launch {
            while (true) {
                _systemHealth.value = healthMonitor.getSystemHealthMetrics()
                delay(3000) // Sinkronisasi setiap 3 detik sekali
            }
        }
    }

    //  1: Mengirim Perintah Saklar Utama
    fun toggleLight(isOn: Boolean) {
        homeRepository.controlLight(isOn)
        _uiState.update { it.copy(isLightOn = isOn) }
    }

    //  10: Mengatur Tingkat Dimmer Kecerahan
    fun setDimmerValue(value: Int) {
        homeRepository.adjustDimmer(value)
        _uiState.update { it.copy(brightnessDimmer = value) }
    }

    //  11: Mengatur Warna RGB Fisik Lampu
    fun setRgbColor(r: Int, g: Int, b: Int) {
        homeRepository.adjustRgbColor(r, g, b)
        _uiState.update { it.copy(rgbColor = RgbColor(r, g, b)) }
    }

    //  45: Mode Nonton Film (Cinema Mode) Nyata
    fun activateCinemaMode() {
        viewModelScope.launch {
            // Meredupkan lampu perlahan (Simulasi efek transisi  43 pada perintah hardware)
            homeRepository.adjustRgbColor(20, 10, 40) // Warna biru/ungu redup khas bioskop
            homeRepository.adjustDimmer(15)           // Set tingkat kecerahan ke 15%
            homeRepository.controlLight(true)
            
            _uiState.update { 
                it.copy(isLightOn = true, brightnessDimmer = 15, rgbColor = RgbColor(20, 10, 40)) 
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        homeRepository.disconnectFromBroker()
    }
}
