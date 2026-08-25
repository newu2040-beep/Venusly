package com.example.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object AiImageEngine {

    private const val MODEL_PRIMARY = "gemini-3.1-flash-image-preview"
    private const val MODEL_FALLBACK = "gemini-2.5-flash-image"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun bitmapToBase64(bitmap: Bitmap, maxDimension: Int = 1024): String {
        val scaled = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val (w, h) = if (bitmap.width >= bitmap.height) {
                maxDimension to (maxDimension / ratio).toInt()
            } else {
                (maxDimension * ratio).toInt() to maxDimension
            }
            Bitmap.createScaledBitmap(bitmap, w, h, true)
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    suspend fun generateOrEditImage(
        prompt: String,
        sourceBitmap: Bitmap? = null,
        aspectRatio: String = "1:1",
        providedApiKey: String? = null
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        val apiKey = providedApiKey?.takeIf { it.isNotBlank() } ?: try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is missing. Please set your key in the AI Gen tab or in the Secrets panel.")
            )
        }

        // Try primary model first, fallback if needed
        val primaryResult = executeGeminiRequest(MODEL_PRIMARY, apiKey, prompt, sourceBitmap, aspectRatio)
        if (primaryResult.isSuccess) {
            return@withContext primaryResult
        }

        // Fallback to gemini-2.5-flash-image
        executeGeminiRequest(MODEL_FALLBACK, apiKey, prompt, sourceBitmap, aspectRatio)
    }

    private fun executeGeminiRequest(
        model: String,
        apiKey: String,
        prompt: String,
        sourceBitmap: Bitmap?,
        aspectRatio: String
    ): Result<Bitmap> {
        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val partsArray = JSONArray()

            // 1. Text prompt
            val textPart = JSONObject().apply {
                put("text", prompt)
            }
            partsArray.put(textPart)

            // 2. Base image for image editing mode
            if (sourceBitmap != null) {
                val base64Data = bitmapToBase64(sourceBitmap)
                val inlineData = JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", base64Data)
                }
                val imagePart = JSONObject().apply {
                    put("inlineData", inlineData)
                }
                partsArray.put(imagePart)
            }

            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", partsArray)
                })
            }

            val imageConfig = JSONObject().apply {
                put("aspectRatio", aspectRatio)
                put("imageSize", "1K")
            }

            val generationConfig = JSONObject().apply {
                put("responseModalities", JSONArray().apply {
                    put("TEXT")
                    put("IMAGE")
                })
                put("imageConfig", imageConfig)
            }

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", generationConfig)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return Result.failure(Exception("API Error (${response.code}): $responseString"))
            }

            val responseObj = JSONObject(responseString)
            val candidates = responseObj.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return Result.failure(Exception("No content generated by Gemini."))
            }

            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            if (parts == null || parts.length() == 0) {
                return Result.failure(Exception("Empty response parts."))
            }

            var resultBitmap: Bitmap? = null

            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.has("inlineData")) {
                    val inlineData = part.getJSONObject("inlineData")
                    val dataBase64 = inlineData.optString("data", "")
                    if (dataBase64.isNotEmpty()) {
                        val decodedBytes = Base64.decode(dataBase64, Base64.DEFAULT)
                        resultBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        break
                    }
                }
            }

            if (resultBitmap != null) {
                Result.success(resultBitmap)
            } else {
                Result.failure(Exception("No image returned in Gemini response."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
