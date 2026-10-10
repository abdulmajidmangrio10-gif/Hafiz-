package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val videoUri: String?,
    val videoDurationMs: Long,
    val originalWidth: Int,
    val originalHeight: Int,
    val canvasSettingsJson: String,
    val videoSettingsJson: String,
    val videoAdjustmentsJson: String,
    val activeFilter: String,
    val activeEffect: String,
    val quranClipsJson: String,
    val textClipsJson: String,
    val imageClipsJson: String,
    val stickerClipsJson: String,
    val audioClipsJson: String,
    val exportSettingsJson: String
)

object ProjectJsonMapper {
    fun toEntity(project: Project): ProjectEntity {
        return ProjectEntity(
            id = project.id,
            name = project.name,
            createdAt = project.createdAt,
            modifiedAt = project.modifiedAt,
            videoUri = project.videoUri,
            videoDurationMs = project.videoDurationMs,
            originalWidth = project.originalWidth,
            originalHeight = project.originalHeight,
            canvasSettingsJson = serializeCanvas(project.canvasSettings),
            videoSettingsJson = serializeVideoSettings(project.videoSettings),
            videoAdjustmentsJson = serializeAdjustments(project.videoAdjustments),
            activeFilter = project.activeFilter.name,
            activeEffect = project.activeEffect.name,
            quranClipsJson = serializeQuranClips(project.quranClips),
            textClipsJson = serializeTextClips(project.textClips),
            imageClipsJson = serializeImageClips(project.imageClips),
            stickerClipsJson = serializeStickerClips(project.stickerClips),
            audioClipsJson = serializeAudioClips(project.audioClips),
            exportSettingsJson = serializeExportSettings(project.exportSettings)
        )
    }

    fun toDomain(entity: ProjectEntity): Project {
        return Project(
            id = entity.id,
            name = entity.name,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt,
            videoUri = entity.videoUri,
            videoDurationMs = entity.videoDurationMs,
            originalWidth = entity.originalWidth,
            originalHeight = entity.originalHeight,
            canvasSettings = parseCanvas(entity.canvasSettingsJson),
            videoSettings = parseVideoSettings(entity.videoSettingsJson),
            videoAdjustments = parseAdjustments(entity.videoAdjustmentsJson),
            activeFilter = try { VideoFilter.valueOf(entity.activeFilter) } catch (e: Exception) { VideoFilter.NONE },
            activeEffect = try { VideoEffect.valueOf(entity.activeEffect) } catch (e: Exception) { VideoEffect.NONE },
            quranClips = parseQuranClips(entity.quranClipsJson),
            textClips = parseTextClips(entity.textClipsJson),
            imageClips = parseImageClips(entity.imageClipsJson),
            stickerClips = parseStickerClips(entity.stickerClipsJson),
            audioClips = parseAudioClips(entity.audioClipsJson),
            exportSettings = parseExportSettings(entity.exportSettingsJson)
        )
    }

    private fun serializeCanvas(c: CanvasSettings): String {
        return JSONObject().apply {
            put("aspectRatio", c.aspectRatio.name)
            put("backgroundColorHex", c.backgroundColorHex)
            put("blurBackground", c.blurBackground)
            put("blurRadius", c.blurRadius.toDouble())
            put("fitMode", c.fitMode.name)
            put("zoom", c.zoom.toDouble())
            put("offsetX", c.offsetX.toDouble())
            put("offsetY", c.offsetY.toDouble())
        }.toString()
    }

    private fun parseCanvas(json: String): CanvasSettings {
        return try {
            val obj = JSONObject(json)
            CanvasSettings(
                aspectRatio = AspectRatioType.valueOf(obj.optString("aspectRatio", AspectRatioType.ORIGINAL.name)),
                backgroundColorHex = obj.optString("backgroundColorHex", "#000000"),
                blurBackground = obj.optBoolean("blurBackground", false),
                blurRadius = obj.optDouble("blurRadius", 15.0).toFloat(),
                fitMode = FitMode.valueOf(obj.optString("fitMode", FitMode.FIT.name)),
                zoom = obj.optDouble("zoom", 1.0).toFloat(),
                offsetX = obj.optDouble("offsetX", 0.0).toFloat(),
                offsetY = obj.optDouble("offsetY", 0.0).toFloat()
            )
        } catch (e: Exception) {
            CanvasSettings()
        }
    }

