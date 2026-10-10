package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExportQuality
import com.example.data.model.ExportResolution
import com.example.data.model.ExportSettings
import com.example.editor.render.ExportProgressState
import com.example.editor.render.VideoExporter
import com.example.ui.theme.*
import com.example.viewmodel.EditorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val project by viewModel.projectState.collectAsState()

    val exporter = remember { VideoExporter(context) }
    val exportState by exporter.exportState.collectAsState()

    var exportSettings by remember { mutableStateOf(project.exportSettings) }

    BackHandler {
        exporter.resetState()
        onNavigateBack()
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Export Video", color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        exporter.resetState()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = exportState) {
                is ExportProgressState.Idle -> {
                    // Export Settings Configuration
                    item {
                        Text("Export Configuration", color = QuranGoldLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Configure video resolution, framerate, and compression quality before rendering.", color = TextGray, fontSize = 12.sp)
                    }

                    // Resolution: 480p, 720p, 1080p, Original
                    item {
                        Text("Resolution", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ExportResolution.values()) { res ->
                                FilterChip(
                                    selected = exportSettings.resolution == res,
                                    onClick = { exportSettings = exportSettings.copy(resolution = res) },
                                    label = { Text(res.label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = QuranGoldPrimary,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    // FPS: 24, 30, 60
                    item {
                        Text("Framerate (FPS)", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(24, 30, 60).forEach { fps ->
                                FilterChip(
                                    selected = exportSettings.fps == fps,
                                    onClick = { exportSettings = exportSettings.copy(fps = fps) },
                                    label = { Text("$fps FPS", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = QuranGoldPrimary,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    // Quality: Low, Medium, High, Maximum
                    item {
                        Text("Quality Bitrate", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ExportQuality.values()) { q ->
                                FilterChip(
                                    selected = exportSettings.quality == q,
                                    onClick = { exportSettings = exportSettings.copy(quality = q) },
                                    label = { Text(q.label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = QuranGoldPrimary,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    // Watermark Toggle
                    item {
                        Surface(
                            color = CardDark,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Include Watermark", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Display 'Quran Video Editor' corner mark", color = TextGray, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = exportSettings.includeWatermark,
                                    onCheckedChange = { exportSettings = exportSettings.copy(includeWatermark = it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = QuranGoldPrimary, checkedTrackColor = QuranGoldPrimary.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }

                    // Start Export Button
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.updateExportSettings(exportSettings)
                                coroutineScope.launch {
                                    exporter.exportProject(project.copy(exportSettings = exportSettings))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = QuranGoldPrimary, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.MovieFilter, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RENDER & EXPORT NOW", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                is ExportProgressState.Processing -> {
                    // Rendering in progress with percentage & step
                    item {
                        Surface(
                            color = CardDark,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    progress = { state.progressPercent / 100f },
                                    color = QuranGoldPrimary,
                                    strokeWidth = 6.dp,
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "${state.progressPercent}%",
                                    color = TextWhite,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = state.currentStep,
                                    color = QuranGoldLight,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                LinearProgressIndicator(
                                    progress = { state.progressPercent / 100f },
                                    color = QuranGoldPrimary,
                                    trackColor = SurfaceDark,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                )
                            }
                        }
                    }
                }

                is ExportProgressState.Completed -> {
                    // Export Success Screen: Save to Gallery & Share
                    item {
                        Surface(
                            color = SurfaceVariantDark,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, QuranGoldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = QuranGoldPrimary,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Export Completed!",
                                    color = TextWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Your Quran video composition has been rendered with all verses, Urdu translations, audio, and visual overlays.",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Save to Gallery
                                Button(
                                    onClick = {
                                        val savedUri = exporter.saveVideoToGallery(state.outputFile)
                                        if (savedUri != null) {
                                            Toast.makeText(context, "Saved to Gallery / Movies folder!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Saved in app exports folder.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = QuranGoldPrimary, contentColor = Color.Black)
                                ) {
                                    Icon(Icons.Default.SaveAlt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SAVE TO DEVICE GALLERY", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Share Video
                                OutlinedButton(
                                    onClick = { exporter.shareVideo(context, state.outputUri) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerDark)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SHARE VIDEO")
                                }
                            }
                        }
                    }
                }

                is ExportProgressState.Failed -> {
                    item {
                        Surface(
                            color = CardDark,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PlayheadRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = PlayheadRed, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Export Failed", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(state.errorMessage, color = TextGray, fontSize = 12.sp, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { exporter.resetState() },
                                    colors = ButtonDefaults.buttonColors(containerColor = QuranGoldPrimary, contentColor = Color.Black)
                                ) {
                                    Text("Try Again")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
