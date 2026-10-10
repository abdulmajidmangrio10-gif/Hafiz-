package com.example.data.model

import java.util.UUID

/**
 * Core models representing the video editing project, timeline, and clips.
 */
data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Quran Video",
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val videoUri: String? = null,
    val videoDurationMs: Long = 15000L,
    val originalWidth: Int = 1080,
    val originalHeight: Int = 1920,
    val canvasSettings: CanvasSettings = CanvasSettings(),
    val videoSettings: VideoClipSettings = VideoClipSettings(),
    val videoAdjustments: VideoAdjustments = VideoAdjustments(),
    val activeFilter: VideoFilter = VideoFilter.NONE,
    val activeEffect: VideoEffect = VideoEffect.NONE,
    val quranClips: List<QuranClip> = emptyList(),
    val textClips: List<TextClip> = emptyList(),
    val imageClips: List<ImageClip> = emptyList(),
    val stickerClips: List<StickerClip> = emptyList(),
    val audioClips: List<AudioClip> = emptyList(),
    val exportSettings: ExportSettings = ExportSettings()
)

data class CanvasSettings(
    val aspectRatio: AspectRatioType = AspectRatioType.ORIGINAL,
    val backgroundColorHex: String = "#000000",
    val blurBackground: Boolean = false,
    val blurRadius: Float = 15f,
    val fitMode: FitMode = FitMode.FIT,
    val zoom: Float = 1.0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)

enum class AspectRatioType(val label: String, val ratio: Float?) {
    ORIGINAL("Original", null),
    PORTRAIT_9_16("9:16", 9f / 16f),
    LANDSCAPE_16_9("16:9", 16f / 9f),
    SQUARE_1_1("1:1", 1f),
    PORTRAIT_4_5("4:5", 4f / 5f)
}

enum class FitMode {
    FIT, FILL, CROP
}

data class VideoClipSettings(
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L,
    val speed: Float = 1.0f,
    val rotationDegrees: Int = 0,
    val isFlippedHorizontal: Boolean = false,
    val isFlippedVertical: Boolean = false,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false
)

data class VideoAdjustments(
    val brightness: Float = 0.0f,      // -1.0 to 1.0
    val contrast: Float = 1.0f,        // 0.5 to 2.0
    val saturation: Float = 1.0f,      // 0.0 to 2.0
    val exposure: Float = 0.0f,        // -1.0 to 1.0
    val sharpness: Float = 0.0f,       // 0.0 to 1.0
    val temperature: Float = 0.0f,     // -1.0 (cool) to 1.0 (warm)
    val highlights: Float = 0.0f,      // -1.0 to 1.0
    val shadows: Float = 0.0f          // -1.0 to 1.0
)

enum class VideoFilter(val displayName: String) {
    NONE("Normal"),
    NATURAL("Natural"),
    WARM("Warm"),
    VINTAGE("Vintage"),
    YELLOW("Golden"),
    CINEMATIC("Cinematic"),
    BLACK_WHITE("B&W")
}

enum class VideoEffect(val displayName: String) {
    NONE("None"),
    BLUR("Blur"),
    GLITCH("Glitch"),
    CINEMATIC("Cinematic"),
    LIGHT("Light Leak"),
    SHAKE("Shake"),
    ZOOM("Zoom Pulse"),
    FADE("Fade"),
    VIGNETTE("Vignette")
}

data class QuranClip(
    val id: String = UUID.randomUUID().toString(),
    val surahNumber: Int,
    val surahNameArabic: String,
    val surahNameEnglish: String,
    val surahNameUrdu: String,
    val startAyah: Int,
    val endAyah: Int,
    val arabicText: String,
    val urduTranslation: String,
    val startTimeMs: Long = 0L,
    val durationMs: Long = 5000L,
    val style: QuranCaptionStyle = QuranCaptionStyle()
)

data class QuranCaptionStyle(
    val arabicFont: ArabicFont = ArabicFont.AMIRI,
    val translationFont: TranslationFont = TranslationFont.DEFAULT,
    val arabicFontSizeSp: Float = 26f,
    val translationFontSizeSp: Float = 16f,
    val arabicColorHex: String = "#FFD56B",     // Quran Gold
    val translationColorHex: String = "#FFFFFF",
    val hasOutline: Boolean = true,
    val outlineColorHex: String = "#000000",
    val outlineThickness: Float = 2.0f,
    val hasShadow: Boolean = true,
    val shadowColorHex: String = "#000000",
    val shadowOpacity: Float = 0.8f,
    val shadowRadius: Float = 4.0f,
    val backgroundType: CaptionBackground = CaptionBackground.ROUNDED_BOX,
    val backgroundColorHex: String = "#80000000",
    val positionYFraction: Float = 0.78f,       // 0.0 top, 1.0 bottom
    val positionXFraction: Float = 0.5f,        // 0.0 left, 1.0 right
    val alignment: TextAlignment = TextAlignment.CENTER,
    val arabicLineSpacing: Float = 1.3f,
    val translationSpacing: Float = 1.1f,
    val showTranslation: Boolean = true,
    val animation: OverlayAnimation = OverlayAnimation.FADE
)

