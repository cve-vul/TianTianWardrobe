package com.tiantian.wardrobe.ai

import com.tiantian.wardrobe.data.ClothingItem

data class OutfitRecommendation(
    val top: ClothingItem? = null,
    val bottom: ClothingItem? = null,
    val outerwear: ClothingItem? = null,
    val shoes: ClothingItem? = null,
    val accessory: ClothingItem? = null,
    val score: Int = 0,
    val reason: String = ""
)

class RecommendationEngine(
    private val lunarCalendar: LunarCalendarHelper
) {
    private val colorScore = mapOf(
        "白色" to mapOf("白色" to 8, "黑色" to 9, "蓝色" to 8, "红色" to 7, "灰色" to 9, "棕色" to 7, "绿色" to 6, "粉色" to 7, "紫色" to 6, "黄色" to 5, "橙色" to 5, "其他" to 5),
        "黑色" to mapOf("白色" to 9, "黑色" to 6, "蓝色" to 8, "红色" to 8, "灰色" to 9, "棕色" to 7, "绿色" to 6, "粉色" to 7, "紫色" to 7, "黄色" to 6, "橙色" to 6, "其他" to 5),
        "蓝色" to mapOf("白色" to 9, "黑色" to 8, "蓝色" to 5, "红色" to 5, "灰色" to 8, "棕色" to 6, "绿色" to 4, "粉色" to 6, "紫色" to 4, "黄色" to 6, "橙色" to 5, "其他" to 4),
        "灰色" to mapOf("白色" to 9, "黑色" to 9, "蓝色" to 8, "红色" to 7, "灰色" to 6, "棕色" to 7, "绿色" to 6, "粉色" to 7, "紫色" to 6, "黄色" to 6, "橙色" to 6, "其他" to 5),
        "红色" to mapOf("白色" to 8, "黑色" to 9, "蓝色" to 5, "红色" to 4, "灰色" to 7, "棕色" to 5, "绿色" to 4, "粉色" to 5, "紫色" to 5, "黄色" to 6, "橙色" to 5, "其他" to 4)
    )

    fun recommend(items: List<ClothingItem>, count: Int = 3): List<OutfitRecommendation> {
        if (items.isEmpty()) return emptyList()

        val season = lunarCalendar.getClothingSeason()
        val term = lunarCalendar.getNearestSolarTerm()
        val seasonItems = items.filter { it.season.isEmpty() || it.season == season }

        val tops = seasonItems.filter { it.category == "上衣" }
        val bottoms = seasonItems.filter { it.category == "下装" }
        val outerwears = seasonItems.filter { it.category == "外套" }
        val shoes = seasonItems.filter { it.category == "鞋" }
        val accessories = seasonItems.filter { it.category == "配饰" }

        if (tops.isEmpty() && bottoms.isEmpty()) {
            if (items.isNotEmpty()) {
                val first = items.first()
                return listOf(
                    OutfitRecommendation(
                        top = first,
                        score = 5,
                        reason = "当前衣柜衣物较少，建议添加更多衣物获取穿搭推荐"
                    )
                )
            }
            return emptyList()
        }

        val recommendations = mutableListOf<OutfitRecommendation>()

        val topList = if (tops.isNotEmpty()) tops else items.filter { it.category != "下装" && it.category != "鞋" }
        val bottomList = if (bottoms.isNotEmpty()) bottoms else items.filter { it.category == "下装" || it.category == "连衣裙" }

        for (i in 0 until minOf(topList.size, 5)) {
            val top = topList[i]
            val bottomCandidates = bottomList.shuffled().take(3)
            for (bottom in bottomCandidates) {
                val outfitScore = scoreOutfit(top, bottom, season, term)
                val reason = buildReason(top, bottom, season, term)
                recommendations.add(
                    OutfitRecommendation(
                        top = if (top.category != "连衣裙") top else null,
                        bottom = if (bottom.category == "下装") bottom else null,
                        outerwear = outerwears.firstOrNull { outfitScore > 6 },
                        shoes = shoes.firstOrNull(),
                        accessory = accessories.firstOrNull(),
                        score = outfitScore,
                        reason = reason
                    )
                )
            }
        }

        recommendations.sortByDescending { it.score }
        return recommendations.take(count)
    }

    private fun scoreOutfit(
        top: ClothingItem,
        bottom: ClothingItem,
        season: String,
        term: SolarTerm?
    ): Int {
        var score = 5
        val colorMatch = colorScore[top.color]?.get(bottom.color) ?: 5
        score += colorMatch

        if (top.season == season) score += 2
        if (bottom.season == season) score += 2
        if (top.style == bottom.style) score += 2

        if (term != null && (top.season == "春秋" || top.season == season)) score += 1
        if (top.favorite) score += 1
        if (bottom.favorite) score += 1

        return score
    }

    private fun buildReason(
        top: ClothingItem,
        bottom: ClothingItem,
        season: String,
        term: SolarTerm?
    ): String {
        val parts = mutableListOf<String>()
        when (season) {
            "夏" -> parts.add("适合夏季穿搭")
            "冬" -> parts.add("适合冬季穿搭")
            "春秋" -> parts.add("春秋季穿搭")
        }
        if (top.color.isNotEmpty() && bottom.color.isNotEmpty()) {
            parts.add("${top.color}${top.name}搭配${bottom.color}${bottom.name}")
        }
        if (top.style.isNotEmpty() && top.style == bottom.style) {
            parts.add("${top.style}风格统一")
        }
        if (term != null) {
            parts.add("参考${term.name}节气")
        }
        return parts.joinToString("，")
    }
}
