package com.example.engine

import android.graphics.ColorMatrix
import com.example.model.AdjustmentValues
import kotlin.math.cos
import kotlin.math.sin

object ColorMatrixBuilder {

    fun buildColorMatrix(adjustments: AdjustmentValues, strength: Float = 1.0f): ColorMatrix {
        val result = ColorMatrix()

        // 1. Exposure / Brightness
        // Exposure (-1.0 to 1.0) & Brightness (-100 to 100)
        val exp = adjustments.exposure * strength
        val br = (adjustments.brightness * strength) / 100f
        val expScale = 1.0f + exp + (br * 0.5f)
        val expOffset = br * 40f

        val expMatrix = ColorMatrix(floatArrayOf(
            expScale, 0f, 0f, 0f, expOffset,
            0f, expScale, 0f, 0f, expOffset,
            0f, 0f, expScale, 0f, expOffset,
            0f, 0f, 0f, 1f, 0f
        ))
        result.postConcat(expMatrix)

        // 2. Contrast (-100 to 100)
        val contrastVal = (adjustments.contrast * strength) / 100f
        val scale = 1.0f + contrastVal
        val translate = (-0.5f * scale + 0.5f) * 255f
        val contrastMatrix = ColorMatrix(floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        result.postConcat(contrastMatrix)

        // 3. Saturation & Vibrance (-100 to 100)
        val satVal = 1.0f + ((adjustments.saturation + adjustments.vibrance * 0.6f) * strength) / 100f
        val satMatrix = ColorMatrix()
        satMatrix.setSaturation(satVal.coerceAtLeast(0f))
        result.postConcat(satMatrix)

        // 4. Temperature (Warmth) & Tint
        // Temperature: warm (boost Red, reduce Blue), cool (boost Blue, reduce Red)
        // Tint: magenta (boost Red & Blue, reduce Green), green (boost Green)
        val temp = (adjustments.temperature * strength) / 100f
        val tint = (adjustments.tint * strength) / 100f

        val rFactor = 1.0f + (temp * 0.25f) + (tint * 0.15f)
        val gFactor = 1.0f - (tint * 0.20f)
        val bFactor = 1.0f - (temp * 0.25f) + (tint * 0.10f)

        val tempMatrix = ColorMatrix(floatArrayOf(
            rFactor, 0f, 0f, 0f, 0f,
            0f, gFactor, 0f, 0f, 0f,
            0f, 0f, bFactor, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        result.postConcat(tempMatrix)

        // 5. Highlights & Shadows simulation
        val high = (adjustments.highlights * strength) / 100f
        val shad = (adjustments.shadows * strength) / 100f
        if (high != 0f || shad != 0f) {
            val hOffset = high * 20f
            val sOffset = shad * 20f
            val hsMatrix = ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, hOffset + sOffset,
                0f, 1f, 0f, 0f, hOffset + sOffset,
                0f, 0f, 1f, 0f, hOffset + sOffset,
                0f, 0f, 0f, 1f, 0f
            ))
            result.postConcat(hsMatrix)
        }

        // 6. Split Toning & Advanced Color Balance (Shadow Tint, Highlight Tint, RGB Channels)
        val sTint = (adjustments.shadowTint * strength) / 100f
        val hTint = (adjustments.highlightTint * strength) / 100f
        val rChan = 1.0f + ((adjustments.redChannel * strength) / 100f)
        val gChan = 1.0f + ((adjustments.greenChannel * strength) / 100f)
        val bChan = 1.0f + ((adjustments.blueChannel * strength) / 100f)

        // Shadow tint boosts Blue/Cyan in darks; Highlight tint boosts Red/Gold in lights
        val rOffset = (hTint * 18f)
        val gOffset = (hTint * 10f) - (sTint * 8f)
        val bOffset = (sTint * 22f)

        val splitToneMatrix = ColorMatrix(floatArrayOf(
            rChan, 0f, 0f, 0f, rOffset,
            0f, gChan, 0f, 0f, gOffset,
            0f, 0f, bChan, 0f, bOffset,
            0f, 0f, 0f, 1f, 0f
        ))
        result.postConcat(splitToneMatrix)

        // 6b. Filmic Lifted Blacks (Matte) & Soft Highlight Recovery
        val lifted = (adjustments.liftedBlacks * strength) / 100f
        val comp = (adjustments.highlightCompress * strength) / 100f
        if (lifted > 0f || comp > 0f) {
            val pedestal = lifted * 32f
            val topScale = 1.0f - (comp * 0.15f)
            val toneCurveMatrix = ColorMatrix(floatArrayOf(
                topScale, 0f, 0f, 0f, pedestal,
                0f, topScale, 0f, 0f, pedestal,
                0f, 0f, topScale, 0f, pedestal,
                0f, 0f, 0f, 1f, 0f
            ))
            result.postConcat(toneCurveMatrix)
        }

        // 6c. 3-Way Color Wheels (Shadows, Midtones, Highlights Color Grading)
        val sHue = adjustments.shadowHue * strength
        val sSat = adjustments.shadowSaturation * strength
        val mHue = adjustments.midtoneHue * strength
        val mSat = adjustments.midtoneSaturation * strength
        val hHue = adjustments.highlightHue * strength
        val hSat = adjustments.highlightSaturation * strength

        if (sSat > 0f || mSat > 0f || hSat > 0f) {
            val (sR, sG, sB) = calculateToneShift(sHue, sSat * 0.25f)
            val (mR, mG, mB) = calculateToneShift(mHue, mSat * 0.20f)
            val (hR, hG, hB) = calculateToneShift(hHue, hSat * 0.22f)

            val colorWheelMatrix = ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, sR + mR + hR,
                0f, 1f, 0f, 0f, sG + mG + hG,
                0f, 0f, 1f, 0f, sB + mB + hB,
                0f, 0f, 0f, 1f, 0f
            ))
            result.postConcat(colorWheelMatrix)
        }

