package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FeatureStatus
import com.example.editor.preview.VideoPreviewPlayer
import com.example.editor.timeline.TimelineView
import com.example.ui.screens.editor_panels.*
import com.example.ui.theme.*
import com.example.viewmodel.EditorTool
import com.example.viewmodel.EditorViewModel

@Composable
fun MainEditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val project by viewModel.projectState.collectAsState()
    val currentPositionMs by viewModel.currentPlaybackPositionMs.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val selectedItem by viewModel.selectedTimelineItem.collectAsState()
    val activeTool by viewModel.activeTool.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val userSettings by viewModel.userPreferencesRepository.settings.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Listen to user messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Handle Android system back button
    BackHandler {
        if (activeTool != null) {
            viewModel.selectTool(null)
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // TOP TOOLBAR: Back, Undo, Redo, Canvas, Export (Requirement 9)
            Surface(
                color = SurfaceDark,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, DividerDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button
                    IconButton(
                        onClick = {
                            if (activeTool != null) viewModel.selectTool(null) else onNavigateBack()
                        },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }

                    // Undo / Redo buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = canUndo,
                            modifier = Modifier.testTag("undo_button")
                        ) {
                            Icon(
                                Icons.Default.Undo,
                                contentDescription = "Undo",
                                tint = if (canUndo) TextWhite else TextMuted
                            )
                        }

                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = canRedo,
                            modifier = Modifier.testTag("redo_button")
                        ) {
                            Icon(
                                Icons.Default.Redo,
                                contentDescription = "Redo",
                                tint = if (canRedo) TextWhite else TextMuted
                            )
                        }
                    }

                    // Canvas Quick Shortcut & Export Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick Canvas ratio button
                        FilledTonalButton(
                            onClick = { viewModel.selectTool(EditorTool.CANVAS) },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = CardDark),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.AspectRatio, contentDescription = null, tint = QuranGoldPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(project.canvasSettings.aspectRatio.label, fontSize = 11.sp, color = TextWhite)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // EXPORT BUTTON
                        Button(
                            onClick = onNavigateToExport,
                            colors = ButtonDefaults.buttonColors(containerColor = QuranGoldPrimary, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("export_button")
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        bottomBar = {
            // BOTTOM TOOLBAR & ACTIVE EDITING PANEL CONTAINER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Animated Sub-Panel for Active Tool (Requirement 11)
                AnimatedVisibility(
                    visible = activeTool != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    when (activeTool) {
                        EditorTool.CANVAS -> CanvasPanel(viewModel = viewModel, onClose = { viewModel.selectTool(null) })
                        EditorTool.VIDEO -> VideoPanel(viewModel = viewModel, onClose = { viewModel.selectTool(null) })
                        EditorTool.QURAN -> QuranPanel(viewModel = viewModel, onClose = { viewModel.selectTool(null) })
                        EditorTool.AUTO_CAPTION -> AutoCaptionPanel(viewModel = viewModel, onClose = { viewModel.selectTool(null) })
                        EditorTool.TEXT -> TextPanel(viewModel = viewModel, onClose = { viewModel.selectTool(null) })
                        EditorTool.IMAGE -> ImagePanel(viewModel = viewModel, onClose = { viewModel.selectTool(null) })
                        EditorTool.STICKER -> StickerPanel(viewModel = viewModel, onClose = { viewModel.selectTool(null) })
                        EditorTool.AUDIO -> AudioPanel(viewModel = viewModel, onClose = { viewModel.selectTool(null) })
                        EditorTool.EFFECTS, EditorTool.FILTER, EditorTool.ADJUST, EditorTool.SPEED -> {
                            MediaEffectsPanel(
                                viewModel = viewModel,
                                initialTab = activeTool!!,
                                onClose = { viewModel.selectTool(null) }
                            )
                        }
                        null -> Unit
                    }
                }

                // HORIZONTALLY SCROLLABLE BOTTOM EDITING TOOLBAR (Requirement 11)
                Surface(
                    color = SurfaceDark,
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, DividerDark)
                ) {
                    val toolbarScroll = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(toolbarScroll)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // All 12 Tools filtered by user feature management setting
                        EditorTool.values().forEach { tool ->
                            val status = userSettings.featureStatusMap[tool.title] ?: FeatureStatus.KEEP
                            if (status != FeatureStatus.HIDE) {
                                val isSelected = activeTool == tool
                                val isEnabled = status != FeatureStatus.DISABLE

                                ToolItem(
                                    tool = tool,
                                    isSelected = isSelected,
                                    isEnabled = isEnabled,
                                    onClick = {
                                        if (isEnabled) {
                                            viewModel.selectTool(if (isSelected) null else tool)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // MAIN PREVIEW AREA (ExoPlayer + Synchronized Overlays)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                VideoPreviewPlayer(
                    project = project,
                    currentPlaybackPositionMs = currentPositionMs,
                    isPlaying = isPlaying,
                    onPositionChanged = { newPos -> viewModel.seekTo(newPos) },
                    onPlayPauseToggled = { viewModel.togglePlayPause() },
                    onMuteToggled = { viewModel.toggleMute() },
                    onFullscreenToggled = {},
                    onFitFillToggled = { viewModel.toggleFitFill() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // REAL MULTI-TRACK TIMELINE (Requirement 10)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
            ) {
                TimelineView(
                    project = project,
                    currentPlaybackPositionMs = currentPositionMs,
                    selectedItem = selectedItem,
                    onItemSelected = { viewModel.selectTimelineItem(it) },
                    onSeek = { viewModel.seekTo(it) },
                    onClipMoved = { id, start -> viewModel.moveClip(id, start) },
                    onClipTrimmed = { id, start, dur -> viewModel.trimClip(id, start, dur) },
                    onClipSplit = { id, pos -> viewModel.splitClip(id, pos) },
                    onClipDuplicated = { id -> viewModel.duplicateClip(id) },
                    onClipDeleted = { id -> viewModel.deleteClip(id) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun ToolItem(
    tool: EditorTool,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val icon = when (tool) {
        EditorTool.CANVAS -> Icons.Default.AspectRatio
        EditorTool.VIDEO -> Icons.Default.Movie
        EditorTool.QURAN -> Icons.Default.MenuBook
        EditorTool.AUTO_CAPTION -> Icons.Default.ClosedCaption
        EditorTool.TEXT -> Icons.Default.Title
        EditorTool.IMAGE -> Icons.Default.Image
        EditorTool.STICKER -> Icons.Default.AutoAwesome
        EditorTool.AUDIO -> Icons.Default.Audiotrack
        EditorTool.EFFECTS -> Icons.Default.BlurOn
        EditorTool.FILTER -> Icons.Default.FilterVintage
        EditorTool.ADJUST -> Icons.Default.Tune
        EditorTool.SPEED -> Icons.Default.Speed
    }

    Surface(
        color = if (isSelected) QuranGoldPrimary.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, QuranGoldPrimary) else null,
        modifier = Modifier
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tool.title,
                tint = if (!isEnabled) TextMuted else if (isSelected) QuranGoldLight else TextWhite,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tool.title,
                color = if (!isEnabled) TextMuted else if (isSelected) QuranGoldLight else TextGray,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
