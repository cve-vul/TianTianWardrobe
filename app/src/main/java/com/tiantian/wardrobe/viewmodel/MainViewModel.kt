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
import com.tiantian.wardrobe.data.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val items: List<ClothingItem> = emptyList(),
    val itemCount: Int = 0,
    val dayDescription: String = "",
    val recommendations: List<OutfitRecommendation> = emptyList(),
    val useLLM: Boolean = false,
    val llmLoading: Boolean = false,
    val llmError: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val dao = db.clothingDao()
    val prefs = PreferencesManager(application)
    private val analyzer = ClothingAnalyzer(application)
    private val lunarCalendar = LunarCalendarHelper()
    private val ruleEngine = RecommendationEngine(lunarCalendar)
    private val llmClient = LLMClient(prefs)
    private val visionClient = VisionClient(prefs)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
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
                    generateRecommendations(items)
                }
        }
    }

    private suspend fun generateRecommendations(items: List<ClothingItem>) {
        if (prefs.isConfigured) {
            _uiState.update { it.copy(llmLoading = true, llmError = null) }
            try {
                val llmRecs = llmClient.generateRecommendations(
                    items = items,
                    season = lunarCalendar.getClothingSeason(),
                    solarTerm = lunarCalendar.getNearestSolarTerm()?.name ?: "",
                    dayDescription = lunarCalendar.getDayDescription()
                )
                if (llmRecs.isNotEmpty()) {
                    _uiState.update { it.copy(recommendations = llmRecs, llmLoading = false) }
                    return
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(llmError = "AI 推荐失败: ${e.message}", llmLoading = false) }
            }
        }
        val ruleRecs = ruleEngine.recommend(items)
        _uiState.update { it.copy(recommendations = ruleRecs, llmLoading = false) }
    }

    fun refreshRecommendations() {
        viewModelScope.launch {
            generateRecommendations(_uiState.value.items)
        }
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
