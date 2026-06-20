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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.tiantian.wardrobe.ai.VisionAnalysisResult
import com.tiantian.wardrobe.viewmodel.MainViewModel
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
    val description: String = ""
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: MainViewModel,
    initialPhotoPath: String? = null,
    onPhotoCaptured: (String) -> Unit = {},
    onSave: (name: String, imagePath: String, category: String, color: String, season: String, style: String, description: String) -> Unit,
    onBack: () -> Unit,
    onOpenVisionSettings: () -> Unit
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current
    var captureState by remember {
        mutableStateOf(
            if (initialPhotoPath != null && initialPhotoPath.isNotEmpty()) {
                CaptureState(hasPhoto = true, photoPath = initialPhotoPath)
            } else {
                CaptureState()
            }
        )
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val dest = File(context.filesDir, "gallery_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(it)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            captureState = captureState.copy(hasPhoto = true, photoPath = dest.absolutePath)
            onPhotoCaptured(dest.absolutePath)
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted && !captureState.hasPhoto) {
            cameraPermission.launchPermissionRequest()
        }
    }

    if (captureState.hasPhoto) {
        EditClothingScreen(
            captureState = captureState,
            isVisionConfigured = viewModel.isVisionConfigured,
            onAnalyze = { path, callback -> viewModel.analyzeClothing(path, callback) },
            onNameChange = { captureState = captureState.copy(name = it) },
            onCategoryChange = { captureState = captureState.copy(category = it) },
            onColorChange = { captureState = captureState.copy(color = it) },
            onSeasonChange = { captureState = captureState.copy(season = it) },
            onStyleChange = { captureState = captureState.copy(style = it) },
            onDescriptionChange = { captureState = captureState.copy(description = it) },
            onRetakePhoto = {
                captureState = CaptureState()
            },
            onSave = {
                onSave(
                    captureState.name,
                    captureState.photoPath,
                    captureState.category,
                    captureState.color,
                    captureState.season,
                    captureState.style,
                    captureState.description
                )
            },
            onOpenVisionSettings = onOpenVisionSettings,
            onBack = onBack
        )
        return
    }

    if (!cameraPermission.status.isGranted) {
        PermissionRequest(
            showRationale = cameraPermission.status.shouldShowRationale,
            onRequestPermission = { cameraPermission.launchPermissionRequest() }
        )
        return
    }

    CameraPreview(
        galleryLauncher = galleryLauncher,
        onCapture = { controller ->
            val file = File(context.filesDir, "capture_${System.currentTimeMillis()}.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
            controller.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        captureState = captureState.copy(hasPhoto = true, photoPath = file.absolutePath)
                        onPhotoCaptured(file.absolutePath)
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

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    previewView.controller = camController
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Back button
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 16.dp)
                .size(40.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.4f),
            onClick = onBack
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
            }
        }

        // Bottom controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery button
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.4f),
                onClick = { galleryLauncher.launch("image/*") }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.PhotoLibrary, "从相册选择", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }

            // Capture button
            Surface(
                modifier = Modifier.size(68.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f),
                onClick = { onCapture(camController) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            // Spacer to balance the row
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
private fun PermissionRequest(showRationale: Boolean, onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (showRationale) "需要相机权限才能拍照" else "请授予相机权限",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onRequestPermission,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("授予权限", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditClothingScreen(
    captureState: CaptureState,
    isVisionConfigured: Boolean,
    onAnalyze: (String, (VisionAnalysisResult?, String?) -> Unit) -> Unit,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onSeasonChange: (String) -> Unit,
    onStyleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onRetakePhoto: () -> Unit,
    onSave: () -> Unit,
    onOpenVisionSettings: () -> Unit,
    onBack: () -> Unit
) {
    val categories = listOf("上衣", "下装", "外套", "连衣裙", "鞋", "配饰")
    val seasons = listOf("春秋", "夏", "冬")
    val styles = listOf("休闲", "商务", "运动", "正式")
    val colors = listOf("白色", "黑色", "红色", "蓝色", "绿色", "黄色", "紫色", "粉色", "灰色", "棕色", "橙色", "其他")

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showColorMenu by remember { mutableStateOf(false) }
    var showSeasonMenu by remember { mutableStateOf(false) }
    var showStyleMenu by remember { mutableStateOf(false) }

    var analyzing by remember { mutableStateOf(false) }
    var analyzeError by remember { mutableStateOf<String?>(null) }
    var showNotConfiguredDialog by remember { mutableStateOf(false) }
    var analysisTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!analysisTriggered) {
            analysisTriggered = true
            if (isVisionConfigured) {
                analyzing = true
                onAnalyze(captureState.photoPath) { result, error ->
                    analyzing = false
                    if (result != null) {
                        onNameChange(result.name)
                        onCategoryChange(result.category)
                        onColorChange(result.color)
                        onSeasonChange(result.season)
                        onStyleChange(result.style)
                        onDescriptionChange(result.description)
                    } else {
                        analyzeError = error ?: "AI 识别失败，请手动填写"
                    }
                }
            } else {
                showNotConfiguredDialog = true
            }
        }
    }

    if (showNotConfiguredDialog) {
        AlertDialog(
            onDismissRequest = { showNotConfiguredDialog = false },
            title = { Text("需要配置视觉模型", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "拍照识别功能需要先配置豆包视觉模型 API，配置后即可自动识别衣物类别、颜色、风格等信息。\n\n是否前往配置？",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(onClick = {
                    showNotConfiguredDialog = false
                    onOpenVisionSettings()
                }) { Text("去配置") }
            },
            dismissButton = {
                TextButton(onClick = { showNotConfiguredDialog = false }) {
                    Text("手动填写", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Custom header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            Text(
                "编辑衣物",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Photo display
            Surface(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = File(captureState.photoPath),
                        contentDescription = "衣物照片",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (analyzing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "AI 正在识别衣物...",
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            analyzeError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            analyzeError = null
                            analyzing = true
                            onAnalyze(captureState.photoPath) { result, error2 ->
                                analyzing = false
                                if (result != null) {
                                    onNameChange(result.name)
                                    onCategoryChange(result.category)
                                    onColorChange(result.color)
                                    onSeasonChange(result.season)
                                    onStyleChange(result.style)
                                    onDescriptionChange(result.description)
                                } else {
                                    analyzeError = error2 ?: "AI 识别失败，请手动填写"
                                }
                            }
                        }) {
                            Text("重试", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name field
            OutlinedTextField(
                value = captureState.name,
                onValueChange = onNameChange,
                label = { Text("衣物名称") },
                placeholder = { Text("如：白色衬衫") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category dropdown
            DropdownField(
                label = "类别",
                value = captureState.category,
                options = categories,
                expanded = showCategoryMenu,
                onExpandedChange = { showCategoryMenu = it },
                onOptionSelected = { onCategoryChange(it); showCategoryMenu = false }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Color dropdown
            DropdownField(
                label = "颜色",
                value = captureState.color,
                options = colors,
                expanded = showColorMenu,
                onExpandedChange = { showColorMenu = it },
                onOptionSelected = { onColorChange(it); showColorMenu = false },
                leadingIcon = {
                    if (captureState.color.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(colorFromName(captureState.color))
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Season dropdown
            DropdownField(
                label = "季节",
                value = captureState.season,
                options = seasons,
                expanded = showSeasonMenu,
                onExpandedChange = { showSeasonMenu = it },
                onOptionSelected = { onSeasonChange(it); showSeasonMenu = false }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Style dropdown
            DropdownField(
                label = "风格",
                value = captureState.style,
                options = styles,
                expanded = showStyleMenu,
                onExpandedChange = { showStyleMenu = it },
                onOptionSelected = { onStyleChange(it); showStyleMenu = false }
            )

            if (captureState.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(modifier = Modifier.padding(14.dp)) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = captureState.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
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
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("重拍", fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !analyzing,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("保存", fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOptionSelected: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(it) }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            leadingIcon = leadingIcon,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onOptionSelected(option) }
                )
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
