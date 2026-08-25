package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.model.PastelTheme

val LocalCompactMode = compositionLocalOf { false }
val LocalSelectedPastelTheme = compositionLocalOf { PastelTheme.PASTEL_SKY }

fun createPastelColorScheme(pastelTheme: PastelTheme, isDark: Boolean): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = pastelTheme.accentColor,
            onPrimary = Color(0xFF0F172A),
            primaryContainer = pastelTheme.primaryColor.copy(alpha = 0.35f),
            onPrimaryContainer = pastelTheme.containerColor,
            secondary = pastelTheme.primaryColor,
            onSecondary = Color.White,
            secondaryContainer = pastelTheme.accentColor.copy(alpha = 0.20f),
            onSecondaryContainer = pastelTheme.accentColor,
            tertiary = pastelTheme.accentColor,
            onTertiary = Color(0xFF0F172A),
            background = pastelTheme.darkBgColor,
            onBackground = DarkTextPrimary,
            surface = pastelTheme.darkSurfaceColor,
            onSurface = DarkTextPrimary,
            surfaceVariant = pastelTheme.darkSurfaceVariantColor,
            onSurfaceVariant = DarkTextSecondary,
            outline = pastelTheme.accentColor.copy(alpha = 0.25f),
            outlineVariant = Color(0xFF334155).copy(alpha = 0.5f)
        )
    } else {
        lightColorScheme(
            primary = pastelTheme.primaryColor,
            onPrimary = Color.White,
            primaryContainer = pastelTheme.containerColor,
            onPrimaryContainer = pastelTheme.primaryColor,
            secondary = pastelTheme.accentColor,
            onSecondary = Color.White,
            secondaryContainer = pastelTheme.containerColor.copy(alpha = 0.6f),
            onSecondaryContainer = pastelTheme.primaryColor,
            tertiary = pastelTheme.accentColor,
            onTertiary = Color.White,
            background = pastelTheme.lightBgColor,
            onBackground = LightTextPrimary,
            surface = pastelTheme.lightSurfaceColor,
            onSurface = LightTextPrimary,
            surfaceVariant = pastelTheme.containerColor.copy(alpha = 0.45f),
            onSurfaceVariant = LightTextSecondary,
            outline = pastelTheme.primaryColor.copy(alpha = 0.20f),
            outlineVariant = pastelTheme.primaryColor.copy(alpha = 0.08f)
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pastelTheme: PastelTheme = PastelTheme.PASTEL_SKY,
    compactMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = createPastelColorScheme(pastelTheme, darkTheme)
    
    CompositionLocalProvider(
        LocalCompactMode provides compactMode,
        LocalSelectedPastelTheme provides pastelTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
