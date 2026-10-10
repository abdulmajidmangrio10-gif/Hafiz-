package com.example.editor.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.editor.preview.formatDuration
import com.example.ui.theme.*

sealed class SelectedTimelineItem {
    data class Video(val id: String = "video") : SelectedTimelineItem()
    data class Quran(val id: String) : SelectedTimelineItem()
    data class Text(val id: String) : SelectedTimelineItem()
    data class Image(val id: String) : SelectedTimelineItem()
    data class Sticker(val id: String) : SelectedTimelineItem()
    data class Audio(val id: String) : SelectedTimelineItem()
}

@Composable
fun TimelineView(
    project: Project,
    currentPlaybackPositionMs: Long,
    selectedItem: SelectedTimelineItem?,
    onItemSelected: (SelectedTimelineItem?) -> Unit,
    onSeek: (Long) -> Unit,
    onClipMoved: (clipId: String, newStartTimeMs: Long) -> Unit,
    onClipTrimmed: (clipId: String, newStartTimeMs: Long, newDurationMs: Long) -> Unit,
    onClipSplit: (clipId: String, splitPositionMs: Long) -> Unit,
    onClipDuplicated: (clipId: String) -> Unit,
    onClipDeleted: (clipId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Zoom factor: pixels per millisecond
    var pxPerSecond by remember { mutableStateOf(50f) } // 50dp per second
    val totalDurationMs = project.videoDurationMs.coerceAtLeast(15000L)
    val timelineWidthDp = ((totalDurationMs / 1000f) * pxPerSecond).dp.coerceAtLeast(600.dp)

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .border(width = 1.dp, color = DividerDark)
    ) {
        // Timeline Header: Zoom In/Out & Active Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track selector hint
            Text(
                text = "Multi-Track Timeline",
                color = TextGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            // Zoom In / Out Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { pxPerSecond = (pxPerSecond - 15f).coerceAtLeast(25f) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = TextGray, modifier = Modifier.size(16.dp))
                }
                Text(
                    text = "${pxPerSecond.toInt()}px/s",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(
                    onClick = { pxPerSecond = (pxPerSecond + 15f).coerceAtMost(120f) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = TextGray, modifier = Modifier.size(16.dp))
                }
            }

            // Quick Actions on Selected Item (Split, Duplicate, Delete)
            if (selectedItem != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Split Button
                    FilledTonalButton(
                        onClick = {
                            val id = getSelectedId(selectedItem)
                            if (id != null) onClipSplit(id, currentPlaybackPositionMs)
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = CardDark),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(Icons.Default.CallSplit, contentDescription = "Split", tint = QuranGoldPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Split", color = TextWhite, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Duplicate Button
                    FilledTonalButton(
                        onClick = {
                            val id = getSelectedId(selectedItem)
                            if (id != null) onClipDuplicated(id)
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = CardDark),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = QuranGoldPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Duplicate", color = TextWhite, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Delete Button
                    IconButton(
                        onClick = {
                            val id = getSelectedId(selectedItem)
                            if (id != null) onClipDeleted(id)
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PlayheadRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Horizontal Scrollable Multi-Track Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .horizontalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .width(timelineWidthDp)
                    .fillMaxHeight()
            ) {
                // 1. Time Ruler at top
                TimelineRuler(
                    totalDurationMs = totalDurationMs,
                    pxPerSecond = pxPerSecond,
                    onSeek = onSeek,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                )

                // 2. Tracks Stack
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp)
                ) {
                    // TRACK 1: VIDEO TRACK
                    TrackRow(label = "VIDEO", color = RoyalBlue) {
                        VideoTrackItem(
                            durationMs = totalDurationMs,
                            pxPerSecond = pxPerSecond,
                            isSelected = selectedItem is SelectedTimelineItem.Video,
                            onClick = { onItemSelected(SelectedTimelineItem.Video()) }
                        )
                    }

                    // TRACK 2: QURAN TRACK
                    TrackRow(label = "QURAN", color = QuranGoldPrimary) {
                        project.quranClips.forEach { quranClip ->
                            ClipItem(
                                title = "﴿ ${quranClip.surahNameArabic} ﴾",
                                subtitle = "Ayah ${quranClip.startAyah}",
                                startTimeMs = quranClip.startTimeMs,
                                durationMs = quranClip.durationMs,
                                pxPerSecond = pxPerSecond,
                                color = QuranGoldPrimary,
                                textColor = Color.Black,
                                isSelected = (selectedItem as? SelectedTimelineItem.Quran)?.id == quranClip.id,
                                onClick = { onItemSelected(SelectedTimelineItem.Quran(quranClip.id)) },
                                onMoved = { newStart -> onClipMoved(quranClip.id, newStart) },
                                onTrimmed = { newStart, newDur -> onClipTrimmed(quranClip.id, newStart, newDur) }
                            )
                        }
                    }

                    // TRACK 3: TEXT TRACK
                    TrackRow(label = "TEXT", color = IslamicTeal) {
                        project.textClips.forEach { textClip ->
                            ClipItem(
                                title = textClip.text,
                                subtitle = "${textClip.fontSizeSp.toInt()}sp",
                                startTimeMs = textClip.startTimeMs,
                                durationMs = textClip.durationMs,
                                pxPerSecond = pxPerSecond,
                                color = IslamicTeal,
                                textColor = Color.Black,
                                isSelected = (selectedItem as? SelectedTimelineItem.Text)?.id == textClip.id,
                                onClick = { onItemSelected(SelectedTimelineItem.Text(textClip.id)) },
                                onMoved = { newStart -> onClipMoved(textClip.id, newStart) },
                                onTrimmed = { newStart, newDur -> onClipTrimmed(textClip.id, newStart, newDur) }
                            )
                        }
                    }

                    // TRACK 4: IMAGE / STICKER TRACK
                    TrackRow(label = "OVERLAYS", color = MajesticPurple) {
                        project.imageClips.forEach { imgClip ->
                            ClipItem(
                                title = "Image Overlay",
                                subtitle = "",
                                startTimeMs = imgClip.startTimeMs,
                                durationMs = imgClip.durationMs,
                                pxPerSecond = pxPerSecond,
                                color = MajesticPurple,
                                textColor = TextWhite,
                                isSelected = (selectedItem as? SelectedTimelineItem.Image)?.id == imgClip.id,
                                onClick = { onItemSelected(SelectedTimelineItem.Image(imgClip.id)) },
                                onMoved = { newStart -> onClipMoved(imgClip.id, newStart) },
                                onTrimmed = { newStart, newDur -> onClipTrimmed(imgClip.id, newStart, newDur) }
                            )
                        }
                        project.stickerClips.forEach { stickerClip ->
                            val def = StickerRepository.allStickers.find { it.key == stickerClip.stickerKey }
                            ClipItem(
                                title = def?.previewIcon ?: "Sticker",
                                subtitle = def?.name ?: "",
                                startTimeMs = stickerClip.startTimeMs,
                                durationMs = stickerClip.durationMs,
                                pxPerSecond = pxPerSecond,
                                color = Color(0xFF6B46C1),
                                textColor = TextWhite,
                                isSelected = (selectedItem as? SelectedTimelineItem.Sticker)?.id == stickerClip.id,
                                onClick = { onItemSelected(SelectedTimelineItem.Sticker(stickerClip.id)) },
                                onMoved = { newStart -> onClipMoved(stickerClip.id, newStart) },
                                onTrimmed = { newStart, newDur -> onClipTrimmed(stickerClip.id, newStart, newDur) }
                            )
                        }
                    }

                    // TRACK 5: AUDIO TRACK
                    TrackRow(label = "AUDIO", color = Color(0xFF10B981)) {
                        project.audioClips.forEach { audioClip ->
                            ClipItem(
                                title = audioClip.title,
                                subtitle = "♪ Audio",
                                startTimeMs = audioClip.startTimeMs,
                                durationMs = audioClip.durationMs,
                                pxPerSecond = pxPerSecond,
                                color = Color(0xFF10B981),
                                textColor = Color.Black,
                                isSelected = (selectedItem as? SelectedTimelineItem.Audio)?.id == audioClip.id,
                                onClick = { onItemSelected(SelectedTimelineItem.Audio(audioClip.id)) },
                                onMoved = { newStart -> onClipMoved(audioClip.id, newStart) },
                                onTrimmed = { newStart, newDur -> onClipTrimmed(audioClip.id, newStart, newDur) }
                            )
                        }
                    }
                }

                // 3. Red Playhead Indicator (Synchronized with video playback)
                val playheadOffsetDp = ((currentPlaybackPositionMs / 1000f) * pxPerSecond).dp
                Box(
                    modifier = Modifier
                        .offset(x = playheadOffsetDp - 8.dp)
                        .fillMaxHeight()
                        .width(16.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaMs = ((dragAmount.x / pxPerSecond) * 1000).toLong()
                                val newPos = (currentPlaybackPositionMs + deltaMs).coerceIn(0L, totalDurationMs)
                                onSeek(newPos)
                            }
                        }
                ) {
                    // Playhead Top Pointer
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(16.dp, 16.dp)
                            .background(PlayheadRed, CircleShape)
                    )
                    // Playhead Red Vertical Line
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(PlayheadRed)
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineRuler(
    totalDurationMs: Long,
    pxPerSecond: Float,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (totalDurationMs / 1000).toInt() + 1

    Box(
        modifier = modifier
            .background(CardDark)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val seekSec = offset.x / pxPerSecond
                    onSeek((seekSec * 1000).toLong().coerceIn(0L, totalDurationMs))
                }
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            for (sec in 0..totalSeconds) {
                Box(
                    modifier = Modifier
                        .width((pxPerSecond).dp)
                        .fillMaxHeight()
                ) {
                    // Tick line
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .width(1.dp)
                            .height(if (sec % 5 == 0) 14.dp else 7.dp)
                            .background(if (sec % 5 == 0) QuranGoldPrimary else TextMuted)
                    )
                    // Time Label every 5 seconds
                    if (sec % 5 == 0) {
                        Text(
                            text = formatDuration(sec * 1000L),
                            color = TextGray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 2.dp, top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrackRow(
    label: String,
    color: Color,
    content: @Composable BoxScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .border(0.5.dp, DividerDark)
            .background(SurfaceVariantDark),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track Header Label
        Box(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .background(SurfaceDark)
                .border(0.5.dp, DividerDark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = color,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Track Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 2.dp),
            content = content
        )
    }
}

@Composable
fun VideoTrackItem(
    durationMs: Long,
    pxPerSecond: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val widthDp = ((durationMs / 1000f) * pxPerSecond).dp
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = RoyalBlue.copy(alpha = 0.85f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color.White) else null,
        modifier = Modifier
            .width(widthDp)
            .fillMaxHeight()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Movie, contentDescription = null, tint = TextWhite, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Primary Video (${formatDuration(durationMs)})",
                color = TextWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ClipItem(
    title: String,
    subtitle: String,
    startTimeMs: Long,
    durationMs: Long,
    pxPerSecond: Float,
    color: Color,
    textColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    onMoved: (Long) -> Unit,
    onTrimmed: (Long, Long) -> Unit
) {
    val startOffsetDp = ((startTimeMs / 1000f) * pxPerSecond).dp
    val widthDp = ((durationMs / 1000f) * pxPerSecond).dp.coerceAtLeast(40.dp)

    Box(
        modifier = Modifier
            .offset(x = startOffsetDp)
            .width(widthDp)
            .fillMaxHeight()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val deltaMs = ((dragAmount.x / pxPerSecond) * 1000).toLong()
                    val newStart = (startTimeMs + deltaMs).coerceAtLeast(0L)
                    onMoved(newStart)
                }
            }
            .clickable(onClick = onClick)
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = color,
            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color.White) else null,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = subtitle,
                        color = textColor.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // Left trim handle when selected
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(8.dp, 24.dp)
                    .background(Color.White, RoundedCornerShape(2.dp))
            )
            // Right trim handle when selected
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(8.dp, 24.dp)
                    .background(Color.White, RoundedCornerShape(2.dp))
            )
        }
    }
}

private fun getSelectedId(item: SelectedTimelineItem): String? {
    return when (item) {
        is SelectedTimelineItem.Quran -> item.id
        is SelectedTimelineItem.Text -> item.id
        is SelectedTimelineItem.Image -> item.id
        is SelectedTimelineItem.Sticker -> item.id
        is SelectedTimelineItem.Audio -> item.id
        is SelectedTimelineItem.Video -> null
    }
}
