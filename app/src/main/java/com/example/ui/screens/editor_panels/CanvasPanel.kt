package com.example.ui.screens.editor_panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.AspectRatioType
import com.example.data.model.CanvasSettings
import com.example.data.model.FitMode
import com.example.ui.theme.*
import com.example.viewmodel.EditorViewModel

@Composable
fun CanvasPanel(
    viewModel: EditorViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val project by viewModel.projectState.collectAsState()
    val canvas = project.canvasSettings

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .border(1.dp, DividerDark)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Canvas & Aspect Ratio", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGray)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Aspect Ratios: Original, 16:9, 9:16, 1:1, 4:5
        Text("Aspect Ratio", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AspectRatioType.values()) { ratio ->
                FilterChip(
                    selected = canvas.aspectRatio == ratio,
                    onClick = {
                        viewModel.updateCanvasSettings(canvas.copy(aspectRatio = ratio))
                    },
                    label = { Text(ratio.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = QuranGoldPrimary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fit / Fill / Crop
        Text("Display Mode", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FitMode.values().forEach { mode ->
                FilterChip(
                    selected = canvas.fitMode == mode,
                    onClick = {
                        viewModel.updateCanvasSettings(canvas.copy(fitMode = mode))
                    },
                    label = { Text(mode.name, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = QuranGoldPrimary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Blur Background Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Blur Background", color = TextWhite, fontSize = 13.sp)
            Switch(
                checked = canvas.blurBackground,
                onCheckedChange = {
                    viewModel.updateCanvasSettings(canvas.copy(blurBackground = it))
                },
                colors = SwitchDefaults.colors(checkedThumbColor = QuranGoldPrimary, checkedTrackColor = QuranGoldPrimary.copy(alpha = 0.5f))
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Background Color Selector
        Text("Background Color", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        val colors = listOf("#000000", "#0B131A", "#1E293B", "#14322D", "#2D1B36", "#FFFFFF")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(colors) { hex ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                        .border(
                            width = if (canvas.backgroundColorHex == hex) 2.dp else 1.dp,
                            color = if (canvas.backgroundColorHex == hex) QuranGoldPrimary else DividerDark,
                            shape = CircleShape
                        )
                        .clickable {
                            viewModel.updateCanvasSettings(canvas.copy(backgroundColorHex = hex))
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Zoom Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Zoom Scale", color = TextWhite, fontSize = 13.sp)
            Text("${String.format("%.1f", canvas.zoom)}x", color = QuranGoldLight, fontSize = 13.sp)
        }
        Slider(
            value = canvas.zoom,
            onValueChange = {
                viewModel.updateCanvasSettings(canvas.copy(zoom = it))
            },
            valueRange = 0.5f..2.5f,
            colors = SliderDefaults.colors(thumbColor = QuranGoldPrimary, activeTrackColor = QuranGoldPrimary)
        )
    }
}