    private fun serializeVideoSettings(v: VideoClipSettings): String {
        return JSONObject().apply {
            put("trimStartMs", v.trimStartMs)
            put("trimEndMs", v.trimEndMs)
            put("speed", v.speed.toDouble())
            put("rotationDegrees", v.rotationDegrees)
            put("isFlippedHorizontal", v.isFlippedHorizontal)
            put("isFlippedVertical", v.isFlippedVertical)
            put("volume", v.volume.toDouble())
            put("isMuted", v.isMuted)
        }.toString()
    }

    private fun parseVideoSettings(json: String): VideoClipSettings {
        return try {
            val obj = JSONObject(json)
            VideoClipSettings(
                trimStartMs = obj.optLong("trimStartMs", 0L),
                trimEndMs = obj.optLong("trimEndMs", 0L),
                speed = obj.optDouble("speed", 1.0).toFloat(),
                rotationDegrees = obj.optInt("rotationDegrees", 0),
                isFlippedHorizontal = obj.optBoolean("isFlippedHorizontal", false),
                isFlippedVertical = obj.optBoolean("isFlippedVertical", false),
                volume = obj.optDouble("volume", 1.0).toFloat(),
                isMuted = obj.optBoolean("isMuted", false)
            )
        } catch (e: Exception) {
            VideoClipSettings()
        }
    }

    private fun serializeAdjustments(a: VideoAdjustments): String {
        return JSONObject().apply {
            put("brightness", a.brightness.toDouble())
            put("contrast", a.contrast.toDouble())
            put("saturation", a.saturation.toDouble())
            put("exposure", a.exposure.toDouble())
            put("sharpness", a.sharpness.toDouble())
            put("temperature", a.temperature.toDouble())
            put("highlights", a.highlights.toDouble())
            put("shadows", a.shadows.toDouble())
        }.toString()
    }

    private fun parseAdjustments(json: String): VideoAdjustments {
        return try {
            val obj = JSONObject(json)
            VideoAdjustments(
                brightness = obj.optDouble("brightness", 0.0).toFloat(),
                contrast = obj.optDouble("contrast", 1.0).toFloat(),
                saturation = obj.optDouble("saturation", 1.0).toFloat(),
                exposure = obj.optDouble("exposure", 0.0).toFloat(),
                sharpness = obj.optDouble("sharpness", 0.0).toFloat(),
                temperature = obj.optDouble("temperature", 0.0).toFloat(),
                highlights = obj.optDouble("highlights", 0.0).toFloat(),
                shadows = obj.optDouble("shadows", 0.0).toFloat()
            )
        } catch (e: Exception) {
            VideoAdjustments()
        }
    }

