package com.example.editor.preview

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File

@OptIn(UnstableApi::class)
@Composable
fun VideoPreviewPlayer(
    project: Project,
    currentPlaybackPositionMs: Long,
    isPlaying: Boolean,
    onPositionChanged: (Long) -> Unit,
    onPlayPauseToggled: () -> Unit,
    onMuteToggled: () -> Unit,
    onFullscreenToggled: () -> Unit,
    onFitFillToggled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Initialize ExoPlayer
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    // Set video source when URI changes
    LaunchedEffect(project.videoUri) {
        if (!project.videoUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(project.videoUri)
                val mediaItem = MediaItem.fromUri(uri)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
            } catch (e: Exception) {
                // Video URI loading error handled gracefully
            }
        }
    }

    // Synchronize play/pause
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    // Synchronize playback speed
    LaunchedEffect(project.videoSettings.speed) {
        exoPlayer.setPlaybackSpeed(project.videoSettings.speed)
    }

    // Synchronize volume / mute
    LaunchedEffect(project.videoSettings.volume, project.videoSettings.isMuted) {
        exoPlayer.volume = if (project.videoSettings.isMuted) 0f else project.videoSettings.volume
    }

    // Polling player position when playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val currentPos = exoPlayer.currentPosition
            if (currentPos != currentPlaybackPositionMs) {
                onPositionChanged(currentPos)
            }
            delay(50)
        }
    }

    // Seek player when position is changed externally (e.g. user drags timeline playhead)
    LaunchedEffect(currentPlaybackPositionMs) {
        val diff = Math.abs(exoPlayer.currentPosition - currentPlaybackPositionMs)
        if (diff > 250) {
            exoPlayer.seekTo(currentPlaybackPositionMs)
        }
    }

    // Clean up player on dispose
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Canvas Aspect Ratio calculation
    val targetAspectRatio = project.canvasSettings.aspectRatio.ratio ?: (
        if (project.originalWidth > 0 && project.originalHeight > 0) {
            project.originalWidth.toFloat() / project.originalHeight.toFloat()
        } else 9f / 16f
    )

    // Color matrix filter for adjustments & filters
    val colorMatrix = remember(project.videoAdjustments, project.activeFilter) {
        createAdjustmentsColorMatrix(project.videoAdjustments, project.activeFilter)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Outer Container enforcing the Canvas Aspect Ratio
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(targetAspectRatio)
                .background(
                    try {
                        Color(android.graphics.Color.parseColor(project.canvasSettings.backgroundColorHex))
                    } catch (e: Exception) { Color.Black }
                )
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Background Blur layer if enabled in Canvas Settings
            if (project.canvasSettings.blurBackground && project.videoUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(20.dp)
                        .alpha(0.6f)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = false
                                player = exoPlayer
                                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Real ExoPlayer Video Surface
            if (project.videoUri != null) {
                val scale = if (project.canvasSettings.fitMode == FitMode.FILL) 1.25f else project.canvasSettings.zoom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .rotate(project.videoSettings.rotationDegrees.toFloat()),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = false
                                player = exoPlayer
                                resizeMode = if (project.canvasSettings.fitMode == FitMode.FILL) {
                                    androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                } else {
                                    androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Video Effects overlay (Glitch, Cinematic, Vignette, etc.)
                    if (project.activeEffect != VideoEffect.NONE) {
                        VideoEffectOverlay(effect = project.activeEffect)
                    }
                }
            } else {
                // Empty state when no video is selected yet
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF0B131A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "No video loaded",
                            tint = QuranGoldPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Video Loaded",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap + ADD VIDEO to import media from gallery",
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ==========================================
            // LIVE TIMELINE OVERLAYS (SYNCHRONIZED WITH PLAYHEAD)
            // ==========================================

            // 1. Image Clips active at current time
            project.imageClips.forEach { imgClip ->
                if (currentPlaybackPositionMs in imgClip.startTimeMs..(imgClip.startTimeMs + imgClip.durationMs)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = imgClip.imageUri,
                            contentDescription = "Overlay Image",
                            modifier = Modifier
                                .scale(imgClip.scale)
                                .rotate(imgClip.rotationDegrees)
                                .alpha(imgClip.opacity)
                                .size(140.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // 2. Sticker Clips active at current time
            project.stickerClips.forEach { sticker ->
                if (currentPlaybackPositionMs in sticker.startTimeMs..(sticker.startTimeMs + sticker.durationMs)) {
                    val def = StickerRepository.allStickers.find { it.key == sticker.stickerKey }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = androidx.compose.ui.BiasAlignment(
                            (sticker.positionX * 2 - 1).coerceIn(-1f, 1f),
                            (sticker.positionY * 2 - 1).coerceIn(-1f, 1f)
                        )
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0x66000000),
                            border = androidx.compose.foundation.BorderStroke(1.dp, QuranGoldPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .scale(sticker.scale)
                                .rotate(sticker.rotationDegrees)
                                .alpha(sticker.opacity)
                        ) {
                            Text(
                                text = def?.previewIcon ?: "✨",
                                fontSize = 38.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // 3. User Text Clips active at current time
            project.textClips.forEach { textClip ->
                if (currentPlaybackPositionMs in textClip.startTimeMs..(textClip.startTimeMs + textClip.durationMs)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = androidx.compose.ui.BiasAlignment(
                            (textClip.positionX * 2 - 1).coerceIn(-1f, 1f),
                            (textClip.positionY * 2 - 1).coerceIn(-1f, 1f)
                        )
                    ) {
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(textClip.textColorHex))
                        } catch (e: Exception) { Color.White }

                        val bgColor = try {
                            Color(android.graphics.Color.parseColor(textClip.backgroundColorHex))
                        } catch (e: Exception) { Color.Transparent }

                        Box(
                            modifier = Modifier
                                .background(bgColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = textClip.text,
                                color = parsedColor,
                                fontSize = textClip.fontSizeSp.sp,
                                fontWeight = if (textClip.isBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (textClip.isItalic) FontStyle.Italic else FontStyle.Normal,
                                textAlign = when (textClip.alignment) {
                                    TextAlignment.LEFT -> TextAlign.Start
                                    TextAlignment.CENTER -> TextAlign.Center
                                    TextAlignment.RIGHT -> TextAlign.End
                                }
                            )
                        }
                    }
                }
            }

            // 4. Quran Clips active at current time
            project.quranClips.forEach { quranClip ->
                if (currentPlaybackPositionMs in quranClip.startTimeMs..(quranClip.startTimeMs + quranClip.durationMs)) {
                    QuranOverlayView(
                        clip = quranClip,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            // Watermark if enabled in export settings
            if (project.exportSettings.includeWatermark) {
                Text(
                    text = project.exportSettings.watermarkText,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }
        }

        // Overlay Player Controls (Play/Pause, Mute, Fullscreen, Aspect toggle)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xCC000000))
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Play / Pause Button
            IconButton(
                onClick = onPlayPauseToggled,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = QuranGoldPrimary
                )
            }

            // Time indicator (Current / Total)
            Text(
                text = "${formatDuration(currentPlaybackPositionMs)} / ${formatDuration(project.videoDurationMs)}",
                color = TextWhite,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Fit / Fill Toggle
                IconButton(
                    onClick = onFitFillToggled,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (project.canvasSettings.fitMode == FitMode.FILL) Icons.Default.AspectRatio else Icons.Default.CropFree,
                        contentDescription = "Fit/Fill",
                        tint = TextWhite
                    )
                }

                // Mute / Unmute
                IconButton(
                    onClick = onMuteToggled,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (project.videoSettings.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Mute",
                        tint = TextWhite
                    )
                }

                // Fullscreen
                IconButton(
                    onClick = onFullscreenToggled,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Fullscreen",
                        tint = TextWhite
                    )
                }
            }
        }
    }
}

@Composable
fun QuranOverlayView(
    clip: QuranClip,
    modifier: Modifier = Modifier
) {
    val style = clip.style
    val arColor = try {
        Color(android.graphics.Color.parseColor(style.arabicColorHex))
    } catch (e: Exception) { QuranGoldLight }

    val trColor = try {
        Color(android.graphics.Color.parseColor(style.translationColorHex))
    } catch (e: Exception) { TextWhite }

    val bgModifier = when (style.backgroundType) {
        CaptionBackground.ROUNDED_BOX -> Modifier
            .background(Color(0xCC0B1218), RoundedCornerShape(12.dp))
            .border(1.dp, QuranGoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
        CaptionBackground.SOLID -> Modifier
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
        CaptionBackground.GRADIENT -> Modifier
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color(0xEE0B1218))
                ),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
        CaptionBackground.TRANSPARENT -> Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = bgModifier.fillMaxWidth(0.92f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Surah & Ayah Badge Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    text = "﴿ سورة ${clip.surahNameArabic} • آية ${clip.startAyah} ﴾",
                    color = QuranGoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Arabic Quran Verse with proper RTL alignment & font size
            CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                Text(
                    text = clip.arabicText,
                    color = arColor,
                    fontSize = style.arabicFontSizeSp.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = when (style.alignment) {
                        TextAlignment.LEFT -> TextAlign.Start
                        TextAlignment.CENTER -> TextAlign.Center
                        TextAlignment.RIGHT -> TextAlign.End
                    },
                    lineHeight = (style.arabicFontSizeSp * style.arabicLineSpacing).sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Verified Urdu Translation
            if (style.showTranslation && clip.urduTranslation.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                    Text(
                        text = clip.urduTranslation,
                        color = trColor,
                        fontSize = style.translationFontSizeSp.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = (style.translationFontSizeSp * style.translationSpacing).sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun VideoEffectOverlay(effect: VideoEffect) {
    when (effect) {
        VideoEffect.VIGNETTE -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(Color.Transparent, Color(0xAA000000))
                        )
                    )
            )
        }
        VideoEffect.CINEMATIC -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x221E3A8A))
            )
        }
        VideoEffect.LIGHT -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0x44FFD700), Color.Transparent)
                        )
                    )
            )
        }
        VideoEffect.GLITCH -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x1A00FFFF))
            )
        }
        else -> Unit
    }
}

fun createAdjustmentsColorMatrix(
    adj: VideoAdjustments,
    filter: VideoFilter
): ColorMatrix {
    val cm = ColorMatrix()
    // Saturation
    cm.setToSaturation(adj.saturation)

    // Filter presets
    return when (filter) {
        VideoFilter.WARM, VideoFilter.YELLOW -> {
            ColorMatrix(floatArrayOf(
                1.1f, 0f, 0f, 0f, 15f,
                0f, 1.0f, 0f, 0f, 10f,
                0f, 0f, 0.9f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
        }
        VideoFilter.CINEMATIC -> {
            ColorMatrix(floatArrayOf(
                0.95f, 0f, 0f, 0f, 0f,
                0f, 1.05f, 0f, 0f, 5f,
                0f, 0f, 1.15f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
            ))
        }
        VideoFilter.BLACK_WHITE -> {
            val bw = ColorMatrix()
            bw.setToSaturation(0f)
            bw
        }
        VideoFilter.VINTAGE -> {
            ColorMatrix(floatArrayOf(
                1.0f, 0f, 0f, 0f, 25f,
                0f, 0.9f, 0f, 0f, 15f,
                0f, 0f, 0.75f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
        }
        else -> cm
    }
}

fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
