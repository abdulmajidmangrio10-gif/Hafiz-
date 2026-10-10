package com.example.ui.screens.editor_panels

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.EditorViewModel

@Composable
fun VideoPanel(
    viewModel: EditorViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val project by viewModel.projectState.collectAsState()
    val videoSettings = project.videoSettings

    // Real video replace picker
    val replaceVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.createProjectFromVideo(context, uri)
        }
    }

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
            Text("Video Controls", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGray)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick action buttons: Rotate 90, Flip, Replace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.rotateVideo90Degrees() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CardDark, contentColor = TextWhite)
            ) {
                Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = QuranGoldPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rotate 90°", fontSize = 11.sp)
            }

            Button(
                onClick = { viewModel.flipVideoHorizontal() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CardDark, contentColor = TextWhite)
            ) {
                Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp), tint = QuranGoldPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Flip H", fontSize = 11.sp)
            }

            Button(
                onClick = { replaceVideoLauncher.launch("video/*") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CardDark, contentColor = TextWhite)
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp), tint = QuranGoldPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Replace", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Playback Speed Options: 0.25x, 0.5x, 0.75x, 1x, 1.25x, 1.5x, 2x, 3x
        Text("Playback Speed", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(speeds) { spd ->
                FilterChip(
                    selected = Math.abs(videoSettings.speed - spd) < 0.05f,
                    onClick = { viewModel.updateVideoSpeed(spd) },
                    label = { Text("${spd}x", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = QuranGoldPrimary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Volume Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Clip Volume", color = TextWhite, fontSize = 13.sp)
            Text("${(videoSettings.volume * 100).toInt()}%", color = QuranGoldLight, fontSize = 13.sp)
        }
        Slider(
            value = videoSettings.volume,
            onValueChange = { viewModel.updateVideoVolume(it) },
            valueRange = 0f..1.5f,
            colors = SliderDefaults.colors(thumbColor = QuranGoldPrimary, activeTrackColor = QuranGoldPrimary)
        )
    }
}
