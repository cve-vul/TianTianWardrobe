package com.tiantian.wardrobe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clothing_items")
data class ClothingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val imagePath: String = "",
    val category: String = "",        // 上衣/下装/外套/连衣裙/鞋/配饰
    val color: String = "",           // 颜色
    val season: String = "",          // 春秋/夏/冬
    val style: String = "",           // 休闲/商务/运动/正式
    val tags: String = "",            // AI识别标签,逗号分隔
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val wearCount: Int = 0,
    val favorite: Boolean = false
)
