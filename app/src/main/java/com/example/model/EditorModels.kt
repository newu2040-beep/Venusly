package com.example.model

import android.graphics.Bitmap
import android.net.Uri

data class AdjustmentValues(
    val exposure: Float = 0f,        // -1.0f .. 1.0f
    val brightness: Float = 0f,      // -100f .. 100f
    val contrast: Float = 0f,        // -100f .. 100f
    val highlights: Float = 0f,      // -100f .. 100f
    val shadows: Float = 0f,         // -100f .. 100f
    val saturation: Float = 0f,      // -100f .. 100f
    val temperature: Float = 0f,     // -100f .. 100f (Warm / Cool)
    val tint: Float = 0f,            // -100f .. 100f (Magenta / Green)
    val vibrance: Float = 0f,        // -100f .. 100f
    val sharpen: Float = 0f,         // 0f .. 100f
    val grain: Float = 0f,           // 0f .. 100f
    val vignette: Float = 0f,        // 0f .. 100f
    val glow: Float = 0f,            // 0f .. 100f
    val blur: Float = 0f,            // 0f .. 100f
    val lightLeak: Float = 0f,       // 0f .. 100f
    val dustEffect: Float = 0f,      // 0f .. 100f
    val hueShift: Float = 0f,        // -180f .. 180f
    val frame: AestheticFrame = AestheticFrame.NONE,
    val rotationDegrees: Float = 0f, // 0, 90, 180, 270 + fine tune
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false
)

enum class AestheticFrame(val id: String, val displayName: String, val subtitle: String) {
    NONE("none", "None", "Clean edge"),
    POLAROID_WHITE("polaroid_white", "Polaroid Classic", "Instant white border"),
    POLAROID_DARK("polaroid_dark", "Polaroid Noir", "Minimalist matte dark"),
    FILM_35MM("film_35mm", "35mm Filmstrip", "Analog frame with perforations"),
    PASTEL_AURA("pastel_aura", "Pastel Aura", "Soft gradient glow frame"),
    CLEAN_MAT("clean_mat", "Gallery Mat", "Museum white passe-partout"),
    MINIMAL_KEYLINE("minimal_keyline", "Minimal Keyline", "Crisp double hairline"),
    VINTAGE_STAMP("vintage_stamp", "Postage Stamp", "Perforated aesthetic edges"),
    PASTEL_CARD("pastel_card", "Rounded Card", "Soft rounded pastel border"),
    RETRO_TV("retro_tv", "Retro CRT", "Vintage rounded monitor bezel")
}

enum class FilterCategory(val displayName: String) {
    ALL("All"),
    FUJI_FILM("Fuji Film"),
    RETRO("Retro"),
    DREAMY("Dreamy"),
    PASTEL("Pastel"),
    DIGI_CAM("Digi Cam"),
    CINEMATIC("Cinematic"),
    MOODY("Moody")
}

data class FilterPreset(
    val id: String,
    val name: String,
    val category: FilterCategory,
    val description: String,
    val previewDrawableRes: Int? = null,
    val adjustments: AdjustmentValues = AdjustmentValues(),
    val strength: Float = 1.0f, // 0.0f to 1.0f
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false
)

data class TextOverlay(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String = "VENUSLY",
    val xPercent: Float = 0.5f,
    val yPercent: Float = 0.85f,
    val fontSizeSp: Float = 18f,
    val colorHex: String = "#FFFFFF",
    val fontStyle: String = "Serif",
    val hasBackgroundPill: Boolean = true,
    val isDateStamp: Boolean = false
)

data class StickerOverlay(
    val id: String = java.util.UUID.randomUUID().toString(),
    val symbol: String = "✨",
    val xPercent: Float = 0.5f,
    val yPercent: Float = 0.5f,
    val sizeDp: Float = 48f,
    val rotation: Float = 0f
)

enum class EditorTab(val title: String) {
    ADJUST("Adjust"),
    COLORS("Colors"),
    EFFECTS("Effects"),
    FRAMES("Frames"),
    DETAILS("Details"),
    LIGHT("Light"),
    OVERLAYS("Overlays")
}

enum class CropAspectRatio(val displayName: String, val ratio: Float?) {
    ORIGINAL("Original", null),
    FREE("Free", null),
    SQUARE_1_1("1:1", 1.0f),
    PORTRAIT_4_5("4:5", 0.8f),
    STORY_9_16("9:16", 0.5625f),
    PHOTO_3_4("3:4", 0.75f),
    LANDSCAPE_16_9("16:9", 1.777f),
    CLASSIC_2_3("2:3", 0.666f)
}
