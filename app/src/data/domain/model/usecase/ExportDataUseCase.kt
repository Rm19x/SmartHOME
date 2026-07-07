/*
 * Copyright (c) 2026 Mr.Rm19
 * GitHub: https://github.com/Rm19x
 * All rights reserved.
 *
 * This file is part of the Real-World Smart Home Automation System.
 * Built with Kotlin & Jetpack Compose for production environment.
 */

package com.mrrm19.smarthome.domain.usecase

import android.content.Context
import android.os.Environment
import android.util.Log
import com.mrrm19.smarthome.data.repository.HomeRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportDataUseCase(private val homeRepository: HomeRepositoryImpl) {

    private companion object {
        const val TAG = "ExportDataUseCase"
    }

    /**
     * Membaca riwayat suhu dari Room DB lalu menyusunnya ke dalam file CSV fisik di storage HP ( 44).
     * @param context Context aplikasi untuk mengakses direktori penyimpanan publik.
     * @param startTime Batasan waktu awal pencarian log (Epoch millis).
     * @return File objek dokumen CSV jika berhasil, atau null jika gagal.
     */
    suspend fun exportTemperatureLogsToCsv(context: Context, startTime: Long): File? = withContext(Dispatchers.IO) {
        try {
            // 1. Ambil data mentah asli dari repositori database Room
            val records = homeRepository.getTemperatureLogsForExport(startTime)
            if (records.isEmpty()) {
                Log.w(TAG, "Ekspor dibatalkan karena tidak ada catatan data suhu di database.")
                return@withContext null
            }

            // 2. Siapkan file tujuan di folder Downloads eksternal perangkat yang sah
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "SmartHome_Suhu_MrRm19_$timestamp.csv"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val csvFile = File(downloadsDir, fileName)

            // 3. Mulai menulis baris data terstruktur (Comma-Separated Values)
            val writer = FileWriter(csvFile)
            
            // Menulis header dokumen CSV
            writer.append("ID,Waktu_Lokal,Nama_Ruangan,Suhu_Celsius,Kelembaban_Persen\n")
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            // Menulis seluruh baris rekaman data dari database
            for (record in records) {
                val formattedDate = dateFormat.format(Date(record.timestamp))
                writer.append("${record.id},")
                writer.append("$formattedDate,")
                writer.append("${record.roomName},")
                writer.append("${record.temperature},")
                writer.append("${record.humidity}\n")
            }

            writer.flush()
            writer.close()

            Log.d(TAG, "Dokumen CSV berhasil dibuat secara nyata di: ${csvFile.absolutePath}")
            return@withContext csvFile

        } catch (e: Exception) {
            Log.e(TAG, "Terjadi kesalahan fatal saat mengekspor data ke file CSV", e)
            return@withContext null
        }
    }
}
