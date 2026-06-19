package com.tiantian.wardrobe.ui.camera

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.tiantian.wardrobe.ai.AnalysisResult
import java.io.File
import java.util.concurrent.Executors

data class CaptureState(
    val hasPhoto: Boolean = false,
    val photoPath: String = "",
    val name: String = "",
    val category: String = "上衣",
    val color: String = "",
    val season: String = "春秋",
    val style: String = "休闲",
    val analysisResult: AnalysisResult? = null
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onSave: (name: String, imagePath: String, category: String, color: String, season: String, style: String) -> Unit,
    onBack: () -> Unit
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current
    var captureState by remember { mutableStateOf(CaptureState()) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showSeasonMenu by remember { mutableStateOf(false) }
    var showStyleMenu by remember { mutableStateOf(false) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val categories = listOf("上衣", "下装", "外套", "连衣裙", "鞋", "配饰")
    val seasons = listOf("春秋", "夏", "冬")
    val styles = listOf("休闲", "商务", "运动", "正式")

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val dest = File(context.cacheDir, "gallery_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(it)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            captureState = captureState.copy(hasPhoto = true, photoPath = dest.absolutePath)
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    if (!cameraPermission.status.isGranted) {
        PermissionRequest(
            showRationale = cameraPermission.status.shouldShowRationale,
            onRequestPermission = { cameraPermission.launchPermissionRequest() }
        )
        return
    }

    if (captureState.hasPhoto) {
        EditClothingScreen(
            captureState = captureState,
            onNameChange = { captureState = captureState.copy(name = it) },
            onCategoryChange = { captureState = captureState.copy(category = it) },
            onColorChange = { captureState = captureState.copy(color = it) },
            onSeasonChange = { captureState = captureState.copy(season = it) },
            onStyleChange = { captureState = captureState.copy(style = it) },
            showCategoryMenu = showCategoryMenu,
            showSeasonMenu = showSeasonMenu,
            showStyleMenu = showStyleMenu,
            onToggleCategoryMenu = { showCategoryMenu = !showCategoryMenu },
            onToggleSeasonMenu = { showSeasonMenu = !showSeasonMenu },
            onToggleStyleMenu = { showStyleMenu = !showStyleMenu },
            onRetakePhoto = {
                captureState = CaptureState()
                showCategoryMenu = false; showSeasonMenu = false; showStyleMenu = false
            },
            onSave = {
                onSave(captureState.name, captureState.photoPath, captureState.category, captureState.color, captureState.season, captureState.style)
            },
            onBack = onBack
        )
        return
    }

    CameraPreview(
        galleryLauncher = galleryLauncher,
        onCapture = { controller ->
            val file = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
            controller.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        captureState = captureState.copy(hasPhoto = true, photoPath = file.absolutePath)
                    }
                    override fun onError(exception: ImageCaptureException) { }
                }
            )
        },
        onBack = onBack
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CameraPreview(
    galleryLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    onCapture: (LifecycleCameraController) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val camController = remember { LifecycleCameraController(context) }

    LaunchedEffect(lifecycleOwner) {
        camController.bindToLifecycle(lifecycleOwner)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    previewView.controller = camController
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .padding(horizontal = 16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.PhotoLibrary, "从相册选择", tint = Color.White, modifier = Modifier.size(28.dp))
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onCapture(camController) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

@Composable
private fun PermissionRequest(showRationale: Boolean, onRequestPermission: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (showRationale) "需要相机权限才能拍照" else "请授予相机权限",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRequestPermission) { Text("授予权限") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditClothingScreen(
    captureState: CaptureState,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onSeasonChange: (String) -> Unit,
    onStyleChange: (String) -> Unit,
    showCategoryMenu: Boolean,
    showSeasonMenu: Boolean,
    showStyleMenu: Boolean,
    onToggleCategoryMenu: () -> Unit,
    onToggleSeasonMenu: () -> Unit,
    onToggleStyleMenu: () -> Unit,
    onRetakePhoto: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val categories = listOf("上衣", "下装", "外套", "连衣裙", "鞋", "配饰")
    val seasons = listOf("春秋", "夏", "冬")
    val styles = listOf("休闲", "商务", "运动", "正式")
    val colors = listOf("白色", "黑色", "红色", "蓝色", "绿色", "黄色", "紫色", "粉色", "灰色", "棕色", "橙色", "其他")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("编辑衣物信息") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") }
            }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = captureState.name,
                onValueChange = onNameChange,
                label = { Text("衣物名称") },
                placeholder = { Text("如：白色衬衫") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = showCategoryMenu,
                onExpandedChange = { onToggleCategoryMenu() }
            ) {
                OutlinedTextField(
                    value = captureState.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("类别") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { if (showCategoryMenu) onToggleCategoryMenu() }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = { onCategoryChange(cat); if (showCategoryMenu) onToggleCategoryMenu() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("颜色", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                colors.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { colorName ->
                            FilterChip(
                                selected = captureState.color == colorName,
                                onClick = { onColorChange(colorName) },
                                label = { Text(colorName, fontSize = 12.sp) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier.size(12.dp).clip(CircleShape).background(colorFromName(colorName))
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = showSeasonMenu,
                onExpandedChange = { onToggleSeasonMenu() }
            ) {
                OutlinedTextField(
                    value = captureState.season,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("季节") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSeasonMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = showSeasonMenu,
                    onDismissRequest = { if (showSeasonMenu) onToggleSeasonMenu() }
                ) {
                    seasons.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = { onSeasonChange(s); if (showSeasonMenu) onToggleSeasonMenu() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = showStyleMenu,
                onExpandedChange = { onToggleStyleMenu() }
            ) {
                OutlinedTextField(
                    value = captureState.style,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("风格") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStyleMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = showStyleMenu,
                    onDismissRequest = { if (showStyleMenu) onToggleStyleMenu() }
                ) {
                    styles.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = { onStyleChange(s); if (showStyleMenu) onToggleStyleMenu() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRetakePhoto,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("重拍")
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("保存")
                }
            }
        }
    }
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
