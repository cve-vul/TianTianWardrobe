package com.tiantian.wardrobe.data

import android.content.Context
import androidx.core.content.edit

class PreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences("tiantian_wardrobe_prefs", Context.MODE_PRIVATE)

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

    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_API_ENDPOINT = "api_endpoint"
        private const val KEY_MODEL_NAME = "model_name"
        const val DEFAULT_ENDPOINT = "https://api.openai.com/v1"
        const val DEFAULT_MODEL = "gpt-4o-mini"
    }
}
