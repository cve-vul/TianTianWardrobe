package com.tiantian.wardrobe.ai

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AnalysisResult(
    val labels: List<String>,
    val primaryCategory: String,
    val detectedColor: String,
    val confidence: Float
)

class ClothingAnalyzer(private val context: Context) {

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.5f)
            .build()
    )

    suspend fun analyzeImage(bitmap: Bitmap): AnalysisResult = withContext(Dispatchers.IO) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val labels = mutableListOf<String>()

        try {
            val results = Tasks.await(labeler.process(image))
            for (label in results) {
                labels.add(label.text.lowercase())
            }
        } catch (_: Exception) { }

        val category = categorizeClothing(labels)
        val color = detectDominantColor(bitmap)

        AnalysisResult(
            labels = labels,
            primaryCategory = category,
            detectedColor = color,
            confidence = if (labels.isNotEmpty()) 0.8f else 0.0f
        )
    }

    private fun categorizeClothing(labels: List<String>): String {
        val categoryMap = listOf(
            "shirt" to "上衣", "t-shirt" to "上衣", "blouse" to "上衣",
            "jacket" to "外套", "coat" to "外套", "hoodie" to "外套",
            "pants" to "下装", "jeans" to "下装", "trousers" to "下装",
            "shorts" to "下装", "skirt" to "下装",
            "dress" to "连衣裙", "gown" to "连衣裙",
            "shoe" to "鞋", "sneaker" to "鞋", "boot" to "鞋",
            "hat" to "配饰", "bag" to "配饰", "scarf" to "配饰",
            "sweater" to "上衣", "vest" to "上衣", "suit" to "外套"
        )

        for (label in labels) {
            val matched = categoryMap.firstOrNull { label.contains(it.first) }
            if (matched != null) return matched.second
        }
        return "上衣"
    }

    private fun detectDominantColor(bitmap: Bitmap): String {
        val sampled = Bitmap.createScaledBitmap(bitmap, 1, 1, true)
        val pixel = sampled.getPixel(0, 0)
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        sampled.recycle()

        return when {
            r > 200 && g > 200 && b > 200 -> "白色"
            r < 60 && g < 60 && b < 60 -> "黑色"
            r > 200 && g < 100 && b < 100 -> "红色"
            r > 150 && g > 100 && b < 80 -> "棕色"
            r > 200 && g > 150 && b < 80 -> "橙色"
            r > 200 && g > 200 && b < 100 -> "黄色"
            r < 100 && g > 150 && b < 100 -> "绿色"
            r < 100 && g > 100 && b > 200 -> "蓝色"
            r > 150 && g < 100 && b > 150 -> "紫色"
            r > 200 && g > 150 && b > 150 -> "粉色"
            r > 150 && g > 150 && b > 150 -> "灰色"
            else -> "其他"
        }
    }

    fun getSeasonByCategory(category: String): String {
        return when (category) {
            "外套" -> "春秋"
            "连衣裙" -> "夏"
            "鞋" -> "春秋"
            "配饰" -> "春秋"
            else -> "春秋"
        }
    }

    fun getStyleByLabels(labels: List<String>): String {
        return when {
            labels.any { it.contains("suit") || it.contains("formal") || it.contains("tie") } -> "正式"
            labels.any { it.contains("sport") || it.contains("running") || it.contains("gym") } -> "运动"
            labels.any { it.contains("jean") || it.contains("casual") } -> "休闲"
            else -> "休闲"
        }
    }
}
