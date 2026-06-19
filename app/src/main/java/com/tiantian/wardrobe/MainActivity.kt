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
import com.tiantian.wardrobe.ui.settings.VisionSettingsScreen
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
    var showVisionSettings by remember { mutableStateOf(false) }
    var pendingPhotoPath by remember { mutableStateOf<String?>(null) }

    when {
        showVisionSettings -> VisionSettingsScreen(
            prefs = viewModel.prefs,
            onBack = {
                showVisionSettings = false
            }
        )

        showSettings -> ApiSettingsScreen(
            prefs = viewModel.prefs,
            onBack = {
                showSettings = false
                viewModel.refreshRecommendations()
            }
        )

        showCamera -> CameraScreen(
            viewModel = viewModel,
            initialPhotoPath = pendingPhotoPath,
            onPhotoCaptured = { path -> pendingPhotoPath = path },
            onSave = { name, imagePath, category, color, season, style, description ->
                viewModel.addItem(name, imagePath, category, color, season, style, description)
                showCamera = false
                pendingPhotoPath = null
            },
            onBack = {
                showCamera = false
                pendingPhotoPath = null
            },
            onOpenVisionSettings = { showVisionSettings = true }
        )

        else -> MainScreen(
            viewModel = viewModel,
            onAddClick = { showCamera = true },
            onItemClick = { },
            onOpenSettings = { showSettings = true },
            onOpenVisionSettings = { showVisionSettings = true }
        )
    }
}
