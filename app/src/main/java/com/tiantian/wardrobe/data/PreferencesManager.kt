package com.tiantian.wardrobe.data

import android.content.Context
import androidx.core.content.edit

class PreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences("tiantian_wardrobe_prefs", Context.MODE_PRIVATE)

    // ---- LLM 推荐接口 (OpenAI 兼容) ----
    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit { putString(KEY_API_KEY, value) }

    var apiEndpoint: String
        get() = prefs.getString(KEY_API_ENDPOINT, DEFAULT_ENDPOINT) ?: DEFAULT_ENDPOINT
        set(value) = prefs.edit { putString(KEY_API_ENDPOINT, value) }

    var modelName: String
        get() = prefs.getString(KEY_MODEL_NAME, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs.edit { putString(KEY_MODEL_NAME, value) }

    val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    fun clear() {
        prefs.edit {
            remove(KEY_API_KEY)
            remove(KEY_API_ENDPOINT)
            remove(KEY_MODEL_NAME)
        }
    }

    // ---- 视觉模型接口 (豆包 Doubao / 火山方舟) ----
    var visionApiKey: String
        get() = prefs.getString(KEY_VISION_API_KEY, "") ?: ""
        set(value) = prefs.edit { putString(KEY_VISION_API_KEY, value) }

    var visionApiEndpoint: String
        get() = prefs.getString(KEY_VISION_API_ENDPOINT, DEFAULT_VISION_ENDPOINT) ?: DEFAULT_VISION_ENDPOINT
        set(value) = prefs.edit { putString(KEY_VISION_API_ENDPOINT, value) }

    var visionModelName: String
        get() {
            val saved = prefs.getString(KEY_VISION_MODEL_NAME, DEFAULT_VISION_MODEL) ?: DEFAULT_VISION_MODEL
            if (saved == OLD_VISION_MODEL_DEPRECATED) {
                prefs.edit { putString(KEY_VISION_MODEL_NAME, DEFAULT_VISION_MODEL) }
                return DEFAULT_VISION_MODEL
            }
            return saved
        }
        set(value) = prefs.edit { putString(KEY_VISION_MODEL_NAME, value) }

    val isVisionConfigured: Boolean
        get() = visionApiKey.isNotBlank()

    fun clearVision() {
        prefs.edit {
            remove(KEY_VISION_API_KEY)
            remove(KEY_VISION_API_ENDPOINT)
            remove(KEY_VISION_MODEL_NAME)
        }
    }

    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_API_ENDPOINT = "api_endpoint"
        private const val KEY_MODEL_NAME = "model_name"

        private const val KEY_VISION_API_KEY = "vision_api_key"
        private const val KEY_VISION_API_ENDPOINT = "vision_api_endpoint"
        private const val KEY_VISION_MODEL_NAME = "vision_model_name"

        const val DEFAULT_ENDPOINT = "https://api.openai.com/v1"
        const val DEFAULT_MODEL = "doubao-1-5-pro-32k-250115"

        const val DEFAULT_VISION_ENDPOINT = "https://ark.cn-beijing.volces.com/api/v3"
        const val DEFAULT_VISION_MODEL = "doubao-1.5-vision-pro-250328"
        private const val OLD_VISION_MODEL_DEPRECATED = "doubao-1.5-vision-pro-32k"
    }
}
