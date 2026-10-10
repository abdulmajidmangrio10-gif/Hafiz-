package com.example.ui.screens.editor_panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.quran.*
import com.example.ui.theme.*
import com.example.viewmodel.EditorViewModel
import kotlinx.coroutines.launch

@Composable
fun QuranPanel(
    viewModel: EditorViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // 0: Select Surah/Ayah, 1: Style, 2: Basmala Tool

    // Surah Selection state
    var searchQuery by remember { mutableStateOf("") }
    val surahs = remember(searchQuery) { viewModel.quranRepository.searchSurahs(searchQuery) }
    var selectedSurah by remember { mutableStateOf<Surah?>(null) }
    var surahAyahs by remember { mutableStateOf<List<Ayah>>(emptyList()) }
    var isLoadingAyahs by remember { mutableStateOf(false) }
    val selectedAyahs = remember { mutableStateListOf<Ayah>() }

    // Caption Style state
    var currentStyle by remember { mutableStateOf(QuranCaptionStyle()) }

    // Fetch Ayahs when a Surah is clicked
    fun onSurahSelected(surah: Surah) {
        selectedSurah = surah
        selectedAyahs.clear()
        isLoadingAyahs = true
        coroutineScope.launch {
            val ayahs = viewModel.quranRepository.getAyahsForSurah(surah.number)
            surahAyahs = ayahs
            if (ayahs.isNotEmpty()) {
                selectedAyahs.add(ayahs.first())
            }
            isLoadingAyahs = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 360.dp, max = 500.dp)
            .background(SurfaceDark)
            .border(1.dp, DividerDark)
    ) {
        // Top Bar: Back / Title / Close
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectedSurah != null && selectedTab == 0) {
                    IconButton(onClick = { selectedSurah = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Surah List", tint = QuranGoldPrimary)
                    }
                }
                Text(
                    text = if (selectedSurah != null && selectedTab == 0) "Surah ${selectedSurah?.transliteration}" else "Quran Editor",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGray)
            }
        }

        // Tabs: 1. Verses, 2. Style, 3. Basmala
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = QuranGoldPrimary,
            divider = { HorizontalDivider(color = DividerDark) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Verses (${surahs.size} Surahs)", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Typography & Style", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Basmala Tool", fontSize = 12.sp) }
            )
        }

        // Content per Tab
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> {
                    if (selectedSurah == null) {
                        // All 114 Surahs list with live search
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Search bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                placeholder = { Text("Search Surah name, number, or Arabic...", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = QuranGoldPrimary) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextGray)
                                        }
                                    }
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = QuranGoldPrimary,
                                    unfocusedBorderColor = DividerDark,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                )
                            )

                            // Surahs list
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(surahs) { surah ->
                                    SurahListItem(
                                        surah = surah,
                                        onClick = { onSurahSelected(surah) }
                                    )
                                }
                            }
                        }
                    } else {
                        // Ayahs list for selected Surah
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Selection Action Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedAyahs.size} Ayah(s) selected",
                                    color = QuranGoldPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Row {
                                    TextButton(onClick = {
                                        selectedAyahs.clear()
                                        selectedAyahs.addAll(surahAyahs)
                                    }) {
                                        Text("Select All", color = QuranGoldLight, fontSize = 11.sp)
                                    }
                                    TextButton(onClick = { selectedAyahs.clear() }) {
                                        Text("Clear", color = TextGray, fontSize = 11.sp)
                                    }
                                }
                            }

                            if (isLoadingAyahs) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = QuranGoldPrimary)
                                }
                            } else if (surahAyahs.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Connecting to verified Quran dataset...",
                                        color = TextGray,
                                        fontSize = 13.sp
                                    )
                                }
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(surahAyahs) { ayah ->
                                        val isSelected = selectedAyahs.any { it.id == ayah.id }
                                        AyahListItem(
                                            ayah = ayah,
                                            isSelected = isSelected,
                                            onToggle = {
                                                if (isSelected) {
                                                    selectedAyahs.removeAll { it.id == ayah.id }
                                                } else {
                                                    selectedAyahs.add(ayah)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Style & Typography customization
                    QuranStyleEditor(
                        style = currentStyle,
                        onStyleChanged = { currentStyle = it }
                    )
                }
                2 -> {
                    // Basmala tool
                    BasmalaToolContent(
                        onAddBasmala = {
                            viewModel.addBasmalaToVideo(currentStyle)
                            onClose()
                        }
                    )
                }
            }
        }

        // Bottom Action Bar: CANCEL / ADD TO VIDEO
        if (selectedTab != 2) {
            Surface(
                color = CardDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DividerDark)
                    ) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = {
                            if (selectedSurah != null && selectedAyahs.isNotEmpty()) {
                                viewModel.addQuranToVideo(
                                    surahNumber = selectedSurah!!.number,
                                    surahNameArabic = selectedSurah!!.arabicName,
                                    surahNameEnglish = selectedSurah!!.englishName,
                                    surahNameUrdu = selectedSurah!!.urduName,
                                    ayahs = selectedAyahs.sortedBy { it.ayahNumber },
                                    style = currentStyle
                                )
                                onClose()
                            }
                        },
                        enabled = selectedSurah != null && selectedAyahs.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = QuranGoldPrimary, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ADD TO VIDEO", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SurahListItem(
    surah: Surah,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Surah Number Badge
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(SurfaceVariantDark, CircleShape)
                        .border(1.dp, QuranGoldPrimary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${surah.number}",
                        color = QuranGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = surah.transliteration,
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${surah.englishName} • ${surah.ayahCount} Verses • ${surah.revelationType.englishLabel}",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }

            // Arabic Surah Name
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = surah.arabicName,
                    color = QuranGoldLight,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = surah.urduName,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
    HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
}

@Composable
fun AyahListItem(
    ayah: Ayah,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        color = if (isSelected) QuranGoldPrimary.copy(alpha = 0.12f) else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = QuranGoldPrimary,
                    checkmarkColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Ayah Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ayah ${ayah.ayahNumber}",
                        color = QuranGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Arabic Text
                CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                    Text(
                        text = "${ayah.arabicText} ۝",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Urdu Translation
                if (ayah.urduTranslation.isNotBlank()) {
                    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                        Text(
                            text = ayah.urduTranslation,
                            color = TextGray,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
    HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
}

@Composable
fun QuranStyleEditor(
    style: QuranCaptionStyle,
    onStyleChanged: (QuranCaptionStyle) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Arabic Font Selector
        item {
            Text("Arabic Calligraphy Font", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ArabicFont.values()) { font ->
                    FilterChip(
                        selected = style.arabicFont == font,
                        onClick = { onStyleChanged(style.copy(arabicFont = font)) },
                        label = { Text(font.displayName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = QuranGoldPrimary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }

        // Arabic Font Size with +/- and slider
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Arabic Text Size: ${style.arabicFontSizeSp.toInt()}sp", color = TextWhite, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onStyleChanged(style.copy(arabicFontSizeSp = (style.arabicFontSizeSp - 2).coerceAtLeast(16f))) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = QuranGoldPrimary)
                    }
                    IconButton(
                        onClick = { onStyleChanged(style.copy(arabicFontSizeSp = (style.arabicFontSizeSp + 2).coerceAtMost(46f))) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = QuranGoldPrimary)
                    }
                }
            }
            Slider(
                value = style.arabicFontSizeSp,
                onValueChange = { onStyleChanged(style.copy(arabicFontSizeSp = it)) },
                valueRange = 16f..46f,
                colors = SliderDefaults.colors(thumbColor = QuranGoldPrimary, activeTrackColor = QuranGoldPrimary)
            )
        }

        // Translation Visibility toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Urdu Translation", color = TextWhite, fontSize = 13.sp)
                Switch(
                    checked = style.showTranslation,
                    onCheckedChange = { onStyleChanged(style.copy(showTranslation = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = QuranGoldPrimary, checkedTrackColor = QuranGoldPrimary.copy(alpha = 0.5f))
                )
            }
        }

        // Translation Size
        if (style.showTranslation) {
            item {
                Text("Translation Text Size: ${style.translationFontSizeSp.toInt()}sp", color = TextWhite, fontSize = 13.sp)
                Slider(
                    value = style.translationFontSizeSp,
                    onValueChange = { onStyleChanged(style.copy(translationFontSizeSp = it)) },
                    valueRange = 12f..28f,
                    colors = SliderDefaults.colors(thumbColor = QuranGoldPrimary, activeTrackColor = QuranGoldPrimary)
                )
            }
        }

        // Arabic Color Presets
        item {
            Text("Arabic Color", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            val colors = listOf("#FFD56B", "#FFFFFF", "#26A69A", "#388BFD", "#FFED4A", "#F87171")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(colors) { hex ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                            .border(
                                width = if (style.arabicColorHex == hex) 2.dp else 1.dp,
                                color = if (style.arabicColorHex == hex) Color.White else DividerDark,
                                shape = CircleShape
                            )
                            .clickable { onStyleChanged(style.copy(arabicColorHex = hex)) }
                    )
                }
            }
        }

        // Background Type
        item {
            Text("Caption Background", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CaptionBackground.values()) { bg ->
                    FilterChip(
                        selected = style.backgroundType == bg,
                        onClick = { onStyleChanged(style.copy(backgroundType = bg)) },
                        label = { Text(bg.displayName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = QuranGoldPrimary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }

        // Alignment (Left, Center, Right)
        item {
            Text("Alignment", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    TextAlignment.CENTER to "Center",
                    TextAlignment.RIGHT to "Right (RTL)",
                    TextAlignment.LEFT to "Left"
                ).forEach { (align, label) ->
                    FilterChip(
                        selected = style.alignment == align,
                        onClick = { onStyleChanged(style.copy(alignment = align)) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = QuranGoldPrimary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }

        // Outline & Shadow Toggles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Text Outline", color = TextWhite, fontSize = 13.sp)
                Switch(
                    checked = style.hasOutline,
                    onCheckedChange = { onStyleChanged(style.copy(hasOutline = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = QuranGoldPrimary, checkedTrackColor = QuranGoldPrimary.copy(alpha = 0.5f))
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Text Shadow", color = TextWhite, fontSize = 13.sp)
                Switch(
                    checked = style.hasShadow,
                    onCheckedChange = { onStyleChanged(style.copy(hasShadow = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = QuranGoldPrimary, checkedTrackColor = QuranGoldPrimary.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
fun BasmalaToolContent(
    onAddBasmala: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = SurfaceVariantDark,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, QuranGoldPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    color = QuranGoldLight,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "شروع اللہ کا نام لے کر جو بڑا مہربان نہایت رحم والا ہے",
                    color = TextGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddBasmala,
            colors = ButtonDefaults.buttonColors(containerColor = QuranGoldPrimary, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("ADD BASMALA TO VIDEO", fontWeight = FontWeight.Bold)
        }
    }
}
