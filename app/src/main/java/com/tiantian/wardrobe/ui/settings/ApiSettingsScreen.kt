package com.tiantian.wardrobe.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiantian.wardrobe.data.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiSettingsScreen(
    prefs: PreferencesManager,
    onBack: () -> Unit
) {
    var apiKey by remember { mutableStateOf(prefs.apiKey) }
    var apiEndpoint by remember { mutableStateOf(prefs.apiEndpoint) }
    var modelName by remember { mutableStateOf(prefs.modelName) }
    var showKey by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("AI 接口配置") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "配置说明",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("支持所有 OpenAI 兼容接口：", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    BulletText("OpenAI (api.openai.com)")
                    BulletText("DeepSeek (api.deepseek.com)")
                    BulletText("通义千问 (dashscope.aliyuncs.com/compatible-mode/v1)")
                    BulletText("本地 Ollama (http://192.168.x.x:11434/v1)")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = apiEndpoint,
                onValueChange = { apiEndpoint = it; saved = false },
                label = { Text("API 地址") },
                placeholder = { Text(PreferencesManager.DEFAULT_ENDPOINT) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it; saved = false },
                label = { Text("API Key") },
                placeholder = { Text("sk-...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showKey) "隐藏" else "显示"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = modelName,
                onValueChange = { modelName = it; saved = false },
                label = { Text("模型名称") },
                placeholder = { Text(PreferencesManager.DEFAULT_MODEL) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Memory, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "常用模型：gpt-4o-mini / gpt-4o / deepseek-chat / qwen-turbo",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    prefs.apiKey = apiKey
                    prefs.apiEndpoint = apiEndpoint
                    prefs.modelName = modelName
                    saved = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存配置")
            }

            if (saved) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✓ 配置已保存",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            if (prefs.isConfigured) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { prefs.clear(); apiKey = ""; saved = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("清除 API Key")
                }
            }
        }
    }
}

@Composable
private fun BulletText(text: String) {
    Row(modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
        Text("• ", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
