package com.example.ui.screens.editor_panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoAdjustments
import com.example.data.model.VideoEffect
import com.example.data.model.VideoFilter
import com.example.ui.theme.*
import com.example.viewmodel.EditorTool
import com.example.viewmodel.EditorViewModel

@Composable
fun MediaEffectsPanel(
    viewModel: EditorViewModel,
    initialTab: EditorTool,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val project by viewModel.projectState.collectAsState()
    var selectedTab by remember {
        mutableStateOf(
            when (initialTab) {
                EditorTool.EFFECTS -> 0
                EditorTool.FILTER -> 1
                EditorTool.ADJUST -> 2
                EditorTool.SPEED -> 3
                else -> 0
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 360.dp, max = 480.dp)
            .background(SurfaceDark)
            .border(1.dp, DividerDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Visual Styling & Corrections", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGray)
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = QuranGoldPrimary,
            divider = { HorizontalDivider(color = DividerDark) }
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Effects", fontSize = 12.sp) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Filters", fontSize = 12.sp) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Adjust", fontSize = 12.sp) })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Speed", fontSize = 12.sp) })
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Effects: Blur, Glitch, Cinematic, Light, Shake, Zoom, Fade, Vignette
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(VideoEffect.values()) { effect ->
                            FilterChip(
                                selected = project.activeEffect == effect,
                                onClick = { viewModel.setActiveEffect(effect) },
                                label = { Text(effect.displayName, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = QuranGoldPrimary,
                                    selectedLabelColor = Color.Black
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                1 -> {
                    // Filters: Natural, Warm, Vintage, Yellow/Golden, Cinematic, Black & White
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(VideoFilter.values()) { filter ->
                            FilterChip(
                                selected = project.activeFilter == filter,
                                onClick = { viewModel.setActiveFilter(filter) },
                                label = { Text(filter.displayName, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = QuranGoldPrimary,
                                    selectedLabelColor = Color.Black
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                2 -> {
                    // Adjustments Sliders: Brightness, Contrast, Saturation, Exposure, Sharpness, Temperature, Highlights, Shadows
                    val adj = project.videoAdjustments
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        item {
                            AdjustmentSlider(
                                label = "Brightness",
                                value = adj.brightness,
                                range = -0.5f..0.5f,
                                onValueChange = { viewModel.updateAdjustments(adj.copy(brightness = it)) }
                            )
                        }
                        item {
                            AdjustmentSlider(
                                label = "Contrast",
                                value = adj.contrast,
                                range = 0.5f..1.8f,
                                onValueChange = { viewModel.updateAdjustments(adj.copy(contrast = it)) }
                            )
                        }
                        item {
                            AdjustmentSlider(
                                label = "Saturation",
                                value = adj.saturation,
                                range = 0.0f..2.0f,
                                onValueChange = { viewModel.updateAdjustments(adj.copy(saturation = it)) }
                            )
                        }
                        item {
                            AdjustmentSlider(
                                label = "Temperature (Warmth)",
                                value = adj.temperature,
                                range = -0.5f..0.5f,
                                onValueChange = { viewModel.updateAdjustments(adj.copy(temperature = it)) }
                            )
                        }
                        item {
                            AdjustmentSlider(
                                label = "Exposure",
                                value = adj.exposure,
                                range = -0.5f..0.5f,
                                onValueChange = { viewModel.updateAdjustments(adj.copy(exposure = it)) }
                            )
                        }
                    }
                }
                3 -> {
                    // Speed: 0.25x, 0.5x, 0.75x, 1x, 1.25x, 1.5x, 2x, 3x
                    Column {
                        Text("Selected Speed: ${project.videoSettings.speed}x", color = QuranGoldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(speeds) { spd ->
                                FilterChip(
                                    selected = Math.abs(project.videoSettings.speed - spd) < 0.05f,
                                    onClick = { viewModel.updateVideoSpeed(spd) },
                                    label = { Text("${spd}x", fontSize = 12.sp) },
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
        }
    }
}

@Composable
fun AdjustmentSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextWhite, fontSize = 12.sp)
            Text(String.format("%.2f", value), color = QuranGoldLight, fontSize = 12.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = QuranGoldPrimary, activeTrackColor = QuranGoldPrimary)
        )
    }
}
