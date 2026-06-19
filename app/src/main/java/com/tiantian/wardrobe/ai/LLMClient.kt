package com.tiantian.wardrobe.ai

import com.tiantian.wardrobe.data.ClothingItem
import com.tiantian.wardrobe.data.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class LLMRecommendation(
    val title: String,
    val top: String,
    val bottom: String,
    val outerwear: String,
    val shoes: String,
    val reason: String
)

class LLMClient(private val prefs: PreferencesManager) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateRecommendations(
        items: List<ClothingItem>,
        season: String,
        solarTerm: String,
        dayDescription: String
    ): List<OutfitRecommendation> {
        val prompt = buildPrompt(items, season, solarTerm, dayDescription)
        val responseJson = callAPI(prompt)
        return parseResponse(responseJson, items)
    }

    private fun buildPrompt(
        items: List<ClothingItem>,
        season: String,
        solarTerm: String,
        dayDescription: String
    ): String {
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE)
        val today = dateFormat.format(Date())

        val tops = items.filter { it.category == "上衣" }
        val bottoms = items.filter { it.category == "下装" }
        val outerwears = items.filter { it.category == "外套" }
        val dresses = items.filter { it.category == "连衣裙" }
        val shoes = items.filter { it.category == "鞋" }
        val accessories = items.filter { it.category == "配饰" }

        fun formatItems(list: List<ClothingItem>): String {
            if (list.isEmpty()) return "（无）"
            return list.withIndex().joinToString("\n") { (i, item) ->
                "  ${i + 1}. ${item.name}（颜色:${item.color}, 风格:${item.style}, 季节:${item.season}）"
            }
        }

        return """
你是一个专业的穿搭顾问。请根据以下信息推荐3套今日穿搭组合。

日期：$today
季节：$season
节气：$solarTerm
节气描述：$dayDescription

用户的衣柜：

【上衣】
${formatItems(tops)}

【下装】
${formatItems(bottoms)}

【外套】
${formatItems(outerwears)}

【连衣裙】
${formatItems(dresses)}

【鞋】
${formatItems(shoes)}

【配饰】
${formatItems(accessories)}

请以严格的JSON数组格式返回3套推荐，不要包含其他文字。格式如下：
[
  {
    "title": "搭配名称",
    "top": "上衣名称（若无则填空字符串）",
    "bottom": "下装名称（若无则填空字符串）",
    "outerwear": "外套名称（若无则填空字符串）",
    "dress": "连衣裙名称（若无则填空字符串）",
    "shoes": "鞋名称（若无则填空字符串）",
    "reason": "推荐理由，包括颜色搭配、季节适配、风格协调等方面"
  }
]

注意：
1. 只使用用户衣柜中列出的衣物，不要编造不存在的衣物
2. 如果某类衣物不存在，对应字段填空字符串
3. 连衣裙可作为整体穿搭，此时top和bottom填空
4. 考虑季节适配性，优先推荐适合当前季节的衣物
5. 考虑颜色搭配协调性
6. 如果衣柜衣物不足以组成完整穿搭，请给出可行的简化方案
""".trimIndent()
    }

    private suspend fun callAPI(prompt: String): String = withContext(Dispatchers.IO) {
        val endpoint = prefs.apiEndpoint.trimEnd('/')
        val model = prefs.modelName
        val key = prefs.apiKey

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
        }

        val body = JSONObject().apply {
            put("model", model)
            put("messages", messages)
            put("temperature", 0.7)
            put("max_tokens", 2048)
        }

        val request = Request.Builder()
            .url("$endpoint/chat/completions")
            .addHeader("Authorization", "Bearer $key")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseStr = response.body?.string() ?: ""

        try {
            val json = JSONObject(responseStr)
            val choices = json.getJSONArray("choices")
            if (choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message")
                return@withContext message.getString("content")
            }
        } catch (_: Exception) { }
        ""
    }

    private fun parseResponse(jsonStr: String, items: List<ClothingItem>): List<OutfitRecommendation> {
        try {
            val cleaned = jsonStr.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val arr = JSONArray(cleaned)
            val results = mutableListOf<OutfitRecommendation>()

            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val topName = obj.optString("top", "")
                val bottomName = obj.optString("bottom", "")
                val outerwearName = obj.optString("outerwear", "")
                val dressName = obj.optString("dress", "")
                val shoesName = obj.optString("shoes", "")
                val reason = obj.optString("reason", "")

                val top = if (topName.isNotBlank()) findBestMatch(items, topName) else null
                val bottom = if (bottomName.isNotBlank()) findBestMatch(items, bottomName) else null
                val dress = if (dressName.isNotBlank()) findBestMatch(items, dressName) else null
                val outerwear = if (outerwearName.isNotBlank()) findBestMatch(items, outerwearName) else null
                val shoes = if (shoesName.isNotBlank()) findBestMatch(items, shoesName) else null

                results.add(
                    OutfitRecommendation(
                        top = top ?: dress,
                        bottom = bottom,
                        outerwear = outerwear,
                        shoes = shoes,
                        score = 10,
                        reason = reason
                    )
                )
            }
            return results
        } catch (_: Exception) {
            return emptyList()
        }
    }

    private fun findBestMatch(items: List<ClothingItem>, name: String): ClothingItem? {
        val normalized = name.trim().lowercase()
        val exact = items.firstOrNull { it.name.lowercase() == normalized }
        if (exact != null) return exact
        return items.firstOrNull { normalized.contains(it.name.lowercase()) || it.name.lowercase().contains(normalized) }
    }
}
