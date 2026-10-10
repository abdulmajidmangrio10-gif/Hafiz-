package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userPreferencesRepository: UserPreferencesRepository,
    modifier: Modifier = Modifier
) {
    val settings by userPreferencesRepository.settings.collectAsState()

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf(settings.aiSpeechApiKey) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = TextWhite, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: EDITOR CUSTOMIZATION
            item {
                Text("EDITOR CUSTOMIZATION", color = QuranGoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = CardDark,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Accent Color
                        Text("Theme Accent Color", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(AccentColorChoice.values()) { choice ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(android.graphics.Color.parseColor(choice.hex)), CircleShape)
                                        .border(
                                            width = if (settings.accentColor == choice) 2.5.dp else 1.dp,
                                            color = if (settings.accentColor == choice) Color.White else DividerDark,
                                            shape = CircleShape
                                        )
                                        .clickable { userPreferencesRepository.updateAccent(choice) }
                                )
                            }
                        }

                        HorizontalDivider(color = DividerDark, modifier = Modifier.padding(vertical = 12.dp))

                        // Theme Mode (Dark, Light, System)
                        Text("Appearance Theme", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppThemeMode.values().forEach { mode ->
                                FilterChip(
                                    selected = settings.themeMode == mode,
                                    onClick = { userPreferencesRepository.updateTheme(mode) },
                                    label = { Text(mode.displayName, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = QuranGoldPrimary,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = DividerDark, modifier = Modifier.padding(vertical = 12.dp))

                        // Language (English, Urdu, Arabic)
                        Text("Application Language", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppLanguage.values().forEach { lang ->
                                FilterChip(
                                    selected = settings.language == lang,
                                    onClick = { userPreferencesRepository.updateLanguage(lang) },
                                    label = { Text("${lang.displayName} (${lang.nativeName})", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = QuranGoldPrimary,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = DividerDark, modifier = Modifier.padding(vertical = 12.dp))

                        // Icon Size
                        Text("Toolbar Icon Size", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EditorIconSize.values().forEach { size ->
                                FilterChip(
                                    selected = settings.iconSize == size,
                                    onClick = { userPreferencesRepository.updateIconSize(size) },
                                    label = { Text(size.displayName, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = QuranGoldPrimary,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: FEATURE MANAGEMENT (Keep, Hide, Disable)
            item {
                Text("FEATURE MANAGEMENT", color = QuranGoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Show, hide, or disable specific editing tools from the main toolbar.", color = TextGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = CardDark,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        val features = listOf("Quran", "Auto Caption", "Audio", "Text", "Image", "Sticker", "Effects", "Filter", "Adjust", "Speed")
                        features.forEachIndexed { index, feat ->
                            val currentStatus = settings.featureStatusMap[feat] ?: FeatureStatus.KEEP
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(feat, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    FeatureStatus.values().forEach { st ->
                                        FilterChip(
                                            selected = currentStatus == st,
                                            onClick = { userPreferencesRepository.updateFeatureStatus(feat, st) },
                                            label = { Text(st.name, fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = QuranGoldPrimary,
                                                selectedLabelColor = Color.Black
                                            ),
                                            modifier = Modifier.height(28.dp)
                                        )
                                    }
                                }
                            }
                            if (index < features.size - 1) {
                                HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            // Section 3: AI SPEECH RECOGNITION API INTEGRATION
            item {
                Text("AI SERVICES & INTEGRATION", color = QuranGoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = CardDark,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Speech-to-Text Recognition API", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = if (settings.aiSpeechApiKey.isNotBlank()) "Configured: ••••••••" else "Not Configured (Requires API Key)",
                                    color = if (settings.aiSpeechApiKey.isNotBlank()) IslamicTeal else TextGray,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = {
                                    apiKeyInput = settings.aiSpeechApiKey
                                    showApiKeyDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = QuranGoldPrimary, contentColor = Color.Black),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Configure", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 4: ABOUT & APP IDENTITY
            item {
                Text("ABOUT", color = QuranGoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = CardDark,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = SurfaceDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, QuranGoldPrimary),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.MovieCreation, contentDescription = null, tint = QuranGoldPrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Quran Video Editor", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Version 1.0 (Production Build)", color = TextGray, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = DividerDark)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Creator:", color = TextGray, fontSize = 12.sp)
                        Text("Hafiz Abdul Majid Mangrew", color = QuranGoldLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Authentic Quranic Text Verification:\nAll 114 Surahs and verified verses utilize authentic Uthmani calligraphy script and Fateh Muhammad Jalandhari Urdu translation with zero AI-generated or invented verses.",
                            color = TextGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Configure API Key Dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Configure AI Speech Recognition API", color = TextWhite, fontSize = 15.sp) },
            text = {
                Column {
                    Text(
                        text = "Enter your Speech-to-Text / Recitation Alignment API Key to enable real Auto Quran Captioning.",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        placeholder = { Text("Enter API Key...", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = QuranGoldPrimary,
                            unfocusedBorderColor = DividerDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        userPreferencesRepository.updateAiApiKey(apiKeyInput.trim())
                        showApiKeyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = QuranGoldPrimary, contentColor = Color.Black)
                ) {
                    Text("Save Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
