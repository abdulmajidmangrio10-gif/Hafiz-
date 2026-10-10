package com.example.ui.screens.editor_panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.TextAlignment
import com.example.ui.theme.*
import com.example.viewmodel.EditorViewModel

@Composable
fun TextPanel(
    viewModel: EditorViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textContent by remember { mutableStateOf("Quranic Reflection") }
    var fontSizeSp by remember { mutableStateOf(24f) }
    var selectedColorHex by remember { mutableStateOf("#FFFFFF") }
    var isBold by remember { mutableStateOf(true) }
    var isItalic by remember { mutableStateOf(false) }

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
            Text("Add Custom Text", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGray)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Text Input Field
        OutlinedTextField(
            value = textContent,
            onValueChange = { textContent = it },
            label = { Text("Text Content", color = TextGray) },
            singleLine = false,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = QuranGoldPrimary,
                unfocusedBorderColor = DividerDark,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Formatting controls (Bold, Italic, Size)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = isBold,
                    onClick = { isBold = !isBold },
                    label = { Text("B", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = QuranGoldPrimary,
                        selectedLabelColor = Color.Black
                    )
                )
                FilterChip(
                    selected = isItalic,
                    onClick = { isItalic = !isItalic },
                    label = { Text("I", fontWeight = FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = QuranGoldPrimary,
                        selectedLabelColor = Color.Black
                    )
                )
            }

            Text("Size: ${fontSizeSp.toInt()}sp", color = QuranGoldLight, fontSize = 13.sp)
        }

        Slider(
            value = fontSizeSp,
            onValueChange = { fontSizeSp = it },
            valueRange = 14f..48f,
            colors = SliderDefaults.colors(thumbColor = QuranGoldPrimary, activeTrackColor = QuranGoldPrimary)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Color selector
        Text("Text Color", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        val colors = listOf("#FFFFFF", "#FFD56B", "#26A69A", "#388BFD", "#EF4444", "#000000")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(colors) { hex ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                        .border(
                            width = if (selectedColorHex == hex) 2.dp else 1.dp,
                            color = if (selectedColorHex == hex) QuranGoldPrimary else DividerDark,
                            shape = CircleShape
                        )
                        .clickable { selectedColorHex = hex }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add to Video Button
        Button(
            onClick = {
                if (textContent.isNotBlank()) {
                    viewModel.addTextClip(
                        text = textContent,
                        fontSizeSp = fontSizeSp,
                        colorHex = selectedColorHex,
                        isBold = isBold
                    )
                    onClose()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = QuranGoldPrimary, contentColor = Color.Black)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("ADD TEXT TO VIDEO", fontWeight = FontWeight.Bold)
        }
    }
}
