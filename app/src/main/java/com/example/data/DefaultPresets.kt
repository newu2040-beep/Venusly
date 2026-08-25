package com.example.data

import com.example.R
import com.example.model.AdjustmentValues
import com.example.model.AestheticFrame
import com.example.model.FilterCategory
import com.example.model.FilterPreset

object DefaultPresets {
    val presets = listOf(
        // Digital Camera (DigiCam) Filters
        FilterPreset(
            id = "digi_ccd_2000",
            name = "Sony CCD 00s",
            category = FilterCategory.DIGI_CAM,
            description = "Iconic early 2000s Sony Cyber-shot CCD sensor with saturated blues, bright flash pop and fine digital grain.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.22f,
                contrast = 24f,
                highlights = 18f,
                shadows = -8f,
                saturation = 18f,
                temperature = -10f,
                tint = -6f,
                sharpen = 28f,
                grain = 20f
            )
        ),
        FilterPreset(
            id = "canon_powershot_g2",
            name = "PowerShot G2",
            category = FilterCategory.DIGI_CAM,
            description = "Warm Y2K point-and-shoot camera look with luminous peach skin tones and punchy primary colors.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.25f,
                contrast = 18f,
                highlights = 12f,
                shadows = 5f,
                saturation = 22f,
                temperature = 16f,
                tint = 8f,
                sharpen = 20f,
                grain = 15f
            )
        ),
        FilterPreset(
            id = "casio_exilim",
            name = "Casio Exilim",
            category = FilterCategory.DIGI_CAM,
            description = "High-key Japanese digital compact camera aesthetic with luminous bloom and soft pastel overexposure.",
            previewDrawableRes = R.drawable.sample_pastel_clouds,
            adjustments = AdjustmentValues(
                exposure = 0.35f,
                contrast = -12f,
                highlights = -20f,
                shadows = 22f,
                saturation = 12f,
                temperature = -4f,
                tint = 14f,
                glow = 32f,
                sharpen = 15f
            )
        ),
        FilterPreset(
            id = "olympus_camedia",
            name = "Olympus Camedia",
            category = FilterCategory.DIGI_CAM,
            description = "Y2K millennium CCD rendering with deep saturated cyan sky tones and warm amber midtones.",
            previewDrawableRes = R.drawable.sample_dreamy_flowers,
            adjustments = AdjustmentValues(
                exposure = 0.16f,
                contrast = 20f,
                highlights = 10f,
                shadows = -5f,
                saturation = 16f,
                temperature = -14f,
                tint = -10f,
                sharpen = 24f,
                vignette = 15f
            )
        ),
        FilterPreset(
            id = "y2k_night_flash",
            name = "Y2K Night Flash",
            category = FilterCategory.DIGI_CAM,
            description = "Club & party direct flash photography with harsh edge falloff, vivid contrast, and pop colors.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.30f,
                contrast = 32f,
                highlights = 25f,
                shadows = -25f,
                saturation = 25f,
                temperature = 6f,
                vignette = 38f,
                sharpen = 30f,
                grain = 25f
            )
        ),
        FilterPreset(
            id = "nikon_coolpix",
            name = "Coolpix 995",
            category = FilterCategory.DIGI_CAM,
            description = "Early 2000s swivel-lens digital camera look with cool sensor rendering and nostalgic digital noise.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.18f,
                contrast = 15f,
                highlights = 8f,
                shadows = 0f,
                saturation = 10f,
                temperature = -18f,
                tint = 4f,
                sharpen = 35f,
                grain = 18f
            )
        ),

        // Fuji Film Filters
        FilterPreset(
            id = "fuji_400",
            name = "Fuji 400H",
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
            id = "fuji_pro_160",
            name = "Fuji Pro 160NS",
            category = FilterCategory.FUJI_FILM,
            description = "Subtle, natural portrait tone with gentle contrast and luminous turquoise shadows.",
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
            id = "fuji_velvia_50",
            name = "Fuji Velvia 50",
            category = FilterCategory.FUJI_FILM,
            description = "Legendary ultra-vivid slide film with saturated landscape colors, emerald foliage, and deep blacks.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.08f,
                contrast = 28f,
                highlights = -5f,
                shadows = -12f,
                saturation = 35f,
                temperature = -6f,
                tint = -8f,
                vignette = 18f
            )
        ),
        FilterPreset(
            id = "fuji_superia_800",
            name = "Fuji Superia 800",
            category = FilterCategory.FUJI_FILM,
            description = "Fast 35mm film stock with distinct emerald-magenta split and organic medium grain.",
            previewDrawableRes = R.drawable.sample_pastel_clouds,
            adjustments = AdjustmentValues(
                exposure = 0.15f,
                contrast = 14f,
                highlights = -12f,
                shadows = 12f,
                saturation = 14f,
                temperature = -8f,
                tint = 12f,
                grain = 32f
            )
        ),

        // Retro & Analog Filters
        FilterPreset(
            id = "kodak_portra_400",
            name = "Kodak Portra 400",
            category = FilterCategory.RETRO,
            description = "The gold standard portrait film with warm creamy highlights, smooth skin roll-off, and fine grain.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.18f,
                contrast = -4f,
                highlights = -16f,
                shadows = 14f,
                saturation = 12f,
                temperature = 18f,
                tint = 6f,
                grain = 16f
            )
        ),
        FilterPreset(
            id = "retro_80s",
            name = "Retro 80s Golden",
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
            id = "vintage_dust",
            name = "Vintage Dust & Scratch",
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
                dustEffect = 45f,
                lightLeak = 28f,
                grain = 25f
            )
        ),
        FilterPreset(
            id = "polaroid_sx70",
            name = "Polaroid SX-70",
            category = FilterCategory.RETRO,
            description = "Vintage instant film aesthetic with muted sage greens, creamy warm whites, and light vignette.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.20f,
                contrast = -6f,
                highlights = -18f,
                shadows = 18f,
                saturation = 8f,
                temperature = 15f,
                tint = 8f,
                vignette = 24f,
                grain = 20f,
                frame = AestheticFrame.POLAROID_WHITE
            )
        ),

        // Dreamy & Pastel Filters
        FilterPreset(
            id = "dreamy_glow",
            name = "Dreamy Glow Bloom",
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
            name = "Pastel Air & Clouds",
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
            id = "prism_halo",
            name = "Prism Rainbow Halo",
            category = FilterCategory.DREAMY,
            description = "Dreamy chromatic dispersion effect with radiant prismatic light leak and luminous warmth.",
            previewDrawableRes = R.drawable.sample_dreamy_flowers,
            adjustments = AdjustmentValues(
                exposure = 0.22f,
                contrast = -10f,
                highlights = -22f,
                shadows = 16f,
                saturation = 18f,
                temperature = 8f,
                tint = 10f,
                lightLeak = 45f,
                glow = 30f
            )
        ),
        FilterPreset(
            id = "fairy_dust",
            name = "Fairy Dust Sparkle",
            category = FilterCategory.PASTEL,
            description = "Soft glowing fairytale aesthetic with fine luminous grain, blush tones, and delicate brightness.",
            previewDrawableRes = R.drawable.sample_pastel_clouds,
            adjustments = AdjustmentValues(
                exposure = 0.28f,
                contrast = -12f,
                highlights = -20f,
                shadows = 20f,
                saturation = 14f,
                temperature = 4f,
                tint = 18f,
                glow = 38f,
                dustEffect = 25f
            )
        ),

        // Cinematic & Moody Filters
        FilterPreset(
            id = "cinematic_teal_gold",
            name = "Cinema Gold & Teal",
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
            id = "cinestill_800t",
            name = "CineStill 800T",
            category = FilterCategory.CINEMATIC,
            description = "Tungsten movie film look with characteristic red halation around highlights and rich cyan night tones.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.10f,
                contrast = 22f,
                highlights = -5f,
                shadows = -10f,
                saturation = 20f,
                temperature = -15f,
                tint = 10f,
                glow = 35f,
                grain = 24f
            )
        ),
        FilterPreset(
            id = "moody_noir",
            name = "Noir Silver 35mm",
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
        )
    )

    fun getPresetById(id: String): FilterPreset? {
        return presets.find { it.id == id }
    }
}
