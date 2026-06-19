package com.tiantian.wardrobe.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiantian.wardrobe.ui.home.HomeScreen
import com.tiantian.wardrobe.ui.wardrobe.WardrobeScreen
import com.tiantian.wardrobe.ui.profile.ProfileScreen
import com.tiantian.wardrobe.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onAddClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVisionSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onAddClick = onAddClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    itemCount = uiState.itemCount,
                    dayDescription = uiState.dayDescription,
                    recommendations = uiState.recommendations,
                    llmLoading = uiState.llmLoading,
                    llmError = uiState.llmError,
                    isLLMConfigured = uiState.useLLM,
                    onRefresh = { viewModel.refreshRecommendations() },
                    onItemClick = { onItemClick(it.id) }
                )
                1 -> WardrobeScreen(
                    items = uiState.items,
                    onItemClick = { onItemClick(it.id) },
                    onDeleteItem = { viewModel.deleteItem(it) }
                )
                2 -> ProfileScreen(
                    itemCount = uiState.itemCount,
                    items = uiState.items,
                    isLLMConfigured = uiState.useLLM,
                    isVisionConfigured = viewModel.isVisionConfigured,
                    onOpenSettings = onOpenSettings,
                    onOpenVisionSettings = onOpenVisionSettings,
                    onResetData = { viewModel.resetAllData() }
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    Column {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(Icons.Outlined.Home, "首页", selectedTab == 0) { onTabSelected(0) }
            NavItem(Icons.Outlined.Checkroom, "衣柜", selectedTab == 1) { onTabSelected(1) }
            AddItem(onAddClick)
            NavItem(Icons.Outlined.Person, "我的", selectedTab == 2) { onTabSelected(2) }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AddItem(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "拍照添加",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(28.dp)
        )
    }
}
