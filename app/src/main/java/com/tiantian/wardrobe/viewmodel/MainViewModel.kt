package com.tiantian.wardrobe.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tiantian.wardrobe.ai.ClothingAnalyzer
import com.tiantian.wardrobe.ai.LLMClient
import com.tiantian.wardrobe.ai.LunarCalendarHelper
import com.tiantian.wardrobe.ai.OutfitRecommendation
import com.tiantian.wardrobe.ai.RecommendationEngine
import com.tiantian.wardrobe.ai.VisionAnalysisResult
import com.tiantian.wardrobe.ai.VisionClient
import com.tiantian.wardrobe.data.AppDatabase
import com.tiantian.wardrobe.data.ClothingItem
import com.tiantian.wardrobe.data.DailyRecommendation
import com.tiantian.wardrobe.data.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UiState(
    val items: List<ClothingItem> = emptyList(),
    val itemCount: Int = 0,
    val dayDescription: String = "",
    val recommendations: List<OutfitRecommendation> = emptyList(),
    val dailyRecommendation: DailyRecommendation? = null,
    val useLLM: Boolean = false,
    val llmLoading: Boolean = false,
    val llmError: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val dao = db.clothingDao()
    private val recDao = db.dailyRecommendationDao()
    val prefs = PreferencesManager(application)
    private val analyzer = ClothingAnalyzer(application)
    private val lunarCalendar = LunarCalendarHelper()
    private val ruleEngine = RecommendationEngine(lunarCalendar)
    private val llmClient = LLMClient(prefs)
    private val visionClient = VisionClient(prefs)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadDailyRecommendation()
        viewModelScope.launch {
            combine(
                dao.getAllItems(),
                dao.getItemCount()
            ) { items, count -> Pair(items, count) }
                .collect { (items, count) ->
                    val dayDesc = lunarCalendar.getDayDescription()
                    _uiState.update {
                        it.copy(
                            items = items,
                            itemCount = count,
                            dayDescription = dayDesc,
                            useLLM = prefs.isConfigured
                        )
                    }
                }
        }
    }

    private fun loadDailyRecommendation() {
        viewModelScope.launch(Dispatchers.IO) {
            val rec = recDao.getRecommendationOnce()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())
            val validRec = if (rec != null && rec.date == today) rec else null
            _uiState.update { it.copy(dailyRecommendation = validRec) }
        }
    }

    fun refreshConfiguredStatus() {
        _uiState.update { it.copy(useLLM = prefs.isConfigured) }
    }

    fun generateTodayRecommendation() {
        viewModelScope.launch {
            val items = _uiState.value.items
            if (items.isEmpty()) {
                _uiState.update { it.copy(llmError = "衣柜中没有衣物，请先添加", llmLoading = false) }
                return@launch
            }

            val season = lunarCalendar.getClothingSeason()
            val solarTerm = lunarCalendar.getNearestSolarTerm()?.name ?: ""
            val dayDesc = lunarCalendar.getDayDescription()

            _uiState.update { it.copy(llmLoading = true, llmError = null) }

            if (prefs.isConfigured) {
                try {
                    val rec = llmClient.generateRecommendation(items, season, solarTerm, dayDesc)
                    if (rec != null) {
                        saveAndUpdateRec(rec)
                        return@launch
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(llmError = "AI 推荐失败: ${e.message}", llmLoading = false) }
                }
            }

            val ruleRecs = ruleEngine.recommend(items)
            if (ruleRecs.isNotEmpty()) {
                saveAndUpdateRec(ruleRecs.first())
            } else {
                _uiState.update { it.copy(llmError = "无法生成推荐，衣物不足", llmLoading = false) }
            }
        }
    }

    private suspend fun saveAndUpdateRec(rec: OutfitRecommendation) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val daily = DailyRecommendation(
            id = 1,
            date = today,
            topName = rec.top?.name ?: "",
            topId = rec.top?.id ?: 0,
            topImagePath = rec.top?.imagePath ?: "",
            bottomName = rec.bottom?.name ?: "",
            bottomId = rec.bottom?.id ?: 0,
            bottomImagePath = rec.bottom?.imagePath ?: "",
            outerwearName = rec.outerwear?.name ?: "",
            outerwearId = rec.outerwear?.id ?: 0,
            outerwearImagePath = rec.outerwear?.imagePath ?: "",
            shoesName = rec.shoes?.name ?: "",
            shoesId = rec.shoes?.id ?: 0,
            shoesImagePath = rec.shoes?.imagePath ?: "",
            dressName = if (rec.top != null && rec.top.category == "连衣裙") rec.top.name else "",
            dressId = if (rec.top != null && rec.top.category == "连衣裙") rec.top.id else 0,
            dressImagePath = if (rec.top != null && rec.top.category == "连衣裙") rec.top.imagePath else "",
            reason = rec.reason,
            createdAt = System.currentTimeMillis()
        )

        recDao.upsert(daily)
        _uiState.update { it.copy(dailyRecommendation = daily, llmLoading = false, llmError = null) }
    }

    fun addItem(
        name: String,
        imagePath: String,
        category: String,
        color: String,
        season: String,
        style: String,
        description: String = ""
    ) {
        viewModelScope.launch {
            val item = ClothingItem(
                name = name.ifEmpty { "${color}${category}" },
                imagePath = imagePath,
                category = category,
                color = color,
                season = season,
                style = style,
                description = description,
                createdAt = System.currentTimeMillis()
            )
            dao.insertItem(item)
        }
    }

    fun deleteItem(item: ClothingItem) {
        viewModelScope.launch {
            dao.deleteItem(item)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            _uiState.value.items.forEach { dao.deleteItem(it) }
            recDao.deleteAll()
        }
    }

    fun getItemById(id: Long, callback: (ClothingItem?) -> Unit) {
        viewModelScope.launch {
            val item = dao.getItemById(id)
            callback(item)
        }
    }

    val isVisionConfigured: Boolean
        get() = prefs.isVisionConfigured

    fun analyzeClothing(imagePath: String, onResult: (VisionAnalysisResult?, error: String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = visionClient.analyzeClothing(imagePath)
                onResult(result, null)
            } catch (e: Exception) {
                onResult(null, e.message ?: "未知错误")
            }
        }
    }
}
