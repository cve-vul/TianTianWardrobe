package com.tiantian.wardrobe.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onOpenSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier
                    .size(64.dp)
                    .offset(y = (-16).dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Icon(Icons.Default.Add, "添加衣物", modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
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
                    onOpenSettings = onOpenSettings,
                    onResetData = { viewModel.resetAllData() }
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(Icons.Default.Home, "首页", selectedTab == 0) { onTabSelected(0) }
            NavItem(Icons.Default.Checkroom, "衣柜", selectedTab == 1) { onTabSelected(1) }
            NavItem(Icons.Default.Person, "我的", selectedTab == 2) { onTabSelected(2) }
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
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}
