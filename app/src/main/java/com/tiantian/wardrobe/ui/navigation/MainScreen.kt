package com.tiantian.wardrobe.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiantian.wardrobe.ui.home.HomeScreen
import com.tiantian.wardrobe.ui.recommend.RecommendScreen
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
                    dailyRecommendation = uiState.dailyRecommendation
                )
                1 -> RecommendScreen(
                    dailyRecommendation = uiState.dailyRecommendation,
                    isLLMConfigured = uiState.useLLM,
                    llmLoading = uiState.llmLoading,
                    llmError = uiState.llmError,
                    onGenerate = { viewModel.generateTodayRecommendation() },
                    onOpenSettings = onOpenSettings
                )
                2 -> WardrobeScreen(
                    items = uiState.items,
                    onItemClick = { onItemClick(it.id) },
                    onDeleteItem = { viewModel.deleteItem(it) }
                )
                3 -> ProfileScreen(
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
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(Icons.Outlined.Home, "首页", selectedTab == 0) { onTabSelected(0) }
            NavItem(Icons.Outlined.AutoAwesome, "推荐", selectedTab == 1) { onTabSelected(1) }
            NavItem(Icons.Filled.Add, "添加", selectedTab == -1) { onAddClick() }
            NavItem(Icons.Outlined.Checkroom, "衣柜", selectedTab == 2) { onTabSelected(2) }
            NavItem(Icons.Outlined.Person, "我的", selectedTab == 3) { onTabSelected(3) }
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
    val iconColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant
    val weight = if (isSelected) FontWeight.Bold else FontWeight.Normal

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = weight,
            color = textColor
        )
    }
}
