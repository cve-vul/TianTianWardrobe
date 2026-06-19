package com.tiantian.wardrobe.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiantian.wardrobe.data.ClothingItem
import com.tiantian.wardrobe.ui.theme.Success

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    itemCount: Int,
    items: List<ClothingItem>,
    isLLMConfigured: Boolean,
    isVisionConfigured: Boolean,
    onOpenSettings: () -> Unit,
    onOpenVisionSettings: () -> Unit,
    onResetData: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("重置数据", fontWeight = FontWeight.Bold) },
            text = { Text("确定要清空所有衣物数据吗？此操作不可恢复。", fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { onResetData(); showResetDialog = false }) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("取消") }
            }
        )
    }

    if (showStorageDialog) {
        AlertDialog(
            onDismissRequest = { showStorageDialog = false },
            title = { Text("数据存储", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    StorageInfoRow("存储方式", "本地数据库 (Room)")
                    Spacer(modifier = Modifier.height(8.dp))
                    StorageInfoRow("数据库名称", "tiantian_wardrobe.db")
                    Spacer(modifier = Modifier.height(8.dp))
                    StorageInfoRow("衣物数量", "$itemCount 件")
                    Spacer(modifier = Modifier.height(8.dp))
                    StorageInfoRow("数据安全", "所有数据存储在本地设备，不上传服务器")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "清除应用数据可通过下方「重置所有数据」完成。",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showStorageDialog = false }) { Text("关闭") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "我的",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
        )

        // User card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("衣橱管家用户", fontSize = 17.sp, fontWeight = FontWeight.Medium)
                Text(
                    "已收录 $itemCount 件衣物",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Settings section
        Text(
            text = "功能设置",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            SettingsRow(
                icon = Icons.Outlined.AutoAwesome,
                title = "AI 推荐设置",
                subtitle = if (isLLMConfigured) "已配置" else "未配置",
                showBadge = isLLMConfigured,
                onClick = onOpenSettings
            )
            DividerRow()
            SettingsRow(
                icon = Icons.Outlined.CameraAlt,
                title = "视觉模型设置",
                subtitle = if (isVisionConfigured) "已配置" else "未配置",
                showBadge = isVisionConfigured,
                onClick = onOpenVisionSettings
            )
            DividerRow()
            SettingsRow(
                icon = Icons.Outlined.Storage,
                title = "数据存储",
                subtitle = "本地数据库",
                onClick = { showStorageDialog = true }
            )
            DividerRow()
            SettingsRow(
                icon = Icons.Outlined.Info,
                title = "应用介绍",
                subtitle = "天天衣橱 - 智能穿搭助手",
                onClick = { }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reset data
        Text(
            text = "数据管理",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showResetDialog = true }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DeleteForever,
                    null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    "重置所有数据",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        Text(
            "天天衣橱 v1.0",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    showBadge: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp)
            Text(
                subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        if (showBadge) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Success)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun DividerRow() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun StorageInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
