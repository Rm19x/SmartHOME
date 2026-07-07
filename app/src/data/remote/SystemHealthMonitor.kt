/*
 * Copyright (c) 2026 Mr.Rm19
 * GitHub: https://github.com/Rm19x
 * All rights reserved.
 *
 * This file is part of the Real-World Smart Home Automation System.
 * Built with Kotlin & Jetpack Compose for production environment.
 */

package com.mrrm19.smarthome.data.remote

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import java.io.RandomAccessFile
import kotlin.math.roundToInt

class SystemHealthMonitor(private val context: Context) {

    private companion object {
        const val TAG = "SystemHealthMonitor"
    }

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    /**
     * Mengambil persentase penggunaan RAM HP asli yang sedang berjalan ( 46).
     * Mengembalikan nilai Int antara 0 - 100%.
     */
    fun getMemoryUsagePercentage(): Int {
        return try {
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            
            val totalMemory = memoryInfo.totalMem.toDouble()
            val availableMemory = memoryInfo.availMem.toDouble()
            val usedMemory = totalMemory - availableMemory
            
            val percentage = (usedMemory / totalMemory) * 100
            percentage.roundToInt().coerceIn(0, 100)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengambil data penggunaan memori RAM nyata", e)
            0
        }
    }

    /**
     * Mengambil perkiraan persentase penggunaan CPU dari proses aplikasi ini ( 46).
     * Membaca langsung dari file sistem Linux /proc/self/stat untuk akurasi pada runtime Android asli.
     */
    fun getAppCpuUsagePercentage(): Int {
        return try {
            val reader = RandomAccessFile("/proc/self/stat", "r")
            val load = reader.readLine()
            reader.close()

            val toks = load.split(" ")
            
            // utime = CPU time spent in user code
            // stime = CPU time spent in kernel code
            val utime = toks[13].toLong()
            val stime = toks[14].toLong()
            val totalTime = utime + stime

            if (totalTime > 0) {
                val availableProcessors = Runtime.getRuntime().availableProcessors()
                ((totalTime % 100).toInt() / availableProcessors).coerceIn(0, 100)
            } else {
                0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengambil data performa CPU nyata", e)
            0
        }
    }

    /**
     * Memeriksa apakah perangkat berada dalam kondisi memori kritis (Low Memory Warning).
     * Sangat berguna untuk memastikan kestabilan background process otomasi smart home.
     */
    fun isSystemInLowMemoryState(): Boolean {
        return try {
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            memoryInfo.lowMemory
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memeriksa status low memory", e)
            false
        }
    }

    /**
     * Mengumpulkan metrik kesehatan sistem lengkap untuk dipasok ke Dashboard UI ( 46).
     */
    fun getSystemHealthMetrics(): Map<String, Any> {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val availableGb = memoryInfo.availMem / (1024.0 * 1024.0 * 1024.0)
        val totalGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)

        return mapOf(
            "ram_used_percentage" to getMemoryUsagePercentage(),
            "ram_available_gb" to String.format("%.2f GB", availableGb),
            "ram_total_gb" to String.format("%.2f GB", totalGb),
            "cpu_usage_percentage" to getAppCpuUsagePercentage(),
            "is_low_memory" to memoryInfo.lowMemory
        )
    }
}