    private fun serializeQuranClips(clips: List<QuranClip>): String {
        val array = JSONArray()
        for (c in clips) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("surahNumber", c.surahNumber)
                put("surahNameArabic", c.surahNameArabic)
                put("surahNameEnglish", c.surahNameEnglish)
                put("surahNameUrdu", c.surahNameUrdu)
                put("startAyah", c.startAyah)
                put("endAyah", c.endAyah)
                put("arabicText", c.arabicText)
                put("urduTranslation", c.urduTranslation)
                put("startTimeMs", c.startTimeMs)
                put("durationMs", c.durationMs)
                put("style", JSONObject().apply {
                    put("arabicFont", c.style.arabicFont.name)
                    put("translationFont", c.style.translationFont.name)
                    put("arabicFontSizeSp", c.style.arabicFontSizeSp.toDouble())
                    put("translationFontSizeSp", c.style.translationFontSizeSp.toDouble())
                    put("arabicColorHex", c.style.arabicColorHex)
                    put("translationColorHex", c.style.translationColorHex)
                    put("hasOutline", c.style.hasOutline)
                    put("outlineColorHex", c.style.outlineColorHex)
                    put("outlineThickness", c.style.outlineThickness.toDouble())
                    put("hasShadow", c.style.hasShadow)
                    put("shadowColorHex", c.style.shadowColorHex)
                    put("shadowOpacity", c.style.shadowOpacity.toDouble())
                    put("shadowRadius", c.style.shadowRadius.toDouble())
                    put("backgroundType", c.style.backgroundType.name)
                    put("backgroundColorHex", c.style.backgroundColorHex)
                    put("positionYFraction", c.style.positionYFraction.toDouble())
                    put("positionXFraction", c.style.positionXFraction.toDouble())
                    put("alignment", c.style.alignment.name)
                    put("arabicLineSpacing", c.style.arabicLineSpacing.toDouble())
                    put("translationSpacing", c.style.translationSpacing.toDouble())
                    put("showTranslation", c.style.showTranslation)
                    put("animation", c.style.animation.name)
                })
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseQuranClips(json: String): List<QuranClip> {
        val list = mutableListOf<QuranClip>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val styleObj = obj.optJSONObject("style")
                val style = if (styleObj != null) {
                    QuranCaptionStyle(
                        arabicFont = ArabicFont.valueOf(styleObj.optString("arabicFont", ArabicFont.AMIRI.name)),
                        translationFont = TranslationFont.valueOf(styleObj.optString("translationFont", TranslationFont.DEFAULT.name)),
                        arabicFontSizeSp = styleObj.optDouble("arabicFontSizeSp", 26.0).toFloat(),
                        translationFontSizeSp = styleObj.optDouble("translationFontSizeSp", 16.0).toFloat(),
                        arabicColorHex = styleObj.optString("arabicColorHex", "#FFD56B"),
                        translationColorHex = styleObj.optString("translationColorHex", "#FFFFFF"),
                        hasOutline = styleObj.optBoolean("hasOutline", true),
                        outlineColorHex = styleObj.optString("outlineColorHex", "#000000"),
                        outlineThickness = styleObj.optDouble("outlineThickness", 2.0).toFloat(),
                        hasShadow = styleObj.optBoolean("hasShadow", true),
                        shadowColorHex = styleObj.optString("shadowColorHex", "#000000"),
                        shadowOpacity = styleObj.optDouble("shadowOpacity", 0.8).toFloat(),
                        shadowRadius = styleObj.optDouble("shadowRadius", 4.0).toFloat(),
                        backgroundType = CaptionBackground.valueOf(styleObj.optString("backgroundType", CaptionBackground.ROUNDED_BOX.name)),
                        backgroundColorHex = styleObj.optString("backgroundColorHex", "#80000000"),
                        positionYFraction = styleObj.optDouble("positionYFraction", 0.78).toFloat(),
                        positionXFraction = styleObj.optDouble("positionXFraction", 0.5).toFloat(),
                        alignment = TextAlignment.valueOf(styleObj.optString("alignment", TextAlignment.CENTER.name)),
                        arabicLineSpacing = styleObj.optDouble("arabicLineSpacing", 1.3).toFloat(),
                        translationSpacing = styleObj.optDouble("translationSpacing", 1.1).toFloat(),
                        showTranslation = styleObj.optBoolean("showTranslation", true),
                        animation = OverlayAnimation.valueOf(styleObj.optString("animation", OverlayAnimation.FADE.name))
                    )
                } else QuranCaptionStyle()

                list.add(
                    QuranClip(
                        id = obj.getString("id"),
                        surahNumber = obj.getInt("surahNumber"),
                        surahNameArabic = obj.optString("surahNameArabic", ""),
                        surahNameEnglish = obj.optString("surahNameEnglish", ""),
                        surahNameUrdu = obj.optString("surahNameUrdu", ""),
                        startAyah = obj.getInt("startAyah"),
                        endAyah = obj.getInt("endAyah"),
                        arabicText = obj.getString("arabicText"),
                        urduTranslation = obj.getString("urduTranslation"),
                        startTimeMs = obj.getLong("startTimeMs"),
                        durationMs = obj.getLong("durationMs"),
                        style = style
                    )
                )
            }
        } catch (e: Exception) {
            // Log and return parsed so far
        }
        return list
    }

    private fun serializeTextClips(clips: List<TextClip>): String {
        val array = JSONArray()
        for (t in clips) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("text", t.text)
                put("startTimeMs", t.startTimeMs)
                put("durationMs", t.durationMs)
                put("positionX", t.positionX.toDouble())
                put("positionY", t.positionY.toDouble())
                put("fontSizeSp", t.fontSizeSp.toDouble())
                put("textColorHex", t.textColorHex)
                put("isBold", t.isBold)
                put("isItalic", t.isItalic)
                put("hasOutline", t.hasOutline)
                put("outlineColorHex", t.outlineColorHex)
                put("hasShadow", t.hasShadow)
                put("shadowColorHex", t.shadowColorHex)
                put("backgroundColorHex", t.backgroundColorHex)
                put("opacity", t.opacity.toDouble())
                put("alignment", t.alignment.name)
                put("animation", t.animation.name)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseTextClips(json: String): List<TextClip> {
        val list = mutableListOf<TextClip>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TextClip(
                        id = obj.getString("id"),
                        text = obj.getString("text"),
                        startTimeMs = obj.getLong("startTimeMs"),
                        durationMs = obj.getLong("durationMs"),
                        positionX = obj.optDouble("positionX", 0.5).toFloat(),
                        positionY = obj.optDouble("positionY", 0.5).toFloat(),
                        fontSizeSp = obj.optDouble("fontSizeSp", 22.0).toFloat(),
                        textColorHex = obj.optString("textColorHex", "#FFFFFF"),
                        isBold = obj.optBoolean("isBold", false),
                        isItalic = obj.optBoolean("isItalic", false),
                        hasOutline = obj.optBoolean("hasOutline", false),
                        outlineColorHex = obj.optString("outlineColorHex", "#000000"),
                        hasShadow = obj.optBoolean("hasShadow", true),
                        shadowColorHex = obj.optString("shadowColorHex", "#000000"),
                        backgroundColorHex = obj.optString("backgroundColorHex", "#00000000"),
                        opacity = obj.optDouble("opacity", 1.0).toFloat(),
                        alignment = TextAlignment.valueOf(obj.optString("alignment", TextAlignment.CENTER.name)),
                        animation = OverlayAnimation.valueOf(obj.optString("animation", OverlayAnimation.FADE.name))
                    )
                )
            }
        } catch (e: Exception) { }
        return list
    }

    private fun serializeImageClips(clips: List<ImageClip>): String {
        val array = JSONArray()
        for (im in clips) {
            val obj = JSONObject().apply {
                put("id", im.id)
                put("imageUri", im.imageUri)
                put("startTimeMs", im.startTimeMs)
                put("durationMs", im.durationMs)
                put("positionX", im.positionX.toDouble())
                put("positionY", im.positionY.toDouble())
                put("scale", im.scale.toDouble())
                put("rotationDegrees", im.rotationDegrees.toDouble())
                put("opacity", im.opacity.toDouble())
                put("animation", im.animation.name)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseImageClips(json: String): List<ImageClip> {
        val list = mutableListOf<ImageClip>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ImageClip(
                        id = obj.getString("id"),
                        imageUri = obj.getString("imageUri"),
                        startTimeMs = obj.getLong("startTimeMs"),
                        durationMs = obj.getLong("durationMs"),
                        positionX = obj.optDouble("positionX", 0.5).toFloat(),
                        positionY = obj.optDouble("positionY", 0.5).toFloat(),
                        scale = obj.optDouble("scale", 1.0).toFloat(),
                        rotationDegrees = obj.optDouble("rotationDegrees", 0.0).toFloat(),
                        opacity = obj.optDouble("opacity", 1.0).toFloat(),
                        animation = OverlayAnimation.valueOf(obj.optString("animation", OverlayAnimation.NONE.name))
                    )
                )
            }
        } catch (e: Exception) { }
        return list
    }

    private fun serializeStickerClips(clips: List<StickerClip>): String {
        val array = JSONArray()
        for (st in clips) {
            val obj = JSONObject().apply {
                put("id", st.id)
                put("stickerKey", st.stickerKey)
                put("category", st.category.name)
                put("startTimeMs", st.startTimeMs)
                put("durationMs", st.durationMs)
                put("positionX", st.positionX.toDouble())
                put("positionY", st.positionY.toDouble())
                put("scale", st.scale.toDouble())
                put("rotationDegrees", st.rotationDegrees.toDouble())
                put("opacity", st.opacity.toDouble())
                put("animation", st.animation.name)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseStickerClips(json: String): List<StickerClip> {
        val list = mutableListOf<StickerClip>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    StickerClip(
                        id = obj.getString("id"),
                        stickerKey = obj.getString("stickerKey"),
                        category = StickerCategory.valueOf(obj.optString("category", StickerCategory.ISLAMIC.name)),
                        startTimeMs = obj.getLong("startTimeMs"),
                        durationMs = obj.getLong("durationMs"),
                        positionX = obj.optDouble("positionX", 0.5).toFloat(),
                        positionY = obj.optDouble("positionY", 0.5).toFloat(),
                        scale = obj.optDouble("scale", 1.0).toFloat(),
                        rotationDegrees = obj.optDouble("rotationDegrees", 0.0).toFloat(),
                        opacity = obj.optDouble("opacity", 1.0).toFloat(),
                        animation = OverlayAnimation.valueOf(obj.optString("animation", OverlayAnimation.FADE.name))
                    )
                )
            }
        } catch (e: Exception) { }
        return list
    }

    private fun serializeAudioClips(clips: List<AudioClip>): String {
        val array = JSONArray()
        for (a in clips) {
            val obj = JSONObject().apply {
                put("id", a.id)
                put("audioUri", a.audioUri)
                put("title", a.title)
                put("startTimeMs", a.startTimeMs)
                put("durationMs", a.durationMs)
                put("volume", a.volume.toDouble())
                put("fadeInMs", a.fadeInMs)
                put("fadeOutMs", a.fadeOutMs)
                put("speed", a.speed.toDouble())
                put("isMuted", a.isMuted)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseAudioClips(json: String): List<AudioClip> {
        val list = mutableListOf<AudioClip>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AudioClip(
                        id = obj.getString("id"),
                        audioUri = obj.getString("audioUri"),
                        title = obj.optString("title", "Audio Track"),
                        startTimeMs = obj.getLong("startTimeMs"),
                        durationMs = obj.getLong("durationMs"),
                        volume = obj.optDouble("volume", 1.0).toFloat(),
                        fadeInMs = obj.optLong("fadeInMs", 0L),
                        fadeOutMs = obj.optLong("fadeOutMs", 0L),
                        speed = obj.optDouble("speed", 1.0).toFloat(),
                        isMuted = obj.optBoolean("isMuted", false)
                    )
                )
            }
        } catch (e: Exception) { }
        return list
    }

    private fun serializeExportSettings(e: ExportSettings): String {
        return JSONObject().apply {
            put("resolution", e.resolution.name)
            put("fps", e.fps)
            put("quality", e.quality.name)
            put("format", e.format)
            put("highQualityAudio", e.highQualityAudio)
            put("includeWatermark", e.includeWatermark)
            put("watermarkText", e.watermarkText)
        }.toString()
    }

    private fun parseExportSettings(json: String): ExportSettings {
        return try {
            val obj = JSONObject(json)
            ExportSettings(
                resolution = ExportResolution.valueOf(obj.optString("resolution", ExportResolution.RES_1080P.name)),
                fps = obj.optInt("fps", 30),
                quality = ExportQuality.valueOf(obj.optString("quality", ExportQuality.HIGH.name)),
                format = obj.optString("format", "MP4"),
                highQualityAudio = obj.optBoolean("highQualityAudio", true),
                includeWatermark = obj.optBoolean("includeWatermark", false),
                watermarkText = obj.optString("watermarkText", "Quran Video Editor")
            )
        } catch (e: Exception) {
            ExportSettings()
        }
    }
}
