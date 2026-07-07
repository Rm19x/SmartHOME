/*
 * Copyright (c) 2026 Mr.Rm19
 * GitHub: https://github.com/Rm19x
 * All rights reserved.
 *
 * 
 * Built with Kotlin & Jetpack Compose for production environment.
 */

package com.mrrm19.smarthome.data.remote

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.UUID

class MqttClientManager(private val context: Context) {

    private companion object {
        const val TAG = "MqttClientManager"
        // Konfigurasi Server MQTT Nyata (Ganti dengan Broker milikmu, misal HiveMQ / EMQX / Mosquitto lokal)
        const val SERVER_URI = "tcp://broker.hivemq.com:1883" 
        
        // Daftar Topik MQTT Nyata ( 1, 2, 3, 10, 11, 13, 32)
        const val TOPIC_LIGHT_STATUS = "smarthome/mrrm19/light/status"
        const val TOPIC_LIGHT_COMMAND = "smarthome/mrrm19/light/command"
        const val TOPIC_LIGHT_DIMMER  = "smarthome/mrrm19/light/dimmer"
        const val TOPIC_LIGHT_RGB     = "smarthome/mrrm19/light/rgb"
        const val TOPIC_SENSOR_TEMP   = "smarthome/mrrm19/sensor/temperature"
        const val TOPIC_SENSOR_HUMID  = "smarthome/mrrm19/sensor/humidity"
    }

    private var mqttAndroidClient: MqttAndroidClient? = null
    private val clientId = "MrRm19_Android_" + UUID.randomUUID().toString().take(8)
    private val scope = CoroutineScope(Dispatchers.IO)

    // Aliran data real-time (Flow) untuk ditangkap oleh ViewModel/UI
    private val _messageEvents = MutableSharedFlow<Pair<String, String>>(replay = 1)
    val messageEvents: SharedFlow<Pair<String, String>> = _messageEvents.asSharedFlow()

    private val _connectionStatus = MutableSharedFlow<Boolean>(replay = 1)
    val connectionStatus: SharedFlow<Boolean> = _connectionStatus.asSharedFlow()

    init {
        setupMqttClient()
    }

    private fun setupMqttClient() {
        mqttAndroidClient = MqttAndroidClient(context, SERVER_URI, clientId)
        mqttAndroidClient?.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                Log.d(TAG, "Koneksi MQTT Berhasil ke: $serverURI. Reconnect: $reconnect")
                scope.launch { _connectionStatus.emit(true) }
                // Otomatis subscribe ulang topik nyata saat koneksi terhubung/reconnect
                subscribeToTopics()
            }

            override fun connectionLost(cause: Throwable?) {
                Log.e(TAG, "Koneksi MQTT Terputus! ( 13 - Deteksi Lost Connection)", cause)
                scope.launch { _connectionStatus.emit(false) }
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                val payload = message?.payload?.let { String(it) } ?: ""
                Log.d(TAG, "Data Masuk -> Topik: $topic, Payload: $payload")
                topic?.let {
                    scope.launch {
                        _messageEvents.emit(Pair(it, payload))
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // Dipanggil saat data sukses terpublish ke hardware
            }
        })
    }

    fun connect() {
        val options = MqttConnectOptions().apply {
            isAutomaticReconnect = true
            isCleanSession = false
            // Menambahkan aturan Last Will and Testament (LWT) jika koneksi aplikasi drop tiba-tiba
            setWill("smarthome/mrrm19/app/status", "offline".toByteArray(), 1, false)
        }

        try {
            mqttAndroidClient?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Proses inisiasi koneksi ke Broker MQTT sukses.")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "Gagal terhubung ke Broker MQTT server uri: $SERVER_URI", exception)
                    scope.launch { _connectionStatus.emit(false) }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Terjadi error fatal saat mengeksekusi koneksi MQTT", e)
        }
    }

    private fun subscribeToTopics() {
        val topics = arrayOf(
            TOPIC_LIGHT_STATUS,
            TOPIC_LIGHT_DIMMER,
            TOPIC_LIGHT_RGB,
            TOPIC_SENSOR_TEMP,
            TOPIC_SENSOR_HUMID
        )
        val qos = intArrayOf(1, 1, 1, 1, 1) // QoS 1 menjamin data nyata sampai minimal 1 kali

        try {
            mqttAndroidClient?.subscribe(topics, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Berhasil subscribe ke seluruh topik IoT sensor & saklar.")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "Gagal men-subscribe topik IoT.", exception)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error eksekusi subscription", e)
        }
    }

    //  1 & 32: Mengirim Perintah Saklar Utama (ON/OFF) ke Lampu Hardware
    fun publishLightCommand(turnOn: Boolean) {
        val payload = if (turnOn) "ON" else "OFF"
        publish(TOPIC_LIGHT_COMMAND, payload)
    }

    //  10: Mengirim Tingkat Kecerahan Dimmer (0 - 100)
    fun publishDimmerValue(brightness: Int) {
        val clampedValue = brightness.coerceIn(0, 100)
        publish(TOPIC_LIGHT_DIMMER, clampedValue.toString())
    }

    //  11: Mengirim Kode Warna RGB Nyata (Contoh format payload: "255,255,255")
    fun publishRgbColor(red: Int, green: Int, blue: Int) {
        val payload = "$red,$green,$blue"
        publish(TOPIC_LIGHT_RGB, payload)
    }

    private fun publish(topic: String, payload: String, qos: Int = 1, retained: Boolean = false) {
        if (mqttAndroidClient?.isConnected == false) {
            Log.w(TAG, "Gagal publish, client tidak terhubung ke broker MQTT.")
            return
        }
        try {
            val message = MqttMessage(payload.toByteArray()).apply {
                this.qos = qos
                isRetained = retained
            }
            mqttAndroidClient?.publish(topic, message)
            Log.d(TAG, "Sukses Publish -> Topik: $topic, Payload: $payload")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengirim data ke topik $topic", e)
        }
    }

    fun disconnect() {
        try {
            if (mqttAndroidClient?.isConnected == true) {
                mqttAndroidClient?.disconnect()
                Log.d(TAG, "Koneksi MQTT ditutup secara manual oleh aplikasi.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saat menutup koneksi MQTT", e)
        }
    }
}
