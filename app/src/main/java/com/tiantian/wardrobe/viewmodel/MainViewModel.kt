package com.tiantian.wardrobe.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tiantian.wardrobe.ai.ClothingAnalyzer
import com.tiantian.wardrobe.ai.LunarCalendarHelper
import com.tiantian.wardrobe.ai.OutfitRecommendation
import com.tiantian.wardrobe.ai.RecommendationEngine
import com.tiantian.wardrobe.data.AppDatabase
import com.tiantian.wardrobe.data.ClothingItem
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
    val recommendations: List<OutfitRecommendation> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val dao = db.clothingDao()
    private val analyzer = ClothingAnalyzer(application)
    private val lunarCalendar = LunarCalendarHelper()
    private val recommendationEngine = RecommendationEngine(lunarCalendar)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dao.getAllItems(),
                dao.getItemCount()
            ) { items, count ->
                Pair(items, count)
            }.collect { (items, count) ->
                val recommendations = recommendationEngine.recommend(items)
                _uiState.update {
                    it.copy(
                        items = items,
                        itemCount = count,
                        dayDescription = lunarCalendar.getDayDescription(),
                        recommendations = recommendations
                    )
                }
            }
        }
    }

    fun addItem(
        name: String,
        imagePath: String,
        category: String,
        color: String,
        season: String,
        style: String
    ) {
        viewModelScope.launch {
            val item = ClothingItem(
                name = name.ifEmpty { "${color}${category}" },
                imagePath = imagePath,
                category = category,
                color = color,
                season = season,
                style = style,
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
}