        // 6d. Selective HSL Color Adjustments (Skin Warmth, Sky Blue, Foliage Emerald)
        val skinW = (adjustments.skinToneWarmth * strength) / 100f
        val skyB = (adjustments.skyBlueBoost * strength) / 100f
        val folG = (adjustments.foliageGreenBoost * strength) / 100f

        if (skinW != 0f || skyB != 0f || folG != 0f) {
            val rMult = 1.0f + (skinW * 0.18f) - (skyB * 0.08f)
            val gMult = 1.0f + (folG * 0.25f) - (skinW * 0.05f)
            val bMult = 1.0f + (skyB * 0.30f) - (skinW * 0.15f)

            val rOff = skinW * 12f
            val gOff = folG * 10f
            val bOff = skyB * 15f

            val hslMatrix = ColorMatrix(floatArrayOf(
                rMult, 0f, 0f, 0f, rOff,
                0f, gMult, 0f, 0f, gOff,
                0f, 0f, bMult, 0f, bOff,
                0f, 0f, 0f, 1f, 0f
            ))
            result.postConcat(hslMatrix)
        }

        // 7. Clarity / Midtone Structure (-50 to 50)
        val clarityVal = (adjustments.clarity * strength) / 100f
        if (clarityVal != 0f) {
            val cScale = 1.0f + (clarityVal * 0.4f)
            val cTrans = (-0.5f * cScale + 0.5f) * 255f
            val clarityMatrix = ColorMatrix(floatArrayOf(
                cScale, 0f, 0f, 0f, cTrans,
                0f, cScale, 0f, 0f, cTrans,
                0f, 0f, cScale, 0f, cTrans,
                0f, 0f, 0f, 1f, 0f
            ))
            result.postConcat(clarityMatrix)
        }

        // 8. Hue Shift (-180 to 180)
        if (adjustments.hueShift != 0f) {
            val hueDeg = (adjustments.hueShift * strength) * (Math.PI / 180.0)
            val cosV = cos(hueDeg).toFloat()
            val sinV = sin(hueDeg).toFloat()
            val lumR = 0.213f
            val lumG = 0.715f
            val lumB = 0.072f

            val hueMatrix = ColorMatrix(floatArrayOf(
                lumR + cosV * (1f - lumR) + sinV * (-lumR),
                lumG + cosV * (-lumG) + sinV * (-lumG),
                lumB + cosV * (-lumB) + sinV * (1f - lumB),
                0f, 0f,

                lumR + cosV * (-lumR) + sinV * 0.143f,
                lumG + cosV * (1f - lumG) + sinV * 0.140f,
                lumB + cosV * (-lumB) + sinV * (-0.283f),
                0f, 0f,

                lumR + cosV * (-lumR) + sinV * (-(1f - lumR)),
                lumG + cosV * (-lumG) + sinV * (lumG),
                lumB + cosV * (1f - lumB) + sinV * (lumB),
                0f, 0f,

                0f, 0f, 0f, 1f, 0f
            ))
            result.postConcat(hueMatrix)
        }

        return result
    }

    private fun calculateToneShift(hueDeg: Float, intensity: Float): Triple<Float, Float, Float> {
        if (intensity <= 0f) return Triple(0f, 0f, 0f)
        val rad = hueDeg * (Math.PI / 180.0)
        val cosV = cos(rad).toFloat()
        val sinV = sin(rad).toFloat()

        val rShift = (cosV * 0.8f + 0.2f) * intensity * 1.5f
        val gShift = (-0.5f * cosV + 0.866f * sinV) * intensity * 1.5f
        val bShift = (-0.5f * cosV - 0.866f * sinV) * intensity * 1.5f
        return Triple(rShift, gShift, bShift)
    }
}
