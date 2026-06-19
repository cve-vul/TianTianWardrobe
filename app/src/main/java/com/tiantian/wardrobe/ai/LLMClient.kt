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

class LLMClient(private val prefs: PreferencesManager) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateRecommendation(
        items: List<ClothingItem>,
        season: String,
        solarTerm: String,
        dayDescription: String
    ): OutfitRecommendation? {
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

        val hasTop = tops.isNotEmpty()
        val hasBottom = bottoms.isNotEmpty()
        val hasOuterwear = outerwears.isNotEmpty()
        val hasDress = dresses.isNotEmpty()
        val hasShoes = shoes.isNotEmpty()

        fun formatItems(list: List<ClothingItem>): String {
            if (list.isEmpty()) return "（无）"
            return list.withIndex().joinToString("\n") { (i, item) ->
                val id = String.format("DB%03d", i + 1)
                "  $id. ${item.name}（颜色:${item.color}, 风格:${item.style}, 季节:${item.season}）"
            }
        }

        val categories = buildString {
            append("【上衣】\n${formatItems(tops)}\n\n")
            append("【下装】\n${formatItems(bottoms)}\n\n")
            append("【外套】\n${formatItems(outerwears)}\n\n")
            append("【连衣裙】\n${formatItems(dresses)}\n\n")
            append("【鞋】\n${formatItems(shoes)}")
        }

        val requirement = when {
            hasDress && hasShoes -> "衣柜中有连衣裙和鞋子，请推荐1条连衣裙 + 1双鞋"
            hasTop && hasBottom && hasShoes -> "衣柜中有上衣、下装、鞋子，请推荐上衣+下装+鞋子各1件"
            hasTop && hasBottom -> "衣柜中有上衣和下装，请推荐上衣+下装各1件"
            hasDress -> "只有连衣裙，请推荐1条连衣裙"
            else -> "衣柜中衣物较少，请根据现有衣物给出简化推荐"
        }

        val extraNote = if (hasOuterwear) "\n- 如果天气适合，可额外加1件外套" else ""

        return """
你是一个专业的穿搭顾问。请根据用户衣柜中实际存在的衣物推荐1套今日穿搭。

日期：$today
季节：$season
节气：$solarTerm
节气描述：$dayDescription

用户当前衣柜：
$categories

穿搭要求：$requirement

请以严格的JSON格式返回，不要包含其他文字或markdown标记，格式如下：
{
  "top": "上衣名称（无则空字符串）",
  "bottom": "下装名称（无则空字符串）",
  "outerwear": "外套名称（无则空字符串）",
  "dress": "连衣裙名称（无则空字符串，连衣裙需同时填top）",
  "shoes": "鞋名称（无则空字符串）",
  "reason": "推荐理由，包括颜色搭配、季节适配、风格协调"
}

严格规则：
1. 每件衣物的名称必须与上面衣柜中列出的名称完全一致，不得编造、修改或杜撰任何衣物名称
2. 如果某类别衣物为空（无），则对应字段填空字符串""
3. 连衣裙可替代上衣+下装，此时top填连衣裙名，bottom填空
4. 优先推荐适合「$season」季节的衣物
5. 优先选择风格一致的衣物组合
6. 如果衣柜衣物不足以组成完整穿搭，直接填空，不要编造
$extraNote""".trimIndent()
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
            put("max_tokens", 1024)
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

    private fun parseResponse(jsonStr: String, items: List<ClothingItem>): OutfitRecommendation? {
        try {
            val cleaned = jsonStr.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val obj = JSONObject(cleaned)
            val topName = obj.optString("top", "")
            val bottomName = obj.optString("bottom", "")
            val outerwearName = obj.optString("outerwear", "")
            val dressName = obj.optString("dress", "")
            val shoesName = obj.optString("shoes", "")
            val reason = obj.optString("reason", "")

            val top = if (topName.isNotBlank()) findExactMatch(items, topName) else null
            val dress = if (dressName.isNotBlank()) findExactMatch(items, dressName) else null
            val bottom = if (bottomName.isNotBlank()) findExactMatch(items, bottomName) else null
            val outerwear = if (outerwearName.isNotBlank()) findExactMatch(items, outerwearName) else null
            val shoes = if (shoesName.isNotBlank()) findExactMatch(items, shoesName) else null

            val resultTop = top ?: dress

            if (resultTop == null && bottom == null && shoes == null) return null

            return OutfitRecommendation(
                top = resultTop,
                bottom = bottom,
                outerwear = outerwear,
                shoes = shoes,
                score = 10,
                reason = reason
            )
        } catch (_: Exception) {
            return null
        }
    }

    private fun findExactMatch(items: List<ClothingItem>, name: String): ClothingItem? {
        val target = name.trim()
        return items.firstOrNull { it.name.trim() == target }
            ?: items.firstOrNull { it.name.trim().contains(target) || target.contains(it.name.trim()) }
    }
}
