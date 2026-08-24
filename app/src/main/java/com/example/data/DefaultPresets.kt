package com.example.data

import com.example.R
import com.example.model.AdjustmentValues
import com.example.model.FilterCategory
import com.example.model.FilterPreset

object DefaultPresets {
    val presets = listOf(
        FilterPreset(
            id = "fuji_400",
            name = "Fuji 400",
            category = FilterCategory.FUJI_FILM,
            description = "Inspired by classic Fujifilm 400H aesthetics with soft greens, peachy skin tones and bright airy highlights.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.20f,
                contrast = -10f,
                highlights = -20f,
                shadows = 15f,
                saturation = 10f,
                temperature = 8f,
                tint = -5f,
                grain = 18f,
                glow = 12f
            )
        ),
        FilterPreset(
            id = "retro_80s",
            name = "Retro 80s",
            category = FilterCategory.RETRO,
            description = "Nostalgic golden-hour 80s film stock with warm amber light leaks and cinematic faded shadows.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.15f,
                contrast = 12f,
                highlights = -15f,
                shadows = 20f,
                saturation = 18f,
                temperature = 22f,
                tint = 10f,
                grain = 28f,
                vignette = 20f,
                lightLeak = 35f
            )
        ),
        FilterPreset(
            id = "dreamy_glow",
            name = "Dreamy Glow",
            category = FilterCategory.DREAMY,
            description = "Ethereal diffused bloom with romantic pastel pink hues and soft glowing halation.",
            previewDrawableRes = R.drawable.sample_dreamy_flowers,
            adjustments = AdjustmentValues(
                exposure = 0.25f,
                contrast = -18f,
                highlights = -30f,
                shadows = 25f,
                saturation = 12f,
                temperature = 6f,
                tint = 15f,
                glow = 48f,
                blur = 10f
            )
        ),
        FilterPreset(
            id = "pastel_clouds",
            name = "Pastel Air",
            category = FilterCategory.PASTEL,
            description = "Gentle periwinkle skies, cotton-candy pink tones, and delicate luminous exposure.",
            previewDrawableRes = R.drawable.sample_pastel_clouds,
            adjustments = AdjustmentValues(
                exposure = 0.30f,
                contrast = -8f,
                highlights = -15f,
                shadows = 18f,
                saturation = 15f,
                temperature = -8f,
                tint = 12f,
                glow = 25f
            )
        ),
        FilterPreset(
            id = "fuji_pro_160",
            name = "Fuji Pro 160",
            category = FilterCategory.FUJI_FILM,
            description = "Subtle, natural portrait tone with gentle contrast and luminous turquoise tones.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.12f,
                contrast = -5f,
                highlights = -18f,
                shadows = 10f,
                saturation = 5f,
                temperature = -4f,
                tint = 4f,
                grain = 12f
            )
        ),
        FilterPreset(
            id = "digi_cam_2000",
            name = "Digi Cam 00s",
            category = FilterCategory.DIGI_CAM,
            description = "Iconic Y2K point-and-shoot digital camera vibe with sharp flash pop and cool digital grain.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.22f,
                contrast = 24f,
                highlights = 15f,
                shadows = -10f,
                saturation = 14f,
                temperature = -12f,
                tint = -8f,
                sharpen = 25f,
                grain = 22f
            )
        ),
        FilterPreset(
            id = "cinematic_teal_gold",
            name = "Cinema Gold",
            category = FilterCategory.CINEMATIC,
            description = "Hollywood blockbuster palette with deep cyan shadows, warm skin highlights, and film grain.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.05f,
                contrast = 18f,
                highlights = -10f,
                shadows = 8f,
                saturation = 16f,
                temperature = 14f,
                tint = -10f,
                vignette = 25f,
                grain = 20f
            )
        ),
        FilterPreset(
            id = "moody_noir",
            name = "Noir 35mm",
            category = FilterCategory.MOODY,
            description = "Timeless rich black and white film with silver highlights and organic silver-halide grain.",
            previewDrawableRes = R.drawable.sample_dreamy_flowers,
            adjustments = AdjustmentValues(
                exposure = 0.10f,
                contrast = 30f,
                highlights = -20f,
                shadows = -15f,
                saturation = -100f, // True B&W
                temperature = 0f,
                grain = 35f,
                vignette = 30f
            )
        ),
        FilterPreset(
            id = "vintage_dust",
            name = "Vintage Dust",
            category = FilterCategory.RETRO,
            description = "Nostalgic thrift-store film look with real dust specks, light leaks, and retro warmth.",
            previewDrawableRes = R.drawable.sample_pastel_clouds,
            adjustments = AdjustmentValues(
                exposure = 0.18f,
                contrast = 8f,
                highlights = -12f,
                shadows = 16f,
                saturation = 8f,
                temperature = 18f,
                dustEffect = 40f,
                lightLeak = 28f,
                grain = 25f
            )
        )
    )

    fun getPresetById(id: String): FilterPreset? {
        return presets.find { it.id == id }
    }
}
