package com.aarilabs.autohmi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HmiColorScheme = darkColorScheme(
    primary = HmiMintAccent,
    secondary = HmiTealGlow,
    background = HmiBackground,
    surface = HmiPanelBackground,
    surfaceVariant = HmiCardBackground,
    onPrimary = HmiBackground,
    onBackground = HmiTextPrimary,
    onSurface = HmiTextPrimary,
    onSurfaceVariant = HmiTextSecondary
)

@Composable
fun AutoHMITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HmiColorScheme,
        typography = Typography,
        content = content
    )
}
