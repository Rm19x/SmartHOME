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
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuthUseCase(private val context: Context) {

    /**
     * Memeriksa apakah perangkat keras HP user mendukung pemindaian biometrik aktif saat ini ( 50).
     */
    fun isBiometricHardwareAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                             BiometricManager.Authenticators.DEVICE_CREDENTIAL
        
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Membuka gerbang sensor sidik jari atau wajah fisik bawaan sistem operasi Android secara nyata ( 50).
     * @param activity FragmentActivity host tempat memicu dialog prompt otentikasi.
     * @param onSuccess Callback yang dipicu jika sidik jari/wajah sukses divalidasi.
     * @param onError Callback jika validasi gagal, ditolak, atau dibatalkan oleh pengguna.
     */
    fun promptBiometricAuthentication(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Otentikasi ditolak oleh sensor karena sidik jari tidak dikenal
            }
        })

        // Menyusun konfigurasi teks dialog prompt biometrik sistem
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Otentikasi Kredensial Smart Home")
            .setSubtitle("Konfirmasi sidik jari atau wajah Anda untuk melanjutkan kontrol sistem")
            .setDescription("Akses diamankan dengan enkripsi perangkat Mr.Rm19.")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        // Luncurkan sensor fisik secara nyata di layar HP
        biometricPrompt.authenticate(promptInfo)
    }
}
