package com.example.engine

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.media.FaceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SmartCropEngine {

    /**
     * Finds the subject center of mass / face centroid normalized to 0.0f..1.0f.
     */
    suspend fun detectSubjectCentroid(bitmap: Bitmap): Pair<Float, Float> = withContext(Dispatchers.Default) {
        try {
            // 1. Try Android Native Face Detector
            // FaceDetector requires even width & RGB_565 configuration
            val targetW = 512
            val targetH = (512f * (bitmap.height.toFloat() / bitmap.width.toFloat())).toInt().let { if (it % 2 != 0) it + 1 else it }

            val scaledBmp = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            val rgb565Bmp = scaledBmp.copy(Bitmap.Config.RGB_565, false)

            val maxFaces = 5
            val faces = Array<FaceDetector.Face?>(maxFaces) { null }
            val detector = FaceDetector(targetW, targetH, maxFaces)
            val numFaces = detector.findFaces(rgb565Bmp, faces)

            if (numFaces > 0) {
                var totalWeight = 0f
                var weightedX = 0f
                var weightedY = 0f

                val point = android.graphics.PointF()
                for (i in 0 until numFaces) {
                    val face = faces[i] ?: continue
                    face.getMidPoint(point)
                    val eyeDistance = face.eyesDistance()
                    val weight = eyeDistance * eyeDistance

                    // Position eyes slightly above mid-face for portrait headroom (38% offset rule)
                    val faceX = point.x / targetW.toFloat()
                    val faceY = (point.y - eyeDistance * 0.2f) / targetH.toFloat()

                    weightedX += faceX * weight
                    weightedY += faceY * weight
                    totalWeight += weight
                }

                if (totalWeight > 0f) {
                    val finalX = (weightedX / totalWeight).coerceIn(0.15f, 0.85f)
                    val finalY = (weightedY / totalWeight).coerceIn(0.15f, 0.85f)
                    return@withContext Pair(finalX, finalY)
                }
            }

            // 2. Fallback: Saliency / High Contrast Spatial Energy Centroid (for non-face portraits, animals, products)
            val grid = 32
            val sampled = Bitmap.createScaledBitmap(bitmap, grid, grid, false)
            var sumX = 0f
            var sumY = 0f
            var totalEnergy = 0f

            val pixels = IntArray(grid * grid)
            sampled.getPixels(pixels, 0, grid, 0, 0, grid, grid)

            for (y in 1 until grid - 1) {
                for (x in 1 until grid - 1) {
                    val idx = y * grid + x
                    val c = pixels[idx]
                    val r = (c shr 16) and 0xFF
                    val g = (c shr 8) and 0xFF
                    val b = c and 0xFF

                    // Local gradient magnitude (edge sharpness) + chroma variance
                    val rightC = pixels[idx + 1]
                    val rRight = (rightC shr 16) and 0xFF
                    val bottomC = pixels[idx + grid]
                    val rBottom = (bottomC shr 16) and 0xFF

                    val dx = kotlin.math.abs(r - rRight)
                    val dy = kotlin.math.abs(r - rBottom)
                    val chromaVar = kotlin.math.abs(r - g) + kotlin.math.abs(g - b)
                    val energy = (dx + dy + chromaVar).toFloat()

                    val normX = x.toFloat() / grid
                    val normY = y.toFloat() / grid

                    sumX += normX * energy
                    sumY += normY * energy
                    totalEnergy += energy
                }
            }

            if (totalEnergy > 0f) {
                val cx = (sumX / totalEnergy).coerceIn(0.2f, 0.8f)
                val cy = (sumY / totalEnergy).coerceIn(0.2f, 0.8f)
                Pair(cx, cy)
            } else {
                Pair(0.5f, 0.5f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(0.5f, 0.5f)
        }
    }

    /**
     * Calculates optimal crop bounds centered around subject centroid matching desired ratio (width / height).
     */
    fun computeCropBounds(
        imgWidth: Int,
        imgHeight: Int,
        targetRatio: Float,
        centerXPercent: Float = 0.5f,
        centerYPercent: Float = 0.5f
    ): Rect {
        val srcRatio = imgWidth.toFloat() / imgHeight.toFloat()

        val cropWidth: Int
        val cropHeight: Int

        if (srcRatio > targetRatio) {
            // Source is wider than target ratio -> constrained by height
            cropHeight = imgHeight
            cropWidth = (imgHeight * targetRatio).toInt().coerceAtMost(imgWidth)
        } else {
            // Source is taller than target ratio -> constrained by width
            cropWidth = imgWidth
            cropHeight = (imgWidth / targetRatio).toInt().coerceAtMost(imgHeight)
        }

        // Center around subject position
        val desiredLeft = (centerXPercent * imgWidth - cropWidth / 2f).toInt()
        val desiredTop = (centerYPercent * imgHeight - cropHeight / 2f).toInt()

        val maxLeft = imgWidth - cropWidth
        val maxTop = imgHeight - cropHeight

        val left = desiredLeft.coerceIn(0, maxLeft.coerceAtLeast(0))
        val top = desiredTop.coerceIn(0, maxTop.coerceAtLeast(0))

        return Rect(left, top, left + cropWidth, top + cropHeight)
    }

    /**
     * Smart auto-crops bitmap centered on portrait subject.
     */
    suspend fun autoCropSubject(bitmap: Bitmap, targetRatio: Float): Bitmap = withContext(Dispatchers.Default) {
        val (cx, cy) = detectSubjectCentroid(bitmap)
        val bounds = computeCropBounds(bitmap.width, bitmap.height, targetRatio, cx, cy)
        val safeLeft = bounds.left.coerceIn(0, (bitmap.width - 1).coerceAtLeast(0))
        val safeTop = bounds.top.coerceIn(0, (bitmap.height - 1).coerceAtLeast(0))
        val safeWidth = bounds.width().coerceIn(1, bitmap.width - safeLeft)
        val safeHeight = bounds.height().coerceIn(1, bitmap.height - safeTop)
        Bitmap.createBitmap(bitmap, safeLeft, safeTop, safeWidth, safeHeight)
    }
}
