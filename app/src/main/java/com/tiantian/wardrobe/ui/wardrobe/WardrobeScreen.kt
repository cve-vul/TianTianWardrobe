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
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tiantian.wardrobe.data.ClothingItem
import java.io.File

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
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontSize = 13.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Checkroom,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (items.isEmpty()) "衣柜空空如也" else "该分类暂无衣物",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (items.isEmpty()) {
                        Text(
                            "点击下方加号添加衣物",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
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
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }

            // Info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.name.ifEmpty { "未命名" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.category.isNotEmpty()) {
                        Tag(text = item.category)
                    }
                    if (item.season.isNotEmpty()) {
                        Tag(text = item.season)
                    }
                    if (item.color.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(colorFromName(item.color))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Tag(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

private fun colorFromName(name: String): Color {
    return when (name) {
        "白色" -> Color.White
        "黑色" -> Color.Black
        "红色" -> Color.Red
        "蓝色" -> Color.Blue
        "绿色" -> Color(0xFF4CAF50)
        "黄色" -> Color(0xFFFFEB3B)
        "紫色" -> Color(0xFF9C27B0)
        "粉色" -> Color(0xFFFF69B4)
        "灰色" -> Color.Gray
        "棕色" -> Color(0xFF795548)
        "橙色" -> Color(0xFFFF9800)
        else -> Color.LightGray
    }
}
