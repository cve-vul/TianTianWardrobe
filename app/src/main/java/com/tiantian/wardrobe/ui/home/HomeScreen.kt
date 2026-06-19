package com.tiantian.wardrobe.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiantian.wardrobe.ai.OutfitRecommendation
import com.tiantian.wardrobe.data.ClothingItem
import com.tiantian.wardrobe.ui.theme.CalendarToday
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    itemCount: Int,
    dayDescription: String,
    recommendations: List<OutfitRecommendation>,
    llmLoading: Boolean = false,
    llmError: String? = null,
    isLLMConfigured: Boolean = false,
    onRefresh: () -> Unit = {},
    onItemClick: (ClothingItem) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.CHINESE)
    val today = dateFormat.format(Calendar.getInstance().time)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "天天衣橱",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (isLLMConfigured) {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, "刷新推荐", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item { StatsCard(itemCount) }

        item { CalendarCard(today, dayDescription) }

        if (llmLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("AI 正在思考穿搭方案...", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        } else if (llmError != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFE53935), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(llmError, fontSize = 13.sp, color = Color(0xFFC62828), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("已切换为本地规则推荐", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
        }

        if (recommendations.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "今日穿搭推荐",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (isLLMConfigured) {
                        Text("AI 推荐", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            items(recommendations) { rec ->
                RecommendationCard(rec, onItemClick)
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Checkroom, null, modifier = Modifier.size(48.dp), tint = Color(0xFFB8860B))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("暂无穿搭推荐", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF8B6914))
                        Text(
                            if (isLLMConfigured) "正在等待 AI 生成推荐..." else "添加更多衣物后获取穿搭推荐",
                            fontSize = 14.sp, color = Color(0xFF8B6914), textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsCard(itemCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$itemCount",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "已收录衣物",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(50.dp)
                    .background(Color.White.copy(alpha = 0.3f))
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Checkroom,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "智能管理",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun CalendarCard(today: String, dayDescription: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = CalendarToday,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = today,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (dayDescription.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dayDescription,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: OutfitRecommendation,
    onItemClick: (ClothingItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = {
            recommendation.top?.let { onItemClick(it) }
                ?: recommendation.bottom?.let { onItemClick(it) }
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFFB8860B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = recommendation.reason.ifEmpty { "AI智能搭配" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8B6914)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                recommendation.top?.let { item ->
                    ClothingThumb(item, onItemClick)
                }
                recommendation.bottom?.let { item ->
                    ClothingThumb(item, onItemClick)
                }
                recommendation.outerwear?.let { item ->
                    ClothingThumb(item, onItemClick)
                }
                recommendation.shoes?.let { item ->
                    ClothingThumb(item, onItemClick)
                }
            }
        }
    }
}

@Composable
private fun ClothingThumb(item: ClothingItem, onClick: (ClothingItem) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.7f))
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Checkroom,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.name.take(4),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
