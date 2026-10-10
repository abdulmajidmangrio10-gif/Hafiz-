package com.example.data.quran

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class QuranRepository {

    // In-memory cache for loaded Ayahs per Surah
    private val ayahsCache = mutableMapOf<Int, List<Ayah>>()

    val allSurahs: List<Surah> = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "The Opener", "الفاتحہ", 7, RevelationType.MECCAN),
        Surah(2, "البقرة", "Al-Baqarah", "The Cow", "البقرہ", 286, RevelationType.MEDINAN),
        Surah(3, "آل عمران", "Ali 'Imran", "Family of Imran", "آل عمران", 200, RevelationType.MEDINAN),
        Surah(4, "النساء", "An-Nisa", "The Women", "النساء", 176, RevelationType.MEDINAN),
        Surah(5, "المائدة", "Al-Ma'idah", "The Table Spread", "المائدہ", 120, RevelationType.MEDINAN),
        Surah(6, "الأنعام", "Al-An'am", "The Cattle", "الانعام", 165, RevelationType.MECCAN),
        Surah(7, "الأعراف", "Al-A'raf", "The Heights", "الاعراف", 206, RevelationType.MECCAN),
        Surah(8, "الأنفال", "Al-Anfal", "The Spoils of War", "الانفال", 75, RevelationType.MEDINAN),
        Surah(9, "التوبة", "At-Tawbah", "The Repentance", "التوبہ", 129, RevelationType.MEDINAN),
        Surah(10, "يونس", "Yunus", "Jonah", "یونس", 109, RevelationType.MECCAN),
        Surah(11, "هود", "Hud", "Hud", "ھود", 123, RevelationType.MECCAN),
        Surah(12, "يوسف", "Yusuf", "Joseph", "یوسف", 111, RevelationType.MECCAN),
        Surah(13, "الرعد", "Ar-Ra'd", "The Thunder", "الرعد", 43, RevelationType.MEDINAN),
        Surah(14, "ابراهيم", "Ibrahim", "Abraham", "ابراہیم", 52, RevelationType.MECCAN),
        Surah(15, "الحجر", "Al-Hijr", "The Rocky Tract", "الحجر", 99, RevelationType.MECCAN),
        Surah(16, "النحل", "An-Nahl", "The Bee", "النحل", 128, RevelationType.MECCAN),
        Surah(17, "الإسراء", "Al-Isra", "The Night Journey", "الاسراء", 111, RevelationType.MECCAN),
        Surah(18, "الكهف", "Al-Kahf", "The Cave", "الکھف", 110, RevelationType.MECCAN),
        Surah(19, "مريم", "Maryam", "Mary", "مریم", 98, RevelationType.MECCAN),
        Surah(20, "طه", "Taha", "Ta-Ha", "طٰہٰ", 135, RevelationType.MECCAN),
        Surah(21, "الأنبياء", "Al-Anbiya", "The Prophets", "الانبیاء", 112, RevelationType.MECCAN),
        Surah(22, "الحج", "Al-Hajj", "The Pilgrimage", "الحج", 78, RevelationType.MEDINAN),
        Surah(23, "المؤمنون", "Al-Mu'minun", "The Believers", "المؤمنون", 118, RevelationType.MECCAN),
        Surah(24, "النور", "An-Nur", "The Light", "النور", 64, RevelationType.MEDINAN),
        Surah(25, "الفرقان", "Al-Furqan", "The Criterion", "الفرقان", 77, RevelationType.MECCAN),
        Surah(26, "الشعراء", "Ash-Shu'ara", "The Poets", "الشعراء", 227, RevelationType.MECCAN),
        Surah(27, "النمل", "An-Naml", "The Ant", "النمل", 93, RevelationType.MECCAN),
        Surah(28, "القصص", "Al-Qasas", "The Stories", "القصص", 88, RevelationType.MECCAN),
        Surah(29, "العنكبوت", "Al-'Ankabut", "The Spider", "العنکبوت", 69, RevelationType.MECCAN),
        Surah(30, "الروم", "Ar-Rum", "The Romans", "الروم", 60, RevelationType.MECCAN),
        Surah(31, "لقمان", "Luqman", "Luqman", "لقمان", 34, RevelationType.MECCAN),
        Surah(32, "السجدة", "As-Sajdah", "The Prostration", "السجدہ", 30, RevelationType.MECCAN),
        Surah(33, "الأحزاب", "Al-Ahzab", "The Combined Forces", "الاحزاب", 73, RevelationType.MEDINAN),
        Surah(34, "سبإ", "Saba", "Sheba", "سبا", 54, RevelationType.MECCAN),
        Surah(35, "فاطر", "Fatir", "Originator", "فاطر", 45, RevelationType.MECCAN),
        Surah(36, "يس", "Ya-Sin", "Ya-Sin", "یٰسٓ", 83, RevelationType.MECCAN),
        Surah(37, "الصافات", "As-Saffat", "Those who set the Ranks", "الصافات", 182, RevelationType.MECCAN),
        Surah(38, "ص", "Sad", "The Letter 'Saad'", "صٓ", 88, RevelationType.MECCAN),
        Surah(39, "الزمر", "Az-Zumar", "The Troops", "الزمر", 75, RevelationType.MECCAN),
        Surah(40, "غافر", "Ghafir", "The Forgiver", "غافر", 85, RevelationType.MECCAN),
        Surah(41, "فصلت", "Fussilat", "Explained in Detail", "فصلت", 54, RevelationType.MECCAN),
        Surah(42, "الشورى", "Ash-Shuraa", "The Consultation", "الشورٰی", 53, RevelationType.MECCAN),
        Surah(43, "الزخرف", "Az-Zukhruf", "The Ornaments of Gold", "الزخرف", 89, RevelationType.MECCAN),
        Surah(44, "الدخان", "Ad-Dukhan", "The Smoke", "الدخان", 59, RevelationType.MECCAN),
        Surah(45, "الجاثية", "Al-Jathiyah", "The Crouching", "الجاثیہ", 37, RevelationType.MECCAN),
        Surah(46, "الأحقاف", "Al-Ahqaf", "The Wind-Curved Sandhills", "الاحقاف", 35, RevelationType.MECCAN),
        Surah(47, "محمد", "Muhammad", "Muhammad", "محمد", 38, RevelationType.MEDINAN),
        Surah(48, "الفتح", "Al-Fath", "The Victory", "الفتح", 29, RevelationType.MEDINAN),
        Surah(49, "الحجرات", "Al-Hujurat", "The Rooms", "الحجرات", 18, RevelationType.MEDINAN),
        Surah(50, "ق", "Qaf", "The Letter 'Qaf'", "قٓ", 45, RevelationType.MECCAN),
        Surah(51, "الذاريات", "Adh-Dhariyat", "The Winnowing Winds", "الذاریات", 60, RevelationType.MECCAN),
        Surah(52, "الطور", "At-Tur", "The Mount", "الطور", 49, RevelationType.MECCAN),
        Surah(53, "النجم", "An-Najm", "The Star", "النجم", 62, RevelationType.MECCAN),
        Surah(54, "القمر", "Al-Qamar", "The Moon", "القمر", 55, RevelationType.MECCAN),
        Surah(55, "الرحمن", "Ar-Rahman", "The Beneficent", "الرحمٰن", 78, RevelationType.MEDINAN),
        Surah(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", "الواقعہ", 96, RevelationType.MECCAN),
        Surah(57, "الحديد", "Al-Hadid", "The Iron", "الحدید", 29, RevelationType.MEDINAN),
        Surah(58, "المجادلة", "Al-Mujadila", "The Pleading Woman", "المجادلہ", 22, RevelationType.MEDINAN),
        Surah(59, "الحشر", "Al-Hashr", "The Exile", "الحشر", 24, RevelationType.MEDINAN),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "She that is to be examined", "الممتحنہ", 13, RevelationType.MEDINAN),
        Surah(61, "الصف", "As-Saf", "The Ranks", "الصف", 14, RevelationType.MEDINAN),
        Surah(62, "الجمعة", "Al-Jumu'ah", "The Congregation", "الجمعہ", 11, RevelationType.MEDINAN),
        Surah(63, "المنافقون", "Al-Munafiqun", "The Hypocrites", "المنافقون", 11, RevelationType.MEDINAN),
        Surah(64, "التغابن", "At-Taghabun", "The Mutual Disillusion", "التغابن", 18, RevelationType.MEDINAN),
        Surah(65, "الطلاق", "At-Talaq", "The Divorce", "الطلاق", 12, RevelationType.MEDINAN),
        Surah(66, "التحريم", "At-Tahrim", "The Prohibition", "التحریم", 12, RevelationType.MEDINAN),
        Surah(67, "الملك", "Al-Mulk", "The Sovereignty", "الملک", 30, RevelationType.MECCAN),
        Surah(68, "القلم", "Al-Qalam", "The Pen", "القلم", 52, RevelationType.MECCAN),
        Surah(69, "الحاقة", "Al-Haqqah", "The Reality", "الحاقہ", 52, RevelationType.MECCAN),
        Surah(70, "المعارج", "Al-Ma'arij", "The Ascending Stairways", "المعارج", 44, RevelationType.MECCAN),
        Surah(71, "نوح", "Nuh", "Noah", "نوح", 28, RevelationType.MECCAN),
        Surah(72, "الجن", "Al-Jinn", "The Jinn", "الجن", 28, RevelationType.MECCAN),
        Surah(73, "المزمل", "Al-Muzzammil", "The Enshrouded One", "المزمل", 20, RevelationType.MECCAN),
        Surah(74, "المدثر", "Al-Muddaththir", "The Cloaked One", "المدثر", 56, RevelationType.MECCAN),
        Surah(75, "القيامة", "Al-Qiyamah", "The Resurrection", "القیامہ", 40, RevelationType.MECCAN),
        Surah(76, "الإنسان", "Al-Insan", "The Human", "الانسان", 31, RevelationType.MEDINAN),
        Surah(77, "المرسلات", "Al-Mursalat", "The Emissaries", "المرسلات", 50, RevelationType.MECCAN),
        Surah(78, "النبإ", "An-Naba", "The Tidings", "النباء", 40, RevelationType.MECCAN),
        Surah(79, "النازعات", "An-Nazi'at", "Those who drag forth", "النازعات", 46, RevelationType.MECCAN),
        Surah(80, "عبس", "'Abasa", "He Frowned", "عبس", 42, RevelationType.MECCAN),
        Surah(81, "التكوير", "At-Takwir", "The Overthrowing", "التکویر", 29, RevelationType.MECCAN),
        Surah(82, "الانفطار", "Al-Infitar", "The Cleaving", "الانفطار", 19, RevelationType.MECCAN),
        Surah(83, "المطففين", "Al-Mutaffifin", "The Defrauding", "المطففین", 36, RevelationType.MECCAN),
        Surah(84, "الانشقاق", "Al-Inshiqaq", "The Splitting Open", "الانشقاق", 25, RevelationType.MECCAN),
        Surah(85, "البروج", "Al-Buruj", "The Mansions of the Stars", "البروج", 22, RevelationType.MECCAN),
        Surah(86, "الطارق", "At-Tariq", "The Morning Star", "الطارق", 17, RevelationType.MECCAN),
        Surah(87, "الأعلى", "Al-A'la", "The Most High", "الاعلیٰ", 19, RevelationType.MECCAN),
        Surah(88, "الغاشية", "Al-Ghashiyah", "The Overwhelming", "الغاشیہ", 26, RevelationType.MECCAN),
        Surah(89, "الفجر", "Al-Fajr", "The Dawn", "الفجر", 30, RevelationType.MECCAN),
        Surah(90, "البلد", "Al-Balad", "The City", "البلد", 20, RevelationType.MECCAN),
        Surah(91, "الشمس", "Ash-Shams", "The Sun", "الشمس", 15, RevelationType.MECCAN),
        Surah(92, "الليل", "Al-Layl", "The Night", "اللیل", 21, RevelationType.MECCAN),
        Surah(93, "الضحى", "Ad-Duhaa", "The Morning Brightness", "الضحٰی", 11, RevelationType.MECCAN),
        Surah(94, "الشرح", "Ash-Sharh", "The Expansion of Breast", "الانشراح", 8, RevelationType.MECCAN),
        Surah(95, "التين", "At-Tin", "The Fig", "التین", 8, RevelationType.MECCAN),
        Surah(96, "العلق", "Al-'Alaq", "The Clot", "العلق", 19, RevelationType.MECCAN),
        Surah(97, "القدر", "Al-Qadr", "The Night of Decree", "القدر", 5, RevelationType.MECCAN),
        Surah(98, "البينة", "Al-Bayyinah", "The Clear Proof", "البینہ", 8, RevelationType.MEDINAN),
        Surah(99, "الزلزلة", "Az-Zalzalah", "The Earthquake", "الزلزال", 8, RevelationType.MEDINAN),
        Surah(100, "العاديات", "Al-'Adiyat", "The Courser", "العادیات", 11, RevelationType.MECCAN),
        Surah(101, "القارعة", "Al-Qari'ah", "The Calamity", "القارregister", 11, RevelationType.MECCAN),
        Surah(102, "التكاثر", "At-Takathur", "The Rivalry in World Increase", "التکاثر", 8, RevelationType.MECCAN),
        Surah(103, "العصر", "Al-'Asr", "The Declining Day", "العصر", 3, RevelationType.MECCAN),
        Surah(104, "الهمزة", "Al-Humazah", "The Scorner", "الھمزہ", 9, RevelationType.MECCAN),
        Surah(105, "الفيل", "Al-Fil", "The Elephant", "الفیل", 5, RevelationType.MECCAN),
        Surah(106, "قريش", "Quraysh", "Quraysh", "قریش", 4, RevelationType.MECCAN),
        Surah(107, "الماعون", "Al-Ma'un", "The Small kindnesses", "الماعون", 7, RevelationType.MECCAN),
        Surah(108, "الكوثر", "Al-Kawthar", "The Abundance", "الکوثر", 3, RevelationType.MECCAN),
        Surah(109, "الكافرون", "Al-Kafirun", "The Disbelievers", "الکافرون", 6, RevelationType.MECCAN),
        Surah(110, "النصر", "An-Nasr", "The Divine Support", "النصر", 3, RevelationType.MEDINAN),
        Surah(111, "المسد", "Al-Masad", "The Palm Fiber", "المسد", 5, RevelationType.MECCAN),
        Surah(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", "الاخلاص", 4, RevelationType.MECCAN),
        Surah(113, "الفلق", "Al-Falaq", "The Daybreak", "الفلق", 5, RevelationType.MECCAN),
        Surah(114, "الناس", "An-Nas", "Mankind", "الناس", 6, RevelationType.MECCAN)
    )

    init {
        loadBundledEssentialSurahs()
    }

    fun searchSurahs(query: String): List<Surah> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return allSurahs
        return allSurahs.filter { surah ->
            surah.number.toString() == q ||
                    surah.transliteration.lowercase().contains(q) ||
                    surah.englishName.lowercase().contains(q) ||
                    surah.arabicName.contains(query.trim()) ||
                    surah.urduName.contains(query.trim())
        }
    }

    suspend fun getAyahsForSurah(surahNumber: Int): List<Ayah> = withContext(Dispatchers.IO) {
        // Return from cache if already loaded
        ayahsCache[surahNumber]?.let { return@withContext it }

        // Fetch from verified public Quran API
        val fetched = fetchSurahFromApi(surahNumber)
        if (fetched.isNotEmpty()) {
            ayahsCache[surahNumber] = fetched
            return@withContext fetched
        }

        // Return empty if offline and not preloaded
        emptyList()
    }

    private fun fetchSurahFromApi(surahNumber: Int): List<Ayah> {
        return try {
            // Authentic AlQuran.cloud API with Arabic Uthmani and authentic Urdu translation (Jalandhry)
            val urlString = "https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,ur.jalandhry"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val data = json.getJSONArray("data")
                val arabicEdition = data.getJSONObject(0).getJSONArray("ayahs")
                val urduEdition = data.getJSONObject(1).getJSONArray("ayahs")

                val ayahs = mutableListOf<Ayah>()
                val count = minOf(arabicEdition.length(), urduEdition.length())
                for (i in 0 until count) {
                    val arObj = arabicEdition.getJSONObject(i)
                    val urObj = urduEdition.getJSONObject(i)
                    val ayahNum = arObj.getInt("numberInSurah")
                    val arText = arObj.getString("text")
                    val urText = urObj.getString("text")

                    ayahs.add(
                        Ayah(
                            id = "$surahNumber:$ayahNum",
                            surahNumber = surahNumber,
                            ayahNumber = ayahNum,
                            arabicText = arText,
                            urduTranslation = urText
                        )
                    )
                }
                ayahs
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("QuranRepository", "Error fetching Surah $surahNumber: ${e.message}")
            emptyList()
        }
    }

    private fun loadBundledEssentialSurahs() {
        // 1. Surah Al-Fatihah (Complete)
        ayahsCache[1] = listOf(
            Ayah("1:1", 1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "شروع اللہ کا نام لے کر جو بڑا مہربان نہایت رحم والا ہے"),
            Ayah("1:2", 1, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "سب طرح کی تعریف خدا ہی کو (سزاوار) ہے جو تمام مخلوقات کا پروردگار ہے"),
            Ayah("1:3", 1, 3, "الرَّحْمَٰنِ الرَّحِيمِ", "بڑا مہربان نہایت رحم والا"),
            Ayah("1:4", 1, 4, "مَالِكِ يَوْمِ الدِّينِ", "انصاف کے دن کا حاکم"),
            Ayah("1:5", 1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "(اے پروردگار) ہم تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں"),
            Ayah("1:6", 1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "ہم کو سیدھے رستے پر چلا"),
            Ayah("1:7", 1, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "ان لوگوں کے رستے جن پر تو اپنا فضل و کرم کرتا رہا نہ ان کے جن پر غصہ ہوتا رہا اور نہ گمراہوں کے")
        )

        // 2. Surah Al-Baqarah (Opening & Ayat al-Kursi)
        ayahsCache[2] = listOf(
            Ayah("2:1", 2, 1, "الم", "الٓمّٓ"),
            Ayah("2:2", 2, 2, "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ", "یہ وہ کتاب ہے جس میں کوئی شک نہیں، پرہیزگاروں کے لیے ہدایت ہے"),
            Ayah("2:3", 2, 3, "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ", "جو غیب پر ایمان لاتے اور نماز قائم کرتے اور جو کچھ ہم نے ان کو عطا فرمایا اس میں سے خرچ کرتے ہیں"),
            Ayah("2:4", 2, 4, "وَالَّذِينَ يُؤْمِنُونَ بِمَا أُنزِلَ إِلَيْكَ وَمَا أُنزِلَ مِن قَبْلِكَ وَبِالْآخِرَةِ هُمْ يُوقِنُونَ", "اور جو ایمان لاتے ہیں اس پر جو آپ کی طرف نازل کیا گیا اور جو آپ سے پہلے نازل کیا گیا اور آخرت پر وہ یقین رکھتے ہیں"),
            Ayah("2:5", 2, 5, "أُولَٰئِكَ عَلَىٰ هُدًى مِّن رَّبِّهِمْ ۖ وَأُولَٰئِكَ هُمُ الْمُفْلِحُونَ", "یہی لوگ اپنے پروردگار کی طرف سے ہدایت پر ہیں اور یہی نجات پانے والے ہیں"),
            Ayah("2:255", 2, 255, "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ", "اللہ جس کے سوا کوئی معبود نہیں وہ سدا زندہ اور سب کو سنبھالنے والا ہے۔ نہ اسے اونگھ آتی ہے نہ نیند۔ جو کچھ آسمانوں میں ہے اور جو زمین میں ہے سب اسی کا ہے۔ کون ہے جو اس کی اجازت کے بغیر اس کے حضور سفارش کر سکے؟ وہ جانتا ہے جو کچھ ان کے آگے ہے اور جو کچھ ان کے پیچھے ہے، اور وہ اس کے علم میں سے کسی چیز کا احاطہ نہیں کر سکتے مگر جتنا وہ چاہے، اس کی کرسی آسمانوں اور زمین پر محیط ہے اور ان کی نگہبانی اسے نہیں تھکاتی اور وہ بہت بلند عظمت والا ہے۔")
        )

        // 36. Surah Ya-Sin (Key Opening)
        ayahsCache[36] = listOf(
            Ayah("36:1", 36, 1, "يس", "یٰسٓ"),
            Ayah("36:2", 36, 2, "وَالْقُرْآنِ الْحَكِيمِ", "حکمت سے بھرے قرآن کی قسم"),
            Ayah("36:3", 36, 3, "إِنَّكَ لَمِنَ الْمُرْسَلِينَ", "بے شک آپ پیغمبروں میں سے ہیں"),
            Ayah("36:4", 36, 4, "عَلَىٰ صِرَاطٍ مُّسْتَقِيمٍ", "سیدھے رستے پر ہیں"),
            Ayah("36:5", 36, 5, "تَنزِيلَ الْعَزِيزِ الرَّحِيمِ", "یہ زبردست اور نہایت رحم فرمانے والے کا نازل کیا ہوا ہے"),
            Ayah("36:58", 36, 58, "سَلَامٌ قَوْلًا مِّن رَّبٍّ رَّحِيمٍ", "مہربان پروردگار کی طرف سے ان کو سلام فرمایا جائے گا")
        )

        // 55. Surah Ar-Rahman (Key Opening)
        ayahsCache[55] = listOf(
            Ayah("55:1", 55, 1, "الرَّحْمَٰنُ", "رحمٰن (وہ بڑی رحمت والا ہے)"),
            Ayah("55:2", 55, 2, "عَلَّمَ الْقُرْآنَ", "اسی نے قرآن سکھایا"),
            Ayah("55:3", 55, 3, "خَلَقَ الْإِنسَانَ", "اسی نے انسان کو پیدا کیا"),
            Ayah("55:4", 55, 4, "عَلَّمَهُ الْبَيَانَ", "اسے بولنا سکھایا"),
            Ayah("55:13", 55, 13, "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ", "سو تم دونوں اپنے پروردگار کی کون کون سی نعمتوں کو جھٹلاؤ گے؟")
        )

        // 67. Surah Al-Mulk (Key Opening)
        ayahsCache[67] = listOf(
            Ayah("67:1", 67, 1, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "بڑی برکت والا ہے وہ جس کے ہاتھ میں بادشاہی ہے اور وہ ہر چیز پر قادر ہے"),
            Ayah("67:2", 67, 2, "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ", "جس نے موت اور زندگی کو پیدا کیا تاکہ تمہاری آزمائش کرے کہ تم میں کون اچھے عمل والا ہے اور وہ زبردست بخشنے والا ہے")
        )

        // 108. Surah Al-Kawthar (Complete)
        ayahsCache[108] = listOf(
            Ayah("108:1", 108, 1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "بے شک ہم نے آپ کو کوثر عطا فرمایا"),
            Ayah("108:2", 108, 2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "تو اپنے پروردگار کے لیے نماز پڑھئے اور قربانی کیجئے"),
            Ayah("108:3", 108, 3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", "بے شک آپ کا دشمن ہی بے نام و نشان رہے گا")
        )

        // 112. Surah Al-Ikhlas (Complete)
        ayahsCache[112] = listOf(
            Ayah("112:1", 112, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "کہو کہ وہ اللہ ایک ہے"),
            Ayah("112:2", 112, 2, "اللَّهُ الصَّمَدُ", "اللہ بے نیاز ہے"),
            Ayah("112:3", 112, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "نہ اس سے کوئی پیدا ہوا اور نہ وہ کسی سے پیدا ہوا"),
            Ayah("112:4", 112, 4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "اور نہ کوئی اس کا ہمسر ہے")
        )

        // 113. Surah Al-Falaq (Complete)
        ayahsCache[113] = listOf(
            Ayah("113:1", 113, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "کہو کہ میں صبح کے رب کی پناہ مانگتا ہوں"),
            Ayah("113:2", 113, 2, "مِن شَرِّ مَا خَلَقَ", "ہر اس چیز کی برائی سے جو اس نے پیدا کی"),
            Ayah("113:3", 113, 3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "اور اندھیری رات کی برائی سے جب وہ چھا جائے"),
            Ayah("113:4", 113, 4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "اور گرہوں میں پھونکنے والیوں کے شر سے"),
            Ayah("113:5", 113, 5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "اور حسد کرنے والے کی برائی سے جب وہ حسد کرے")
        )

        // 114. Surah An-Nas (Complete)
        ayahsCache[114] = listOf(
            Ayah("114:1", 114, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "کہو کہ میں لوگوں کے پروردگار کی پناہ مانگتا ہوں"),
            Ayah("114:2", 114, 2, "مَلِكِ النَّاسِ", "لوگوں کے بادشاہ کی"),
            Ayah("114:3", 114, 3, "إِلَٰهِ النَّاسِ", "لوگوں کے معبود کی"),
            Ayah("114:4", 114, 4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "وسوسہ ڈالنے والے پیچھے ہٹ جانے والے کے شر سے"),
            Ayah("114:5", 114, 5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "جو لوگوں کے دلوں میں وسوسے ڈالتا ہے"),
            Ayah("114:6", 114, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "جنوں میں سے ہو یا انسانوں میں سے")
        )
    }

    companion object {
        const val BASMALA_ARABIC = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
        const val BASMALA_URDU = "شروع اللہ کا نام لے کر جو بڑا مہربان نہایت رحم والا ہے"
    }
}
