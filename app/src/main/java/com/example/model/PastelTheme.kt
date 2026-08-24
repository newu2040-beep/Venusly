package com.example.model

import androidx.compose.ui.graphics.Color

enum class PastelTheme(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val primaryColor: Color,
    val accentColor: Color,
    val containerColor: Color,
    val lightBgColor: Color,
    val darkBgColor: Color,
    val previewGradient: List<Color>
) {
    PASTEL_SKY(
        id = "sky",
        displayName = "Pastel Sky",
        subtitle = "Apple Minimalist Blue",
        primaryColor = Color(0xFF2563EB),
        accentColor = Color(0xFF60A5FA),
        containerColor = Color(0xFFE0EDFD),
        lightBgColor = Color(0xFFF5F8FC),
        darkBgColor = Color(0xFF0B0F19),
        previewGradient = listOf(Color(0xFF2563EB), Color(0xFF60A5FA), Color(0xFFE0EDFD))
    ),
    SAKURA_ROSE(
        id = "sakura",
        displayName = "Sakura Rose",
        subtitle = "Blush Cherry Bloom",
        primaryColor = Color(0xFFE11D48),
        accentColor = Color(0xFFFB7185),
        containerColor = Color(0xFFFCE7F3),
        lightBgColor = Color(0xFFFFF1F2),
        darkBgColor = Color(0xFF190B10),
        previewGradient = listOf(Color(0xFFE11D48), Color(0xFFFB7185), Color(0xFFFCE7F3))
    ),
    LAVENDER_MIST(
        id = "lavender",
        displayName = "Lavender Mist",
        subtitle = "Dreamy Violet Twilight",
        primaryColor = Color(0xFF7C3AED),
        accentColor = Color(0xFFA78BFA),
        containerColor = Color(0xFFEDE9FE),
        lightBgColor = Color(0xFFF8F5FF),
        darkBgColor = Color(0xFF130B1C),
        previewGradient = listOf(Color(0xFF7C3AED), Color(0xFFA78BFA), Color(0xFFEDE9FE))
    ),
    MATCHA_CREAM(
        id = "matcha",
        displayName = "Matcha Cream",
        subtitle = "Fresh Sage & Mint",
        primaryColor = Color(0xFF059669),
        accentColor = Color(0xFF34D399),
        containerColor = Color(0xFFD1FAE5),
        lightBgColor = Color(0xFFF0FDF4),
        darkBgColor = Color(0xFF071710),
        previewGradient = listOf(Color(0xFF059669), Color(0xFF34D399), Color(0xFFD1FAE5))
    ),
    BUTTERCUP_PEACH(
        id = "buttercup",
        displayName = "Buttercup Peach",
        subtitle = "Warm Honey Glow",
        primaryColor = Color(0xFFD97706),
        accentColor = Color(0xFFFBBF24),
        containerColor = Color(0xFFFEF3C7),
        lightBgColor = Color(0xFFFFFBEB),
        darkBgColor = Color(0xFF1C1307),
        previewGradient = listOf(Color(0xFFD97706), Color(0xFFFBBF24), Color(0xFFFEF3C7))
    ),
    LILAC_CLOUD(
        id = "lilac",
        displayName = "Lilac Cloud",
        subtitle = "Ethereal Amethyst",
        primaryColor = Color(0xFF9333EA),
        accentColor = Color(0xFFC084FC),
        containerColor = Color(0xFFF3E8FF),
        lightBgColor = Color(0xFFFAF5FF),
        darkBgColor = Color(0xFF160B1F),
        previewGradient = listOf(Color(0xFF9333EA), Color(0xFFC084FC), Color(0xFFF3E8FF))
    ),
    SUNSET_CORAL(
        id = "sunset",
        displayName = "Sunset Coral",
        subtitle = "Radiant Golden Amber",
        primaryColor = Color(0xFFEA580C),
        accentColor = Color(0xFFFB923C),
        containerColor = Color(0xFFFFEDD5),
        lightBgColor = Color(0xFFFFF7ED),
        darkBgColor = Color(0xFF1C0F07),
        previewGradient = listOf(Color(0xFFEA580C), Color(0xFFFB923C), Color(0xFFFFEDD5))
    ),
    ARCTIC_GLACIER(
        id = "arctic",
        displayName = "Arctic Glacier",
        subtitle = "Crisp Icy Aquamarine",
        primaryColor = Color(0xFF0284C7),
        accentColor = Color(0xFF38BDF8),
        containerColor = Color(0xFFCFFAFE),
        lightBgColor = Color(0xFFF0F9FF),
        darkBgColor = Color(0xFF07131C),
        previewGradient = listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFCFFAFE))
    ),
    ROSE_QUARTZ(
        id = "rose_quartz",
        displayName = "Rose Quartz",
        subtitle = "Romantic Dusty Mauve",
        primaryColor = Color(0xFFDB2777),
        accentColor = Color(0xFFF472B6),
        containerColor = Color(0xFFFDF2F8),
        lightBgColor = Color(0xFFFFF5F9),
        darkBgColor = Color(0xFF1C0913),
        previewGradient = listOf(Color(0xFFDB2777), Color(0xFFF472B6), Color(0xFFFDF2F8))
    ),
    ESPRESSO_CREAM(
        id = "espresso",
        displayName = "Espresso Cream",
        subtitle = "Vintage Warm Almond",
        primaryColor = Color(0xFF92400E),
        accentColor = Color(0xFFB45309),
        containerColor = Color(0xFFFEF2F2),
        lightBgColor = Color(0xFFFAF6F0),
        darkBgColor = Color(0xFF19120C),
        previewGradient = listOf(Color(0xFF92400E), Color(0xFFB45309), Color(0xFFFEF2F2))
    )
}
