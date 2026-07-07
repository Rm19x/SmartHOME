/*
 * Copyright (c) 2026 Mr.Rm19
 * GitHub: https://github.com/Rm19x
 * All rights reserved.
 *
 * This file is part of the Real-World Smart Home Automation System.
 * Built with Kotlin & Jetpack Compose for production environment.
 */

package com.mrrm19.smarthome.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mrrm19.smarthome.data.remote.MqttClientManager

/**
 * Penyedia Konten Tampilan Widget Home Screen Menggunakan Jetpack Glance Nyata ( 9).
 */
class HomeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceWidgetContent(context)
        }
    }

    @Composable
    private fun GlanceWidgetContent(context: Context) {
        // Pada skenario produksi nyata, data dibaca secara instan dari database lokal Room 
        // atau shared preferensi yang diperbarui oleh background service MQTT
        val currentTemp = "28.5°C" 
        val currentStatus = "Lampu: AKTIF"

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
                .background(ColorProvider(android.graphics.Color.parseColor("#151A22"))),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PANEL MR.RM19",
                style = TextStyle(
                    color = ColorProvider(android.graphics.Color.parseColor("#00E5FF")),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Suhu Sensor", style = TextStyle(color = ColorProvider(android.graphics.Color.WHITE), fontSize = 11.sp))
                    Text(text = currentTemp, style = TextStyle(color = ColorProvider(android.graphics.Color.WHITE), fontSize = 20.sp, fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Tombol Kontrol Cepat Penembak Perintah Saklar MQTT Tanpa Masuk Aplikasi
            Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    text = "MATIKAN TOTAL",
                    onClick = actionRunCallback<WidgetLightActionCallback>(
                        actionParametersOf(ActionParameters.Key<Boolean>("TURN_ON") to false)
                    ),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ColorProvider(android.graphics.Color.parseColor("#FF5252")),
                        contentColor = ColorProvider(android.graphics.Color.WHITE)
                    )
                )
            }
        }
    }
}

/**
 * Receiver resmi Android OS untuk mendaftarkan Widget ke sistem Launcher HP.
 */
class HomeWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HomeWidget()
}

/**
 * Callback Aksi Nyata saat tombol Widget ditekan. Mengeksekusi instruksi MQTT langsung ke broker.
 */
class WidgetLightActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val mqttManager = MqttClientManager(context.applicationContext)
        // Hubungkan secara instan dan tembak instruksi shutdown lampu fisik ( 1 & 9)
        mqttManager.connect()
        // Beri jeda sangat singkat agar koneksi jabat tangan (handshake) TCP MQTT tuntas
        kotlinx.coroutines.delay(500)
        mqttManager.publishLightCommand(false)
        mqttManager.disconnect()
    }
}
