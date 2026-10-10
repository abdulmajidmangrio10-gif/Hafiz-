package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val displayName: String) {
    DARK("Dark"),
    LIGHT("Light"),
    SYSTEM("System")
}

enum class AccentColorChoice(val displayName: String, val hex: String) {
    GOLD("Warm Quran Gold", "#E5A93C"),
    TEAL("Islamic Teal", "#26A69A"),
    BLUE("Royal Blue", "#388BFD"),
    PURPLE("Majestic Purple", "#9C27B0"),
    GRAY("Minimal Charcoal", "#8B949E")
}

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val isRtl: Boolean) {
    ENGLISH("en", "English", "English", false),
    URDU("ur", "Urdu", "اردو", true),
    ARABIC("ar", "Arabic", "العربية", true)
}

enum class EditorIconSize(val displayName: String, val dp: Int) {
    SMALL("Small", 20),
    MEDIUM("Medium", 24),
    LARGE("Large", 28)
}

enum class FeatureStatus {
    KEEP, HIDE, DISABLE
}

data class UserSettings(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val accentColor: AccentColorChoice = AccentColorChoice.GOLD,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val iconSize: EditorIconSize = EditorIconSize.MEDIUM,
    val textSizeScale: Float = 1.0f,
    val featureStatusMap: Map<String, FeatureStatus> = mapOf(
        "Quran" to FeatureStatus.KEEP,
        "Auto Caption" to FeatureStatus.KEEP,
        "Audio" to FeatureStatus.KEEP,
        "Text" to FeatureStatus.KEEP,
        "Image" to FeatureStatus.KEEP,
        "Sticker" to FeatureStatus.KEEP,
        "Effects" to FeatureStatus.KEEP,
        "Filter" to FeatureStatus.KEEP,
        "Adjust" to FeatureStatus.KEEP,
        "Speed" to FeatureStatus.KEEP,
        "Canvas" to FeatureStatus.KEEP,
        "Video" to FeatureStatus.KEEP
    ),
    val aiSpeechApiKey: String = ""
)

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("quran_editor_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val theme = AppThemeMode.valueOf(prefs.getString("theme", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name)
        val accent = AccentColorChoice.valueOf(prefs.getString("accent", AccentColorChoice.GOLD.name) ?: AccentColorChoice.GOLD.name)
        val lang = AppLanguage.valueOf(prefs.getString("lang", AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name)
        val icon = EditorIconSize.valueOf(prefs.getString("icon_size", EditorIconSize.MEDIUM.name) ?: EditorIconSize.MEDIUM.name)
        val scale = prefs.getFloat("text_scale", 1.0f)
        val apiKey = prefs.getString("ai_api_key", "") ?: ""

        val defaultFeatures = UserSettings().featureStatusMap.toMutableMap()
        for (key in defaultFeatures.keys) {
            val statusStr = prefs.getString("feature_$key", FeatureStatus.KEEP.name)
            defaultFeatures[key] = try { FeatureStatus.valueOf(statusStr ?: FeatureStatus.KEEP.name) } catch (e: Exception) { FeatureStatus.KEEP }
        }

        return UserSettings(
            themeMode = theme,
            accentColor = accent,
            language = lang,
            iconSize = icon,
            textSizeScale = scale,
            featureStatusMap = defaultFeatures,
            aiSpeechApiKey = apiKey
        )
    }

    fun updateTheme(mode: AppThemeMode) {
        prefs.edit().putString("theme", mode.name).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun updateAccent(accent: AccentColorChoice) {
        prefs.edit().putString("accent", accent.name).apply()
        _settings.value = _settings.value.copy(accentColor = accent)
    }

    fun updateLanguage(lang: AppLanguage) {
        prefs.edit().putString("lang", lang.name).apply()
        _settings.value = _settings.value.copy(language = lang)
    }

    fun updateIconSize(size: EditorIconSize) {
        prefs.edit().putString("icon_size", size.name).apply()
        _settings.value = _settings.value.copy(iconSize = size)
    }

    fun updateTextSizeScale(scale: Float) {
        prefs.edit().putFloat("text_scale", scale).apply()
        _settings.value = _settings.value.copy(textSizeScale = scale)
    }

    fun updateFeatureStatus(featureName: String, status: FeatureStatus) {
        prefs.edit().putString("feature_$featureName", status.name).apply()
        val updated = _settings.value.featureStatusMap.toMutableMap()
        updated[featureName] = status
        _settings.value = _settings.value.copy(featureStatusMap = updated)
    }

    fun updateAiApiKey(key: String) {
        prefs.edit().putString("ai_api_key", key).apply()
        _settings.value = _settings.value.copy(aiSpeechApiKey = key)
    }
}