enum class ArabicFont(val displayName: String) {
    AMIRI("Amiri (Quranic)"),
    NASKH("Uthmani Naskh"),
    MODERN("Modern Arabic"),
    QURANIC_BOLD("Quranic Bold")
}

enum class TranslationFont(val displayName: String) {
    DEFAULT("Standard"),
    NASTALIQ("Urdu Nastaliq Style"),
    ELEGANT("Elegant Serif")
}

enum class CaptionBackground(val displayName: String) {
    TRANSPARENT("Transparent"),
    SOLID("Solid Black"),
    ROUNDED_BOX("Dark Glass Box"),
    GRADIENT("Subtle Gradient")
}

enum class TextAlignment {
    LEFT, CENTER, RIGHT
}

enum class OverlayAnimation(val displayName: String) {
    NONE("None"),
    FADE("Fade"),
    SLIDE_UP("Slide Up"),
    SLIDE_DOWN("Slide Down"),
    ZOOM_IN("Zoom In"),
    ZOOM_OUT("Zoom Out")
}

data class TextClip(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "Sample Text",
    val startTimeMs: Long = 0L,
    val durationMs: Long = 4000L,
    val positionX: Float = 0.5f,
    val positionY: Float = 0.5f,
    val fontSizeSp: Float = 22f,
    val textColorHex: String = "#FFFFFF",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val hasOutline: Boolean = false,
    val outlineColorHex: String = "#000000",
    val hasShadow: Boolean = true,
    val shadowColorHex: String = "#000000",
    val backgroundColorHex: String = "#00000000",
    val opacity: Float = 1.0f,
    val alignment: TextAlignment = TextAlignment.CENTER,
    val animation: OverlayAnimation = OverlayAnimation.FADE
)

data class ImageClip(
    val id: String = UUID.randomUUID().toString(),
    val imageUri: String,
    val startTimeMs: Long = 0L,
    val durationMs: Long = 5000L,
    val positionX: Float = 0.5f,
    val positionY: Float = 0.5f,
    val scale: Float = 1.0f,
    val rotationDegrees: Float = 0f,
    val opacity: Float = 1.0f,
    val animation: OverlayAnimation = OverlayAnimation.NONE
)

data class StickerClip(
    val id: String = UUID.randomUUID().toString(),
    val stickerKey: String,
    val category: StickerCategory = StickerCategory.ISLAMIC,
    val startTimeMs: Long = 0L,
    val durationMs: Long = 5000L,
    val positionX: Float = 0.5f,
    val positionY: Float = 0.5f,
    val scale: Float = 1.0f,
    val rotationDegrees: Float = 0f,
    val opacity: Float = 1.0f,
    val animation: OverlayAnimation = OverlayAnimation.FADE
)

enum class StickerCategory(val displayName: String) {
    ISLAMIC("Islamic"),
    QURAN("Quran"),
    ARABIC("Arabic Calligraphy"),
    DECORATIVE("Decorative"),
    SHAPES("Shapes")
}

data class AudioClip(
    val id: String = UUID.randomUUID().toString(),
    val audioUri: String,
    val title: String = "Audio Track",
    val startTimeMs: Long = 0L,
    val durationMs: Long = 10000L,
    val volume: Float = 1.0f,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val speed: Float = 1.0f,
    val isMuted: Boolean = false
)

data class ExportSettings(
    val resolution: ExportResolution = ExportResolution.RES_1080P,
    val fps: Int = 30,
    val quality: ExportQuality = ExportQuality.HIGH,
    val format: String = "MP4",
    val highQualityAudio: Boolean = true,
    val includeWatermark: Boolean = false,
    val watermarkText: String = "Quran Video Editor"
)

enum class ExportResolution(val label: String, val width: Int, val height: Int) {
    RES_480P("480p SD", 854, 480),
    RES_720P("720p HD", 1280, 720),
    RES_1080P("1080p Full HD", 1920, 1080),
    RES_ORIGINAL("Original Resolution", 0, 0)
}

enum class ExportQuality(val label: String, val bitrateMultiplier: Float) {
    LOW("Low", 0.6f),
    MEDIUM("Medium", 1.0f),
    HIGH("High", 1.5f),
    MAXIMUM("Maximum", 2.2f)
}
