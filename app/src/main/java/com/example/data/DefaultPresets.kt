package com.example.data

import com.example.R
import com.example.model.AdjustmentValues
import com.example.model.AestheticFrame
import com.example.model.FilterCategory
import com.example.model.FilterPreset

object DefaultPresets {
    fun getPresetById(id: String): FilterPreset? = presets.find { it.id == id }

    val presets = listOf(
        // ==========================================
        // 1. DIGITAL CAMERA (Venusly 3.0 Pack)
        // ==========================================
        FilterPreset(
            id = "digicam_soft",
            name = "Digicam Soft",
            category = FilterCategory.DIGITAL_CAMERA,
            description = "Soft Y2K compact digital look with luminous warm highlights, smooth skin rendering, and gentle glow.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.18f,
                contrast = 10f,
                highlights = -12f,
                shadows = 14f,
                saturation = 8f,
                temperature = 6f,
                tint = 4f,
                glow = 22f,
                sharpen = 12f,
                skinToneWarmth = 15f
            )
        ),
        FilterPreset(
            id = "ccd_glow",
            name = "CCD Glow",
            category = FilterCategory.DIGITAL_CAMERA,
            description = "Luminous early CCD sensor bloom with punchy saturated blues and bright high-key flash exposure.",
            previewDrawableRes = R.drawable.sample_pastel_clouds,
            adjustments = AdjustmentValues(
                exposure = 0.25f,
                contrast = 18f,
                highlights = 15f,
                shadows = -5f,
                saturation = 22f,
                temperature = -8f,
                tint = -6f,
                glow = 35f,
                sharpen = 24f,
                skyBlueBoost = 20f
            )
        ),
        FilterPreset(
            id = "cyber_digicam",
            name = "Cyber Digicam",
            category = FilterCategory.DIGITAL_CAMERA,
            description = "Futuristic 2000s cyber aesthetic with crisp teal shadows, cool highlights, and sharp digital detail.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.14f,
                contrast = 22f,
                highlights = 12f,
                shadows = -10f,
                saturation = 14f,
                temperature = -18f,
                tint = -12f,
                shadowHue = 190f,
                shadowSaturation = 25f,
                sharpen = 32f,
                grain = 15f
            )
        ),
        FilterPreset(
            id = "y2k_flash",
            name = "Y2K Flash",
            category = FilterCategory.DIGITAL_CAMERA,
            description = "Punchy direct flash pop with vivid contrast, luminous skin tones, and bright highlights.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.28f,
                contrast = 30f,
                highlights = 22f,
                shadows = -18f,
                saturation = 24f,
                temperature = 10f,
                vignette = 30f,
                sharpen = 28f,
                skinToneWarmth = 20f
            )
        ),
        FilterPreset(
            id = "cool_ccd",
            name = "Cool CCD",
            category = FilterCategory.DIGITAL_CAMERA,
            description = "Chilled Japanese digital compact camera rendering with soft cyan tones and clean shadows.",
            previewDrawableRes = R.drawable.sample_dreamy_flowers,
            adjustments = AdjustmentValues(
                exposure = 0.15f,
                contrast = 12f,
                highlights = -10f,
                shadows = 8f,
                saturation = 6f,
                temperature = -22f,
                tint = 5f,
                sharpen = 25f,
                skyBlueBoost = 18f
            )
        ),
        FilterPreset(
            id = "soft_flash",
            name = "Soft Flash",
            category = FilterCategory.DIGITAL_CAMERA,
            description = "Velvety diffused portrait flash with soft shadow roll-off and warm peach skin highlights.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.20f,
                contrast = 8f,
                highlights = -15f,
                shadows = 18f,
                saturation = 10f,
                temperature = 8f,
                tint = 6f,
                glow = 18f,
                skinToneWarmth = 22f
            )
        ),
        FilterPreset(
            id = "night_digicam",
            name = "Night Digicam",
            category = FilterCategory.DIGITAL_CAMERA,
            description = "Low-light point-and-shoot camera vibe with deep moody shadows, golden ambient warmth, and subtle noise.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.12f,
                contrast = 26f,
                highlights = 14f,
                shadows = -22f,
                saturation = 18f,
                temperature = 15f,
                shadowHue = 35f,
                shadowSaturation = 20f,
                grain = 28f,
                vignette = 35f
            )
        ),
        FilterPreset(
            id = "digital_dream",
            name = "Digital Dream",
            category = FilterCategory.DIGITAL_CAMERA,
            description = "Dreamy pastel overexposure with soft matte shadows, lifted blacks, and gentle glowing highlights.",
            previewDrawableRes = R.drawable.sample_pastel_clouds,
            adjustments = AdjustmentValues(
                exposure = 0.32f,
                contrast = -15f,
                highlights = -25f,
                shadows = 28f,
                saturation = 12f,
                temperature = -2f,
                tint = 14f,
                liftedBlacks = 25f,
                glow = 40f
            )
        ),
        FilterPreset(
            id = "lcd_pop",
            name = "LCD Pop",
            category = FilterCategory.DIGITAL_CAMERA,
            description = "Vivid camera display preview aesthetic with bright primary color boost and punchy sky tones.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.16f,
                contrast = 24f,
                highlights = 8f,
                shadows = -4f,
                saturation = 32f,
                temperature = 4f,
                vibrance = 25f,
                sharpen = 22f,
                skyBlueBoost = 25f
            )
        ),

        // ==========================================
        // 2. OLD / VINTAGE CAMERA (Venusly 3.0 Pack)
        // ==========================================
        FilterPreset(
            id = "kodak_gold",
            name = "Kodak Gold",
            category = FilterCategory.VINTAGE_CAMERA,
            description = "Iconic warm golden analog film tones, rich amber hues, smooth skin roll-off, and organic film grain.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.15f,
                contrast = 16f,
                highlights = -10f,
                shadows = 10f,
                saturation = 18f,
                temperature = 22f,
                tint = 8f,
                grain = 25f,
                skinToneWarmth = 25f
            )
        ),
        FilterPreset(
            id = "kodak_portra",
            name = "Kodak Portra",
            category = FilterCategory.VINTAGE_CAMERA,
            description = "Professional portrait film stock with natural pastel skin rendering, soft compression, and subtle warmth.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.18f,
                contrast = -8f,
                highlights = -18f,
                shadows = 15f,
                saturation = 10f,
                temperature = 12f,
                tint = 4f,
                grain = 18f,
                liftedBlacks = 15f
            )
        ),
        FilterPreset(
            id = "kodak_ektar",
            name = "Kodak Ektar",
            category = FilterCategory.VINTAGE_CAMERA,
            description = "Ultra-vivid saturated color negative film with punchy blues, radiant reds, and crisp analog clarity.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.10f,
                contrast = 28f,
                highlights = 5f,
                shadows = -10f,
                saturation = 38f,
                temperature = 6f,
                vibrance = 28f,
                sharpen = 20f,
                foliageGreenBoost = 20f
            )
        ),
        FilterPreset(
            id = "fujifilm_soft",
            name = "Fujifilm Soft",
            category = FilterCategory.VINTAGE_CAMERA,
            description = "Airy Japanese pastel greens with a gentle magenta shadow tint and clean filmic highlights.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.22f,
                contrast = -12f,
                highlights = -22f,
                shadows = 20f,
                saturation = 8f,
                temperature = -6f,
                tint = 12f,
                grain = 16f,
                foliageGreenBoost = 18f
            )
        ),
        FilterPreset(
            id = "fujifilm_classic",
            name = "Fujifilm Classic",
            category = FilterCategory.VINTAGE_CAMERA,
            description = "Classic Superia film aesthetic featuring deep emerald foliage, crisp cyan skies, and timeless contrast.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.12f,
                contrast = 20f,
                highlights = -8f,
                shadows = 5f,
                saturation = 16f,
                temperature = -12f,
                tint = 10f,
                grain = 22f,
                skyBlueBoost = 22f
            )
        ),
        FilterPreset(
            id = "agfa_vintage",
            name = "Agfa Vintage",
            category = FilterCategory.VINTAGE_CAMERA,
            description = "Warm European retro film rendering with golden-brown shadow tinting and nostalgic matte shadow lift.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.14f,
                contrast = 14f,
                highlights = -14f,
                shadows = 12f,
                saturation = 12f,
                temperature = 18f,
                tint = -8f,
                shadowHue = 40f,
                shadowSaturation = 22f,
                liftedBlacks = 22f,
                grain = 26f
            )
        ),
        FilterPreset(
            id = "polaroid_fade",
            name = "Polaroid Fade",
            category = FilterCategory.VINTAGE_CAMERA,
            description = "Faded 1970s instant camera print look with high lifted blacks, soft muted tones, and subtle edge vignetting.",
            previewDrawableRes = R.drawable.sample_pastel_clouds,
            adjustments = AdjustmentValues(
                exposure = 0.20f,
                contrast = -18f,
                highlights = -20f,
                shadows = 25f,
                saturation = -12f,
                temperature = 14f,
                tint = -6f,
                liftedBlacks = 35f,
                vignette = 25f,
                grain = 24f
            )
        ),
        FilterPreset(
            id = "olympus_mju",
            name = "Olympus Mju",
            category = FilterCategory.VINTAGE_CAMERA,
            description = "Compact 90s street film camera aesthetic with balanced exposure, punchy midtones, and classic 35mm grain.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.15f,
                contrast = 22f,
                highlights = 8f,
                shadows = -6f,
                saturation = 16f,
                temperature = 4f,
                tint = -4f,
                sharpen = 22f,
                grain = 24f
            )
        ),
        FilterPreset(
            id = "canon_powershot",
            name = "Canon PowerShot",
            category = FilterCategory.VINTAGE_CAMERA,
            description = "Classic early G-series warmth with natural peachy skin tone roll-off and vivid midtone detail.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.22f,
                contrast = 16f,
                highlights = 10f,
                shadows = 6f,
                saturation = 18f,
                temperature = 14f,
                tint = 6f,
                sharpen = 18f,
                skinToneWarmth = 18f
            )
        ),
        FilterPreset(
            id = "sony_cybershot",
            name = "Sony Cybershot",
            category = FilterCategory.VINTAGE_CAMERA,
            description = "Saturated 2000s Cyber-shot CCD sensor rendering with intense blues and crisp flash highlights.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.20f,
                contrast = 24f,
                highlights = 16f,
                shadows = -8f,
                saturation = 25f,
                temperature = -10f,
                tint = -8f,
                sharpen = 28f,
                skyBlueBoost = 25f
            )
        ),
        FilterPreset(
            id = "disposable_film",
            name = "Disposable Film",
            category = FilterCategory.VINTAGE_CAMERA,
            description = "Single-use 35mm snapshot camera look with high contrast flash pop, warm amber glow, and rich grain.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.24f,
                contrast = 32f,
                highlights = 20f,
                shadows = -16f,
                saturation = 20f,
                temperature = 16f,
                vignette = 32f,
                grain = 35f
            )
        ),
        FilterPreset(
            id = "film_90s",
            name = "90s Film",
            category = FilterCategory.VINTAGE_CAMERA,
            description = "Authentic 1990s family album photo aesthetic with warm amber wash and soft atmospheric highlights.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.16f,
                contrast = 12f,
                highlights = -10f,
                shadows = 14f,
                saturation = 14f,
                temperature = 20f,
                tint = 4f,
                liftedBlacks = 18f,
                grain = 28f
            )
        ),
        FilterPreset(
            id = "film_2000s",
            name = "2000s Film",
            category = FilterCategory.VINTAGE_CAMERA,
            description = "Early millennium hybrid snapshot vibe with punchy primary colors and micro digital-analog grain.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.18f,
                contrast = 20f,
                highlights = 12f,
                shadows = -4f,
                saturation = 20f,
                temperature = 2f,
                tint = -6f,
                sharpen = 22f,
                grain = 20f
            )
        ),
        FilterPreset(
            id = "warm_retro",
            name = "Warm Retro",
            category = FilterCategory.VINTAGE_CAMERA,
            description = "Golden hour vintage analog tone with warm sepia shadows and luminous golden highlights.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.18f,
                contrast = 15f,
                highlights = -8f,
                shadows = 10f,
                saturation = 16f,
                temperature = 28f,
                tint = 10f,
                glow = 20f,
                grain = 22f
            )
        ),
        FilterPreset(
            id = "faded_memory",
            name = "Faded Memory",
            category = FilterCategory.VINTAGE_CAMERA,
            description = "Dreamy washed-out nostalgic print aesthetic with low contrast, lifted matte blacks, and soft muted hues.",
            previewDrawableRes = R.drawable.sample_pastel_clouds,
            adjustments = AdjustmentValues(
                exposure = 0.25f,
                contrast = -22f,
                highlights = -30f,
                shadows = 32f,
                saturation = -18f,
                temperature = 8f,
                tint = 8f,
                liftedBlacks = 40f,
                grain = 25f
            )
        ),

        // ==========================================
        // 3. SPECIAL LOOKS (Venusly 3.0 Pack)
        // ==========================================
        FilterPreset(
            id = "flash_portrait",
            name = "Flash Portrait",
            category = FilterCategory.SPECIAL_LOOKS,
            description = "Optimized direct flash lighting for portraits with selective skin tone warmth boost and punchy contrast.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.26f,
                contrast = 25f,
                highlights = 18f,
                shadows = -10f,
                saturation = 16f,
                temperature = 12f,
                tint = 4f,
                skinToneWarmth = 28f,
                sharpen = 20f,
                vignette = 25f
            )
        ),
        FilterPreset(
            id = "film_burn",
            name = "Film Burn",
            category = FilterCategory.SPECIAL_LOOKS,
            description = "Warm golden film edge burn look with elevated shadow warmth, soft highlight bloom, and tactile film grain.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.20f,
                contrast = 14f,
                highlights = 10f,
                shadows = 15f,
                saturation = 18f,
                temperature = 24f,
                tint = 12f,
                lightLeak = 45f,
                glow = 25f,
                grain = 30f
            )
        ),
        FilterPreset(
            id = "light_leak",
            name = "Light Leak",
            category = FilterCategory.SPECIAL_LOOKS,
            description = "Atmospheric side light leak flare with elevated shadow tones and warm golden highlights.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.22f,
                contrast = 6f,
                highlights = 8f,
                shadows = 20f,
                saturation = 14f,
                temperature = 18f,
                tint = 10f,
                lightLeak = 65f,
                glow = 20f,
                liftedBlacks = 20f
            )
        ),
        FilterPreset(
            id = "halation",
            name = "Halation",
            category = FilterCategory.SPECIAL_LOOKS,
            description = "Luminous reddish-golden highlight bloom inspired by motion picture film halation around light sources.",
            previewDrawableRes = R.drawable.sample_pastel_clouds,
            adjustments = AdjustmentValues(
                exposure = 0.18f,
                contrast = 18f,
                highlights = 15f,
                shadows = 5f,
                saturation = 20f,
                temperature = 14f,
                tint = 16f,
                glow = 50f,
                highlightTint = 30f
            )
        ),
        FilterPreset(
            id = "dust_grain",
            name = "Dust & Grain",
            category = FilterCategory.SPECIAL_LOOKS,
            description = "Authentic analog dust specks, subtle scratches, and rich tactile 35mm film grain texture.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.12f,
                contrast = 16f,
                highlights = -10f,
                shadows = 8f,
                saturation = 10f,
                temperature = 6f,
                grain = 55f,
                dustEffect = 60f
            )
        ),
        FilterPreset(
            id = "date_stamp",
            name = "Date Stamp",
            category = FilterCategory.SPECIAL_LOOKS,
            description = "Retro orange LED date stamp look paired with authentic 90s point-and-shoot camera color grading.",
            previewDrawableRes = R.drawable.sample_retro_sunset,
            adjustments = AdjustmentValues(
                exposure = 0.16f,
                contrast = 20f,
                highlights = 10f,
                shadows = -5f,
                saturation = 15f,
                temperature = 16f,
                tint = 6f,
                sharpen = 24f,
                grain = 28f
            )
        ),
        FilterPreset(
            id = "vintage_ccd",
            name = "Vintage CCD",
            category = FilterCategory.SPECIAL_LOOKS,
            description = "Classic 2002 CCD sensor rendering with nostalgic color balance, crisp micro-details, and fine digital noise.",
            previewDrawableRes = R.drawable.sample_fuji_arch,
            adjustments = AdjustmentValues(
                exposure = 0.22f,
                contrast = 24f,
                highlights = 16f,
                shadows = -10f,
                saturation = 20f,
                temperature = -8f,
                tint = -6f,
                sharpen = 30f,
                grain = 22f
            )
        )
    )
}
