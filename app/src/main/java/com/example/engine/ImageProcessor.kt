package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.Typeface
import com.example.model.AdjustmentValues
import com.example.model.AestheticFrame
import com.example.model.StickerOverlay
import com.example.model.TextOverlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Random

object ImageProcessor {

    suspend fun applyAdjustments(
        source: Bitmap,
        adjustments: AdjustmentValues,
        strength: Float = 1.0f,
        textOverlays: List<TextOverlay> = emptyList(),
        stickers: List<StickerOverlay> = emptyList()
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height

        // 1. Base transformation: Rotation & Flip
        var currentBitmap = applyTransformations(source, adjustments)

        // 2. Color / Tone adjustment via ColorMatrix
        val result = Bitmap.createBitmap(currentBitmap.width, currentBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val colorMatrix = ColorMatrixBuilder.buildColorMatrix(adjustments, strength)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(currentBitmap, 0f, 0f, paint)

        // 3. Glow / Bloom overlay
        if (adjustments.glow > 0f) {
            drawGlowEffect(canvas, result.width, result.height, adjustments.glow * strength)
        }

        // 4. Light Leak effect
        if (adjustments.lightLeak > 0f) {
            drawLightLeakEffect(canvas, result.width, result.height, adjustments.lightLeak * strength)
        }

        // 5. Film Grain effect
        if (adjustments.grain > 0f) {
            drawGrainEffect(canvas, result.width, result.height, adjustments.grain * strength)
        }

        // 6. Dust & Scratch effect
        if (adjustments.dustEffect > 0f) {
            drawDustEffect(canvas, result.width, result.height, adjustments.dustEffect * strength)
        }

        // 7. Vignette effect
        if (adjustments.vignette > 0f) {
            drawVignetteEffect(canvas, result.width, result.height, adjustments.vignette * strength)
        }

        // 8. Aesthetic Frames
        if (adjustments.frame != AestheticFrame.NONE) {
            drawAestheticFrame(canvas, result.width, result.height, adjustments.frame)
        }

        // 9. Text & Date stamp overlays
        for (overlay in textOverlays) {
            drawTextOverlay(canvas, result.width, result.height, overlay)
        }

        // 10. Sticker overlays
        for (sticker in stickers) {
            drawStickerOverlay(canvas, result.width, result.height, sticker)
        }

        result
    }

    private fun applyTransformations(source: Bitmap, adjustments: AdjustmentValues): Bitmap {
        val matrix = Matrix()
        var needsTransform = false

        if (adjustments.rotationDegrees != 0f) {
            matrix.postRotate(adjustments.rotationDegrees)
            needsTransform = true
        }
        if (adjustments.flipHorizontal) {
            matrix.postScale(-1f, 1f)
            needsTransform = true
        }
        if (adjustments.flipVertical) {
            matrix.postScale(1f, -1f)
            needsTransform = true
        }

        return if (needsTransform) {
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        } else {
            source
        }
    }

    private fun drawGlowEffect(canvas: Canvas, width: Int, height: Int, glowIntensity: Float) {
        val alpha = (glowIntensity * 1.5f).coerceIn(0f, 100f).toInt()
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.5f, height * 0.4f,
                (width.coerceAtLeast(height) * 0.65f),
                intArrayOf(
                    Color.argb(alpha, 255, 240, 245),
                    Color.argb((alpha * 0.4f).toInt(), 230, 240, 255),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), glowPaint)
    }

