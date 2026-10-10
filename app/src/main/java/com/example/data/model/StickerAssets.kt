package com.example.data.model

data class StickerDefinition(
    val key: String,
    val name: String,
    val arabicText: String? = null,
    val category: StickerCategory,
    val previewIcon: String
)

object StickerRepository {
    val allStickers: List<StickerDefinition> = listOf(
        // Islamic / Calligraphy
        StickerDefinition("bismillah_calligraphy", "Bismillah Calligraphy", "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", StickerCategory.ISLAMIC, "﷽"),
        StickerDefinition("subhanallah", "SubhanAllah", "سُبْحَانَ اللَّهِ", StickerCategory.ISLAMIC, "سُبْحَانَ اللَّه"),
        StickerDefinition("alhamdulillah", "Alhamdulillah", "الْحَمْدُ لِلَّهِ", StickerCategory.ISLAMIC, "الْحَمْدُ لِلَّه"),
        StickerDefinition("allahuakbar", "Allahu Akbar", "اللَّهُ أَكْبَرُ", StickerCategory.ISLAMIC, "اللَّهُ أَكْبَر"),
        StickerDefinition("mashallah", "MashAllah", "مَا شَاءَ اللَّهُ", StickerCategory.ISLAMIC, "مَا شَاءَ اللَّه"),
        StickerDefinition("jazakallah", "JazakAllah Khair", "جَزَاكَ اللَّهُ خَيْرًا", StickerCategory.ISLAMIC, "جَزَاكَ اللَّه"),

        // Quran
        StickerDefinition("quran_book", "Holy Quran", "القرآن الكريم", StickerCategory.QURAN, "📖"),
        StickerDefinition("ayah_frame", "Ayah End Symbol", "۝", StickerCategory.QURAN, "۝"),
        StickerDefinition("quran_stand", "Rihal Stand", null, StickerCategory.QURAN, "📚"),
        StickerDefinition("sajdah_mark", "Sajdah Symbol", "۩", StickerCategory.QURAN, "۩"),

        // Arabic
        StickerDefinition("crescent_star", "Hilal Crescent", null, StickerCategory.ARABIC, "🌙"),
        StickerDefinition("mosque_dome", "Mosque Minaret", null, StickerCategory.ARABIC, "🕌"),
        StickerDefinition("ramadan_lantern", "Fanous Lantern", null, StickerCategory.ARABIC, "🏮"),
        StickerDefinition("islamic_star", "Rub el Hizb 8-Star", "۞", StickerCategory.ARABIC, "۞"),

        // Decorative
        StickerDefinition("ornamental_flower", "Gold Rosette", null, StickerCategory.DECORATIVE, "⚜️"),
        StickerDefinition("gold_sparkles", "Divine Glow Sparkles", null, StickerCategory.DECORATIVE, "✨"),
        StickerDefinition("soft_cloud", "Peaceful Clouds", null, StickerCategory.DECORATIVE, "☁️"),
        StickerDefinition("golden_crown", "Majestic Crown", null, StickerCategory.DECORATIVE, "👑"),

        // Shapes
        StickerDefinition("golden_border", "Golden Frame", null, StickerCategory.SHAPES, "🔲"),
        StickerDefinition("circle_badge", "Circular Seal", null, StickerCategory.SHAPES, "⭕"),
        StickerDefinition("golden_diamond", "Diamond Seal", null, StickerCategory.SHAPES, "🔶"),
        StickerDefinition("star_badge", "Islamic Star Seal", null, StickerCategory.SHAPES, "⭐")
    )

    fun getStickersByCategory(category: StickerCategory): List<StickerDefinition> {
        return allStickers.filter { it.category == category }
    }
}
