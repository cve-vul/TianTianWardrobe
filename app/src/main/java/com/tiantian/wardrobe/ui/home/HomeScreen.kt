package com.tiantian.wardrobe.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tiantian.wardrobe.ai.OutfitRecommendation
import com.tiantian.wardrobe.data.ClothingItem
import java.io.File
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "天天衣橱",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isLLMConfigured) {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Outlined.Refresh, "刷新推荐", modifier = Modifier.size(22.dp))
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
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("AI 正在思考穿搭方案...", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else if (llmError != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(llmError, fontSize = 13.sp, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("已切换为本地规则推荐", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isLLMConfigured) {
                        Text("AI 推荐", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.Checkroom,
                            null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "暂无穿搭推荐",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (isLLMConfigured) "正在等待 AI 生成推荐..." else "添加更多衣物后获取穿搭推荐",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$itemCount",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "已收录衣物",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.Checkroom,
                    null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "智能管理",
                    fontSize = 13.sp,
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(today, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            if (dayDescription.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.WbSunny,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        dayDescription,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecommendationCard(
    recommendation: OutfitRecommendation,
    onItemClick: (ClothingItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = {
            recommendation.top?.let { onItemClick(it) }
                ?: recommendation.bottom?.let { onItemClick(it) }
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = recommendation.reason.ifEmpty { "AI智能搭配" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recommendation.top?.let { item ->
                    ClothingThumb(item, onItemClick, Modifier.weight(1f))
                }
                recommendation.bottom?.let { item ->
                    ClothingThumb(item, onItemClick, Modifier.weight(1f))
                }
                recommendation.outerwear?.let { item ->
                    ClothingThumb(item, onItemClick, Modifier.weight(1f))
                }
                recommendation.shoes?.let { item ->
                    ClothingThumb(item, onItemClick, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ClothingThumb(
    item: ClothingItem,
    onClick: (ClothingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick(item) }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (item.imagePath.isNotEmpty() && File(item.imagePath).exists()) {
                AsyncImage(
                    model = File(item.imagePath),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Outlined.Checkroom,
                    null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.name.take(4),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
