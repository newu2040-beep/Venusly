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
    val shadowTint: Float = 0f,      // -100f .. 100f (Indigo / Cyan Split Tone)
    val highlightTint: Float = 0f,   // -100f .. 100f (Golden Peach / Rose Split Tone)
    val redChannel: Float = 0f,      // -50f .. 50f (RGB Balance)
    val greenChannel: Float = 0f,    // -50f .. 50f
    val blueChannel: Float = 0f,     // -50f .. 50f
    val clarity: Float = 0f,         // -50f .. 50f (Structure / Midtone Detail)
    val noiseReduction: Float = 0f,  // 0f .. 100f (High-ISO Denoise / Grain Smooth)
    val sharpen: Float = 0f,         // 0f .. 100f
    val grain: Float = 0f,           // 0f .. 100f
    val vignette: Float = 0f,        // 0f .. 100f
    val glow: Float = 0f,            // 0f .. 100f
    val blur: Float = 0f,            // 0f .. 100f
    val lightLeak: Float = 0f,       // 0f .. 100f
    val dustEffect: Float = 0f,      // 0f .. 100f
    val hueShift: Float = 0f,        // -180f .. 180f
    // Advanced Color Grading & Selective HSL
    val shadowHue: Float = 0f,         // -180f .. 180f (Shadow Tone Hue)
    val shadowSaturation: Float = 0f,  // 0f .. 100f (Shadow Tone Intensity)
    val midtoneHue: Float = 0f,        // -180f .. 180f (Midtone Color Grading)
    val midtoneSaturation: Float = 0f, // 0f .. 100f (Midtone Intensity)
    val highlightHue: Float = 0f,      // -180f .. 180f (Highlight Color Shift)
    val highlightSaturation: Float = 0f,// 0f .. 100f (Highlight Intensity)
    val liftedBlacks: Float = 0f,      // 0f .. 100f (Filmic Matte / Lifted Shadows)
    val highlightCompress: Float = 0f, // 0f .. 100f (Soft Highlight Recovery)
    val skinToneWarmth: Float = 0f,    // -50f .. 50f (Selective Skin Tone Balance)
    val skyBlueBoost: Float = 0f,      // -50f .. 50f (Selective Sky & Ocean Vibrance)
    val foliageGreenBoost: Float = 0f, // -50f .. 50f (Selective Nature Emerald)
    val photoCornerRadius: Float = 0f, // 0f .. 100f (Rounded Photo Corners)
    val frameMatteWidth: Float = 0f,   // 0f .. 100f (Framed Margin Padding)
    val frameMatteColor: Long = 0xFFFFFFFF, // Matte Frame Color (ARGB)
    val frame: AestheticFrame = AestheticFrame.NONE,
    val rotationDegrees: Float = 0f, // 0, 90, 180, 270 + fine tune
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false
)

enum class AestheticFrame(val id: String, val displayName: String, val subtitle: String) {
    NONE("none", "None", "Clean edge"),
    POLAROID_WHITE("polaroid_white", "Polaroid Classic", "Instant white border"),
    POLAROID_DARK("polaroid_dark", "Polaroid Noir", "Minimalist matte dark"),
    POLAROID_PASTEL("polaroid_pastel", "Pastel Instant", "Soft pink instant border"),
    FILM_35MM("film_35mm", "35mm Filmstrip", "Analog frame with perforations"),
    DIGICAM_OSD("digicam_osd", "DigiCam 2000s OSD", "Y2K point & shoot viewfinder"),
    PASTEL_AURA("pastel_aura", "Pastel Aura", "Soft gradient glow frame"),
    CLEAN_MAT("clean_mat", "Gallery Mat", "Museum white passe-partout"),
    MINIMAL_KEYLINE("minimal_keyline", "Minimal Keyline", "Crisp double hairline"),
    VINTAGE_STAMP("vintage_stamp", "Postage Stamp", "Perforated aesthetic edges"),
    PASTEL_CARD("pastel_card", "Rounded Card", "Soft rounded pastel border"),
    RETRO_TV("retro_tv", "Retro CRT", "Vintage rounded monitor bezel"),
    Y2K_STICKER_FRAME("y2k_scrapbook", "Y2K Scrapbook", "Pastel corner tape & stars"),
    NEON_CYBER_BORDER("neon_cyber", "Cyber Neon", "Dual luminous glowing line"),
    SCALLOPED_LACE("scalloped_lace", "Scalloped Lace", "Wavy aesthetic pastel edge"),
    FILM_SLIDE_MOUNT("film_slide", "35mm Slide Mount", "Archival slide mount holder"),
    GOLD_GLITTER_BORDER("gold_glitter", "Luxury Gold", "Glittering amber metallic border"),
    FLORAL_PASTEL_RIBBON("floral_ribbon", "Pastel Ribbon", "Cute aesthetic corner bow"),
    PAPER_TEAR_SCRAPBOOK("paper_tear", "Ripped Paper", "Torn scrapbook margin edge")
}

