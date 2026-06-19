package com.tiantian.wardrobe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tiantian.wardrobe.ui.camera.CameraScreen
import com.tiantian.wardrobe.ui.navigation.MainScreen
import com.tiantian.wardrobe.ui.settings.ApiSettingsScreen
import com.tiantian.wardrobe.ui.theme.TianTianWardrobeTheme
import com.tiantian.wardrobe.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TianTianWardrobeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WardrobeApp()
                }
            }
        }
    }
}

@Composable
private fun WardrobeApp() {
    val viewModel: MainViewModel = viewModel()
    var showCamera by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    when {
        showCamera -> CameraScreen(
            onSave = { name, imagePath, category, color, season, style ->
                viewModel.addItem(name, imagePath, category, color, season, style)
                showCamera = false
            },
            onBack = { showCamera = false }
        )

        showSettings -> ApiSettingsScreen(
            prefs = viewModel.prefs,
            onBack = {
                showSettings = false
                viewModel.refreshRecommendations()
            }
        )

        else -> MainScreen(
            viewModel = viewModel,
            onAddClick = { showCamera = true },
            onItemClick = { },
            onOpenSettings = { showSettings = true }
        )
    }
}
