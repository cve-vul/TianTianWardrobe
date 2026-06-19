package com.tiantian.wardrobe.ai

import java.util.Calendar

data class SolarTerm(
    val name: String,
    val month: Int,
    val day: Int,
    val season: String,
    val weatherDesc: String,
    val clothingAdvice: String
)

class LunarCalendarHelper {

    private val solarTerms = listOf(
        SolarTerm("立春", 2, 4, "春", "春寒料峭，气温回升", "薄外套+长裤"),
        SolarTerm("雨水", 2, 19, "春", "降雨增多，湿润", "风衣+长裤"),
        SolarTerm("惊蛰", 3, 6, "春", "气温回升，春雷始鸣", "夹克+长裤"),
        SolarTerm("春分", 3, 21, "春", "昼夜平分，温暖", "薄衫+长裤"),
        SolarTerm("清明", 4, 5, "春", "天气晴朗，草木繁茂", "衬衫+长裤"),
        SolarTerm("谷雨", 4, 20, "春", "降雨增多", "薄外套+长裤"),
        SolarTerm("立夏", 5, 6, "夏", "气温升高", "T恤+短裤"),
        SolarTerm("小满", 5, 21, "夏", "炎热渐增", "短袖+短裤"),
        SolarTerm("芒种", 6, 6, "夏", "高温多雨", "轻薄夏装"),
        SolarTerm("夏至", 6, 21, "夏", "炎热至极", "短袖+短裤+防晒"),
        SolarTerm("小暑", 7, 7, "夏", "炎热", "清凉夏装"),
        SolarTerm("大暑", 7, 23, "夏", "酷热", "清凉夏装+防晒"),
        SolarTerm("立秋", 8, 7, "秋", "暑去凉来", "薄衫+长裤"),
        SolarTerm("处暑", 8, 23, "秋", "暑气渐消", "衬衫+长裤"),
        SolarTerm("白露", 9, 8, "秋", "秋意渐浓", "薄外套+长裤"),
        SolarTerm("秋分", 9, 23, "秋", "昼夜平分，凉爽", "外套+长裤"),
        SolarTerm("寒露", 10, 8, "秋", "气温下降", "夹克+长裤"),
        SolarTerm("霜降", 10, 23, "秋", "天气渐冷", "毛衣+外套"),
        SolarTerm("立冬", 11, 7, "冬", "冬天开始", "毛衣+厚外套"),
        SolarTerm("小雪", 11, 22, "冬", "降雪开始", "羽绒服+毛衣"),
        SolarTerm("大雪", 12, 7, "冬", "降雪增多", "羽绒服+厚毛衣"),
        SolarTerm("冬至", 12, 21, "冬", "寒冷至极", "厚羽绒服"),
        SolarTerm("小寒", 1, 6, "冬", "严寒", "最厚羽绒服"),
        SolarTerm("大寒", 1, 20, "冬", "最寒冷", "最厚羽绒服+围巾")
    )

    fun getCurrentSolarTerm(): SolarTerm? {
        val now = Calendar.getInstance()
        val month = now.get(Calendar.MONTH) + 1
        val day = now.get(Calendar.DAY_OF_MONTH)

        return solarTerms.firstOrNull { it.month == month && it.day == day }
    }

    fun getCurrentSeason(): String {
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        return when (month) {
            in 3..5 -> "春"
            in 6..8 -> "夏"
            in 9..11 -> "秋"
            else -> "冬"
        }
    }

    fun getClothingSeason(): String {
        return when (getCurrentSeason()) {
            "春" -> "春秋"
            "秋" -> "春秋"
            "夏" -> "夏"
            "冬" -> "冬"
            else -> "春秋"
        }
    }

    fun getNearestSolarTerm(): SolarTerm? {
        val now = Calendar.getInstance()
        val currentDayOfYear = now.get(Calendar.DAY_OF_YEAR)

        return solarTerms.minByOrNull {
            val cal = Calendar.getInstance()
            cal.set(Calendar.MONTH, it.month - 1)
            cal.set(Calendar.DAY_OF_MONTH, it.day)
            val termDayOfYear = cal.get(Calendar.DAY_OF_YEAR)
            kotlin.math.abs(currentDayOfYear - termDayOfYear)
        }
    }

    fun getDayDescription(): String {
        val term = getCurrentSolarTerm()
        if (term != null) {
            return "今日${term.name}，${term.weatherDesc}"
        }
        val nearest = getNearestSolarTerm()
        return if (nearest != null) {
            "近期的节气为${nearest.name}，${nearest.weatherDesc}"
        } else {
            ""
        }
    }
}
