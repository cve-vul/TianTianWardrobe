package com.tiantian.wardrobe.ui.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiantian.wardrobe.data.ClothingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeScreen(
    items: List<ClothingItem>,
    onItemClick: (ClothingItem) -> Unit,
    onDeleteItem: (ClothingItem) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("全部") }
    val categories = listOf("全部", "上衣", "下装", "外套", "连衣裙", "鞋", "配饰")

    val filteredItems = if (selectedCategory == "全部") {
        items
    } else {
        items.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "我的衣柜",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Checkroom,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (items.isEmpty()) "衣柜空空如也，快去添加衣物吧" else "该分类暂无衣物",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    ClothingCard(item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
private fun ClothingCard(item: ClothingItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Checkroom,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    if (item.color.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(colorFromName(item.color))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name.ifEmpty { "未命名" },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                if (item.category.isNotEmpty()) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(item.category, fontSize = 10.sp) },
                        modifier = Modifier.height(24.dp)
                    )
                }
                if (item.season.isNotEmpty()) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(item.season, fontSize = 10.sp) },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }
    }
}

private fun colorFromName(name: String): androidx.compose.ui.graphics.Color {
    return when (name) {
        "白色" -> androidx.compose.ui.graphics.Color.White
        "黑色" -> androidx.compose.ui.graphics.Color.Black
        "红色" -> androidx.compose.ui.graphics.Color.Red
        "蓝色" -> androidx.compose.ui.graphics.Color.Blue
        "绿色" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        "黄色" -> androidx.compose.ui.graphics.Color(0xFFFFEB3B)
        "紫色" -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
        "粉色" -> androidx.compose.ui.graphics.Color(0xFFFF69B4)
        "灰色" -> androidx.compose.ui.graphics.Color.Gray
        "棕色" -> androidx.compose.ui.graphics.Color(0xFF795548)
        "橙色" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        else -> androidx.compose.ui.graphics.Color.LightGray
    }
}
