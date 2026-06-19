package com.tiantian.wardrobe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_recommendation")
data class DailyRecommendation(
    @PrimaryKey
    val id: Int = 1,
    val date: String = "",
    val topName: String = "",
    val topId: Long = 0,
    val topImagePath: String = "",
    val bottomName: String = "",
    val bottomId: Long = 0,
    val bottomImagePath: String = "",
    val outerwearName: String = "",
    val outerwearId: Long = 0,
    val outerwearImagePath: String = "",
    val shoesName: String = "",
    val shoesId: Long = 0,
    val shoesImagePath: String = "",
    val dressName: String = "",
    val dressId: Long = 0,
    val dressImagePath: String = "",
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
