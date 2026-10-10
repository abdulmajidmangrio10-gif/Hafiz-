package com.example.data.quran

data class Surah(
    val number: Int,
    val arabicName: String,
    val transliteration: String,
    val englishName: String,
    val urduName: String,
    val ayahCount: Int,
    val revelationType: RevelationType
)

enum class RevelationType(val arabicLabel: String, val urduLabel: String, val englishLabel: String) {
    MECCAN("مكية", "مکی", "Meccan"),
    MEDINAN("مدنية", "مدنی", "Medinan")
}

data class Ayah(
    val id: String,
    val surahNumber: Int,
    val ayahNumber: Int,
    val arabicText: String,
    val urduTranslation: String,
    val englishTranslation: String = ""
)

data class SurahWithAyahs(
    val surah: Surah,
    val ayahs: List<Ayah>
)

data class QuranRecitationSegment(
    val ayahNumber: Int,
    val arabicText: String,
    val urduTranslation: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val confidence: Float = 0.95f
)