enum class ExportResolution(val displayName: String, val subtitle: String, val maxDimension: Int) {
    ORIGINAL("Original 4K Ultra", "Full camera sensor clarity (~3840px)", 3840),
    QHD_2K("2K Quad HD", "Crisp studio quality (~2560px)", 2560),
    FHD_1080P("Full HD 1080p", "Standard social & sharing (~1920px)", 1920),
    HD_720P("HD 720p", "Fast lightweight export (~1280px)", 1280)
}

enum class ExportFormatOption(val displayName: String, val subtitle: String, val format: Bitmap.CompressFormat, val extension: String) {
    JPEG("JPEG High-Res", "Universal format with maximum compatibility", Bitmap.CompressFormat.JPEG, ".jpg"),
    PNG("PNG Lossless", "Pixel-perfect uncompressed studio quality", Bitmap.CompressFormat.PNG, ".png"),
    WEBP("WEBP Optimized", "Next-gen compact high fidelity", Bitmap.CompressFormat.WEBP, ".webp")
}

enum class FilterCategory(val displayName: String) {
    ALL("All"),
    DIGITAL_CAMERA("Digital Camera"),
    VINTAGE_CAMERA("Old Camera"),
    SPECIAL_LOOKS("Special Looks"),
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
    val fontSizeSp: Float = 20f,
    val colorHex: String = "#FFFFFF",
    val fontStyle: String = "Serif", // "Serif", "SansSerif", "Monospace", "Cursive", "DisplayBold"
    val hasBackgroundPill: Boolean = true,
    val isDateStamp: Boolean = false,
    val rotation: Float = 0f,
    val blendMode: String = "Normal" // "Normal", "Multiply", "Screen", "Overlay", "Darken", "Lighten", "ColorDodge", "Difference"
)

data class StickerOverlay(
    val id: String = java.util.UUID.randomUUID().toString(),
    val symbol: String = "✨",
    val customImageUri: String? = null,
    val xPercent: Float = 0.5f,
    val yPercent: Float = 0.5f,
    val sizeDp: Float = 56f,
    val rotation: Float = 0f,
    val tintColorHex: String? = null,
    val alpha: Float = 1.0f,
    val blendMode: String = "Normal" // "Normal", "Multiply", "Screen", "Overlay", "Darken", "Lighten", "ColorDodge", "Difference"
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

enum class GridOverlayMode(val displayName: String, val badgeText: String) {
    OFF("Off", "GRID OFF"),
    RULE_OF_THIRDS("Rule of Thirds", "3×3 THIRDS"),
    GOLDEN_RATIO("Golden Ratio", "GOLDEN PHI"),
    SQUARE_GRID("Center Grid", "4×4 CENTER")
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

enum class LayerType(val displayName: String) {
    BASE_IMAGE("Base Photo"),
    ADJUSTMENTS("Color & Filter Stack"),
    GRAIN_LIGHT_LEAK("Grain & Light Effects"),
    FRAME("Frame & Borders"),
    STICKER("Sticker"),
    TEXT_OVERLAY("Text / Date Stamp")
}

data class LayerItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: LayerType,
    val name: String,
    val isVisible: Boolean = true,
    val opacity: Float = 1.0f,
    val isLocked: Boolean = false,
    val associatedId: String? = null
)

enum class BatchItemStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}

data class BatchProcessingItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val uri: android.net.Uri,
    val thumbnailBitmap: android.graphics.Bitmap? = null,
    val status: BatchItemStatus = BatchItemStatus.PENDING,
    val resultUri: android.net.Uri? = null,
    val errorMessage: String? = null
)

