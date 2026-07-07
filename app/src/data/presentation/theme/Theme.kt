/*
 * Copyright (c) 2026 Mr.Rm19
 * GitHub: https://github.com/Rm19x
 * All rights reserved.
 *
 * This file is part of the Real-World Smart Home Automation System.
 * Built with Kotlin & Jetpack Compose for production environment.
 */

package com.mrrm19.smarthome.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryCyber,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    error = AccentError,
    onBackground = LightBackground,
    onSurface = LightBackground
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDarkVibrant,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    error = AccentError,
    onBackground = DarkBackground,
    onSurface = DarkBackground
)

/**
 * Wrapper Utama Komponen Desain Material 3 Smart Home ( 16).
 * @param isCustomDarkMode Nilai preferensi manual dari DataStore database. Jika null, akan mengikuti default sistem Android.
 */
@Composable
fun MrRm19SmartHomeTheme(
    isCustomDarkMode: Boolean?,
    content: @Composable () -> Unit
) {
    // Tentukan skema warna berdasarkan preferensi tersimpan atau bawaan OS HP nyata
    val useDarkMode = isCustomDarkMode ?: isSystemInDarkTheme()
    val colorScheme = if (useDarkMode) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
