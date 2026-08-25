package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
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
import com.example.model.LayerItem
import com.example.model.LayerType
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
        stickers: List<StickerOverlay> = emptyList(),
        layers: List<LayerItem> = emptyList()
    ): Bitmap = withContext(Dispatchers.Default) {
        try {
            val width = source.width
            val height = source.height

            val baseLayer = layers.find { it.type == LayerType.BASE_IMAGE }
            val adjLayer = layers.find { it.type == LayerType.ADJUSTMENTS }
            val grainLayer = layers.find { it.type == LayerType.GRAIN_LIGHT_LEAK }
            val frameLayer = layers.find { it.type == LayerType.FRAME }

            val isBaseVisible = baseLayer?.isVisible ?: true
            val isAdjVisible = adjLayer?.isVisible ?: true
            val adjOpacity = (adjLayer?.opacity ?: 1.0f) * strength
            val isGrainVisible = grainLayer?.isVisible ?: true
            val grainOpacity = (grainLayer?.opacity ?: 1.0f) * strength
            val isFrameVisible = frameLayer?.isVisible ?: true
            val frameOpacity = (frameLayer?.opacity ?: 1.0f) * strength

            // 1. Base transformation: Rotation & Flip
            var currentBitmap = if (isBaseVisible) {
                applyTransformations(source, adjustments)
            } else {
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            }

            // 1b. Noise Reduction filter pass
            if (adjustments.noiseReduction > 0f && isAdjVisible) {
                currentBitmap = applyNoiseReduction(currentBitmap, adjustments.noiseReduction * adjOpacity)
            }

            // 2. Color / Tone adjustment via ColorMatrix
            val result = Bitmap.createBitmap(currentBitmap.width, currentBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            if (isAdjVisible && adjOpacity > 0f) {
                val colorMatrix = ColorMatrixBuilder.buildColorMatrix(adjustments, adjOpacity)
                paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            }
            canvas.drawBitmap(currentBitmap, 0f, 0f, paint)

            // 3. Glow / Bloom overlay
            if (adjustments.glow > 0f && isGrainVisible && grainOpacity > 0f) {
                drawGlowEffect(canvas, result.width, result.height, adjustments.glow * grainOpacity)
            }

            // 4. Light Leak effect
            if (adjustments.lightLeak > 0f && isGrainVisible && grainOpacity > 0f) {
                drawLightLeakEffect(canvas, result.width, result.height, adjustments.lightLeak * grainOpacity)
            }

            // 5. Film Grain effect
            if (adjustments.grain > 0f && isGrainVisible && grainOpacity > 0f) {
                drawGrainEffect(canvas, result.width, result.height, adjustments.grain * grainOpacity)
            }

            // 6. Dust & Scratch effect
            if (adjustments.dustEffect > 0f && isGrainVisible && grainOpacity > 0f) {
                drawDustEffect(canvas, result.width, result.height, adjustments.dustEffect * grainOpacity)
            }

            // 7. Vignette effect
            if (adjustments.vignette > 0f && isGrainVisible && grainOpacity > 0f) {
                drawVignetteEffect(canvas, result.width, result.height, adjustments.vignette * grainOpacity)
            }

            // 8a. Custom Photo Corner Rounding applied ONLY to imported photo/image layer
            var photoLayerClipped = result
            if (adjustments.photoCornerRadius > 0f && isFrameVisible && frameOpacity > 0f) {
                photoLayerClipped = clipPhotoLayerCorners(photoLayerClipped, adjustments.photoCornerRadius * frameOpacity)
            }

            // 8b. Embed photo layer inside Matte Frame Container if enabled
            var framedResult = photoLayerClipped
            if (adjustments.frameMatteWidth > 0f && isFrameVisible && frameOpacity > 0f) {
                framedResult = applyMatteFrameContainer(
                    photoLayer = photoLayerClipped,
                    photoCornerRadius = adjustments.photoCornerRadius * frameOpacity,
                    matteWidth = adjustments.frameMatteWidth * frameOpacity,
                    matteColorLong = adjustments.frameMatteColor
                )
            }

            // 8c. Aesthetic Frames drawn ON TOP (Polaroid, Film, Vintage, etc.)
            val finalCanvas = Canvas(framedResult)
            if (adjustments.frame != AestheticFrame.NONE && isFrameVisible && frameOpacity > 0f) {
                drawAestheticFrame(finalCanvas, framedResult.width, framedResult.height, adjustments.frame)
            }

            var finalResult = framedResult

            // 9. Overlays according to explicit layer stack order or default order
            val overlayLayers = layers.filter { it.type == LayerType.STICKER || it.type == LayerType.TEXT_OVERLAY }
            if (overlayLayers.isNotEmpty()) {
                for (l in overlayLayers) {
                    if (!l.isVisible || l.opacity <= 0f) continue
                    if (l.type == LayerType.TEXT_OVERLAY) {
                        val overlay = textOverlays.find { it.id == l.associatedId }
                        if (overlay != null) {
                            drawTextOverlay(finalCanvas, finalResult.width, finalResult.height, overlay)
                        }
                    } else if (l.type == LayerType.STICKER) {
                        val sticker = stickers.find { it.id == l.associatedId }
                        if (sticker != null) {
                            val modifiedSticker = sticker.copy(alpha = sticker.alpha * l.opacity)
                            drawStickerOverlay(finalCanvas, finalResult.width, finalResult.height, modifiedSticker)
                        }
                    }
                }
            } else {
                // Default fallbacks if layers list is empty
                for (overlay in textOverlays) {
                    drawTextOverlay(finalCanvas, finalResult.width, finalResult.height, overlay)
                }
                for (sticker in stickers) {
                    drawStickerOverlay(finalCanvas, finalResult.width, finalResult.height, sticker)
                }
            }

            finalResult
        } catch (e: Throwable) {
            e.printStackTrace()
            source
        }
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

    private fun getPorterDuffMode(blendMode: String): PorterDuff.Mode {
        return when (blendMode.uppercase()) {
            "MULTIPLY" -> PorterDuff.Mode.MULTIPLY
            "SCREEN" -> PorterDuff.Mode.SCREEN
            "OVERLAY" -> PorterDuff.Mode.OVERLAY
            "DARKEN" -> PorterDuff.Mode.DARKEN
            "LIGHTEN" -> PorterDuff.Mode.LIGHTEN
            "COLOR_DODGE", "COLORDODGE", "ADD" -> PorterDuff.Mode.ADD
            "XOR" -> PorterDuff.Mode.XOR
            else -> PorterDuff.Mode.SRC_OVER
        }
    }

    private fun drawTextOverlay(canvas: Canvas, width: Int, height: Int, overlay: TextOverlay) {
        val x = overlay.xPercent * width
        val y = overlay.yPercent * height

        val textSizePx = overlay.fontSizeSp * (width / 400f).coerceAtLeast(1.5f)
        val selectedTypeface = when (overlay.fontStyle.uppercase()) {
            "SERIF" -> Typeface.SERIF
            "SANS", "SANSSERIF" -> Typeface.SANS_SERIF
            "MONOSPACE" -> Typeface.MONOSPACE
            "CURSIVE" -> Typeface.create("cursive", Typeface.BOLD)
            "DISPLAYBOLD", "BOLD" -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            else -> if (overlay.isDateStamp) Typeface.MONOSPACE else Typeface.SERIF
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = textSizePx
            textAlign = Paint.Align.CENTER
            typeface = selectedTypeface
            color = try {
                Color.parseColor(overlay.colorHex)
            } catch (e: Exception) {
                if (overlay.isDateStamp) Color.parseColor("#FF9500") else Color.WHITE
            }
            if (overlay.blendMode != "Normal") {
                xfermode = PorterDuffXfermode(getPorterDuffMode(overlay.blendMode))
            }
        }

        canvas.save()
        canvas.rotate(overlay.rotation, x, y)

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
                color = Color.argb(150, 20, 25, 35)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(pillRect, padding * 0.8f, padding * 0.8f, pillPaint)
        }

        // Draw text
        canvas.drawText(overlay.text, x, y, paint)
        canvas.restore()
    }

    private fun drawStickerOverlay(canvas: Canvas, width: Int, height: Int, sticker: StickerOverlay) {
        val x = sticker.xPercent * width
        val y = sticker.yPercent * height
        val sizePx = sticker.sizeDp * (width / 400f).coerceAtLeast(1.5f)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = sizePx
            textAlign = Paint.Align.CENTER
            alpha = (sticker.alpha.coerceIn(0f, 1f) * 255).toInt()
            if (sticker.blendMode != "Normal") {
                xfermode = PorterDuffXfermode(getPorterDuffMode(sticker.blendMode))
            }
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

            AestheticFrame.POLAROID_PASTEL -> {
                val borderLR = w * 0.055f
                val borderTop = h * 0.055f
                val borderBottom = h * 0.16f

                val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(252, 231, 243) // Soft Sakura blush
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, borderLR, h, framePaint)
                canvas.drawRect(w - borderLR, 0f, w, h, framePaint)
                canvas.drawRect(0f, 0f, w, borderTop, framePaint)
                canvas.drawRect(0f, h - borderBottom, w, h, framePaint)

                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(50, 225, 29, 72)
                    style = Paint.Style.STROKE
                    strokeWidth = (w / 600f).coerceAtLeast(1.5f)
                }
                canvas.drawRect(borderLR, borderTop, w - borderLR, h - borderBottom, linePaint)

                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(225, 29, 72)
                    textSize = (w / 32f).coerceAtLeast(12f)
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("🌸 VENUSLY • PASTEL BLOOM", w * 0.5f, h - (borderBottom * 0.4f), textPaint)
            }

            AestheticFrame.DIGICAM_OSD -> {
                val barH = h * 0.065f
                val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(140, 0, 0, 0)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, w, barH, overlayPaint)
                canvas.drawRect(0f, h - barH, w, h, overlayPaint)

                val osdText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(255, 255, 255)
                    textSize = (barH * 0.42f).coerceAtLeast(11f)
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                }
                val redRec = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(239, 68, 68)
                    textSize = (barH * 0.42f).coerceAtLeast(11f)
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                }
                val amberText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(245, 158, 11)
                    textSize = (barH * 0.42f).coerceAtLeast(11f)
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                }

                // Top OSD status
                canvas.drawText("● REC", w * 0.04f, barH * 0.65f, redRec)
                canvas.drawText("HQ 4K", w * 0.28f, barH * 0.65f, osdText)
                canvas.drawText("ISO 100", w * 0.52f, barH * 0.65f, osdText)
                canvas.drawText("[■■■]", w * 0.82f, barH * 0.65f, osdText)

                // Bottom OSD status
                canvas.drawText("F2.8  1/250s  +0.3EV", w * 0.04f, h - barH * 0.35f, osdText)
                canvas.drawText("'04 07 28", w * 0.72f, h - barH * 0.35f, amberText)

                // Center Autofocus bracket
                val focusLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(120, 255, 255, 255)
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                val cx = w * 0.5f
                val cy = h * 0.5f
                val size = w * 0.08f
                canvas.drawRect(cx - size, cy - size, cx + size, cy + size, focusLine)
                canvas.drawLine(cx - size * 0.4f, cy, cx + size * 0.4f, cy, focusLine)
                canvas.drawLine(cx, cy - size * 0.4f, cx, cy + size * 0.4f, focusLine)
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

            AestheticFrame.Y2K_STICKER_FRAME -> {
                val bw = w * 0.045f
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(254, 242, 242)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, bw, h, bgPaint)
                canvas.drawRect(w - bw, 0f, w, h, bgPaint)
                canvas.drawRect(0f, 0f, w, bw, bgPaint)
                canvas.drawRect(0f, h - bw, w, h, bgPaint)

                // Washi tape corners
                val tapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(200, 244, 114, 182)
                    style = Paint.Style.FILL
                }
                val tapeSize = w * 0.10f
                val tapeH = w * 0.035f

                // Top left tape
                canvas.save()
                canvas.rotate(-45f, bw * 1.2f, bw * 1.2f)
                canvas.drawRect(bw * 0.5f, bw * 0.8f, bw * 0.5f + tapeSize, bw * 0.8f + tapeH, tapePaint)
                canvas.restore()

                // Top right tape
                canvas.save()
                canvas.rotate(45f, w - bw * 1.2f, bw * 1.2f)
                canvas.drawRect(w - bw * 1.5f - tapeSize, bw * 0.8f, w - bw * 1.5f, bw * 0.8f + tapeH, tapePaint)
                canvas.restore()

                // Bottom text
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(219, 39, 119)
                    textSize = (w / 36f).coerceAtLeast(11f)
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("★ 2000s Y2K SCRAPBOOK ★", w * 0.5f, h - (bw * 0.3f), textPaint)
            }

            AestheticFrame.NEON_CYBER_BORDER -> {
                val line1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(217, 70, 239) // Neon Magenta
                    style = Paint.Style.STROKE
                    strokeWidth = (w / 200f).coerceAtLeast(3f)
                }
                val line2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(6, 182, 212) // Electric Cyan
                    style = Paint.Style.STROKE
                    strokeWidth = (w / 400f).coerceAtLeast(1.5f)
                }
                val m1 = w * 0.025f
                val m2 = w * 0.045f
                canvas.drawRect(m1, m1, w - m1, h - m1, line1)
                canvas.drawRect(m2, m2, w - m2, h - m2, line2)

                // Corner crosshairs
                val crossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                val cl = w * 0.03f
                // Top-left
                canvas.drawLine(m1, m1, m1 + cl, m1, crossPaint)
                canvas.drawLine(m1, m1, m1, m1 + cl, crossPaint)
                // Top-right
                canvas.drawLine(w - m1, m1, w - m1 - cl, m1, crossPaint)
                canvas.drawLine(w - m1, m1, w - m1, m1 + cl, crossPaint)
                // Bottom-left
                canvas.drawLine(m1, h - m1, m1 + cl, h - m1, crossPaint)
                canvas.drawLine(m1, h - m1, m1, h - m1 - cl, crossPaint)
                // Bottom-right
                canvas.drawLine(w - m1, h - m1, w - m1 - cl, h - m1, crossPaint)
                canvas.drawLine(w - m1, h - m1, w - m1, h - m1 - cl, crossPaint)
            }

            AestheticFrame.SCALLOPED_LACE -> {
                val bw = w * 0.05f
                val bh = h * 0.05f
                val lacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(255, 241, 242)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, bw, h, lacePaint)
                canvas.drawRect(w - bw, 0f, w, h, lacePaint)
                canvas.drawRect(0f, 0f, w, bh, lacePaint)
                canvas.drawRect(0f, h - bh, w, h, lacePaint)

                val scallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(251, 113, 133)
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                val radius = bw * 0.5f
                var curX = bw + radius
                while (curX < w - bw) {
                    canvas.drawArc(RectF(curX - radius, bh - radius, curX + radius, bh + radius), 0f, 180f, false, scallPaint)
                    canvas.drawArc(RectF(curX - radius, h - bh - radius, curX + radius, h - bh + radius), 180f, 180f, false, scallPaint)
                    curX += radius * 2
                }
            }

            AestheticFrame.VINTAGE_STAMP -> {
                val bw = w * 0.06f
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(248, 246, 240)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, bw, h, borderPaint)
                canvas.drawRect(w - bw, 0f, w, h, borderPaint)
                canvas.drawRect(0f, 0f, w, bw, borderPaint)
                canvas.drawRect(0f, h - bw, w, h, borderPaint)

                val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.TRANSPARENT
                    xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
                }
                val step = w * 0.05f
                val r = step * 0.28f
                var x = step * 0.5f
                while (x < w) {
                    canvas.drawCircle(x, 0f, r, holePaint)
                    canvas.drawCircle(x, h, r, holePaint)
                    x += step
                }
                var y = step * 0.5f
                while (y < h) {
                    canvas.drawCircle(0f, y, r, holePaint)
                    canvas.drawCircle(w, y, r, holePaint)
                    y += step
                }

                val watermark = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(60, 180, 80, 50)
                    textSize = (w / 32f).coerceAtLeast(10f)
                    typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                }
                canvas.drawText("PAR AVION • VENUSLY POST", w * 0.1f, h - bw * 0.35f, watermark)
            }

            AestheticFrame.FILM_SLIDE_MOUNT -> {
                val borderLR = w * 0.08f
                val borderTop = h * 0.08f
                val borderBottom = h * 0.14f

                val slidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(240, 238, 230)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, borderLR, h, slidePaint)
                canvas.drawRect(w - borderLR, 0f, w, h, slidePaint)
                canvas.drawRect(0f, 0f, w, borderTop, slidePaint)
                canvas.drawRect(0f, h - borderBottom, w, h, slidePaint)

                val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(220, 38, 38)
                    textSize = (w / 30f).coerceAtLeast(11f)
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                canvas.drawText("KODACHROME", borderLR, borderTop * 0.65f, labelPaint)

                val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(71, 85, 105)
                    textSize = (w / 38f).coerceAtLeast(10f)
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                }
                canvas.drawText("SLIDE #24 • PROCESSED BY VENUSLY", borderLR, h - (borderBottom * 0.45f), subPaint)
            }

            AestheticFrame.GOLD_GLITTER_BORDER -> {
                val bw = w * 0.045f
                val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(251, 191, 36)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, bw, h, goldPaint)
                canvas.drawRect(w - bw, 0f, w, h, goldPaint)
                canvas.drawRect(0f, 0f, w, bw, goldPaint)
                canvas.drawRect(0f, h - bw, w, h, goldPaint)

                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(180, 83, 9)
                    style = Paint.Style.STROKE
                    strokeWidth = (w / 300f).coerceAtLeast(2f)
                }
                canvas.drawRect(bw * 0.5f, bw * 0.5f, w - bw * 0.5f, h - bw * 0.5f, linePaint)
            }

            AestheticFrame.FLORAL_PASTEL_RIBBON -> {
                val bw = w * 0.04f
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(253, 242, 248)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, bw, h, bgPaint)
                canvas.drawRect(w - bw, 0f, w, h, bgPaint)
                canvas.drawRect(0f, 0f, w, bw, bgPaint)
                canvas.drawRect(0f, h - bw, w, h, bgPaint)

                val ribbonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(244, 114, 182)
                    textSize = (w / 22f).coerceAtLeast(14f)
                }
                canvas.drawText("🎀", bw * 0.5f, bw * 2.2f, ribbonPaint)
                canvas.drawText("🎀", w - bw * 2.5f, bw * 2.2f, ribbonPaint)
            }

            AestheticFrame.PAPER_TEAR_SCRAPBOOK -> {
                val bw = w * 0.05f
                val paperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(254, 252, 232)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, bw, h, paperPaint)
                canvas.drawRect(w - bw, 0f, w, h, paperPaint)
                canvas.drawRect(0f, 0f, w, bw, paperPaint)
                canvas.drawRect(0f, h - bw, w, h, paperPaint)

                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(80, 161, 98, 7)
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                canvas.drawRect(bw, bw, w - bw, h - bw, strokePaint)
            }
        }
    }

    private fun applyNoiseReduction(source: Bitmap, intensity: Float): Bitmap {
        if (intensity <= 0f) return source
        return try {
            val width = source.width
            val height = source.height

            // Max scale limit to prevent OOM on high-res photos
            val maxDim = 800
            val scale = if (width > maxDim || height > maxDim) {
                maxDim.toFloat() / Math.max(width, height)
            } else 1.0f

            val workBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(source, (width * scale).toInt().coerceAtLeast(1), (height * scale).toInt().coerceAtLeast(1), true)
            } else {
                source
            }

            val w = workBitmap.width
            val h = workBitmap.height
            val denoised = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

            val norm = (intensity / 100f).coerceIn(0f, 1f)
            val threshold = 12f + norm * 50f
            val smoothingFactor = norm * 0.85f

            val pixels = IntArray(w * h)
            val resultPixels = IntArray(w * h)
            workBitmap.getPixels(pixels, 0, w, 0, 0, w, h)

            val inv2ThresholdSq = -1f / (2f * threshold * threshold)

            for (y in 0 until h) {
                val yMin = (y - 1).coerceAtLeast(0)
                val yMax = (y + 1).coerceAtMost(h - 1)
                for (x in 0 until w) {
                    val centerIdx = y * w + x
                    val centerPixel = pixels[centerIdx]
                    val cA = (centerPixel shr 24) and 0xFF
                    val cR = (centerPixel shr 16) and 0xFF
                    val cG = (centerPixel shr 8) and 0xFF
                    val cB = centerPixel and 0xFF

                    var sumR = 0f
                    var sumG = 0f
                    var sumB = 0f
                    var totalWeight = 0f

                    val xMin = (x - 1).coerceAtLeast(0)
                    val xMax = (x + 1).coerceAtMost(w - 1)

                    for (ny in yMin..yMax) {
                        val rowIdx = ny * w
                        for (nx in xMin..xMax) {
                            val p = pixels[rowIdx + nx]
                            val pR = (p shr 16) and 0xFF
                            val pG = (p shr 8) and 0xFF
                            val pB = p and 0xFF

                            val diffR = (pR - cR).toFloat()
                            val diffG = (pG - cG).toFloat()
                            val diffB = (pB - cB).toFloat()
                            val colorDiffSq = diffR * diffR + diffG * diffG + diffB * diffB

                            val rangeWeight = Math.exp((colorDiffSq * inv2ThresholdSq).toDouble()).toFloat()

                            sumR += pR * rangeWeight
                            sumG += pG * rangeWeight
                            sumB += pB * rangeWeight
                            totalWeight += rangeWeight
                        }
                    }

                    if (totalWeight > 0f) {
                        val smoothR = sumR / totalWeight
                        val smoothG = sumG / totalWeight
                        val smoothB = sumB / totalWeight

                        val finalR = (cR * (1f - smoothingFactor) + smoothR * smoothingFactor).toInt().coerceIn(0, 255)
                        val finalG = (cG * (1f - smoothingFactor) + smoothG * smoothingFactor).toInt().coerceIn(0, 255)
                        val finalB = (cB * (1f - smoothingFactor) + smoothB * smoothingFactor).toInt().coerceIn(0, 255)

                        resultPixels[centerIdx] = (cA shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
                    } else {
                        resultPixels[centerIdx] = centerPixel
                    }
                }
            }

            denoised.setPixels(resultPixels, 0, w, 0, 0, w, h)
            if (scale < 1.0f) {
                Bitmap.createScaledBitmap(denoised, width, height, true)
            } else {
                denoised
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            source
        }
    }

    /**
     * Clips ONLY the imported photo image layer with rounded corners, leaving
     * outer frames, borders, and containers completely untouched.
     */
    private fun clipPhotoLayerCorners(
        photoBitmap: Bitmap,
        cornerRadius: Float
    ): Bitmap {
        if (cornerRadius <= 0f) return photoBitmap

        val srcW = photoBitmap.width
        val srcH = photoBitmap.height
        val minDim = Math.min(srcW, srcH).toFloat()

        val normRadius = (cornerRadius / 100f).coerceIn(0f, 1f)
        val rx = (minDim / 2f) * normRadius

        if (rx <= 0f) return photoBitmap

        val output = Bitmap.createBitmap(srcW, srcH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val photoRect = RectF(0f, 0f, srcW.toFloat(), srcH.toFloat())
        val clipPath = Path().apply {
            addRoundRect(photoRect, rx, rx, Path.Direction.CW)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        canvas.save()
        canvas.clipPath(clipPath)
        canvas.drawBitmap(photoBitmap, 0f, 0f, paint)
        canvas.restore()

        return output
    }

    /**
     * Embeds the photo layer inside a matte frame container if matteWidth > 0.
     * The outer container corners remain square/rectangular, preserving shape,
     * size, shadow, and spacing of the matte border.
     */
    private fun applyMatteFrameContainer(
        photoLayer: Bitmap,
        photoCornerRadius: Float,
        matteWidth: Float,
        matteColorLong: Long
    ): Bitmap {
        if (matteWidth <= 0f) return photoLayer

        val srcW = photoLayer.width
        val srcH = photoLayer.height
        val minDim = Math.min(srcW, srcH).toFloat()

        val normMatte = (matteWidth / 100f).coerceIn(0f, 0.40f)
        val paddingPx = (minDim * 0.20f * normMatte).toInt()

        if (paddingPx <= 0) return photoLayer

        val totalW = srcW + paddingPx * 2
        val totalH = srcH + paddingPx * 2

        val output = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val matteColorInt = matteColorLong.toInt()
        val isTransparentMatte = (matteColorInt and 0xFF000000.toInt()) == 0

        // 1. Solid rectangular background for outer matte container (corners remain square, shape & size preserved)
        if (!isTransparentMatte) {
            canvas.drawColor(matteColorInt)

            // Soft drop shadow matching photo layer outline
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                alpha = 45
            }
            val normRadius = (photoCornerRadius / 100f).coerceIn(0f, 1f)
            val rx = (minDim / 2f) * normRadius
            val shadowRect = RectF(
                paddingPx.toFloat() - 2f,
                paddingPx.toFloat() + 2f,
                (paddingPx + srcW).toFloat() + 2f,
                (paddingPx + srcH).toFloat() + 4f
            )
            canvas.drawRoundRect(shadowRect, rx, rx, shadowPaint)
        }

        // 2. Draw photo layer (already clipped by photoCornerRadius) into photoRect
        val photoRect = RectF(
            paddingPx.toFloat(),
            paddingPx.toFloat(),
            (paddingPx + srcW).toFloat(),
            (paddingPx + srcH).toFloat()
        )

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(photoLayer, paddingPx.toFloat(), paddingPx.toFloat(), paint)

        // 3. Inner stroke around photo cutout
        val normRadius = (photoCornerRadius / 100f).coerceIn(0f, 1f)
        val rx = (minDim / 2f) * normRadius
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = Math.max(2f, minDim * 0.0035f)
            color = if (isTransparentMatte) Color.WHITE else Color.argb(45, 0, 0, 0)
        }
        canvas.drawRoundRect(photoRect, rx, rx, strokePaint)

        return output
    }
}
