package com.tiantian.wardrobe.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.tiantian.wardrobe.data.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

data class VisionAnalysisResult(
    val name: String = "",
    val category: String = "",
    val color: String = "",
    val season: String = "",
    val style: String = "",
    val description: String = ""
)

class VisionClient(private val prefs: PreferencesManager) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun analyzeClothing(imagePath: String): VisionAnalysisResult = withContext(Dispatchers.IO) {
        val imageFile = File(imagePath)
        if (!imageFile.exists()) {
            throw IllegalStateException("图片文件不存在，请重新拍照")
        }

        val base64Image = compressAndEncodeImage(imagePath)
        val prompt = buildPrompt()

        val content = JSONArray().apply {
            put(JSONObject().apply {
                put("type", "text")
                put("text", prompt)
            })
            put(JSONObject().apply {
                put("type", "image_url")
                put("image_url", JSONObject().apply {
                    put("url", "data:image/jpeg;base64,$base64Image")
                })
            })
        }

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", content)
            })
        }

        val body = JSONObject().apply {
            put("model", prefs.visionModelName)
            put("messages", messages)
            put("temperature", 0.2)
            put("max_tokens", 1024)
        }

        val endpoint = prefs.visionApiEndpoint.trimEnd('/')

        val request = Request.Builder()
            .url("$endpoint/chat/completions")
            .addHeader("Authorization", "Bearer ${prefs.visionApiKey}")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseStr = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorDetail = try {
                val errJson = JSONObject(responseStr)
                errJson.optString("error", responseStr)
            } catch (_: Exception) {
                responseStr
            }
            throw IllegalStateException("API 请求失败 (${response.code}): ${errorDetail.take(200)}")
        }

        val json = try {
            JSONObject(responseStr)
        } catch (e: Exception) {
            throw IllegalStateException("API 返回格式异常，无法解析: ${responseStr.take(200)}")
        }

        val choices = json.optJSONArray("choices")
        if (choices == null || choices.length() == 0) {
            throw IllegalStateException("API 未返回识别结果，请检查模型是否支持图片识别")
        }

        val messageContent = try {
            choices.getJSONObject(0).getJSONObject("message").getString("content")
        } catch (e: Exception) {
            throw IllegalStateException("API 返回内容格式异常")
        }

        parseAnalysisResult(messageContent)
    }

    private fun compressAndEncodeImage(imagePath: String): String {
        val originalFile = File(imagePath)
        val originalSize = originalFile.length()

        if (originalSize <= 1024 * 1024) {
            return Base64.encodeToString(originalFile.readBytes(), Base64.NO_WRAP)
        }

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(imagePath, options)

        val maxDimension = 1024
        val scale = maxOf(
            options.outWidth / maxDimension,
            options.outHeight / maxDimension,
            1
        )

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = scale
        }
        val bitmap = BitmapFactory.decodeFile(imagePath, decodeOptions)
            ?: return Base64.encodeToString(originalFile.readBytes(), Base64.NO_WRAP)

        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output)
        bitmap.recycle()

        val compressed = output.toByteArray()
        return Base64.encodeToString(compressed, Base64.NO_WRAP)
    }

    private fun buildPrompt(): String {
        return """请分析图片中的衣物，严格以纯JSON格式返回以下字段，不要包含任何其他文字或markdown标记：
{
  "name": "衣物名称，简洁描述（如：白色纯棉短袖T恤）",
  "category": "从以下选项中选择一个：上衣、下装、外套、连衣裙、鞋、配饰",
  "color": "从以下选项中选择最接近的主色：白色、黑色、红色、蓝色、绿色、黄色、紫色、粉色、灰色、棕色、橙色、其他",
  "season": "从以下选项中选择最适合的季节：春秋、夏、冬",
  "style": "从以下选项中选择最符合的风格：休闲、商务、运动、正式",
  "description": "对衣物样式的简要描述（一句话）"
}

注意：
1. 只返回JSON，不要有其他内容
2. 如果图片中没有衣物，category填"配饰"并在description中说明
3. 根据衣物的厚薄和款式判断适合的季节
4. 根据整体款式风格判断风格类型""".trimIndent()
    }

    private fun parseAnalysisResult(content: String): VisionAnalysisResult {
        val cleaned = content.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val json = try {
            JSONObject(cleaned)
        } catch (e: Exception) {
            throw IllegalStateException("AI 返回格式错误，期望 JSON 但收到: ${cleaned.take(100)}")
        }

        return VisionAnalysisResult(
            name = json.optString("name", ""),
            category = json.optString("category", "上衣"),
            color = json.optString("color", ""),
            season = json.optString("season", "春秋"),
            style = json.optString("style", "休闲"),
            description = json.optString("description", "")
        )
    }
}