    private fun drawLightLeakEffect(canvas: Canvas, width: Int, height: Int, intensity: Float) {
        val alpha = (intensity * 1.8f).coerceIn(0f, 180f).toInt()
        val leakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width * 0.7f, height * 0.5f,
                intArrayOf(
                    Color.argb(alpha, 255, 160, 100),
                    Color.argb((alpha * 0.7f).toInt(), 255, 100, 150),
                    Color.argb((alpha * 0.2f).toInt(), 120, 200, 255),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.35f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), leakPaint)
    }

    private fun drawGrainEffect(canvas: Canvas, width: Int, height: Int, grainValue: Float) {
        val density = (grainValue * 25).toInt().coerceIn(100, 4000)
        val alpha = (grainValue * 1.2f).coerceIn(10f, 90f).toInt()
        val random = Random(42) // Consistent seed

        val grainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(alpha, 255, 255, 255)
            strokeWidth = (width / 700f).coerceAtLeast(1.2f)
            xfermode = PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
        }

        val darkGrainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb((alpha * 0.7f).toInt(), 10, 10, 10)
            strokeWidth = (width / 700f).coerceAtLeast(1.2f)
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DARKEN)
        }

        for (i in 0 until density) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            if (i % 2 == 0) {
                canvas.drawPoint(x, y, grainPaint)
            } else {
                canvas.drawPoint(x, y, darkGrainPaint)
            }
        }
    }

    private fun drawDustEffect(canvas: Canvas, width: Int, height: Int, dustValue: Float) {
        val count = (dustValue * 0.8f).toInt().coerceIn(5, 60)
        val random = Random(101)
        val dustPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(190, 255, 250, 240)
            strokeWidth = (width / 500f).coerceAtLeast(2f)
            style = Paint.Style.STROKE
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
        }

        for (i in 0 until count) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val length = (random.nextFloat() * 15f + 4f) * (width / 1000f).coerceAtLeast(1f)
            val angle = random.nextFloat() * 360f
            val rad = Math.toRadians(angle.toDouble())
            val x2 = x + (length * Math.cos(rad)).toFloat()
            val y2 = y + (length * Math.sin(rad)).toFloat()
            canvas.drawLine(x, y, x2, y2, dustPaint)

            // Random tiny speck
            val speckRadius = random.nextFloat() * (width / 600f) + 1f
            canvas.drawCircle(x, y, speckRadius, dustPaint)
        }
    }

    private fun drawVignetteEffect(canvas: Canvas, width: Int, height: Int, vignetteValue: Float) {
        val alpha = (vignetteValue * 2.2f).coerceIn(0f, 220f).toInt()
        val radius = (Math.hypot(width.toDouble(), height.toDouble()) / 2.0).toFloat()
        val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width / 2f, height / 2f, radius,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.argb((alpha * 0.3f).toInt(), 15, 15, 20),
                    Color.argb(alpha, 5, 5, 10)
                ),
                floatArrayOf(0.4f, 0.75f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignettePaint)
    }

    private fun drawTextOverlay(canvas: Canvas, width: Int, height: Int, overlay: TextOverlay) {
        val x = overlay.xPercent * width
        val y = overlay.yPercent * height

        val textSizePx = overlay.fontSizeSp * (width / 400f).coerceAtLeast(1.5f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = textSizePx
            textAlign = Paint.Align.CENTER
            typeface = if (overlay.isDateStamp) Typeface.MONOSPACE else Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = try {
                Color.parseColor(overlay.colorHex)
            } catch (e: Exception) {
                if (overlay.isDateStamp) Color.parseColor("#FF9800") else Color.WHITE
            }
        }

        if (overlay.hasBackgroundPill && !overlay.isDateStamp) {
            val bounds = Rect()
            paint.getTextBounds(overlay.text, 0, overlay.text.length, bounds)
            val padding = textSizePx * 0.4f
            val pillRect = RectF(
                x - bounds.width() / 2f - padding,
                y + bounds.top - padding * 0.6f,
                x + bounds.width() / 2f + padding,
                y + bounds.bottom + padding * 0.6f
            )
            val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(140, 20, 25, 35)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(pillRect, padding * 0.8f, padding * 0.8f, pillPaint)
        }

        // Draw text
        canvas.drawText(overlay.text, x, y, paint)
    }

    private fun drawStickerOverlay(canvas: Canvas, width: Int, height: Int, sticker: StickerOverlay) {
        val x = sticker.xPercent * width
        val y = sticker.yPercent * height
        val sizePx = sticker.sizeDp * (width / 400f).coerceAtLeast(1.5f)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = sizePx
            textAlign = Paint.Align.CENTER
        }
        canvas.save()
        canvas.rotate(sticker.rotation, x, y)
        canvas.drawText(sticker.symbol, x, y + sizePx * 0.35f, paint)
        canvas.restore()
    }

    private fun drawAestheticFrame(canvas: Canvas, width: Int, height: Int, frame: AestheticFrame) {
        val w = width.toFloat()
        val h = height.toFloat()

        when (frame) {
            AestheticFrame.NONE -> { /* No frame */ }

            AestheticFrame.POLAROID_WHITE -> {
                val borderLR = w * 0.055f
                val borderTop = h * 0.055f
                val borderBottom = h * 0.16f

                val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(250, 250, 248)
                    style = Paint.Style.FILL
                }
                // Left
                canvas.drawRect(0f, 0f, borderLR, h, framePaint)
                // Right
                canvas.drawRect(w - borderLR, 0f, w, h, framePaint)
                // Top
                canvas.drawRect(0f, 0f, w, borderTop, framePaint)
                // Bottom
                canvas.drawRect(0f, h - borderBottom, w, h, framePaint)

                // Inner hairline
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(40, 0, 0, 0)
                    style = Paint.Style.STROKE
                    strokeWidth = (w / 600f).coerceAtLeast(1.5f)
                }
                canvas.drawRect(borderLR, borderTop, w - borderLR, h - borderBottom, linePaint)

                // Polaroid classic imprint text
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(130, 80, 90, 100)
                    textSize = (w / 32f).coerceAtLeast(12f)
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("VENUSLY • INSTANT 600", w * 0.5f, h - (borderBottom * 0.4f), textPaint)
            }

            AestheticFrame.POLAROID_DARK -> {
                val borderLR = w * 0.055f
                val borderTop = h * 0.055f
                val borderBottom = h * 0.16f

                val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(24, 24, 27)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, borderLR, h, framePaint)
                canvas.drawRect(w - borderLR, 0f, w, h, framePaint)
                canvas.drawRect(0f, 0f, w, borderTop, framePaint)
                canvas.drawRect(0f, h - borderBottom, w, h, framePaint)

                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(40, 255, 255, 255)
                    style = Paint.Style.STROKE
                    strokeWidth = (w / 600f).coerceAtLeast(1.5f)
                }
                canvas.drawRect(borderLR, borderTop, w - borderLR, h - borderBottom, linePaint)

                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(217, 119, 6)
                    textSize = (w / 34f).coerceAtLeast(12f)
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("VENUSLY • NOIR 35MM", w * 0.5f, h - (borderBottom * 0.4f), textPaint)
            }

            AestheticFrame.FILM_35MM -> {
                val stripHeight = h * 0.11f
                val filmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(15, 15, 18)
                    style = Paint.Style.FILL
                }
                // Top bar
                canvas.drawRect(0f, 0f, w, stripHeight, filmPaint)
                // Bottom bar
                canvas.drawRect(0f, h - stripHeight, w, h, filmPaint)

                // Perforation holes
                val perfPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(220, 240, 242, 245)
                    style = Paint.Style.FILL
                }
                val perfBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(80, 0, 0, 0)
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                }

                val perfCount = 7
                val perfW = w / (perfCount * 2.2f)
                val perfH = stripHeight * 0.45f
                val stepX = w / perfCount

                for (i in 0 until perfCount) {
                    val cx = (i + 0.5f) * stepX
                    // Top hole
                    val topRect = RectF(cx - perfW / 2f, stripHeight * 0.28f, cx + perfW / 2f, stripHeight * 0.28f + perfH)
                    canvas.drawRoundRect(topRect, perfW * 0.25f, perfW * 0.25f, perfPaint)
                    canvas.drawRoundRect(topRect, perfW * 0.25f, perfW * 0.25f, perfBorder)

                    // Bottom hole
                    val botRect = RectF(cx - perfW / 2f, h - stripHeight + stripHeight * 0.28f, cx + perfW / 2f, h - stripHeight + stripHeight * 0.28f + perfH)
                    canvas.drawRoundRect(botRect, perfW * 0.25f, perfW * 0.25f, perfPaint)
                    canvas.drawRoundRect(botRect, perfW * 0.25f, perfW * 0.25f, perfBorder)
                }

                // Amber Film markings
                val amberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(245, 158, 11)
                    textSize = (stripHeight * 0.22f).coerceAtLeast(10f)
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                }
                canvas.drawText("▶ KODAK PORTRA 400 • 24A", w * 0.08f, stripHeight * 0.22f, amberPaint)
                canvas.drawText("ISO 400 • 35MM", w * 0.65f, stripHeight * 0.22f, amberPaint)
                canvas.drawText("▶ SAFETY FILM • 24", w * 0.08f, h - stripHeight * 0.1f, amberPaint)
                canvas.drawText("400-2", w * 0.75f, h - stripHeight * 0.1f, amberPaint)
            }

            AestheticFrame.PASTEL_AURA -> {
                val bw = w * 0.05f
                val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        0f, 0f, w, h,
                        intArrayOf(
                            Color.rgb(192, 132, 252), // Lilac
                            Color.rgb(96, 165, 250),  // Sky Blue
                            Color.rgb(251, 113, 133), // Pink
                            Color.rgb(251, 191, 36)   // Buttercup
                        ),
                        floatArrayOf(0f, 0.35f, 0.7f, 1f),
                        Shader.TileMode.CLAMP
                    )
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, bw, h, auraPaint)
                canvas.drawRect(w - bw, 0f, w, h, auraPaint)
                canvas.drawRect(0f, 0f, w, bw, auraPaint)
                canvas.drawRect(0f, h - bw, w, h, auraPaint)

                val innerLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(120, 255, 255, 255)
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                canvas.drawRect(bw, bw, w - bw, h - bw, innerLine)
            }

            AestheticFrame.CLEAN_MAT -> {
                val bw = w * 0.07f
                val bh = h * 0.07f
                val matPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(248, 248, 246)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, bw, h, matPaint)
                canvas.drawRect(w - bw, 0f, w, h, matPaint)
                canvas.drawRect(0f, 0f, w, bh, matPaint)
                canvas.drawRect(0f, h - bh, w, h, matPaint)

                val keylinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(203, 213, 225)
                    style = Paint.Style.STROKE
                    strokeWidth = (w / 700f).coerceAtLeast(1.5f)
                }
                val inset = bw * 0.3f
                canvas.drawRect(inset, inset, w - inset, h - inset, keylinePaint)
                canvas.drawRect(bw, bh, w - bw, h - bh, keylinePaint)
            }

            AestheticFrame.MINIMAL_KEYLINE -> {
                val margin1 = w * 0.035f
                val margin2 = w * 0.05f
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(255, 255, 255)
                    style = Paint.Style.STROKE
                    strokeWidth = (w / 600f).coerceAtLeast(1.5f)
                }
                canvas.drawRect(margin1, margin1, w - margin1, h - margin1, linePaint)
                canvas.drawRect(margin2, margin2, w - margin2, h - margin2, linePaint)
            }

            AestheticFrame.VINTAGE_STAMP -> {
                val bw = w * 0.065f
                val bh = h * 0.065f
                val stampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(252, 252, 250)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, bw, h, stampPaint)
                canvas.drawRect(w - bw, 0f, w, h, stampPaint)
                canvas.drawRect(0f, 0f, w, bh, stampPaint)
                canvas.drawRect(0f, h - bh, w, h, stampPaint)

                // Perforated edge circles
                val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(240, 240, 240)
                    style = Paint.Style.FILL
                }
                val radius = (bw * 0.35f).coerceAtLeast(6f)
                val step = radius * 2.8f

                // Top & Bottom edges
                var curX = radius
                while (curX < w) {
                    canvas.drawCircle(curX, 0f, radius, holePaint)
                    canvas.drawCircle(curX, h, radius, holePaint)
                    curX += step
                }
                // Left & Right edges
                var curY = radius
                while (curY < h) {
                    canvas.drawCircle(0f, curY, radius, holePaint)
                    canvas.drawCircle(w, curY, radius, holePaint)
                    curY += step
                }
            }

            AestheticFrame.PASTEL_CARD -> {
                val bw = w * 0.055f
                val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(224, 237, 253) // Signature pastel container
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, bw, h, cardPaint)
                canvas.drawRect(w - bw, 0f, w, h, cardPaint)
                canvas.drawRect(0f, 0f, w, bw, cardPaint)
                canvas.drawRect(0f, h - bw, w, h, cardPaint)

                val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(37, 99, 235)
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                canvas.drawRoundRect(RectF(bw, bw, w - bw, h - bw), 24f, 24f, innerPaint)
            }

            AestheticFrame.RETRO_TV -> {
                val bw = w * 0.06f
                val bh = h * 0.06f
                val bezelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(20, 20, 25)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, bw, h, bezelPaint)
                canvas.drawRect(w - bw, 0f, w, h, bezelPaint)
                canvas.drawRect(0f, 0f, w, bh, bezelPaint)
                canvas.drawRect(0f, h - bh, w, h, bezelPaint)

                // Corner rounded bezel masks
                val cornerRadius = bw * 1.8f
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(100, 255, 255, 255)
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                canvas.drawRoundRect(RectF(bw, bh, w - bw, h - bh), cornerRadius, cornerRadius, linePaint)
            }
        }
    }
}
