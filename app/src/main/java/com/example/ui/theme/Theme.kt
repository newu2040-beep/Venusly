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
            secondary = pastelTheme.accentColor,
            onSecondary = Color(0xFF0F172A),
            secondaryContainer = pastelTheme.primaryColor.copy(alpha = 0.25f),
            onSecondaryContainer = pastelTheme.containerColor,
            tertiary = pastelTheme.accentColor,
            onTertiary = Color(0xFF0F172A),
            background = pastelTheme.darkBgColor,
            onBackground = DarkTextPrimary,
            surface = DarkSurface,
            onSurface = DarkTextPrimary,
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = DarkTextSecondary,
            outline = DarkBorder,
            outlineVariant = Color(0xFF1E293B)
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
            surface = LightSurface,
            onSurface = LightTextPrimary,
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = LightTextSecondary,
            outline = LightBorder,
            outlineVariant = Color(0xFFE2E8F0)
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
