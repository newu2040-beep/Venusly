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

        // 6. Hue Shift (-180 to 180)
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
}
