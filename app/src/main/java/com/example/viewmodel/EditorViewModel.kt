package com.example.viewmodel

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ProjectRepository
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.*
import com.example.data.quran.Ayah
import com.example.data.quran.QuranRepository
import com.example.editor.preview.formatDuration
import com.example.editor.timeline.SelectedTimelineItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class EditorTool(val title: String, val iconName: String) {
    CANVAS("Canvas", "aspect_ratio"),
    VIDEO("Video", "movie"),
    QURAN("Quran", "menu_book"),
    AUTO_CAPTION("Auto Caption", "closed_caption"),
    TEXT("Text", "title"),
    IMAGE("Image", "image"),
    STICKER("Sticker", "auto_awesome"),
    AUDIO("Audio", "audiotrack"),
    EFFECTS("Effects", "blur_on"),
    FILTER("Filter", "filter_vintage"),
    ADJUST("Adjust", "tune"),
    SPEED("Speed", "speed")
}

class EditorViewModel(
    private val projectRepository: ProjectRepository,
    val quranRepository: QuranRepository,
    val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Current Project State
    private val _projectState = MutableStateFlow(Project())
    val projectState: StateFlow<Project> = _projectState.asStateFlow()

    // Playback State
    private val _currentPlaybackPositionMs = MutableStateFlow(0L)
    val currentPlaybackPositionMs: StateFlow<Long> = _currentPlaybackPositionMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Selection State
    private val _selectedTimelineItem = MutableStateFlow<SelectedTimelineItem?>(null)
    val selectedTimelineItem: StateFlow<SelectedTimelineItem?> = _selectedTimelineItem.asStateFlow()

    // Active Bottom Editor Tool Panel
    private val _activeTool = MutableStateFlow<EditorTool?>(null)
    val activeTool: StateFlow<EditorTool?> = _activeTool.asStateFlow()

    // Undo / Redo Stacks (Requirement 11 & 31)
    private val undoStack = mutableListOf<Project>()
    private val redoStack = mutableListOf<Project>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // UI Status / SnackBar message
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            val loaded = projectRepository.getProjectById(projectId)
            if (loaded != null) {
                _projectState.value = loaded
                _currentPlaybackPositionMs.value = 0L
                _isPlaying.value = false
                _selectedTimelineItem.value = null
                undoStack.clear()
                redoStack.clear()
                updateUndoRedoStates()
            }
        }
    }

    fun createProjectFromVideo(context: Context, videoUri: Uri) {
        viewModelScope.launch {
            var durationMs = 15000L
            var width = 1080
            var height = 1920

            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, videoUri)
                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                if (durStr != null) {
                    durationMs = durStr.toLong().coerceAtLeast(3000L)
                }
                val wStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val hStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                if (wStr != null && hStr != null) {
                    width = wStr.toInt()
                    height = hStr.toInt()
                }
                retriever.release()
            } catch (e: Exception) {
                Log.w("EditorViewModel", "Failed to inspect video metadata: ${e.message}")
            }

            val newProject = Project(
                id = UUID.randomUUID().toString(),
                name = "Quran Video ${System.currentTimeMillis() % 10000}",
                videoUri = videoUri.toString(),
                videoDurationMs = durationMs,
                originalWidth = width,
                originalHeight = height
            )

            recordSnapshot()
            _projectState.value = newProject
            _currentPlaybackPositionMs.value = 0L
            _isPlaying.value = false
            _selectedTimelineItem.value = null

            // Persist to Room
            projectRepository.saveProject(newProject)
        }
    }

    private fun recordSnapshot() {
        undoStack.add(_projectState.value)
        if (undoStack.size > 30) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        updateUndoRedoStates()
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(_projectState.value)
            _projectState.value = previous
            updateUndoRedoStates()
            autoSave()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(_projectState.value)
            _projectState.value = next
            updateUndoRedoStates()
            autoSave()
        }
    }

    private fun autoSave() {
        viewModelScope.launch {
            projectRepository.saveProject(_projectState.value)
        }
    }

    // Playback controls
    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun seekTo(positionMs: Long) {
        val total = _projectState.value.videoDurationMs
        _currentPlaybackPositionMs.value = positionMs.coerceIn(0L, total)
    }

    fun toggleMute() {
        recordSnapshot()
        val currentMuted = _projectState.value.videoSettings.isMuted
        _projectState.value = _projectState.value.copy(
            videoSettings = _projectState.value.videoSettings.copy(isMuted = !currentMuted)
        )
        autoSave()
    }

    fun toggleFitFill() {
        recordSnapshot()
        val current = _projectState.value.canvasSettings.fitMode
        val next = if (current == FitMode.FIT) FitMode.FILL else FitMode.FIT
        _projectState.value = _projectState.value.copy(
            canvasSettings = _projectState.value.canvasSettings.copy(fitMode = next)
        )
        autoSave()
    }

    fun selectTool(tool: EditorTool?) {
        _activeTool.value = tool
    }

    fun selectTimelineItem(item: SelectedTimelineItem?) {
        _selectedTimelineItem.value = item
    }

    // ==========================================
    // CANVAS UPDATES (Requirement 12)
    // ==========================================
    fun updateCanvasSettings(newSettings: CanvasSettings) {
        recordSnapshot()
        _projectState.value = _projectState.value.copy(canvasSettings = newSettings)
        autoSave()
    }

    // ==========================================
    // VIDEO CLIP CONTROLS (Requirement 13)
    // ==========================================
    fun updateVideoSpeed(speed: Float) {
        recordSnapshot()
        _projectState.value = _projectState.value.copy(
            videoSettings = _projectState.value.videoSettings.copy(speed = speed)
        )
        autoSave()
    }

    fun updateVideoVolume(volume: Float) {
        recordSnapshot()
        _projectState.value = _projectState.value.copy(
            videoSettings = _projectState.value.videoSettings.copy(volume = volume)
        )
        autoSave()
    }

    fun rotateVideo90Degrees() {
        recordSnapshot()
        val newRot = (_projectState.value.videoSettings.rotationDegrees + 90) % 360
        _projectState.value = _projectState.value.copy(
            videoSettings = _projectState.value.videoSettings.copy(rotationDegrees = newRot)
        )
        autoSave()
    }

    fun flipVideoHorizontal() {
        recordSnapshot()
        val current = _projectState.value.videoSettings.isFlippedHorizontal
        _projectState.value = _projectState.value.copy(
            videoSettings = _projectState.value.videoSettings.copy(isFlippedHorizontal = !current)
        )
        autoSave()
    }

    // ==========================================
    // QURAN CLIPS (Requirements 14, 16, 17, 18, 19)
    // ==========================================
    fun addQuranToVideo(
        surahNumber: Int,
        surahNameArabic: String,
        surahNameEnglish: String,
        surahNameUrdu: String,
        ayahs: List<Ayah>,
        style: QuranCaptionStyle,
        durationMs: Long = 6000L
    ) {
        recordSnapshot()
        val combinedArabic = ayahs.joinToString(" ۝ ") { it.arabicText } + " ۝"
        val combinedUrdu = ayahs.joinToString(" ") { it.urduTranslation }

        val newClip = QuranClip(
            id = UUID.randomUUID().toString(),
            surahNumber = surahNumber,
            surahNameArabic = surahNameArabic,
            surahNameEnglish = surahNameEnglish,
            surahNameUrdu = surahNameUrdu,
            startAyah = ayahs.firstOrNull()?.ayahNumber ?: 1,
            endAyah = ayahs.lastOrNull()?.ayahNumber ?: 1,
            arabicText = combinedArabic,
            urduTranslation = combinedUrdu,
            startTimeMs = _currentPlaybackPositionMs.value,
            durationMs = durationMs,
            style = style
        )

        _projectState.value = _projectState.value.copy(
            quranClips = _projectState.value.quranClips + newClip
        )
        _selectedTimelineItem.value = SelectedTimelineItem.Quran(newClip.id)
        autoSave()
        sendMessage("Quran Ayahs added to timeline at ${formatDuration(_currentPlaybackPositionMs.value)}")
    }

    fun addBasmalaToVideo(style: QuranCaptionStyle) {
        recordSnapshot()
        val newClip = QuranClip(
            id = UUID.randomUUID().toString(),
            surahNumber = 1,
            surahNameArabic = "البسملة",
            surahNameEnglish = "Basmala",
            surahNameUrdu = "بسم اللہ",
            startAyah = 1,
            endAyah = 1,
            arabicText = QuranRepository.BASMALA_ARABIC,
            urduTranslation = QuranRepository.BASMALA_URDU,
            startTimeMs = _currentPlaybackPositionMs.value,
            durationMs = 4500L,
            style = style
        )

        _projectState.value = _projectState.value.copy(
            quranClips = _projectState.value.quranClips + newClip
        )
        _selectedTimelineItem.value = SelectedTimelineItem.Quran(newClip.id)
        autoSave()
        sendMessage("Basmala added to video")
    }

    // ==========================================
    // TEXT CLIPS (Requirement 21)
    // ==========================================
    fun addTextClip(
        text: String,
        fontSizeSp: Float = 22f,
        colorHex: String = "#FFFFFF",
        isBold: Boolean = false,
        durationMs: Long = 4000L
    ) {
        recordSnapshot()
        val newText = TextClip(
            id = UUID.randomUUID().toString(),
            text = text,
            startTimeMs = _currentPlaybackPositionMs.value,
            durationMs = durationMs,
            fontSizeSp = fontSizeSp,
            textColorHex = colorHex,
            isBold = isBold
        )
        _projectState.value = _projectState.value.copy(
            textClips = _projectState.value.textClips + newText
        )
        _selectedTimelineItem.value = SelectedTimelineItem.Text(newText.id)
        autoSave()
    }

    // ==========================================
    // IMAGE CLIPS (Requirement 22)
    // ==========================================
    fun addImageClip(imageUri: String) {
        recordSnapshot()
        val newImage = ImageClip(
            id = UUID.randomUUID().toString(),
            imageUri = imageUri,
            startTimeMs = _currentPlaybackPositionMs.value,
            durationMs = 5000L
        )
        _projectState.value = _projectState.value.copy(
            imageClips = _projectState.value.imageClips + newImage
        )
        _selectedTimelineItem.value = SelectedTimelineItem.Image(newImage.id)
        autoSave()
    }

    // ==========================================
    // STICKER CLIPS (Requirement 23)
    // ==========================================
    fun addStickerClip(stickerKey: String, category: StickerCategory) {
        recordSnapshot()
        val newSticker = StickerClip(
            id = UUID.randomUUID().toString(),
            stickerKey = stickerKey,
            category = category,
            startTimeMs = _currentPlaybackPositionMs.value,
            durationMs = 5000L
        )
        _projectState.value = _projectState.value.copy(
            stickerClips = _projectState.value.stickerClips + newSticker
        )
        _selectedTimelineItem.value = SelectedTimelineItem.Sticker(newSticker.id)
        autoSave()
    }

    // ==========================================
    // AUDIO CLIPS (Requirement 24)
    // ==========================================
    fun addAudioClip(audioUri: String, title: String) {
        recordSnapshot()
        val newAudio = AudioClip(
            id = UUID.randomUUID().toString(),
            audioUri = audioUri,
            title = title,
            startTimeMs = _currentPlaybackPositionMs.value,
            durationMs = 12000L
        )
        _projectState.value = _projectState.value.copy(
            audioClips = _projectState.value.audioClips + newAudio
        )
        _selectedTimelineItem.value = SelectedTimelineItem.Audio(newAudio.id)
        autoSave()
    }

    // ==========================================
    // EFFECTS & FILTERS & ADJUSTMENTS (Requirements 26, 27, 28)
    // ==========================================
    fun setActiveEffect(effect: VideoEffect) {
        recordSnapshot()
        _projectState.value = _projectState.value.copy(activeEffect = effect)
        autoSave()
    }

    fun setActiveFilter(filter: VideoFilter) {
        recordSnapshot()
        _projectState.value = _projectState.value.copy(activeFilter = filter)
        autoSave()
    }

    fun updateAdjustments(adj: VideoAdjustments) {
        recordSnapshot()
        _projectState.value = _projectState.value.copy(videoAdjustments = adj)
        autoSave()
    }

    // ==========================================
    // TIMELINE MANIPULATION (Requirement 10)
    // ==========================================
    fun moveClip(clipId: String, newStartTimeMs: Long) {
        recordSnapshot()
        val p = _projectState.value
        _projectState.value = p.copy(
            quranClips = p.quranClips.map { if (it.id == clipId) it.copy(startTimeMs = newStartTimeMs) else it },
            textClips = p.textClips.map { if (it.id == clipId) it.copy(startTimeMs = newStartTimeMs) else it },
            imageClips = p.imageClips.map { if (it.id == clipId) it.copy(startTimeMs = newStartTimeMs) else it },
            stickerClips = p.stickerClips.map { if (it.id == clipId) it.copy(startTimeMs = newStartTimeMs) else it },
            audioClips = p.audioClips.map { if (it.id == clipId) it.copy(startTimeMs = newStartTimeMs) else it }
        )
        autoSave()
    }

    fun trimClip(clipId: String, newStartTimeMs: Long, newDurationMs: Long) {
        recordSnapshot()
        val p = _projectState.value
        _projectState.value = p.copy(
            quranClips = p.quranClips.map { if (it.id == clipId) it.copy(startTimeMs = newStartTimeMs, durationMs = newDurationMs) else it },
            textClips = p.textClips.map { if (it.id == clipId) it.copy(startTimeMs = newStartTimeMs, durationMs = newDurationMs) else it },
            imageClips = p.imageClips.map { if (it.id == clipId) it.copy(startTimeMs = newStartTimeMs, durationMs = newDurationMs) else it },
            stickerClips = p.stickerClips.map { if (it.id == clipId) it.copy(startTimeMs = newStartTimeMs, durationMs = newDurationMs) else it },
            audioClips = p.audioClips.map { if (it.id == clipId) it.copy(startTimeMs = newStartTimeMs, durationMs = newDurationMs) else it }
        )
        autoSave()
    }

    fun splitClip(clipId: String, splitPositionMs: Long) {
        recordSnapshot()
        val p = _projectState.value

        // Try splitting Quran clip
        val quran = p.quranClips.find { it.id == clipId }
        if (quran != null && splitPositionMs in (quran.startTimeMs + 500)..(quran.startTimeMs + quran.durationMs - 500)) {
            val firstDur = splitPositionMs - quran.startTimeMs
            val secondDur = quran.durationMs - firstDur
            val updatedFirst = quran.copy(durationMs = firstDur)
            val newSecond = quran.copy(id = UUID.randomUUID().toString(), startTimeMs = splitPositionMs, durationMs = secondDur)
            _projectState.value = p.copy(
                quranClips = p.quranClips.filter { it.id != clipId } + listOf(updatedFirst, newSecond)
            )
            autoSave()
            return
        }

        // Try splitting Text clip
        val text = p.textClips.find { it.id == clipId }
        if (text != null && splitPositionMs in (text.startTimeMs + 500)..(text.startTimeMs + text.durationMs - 500)) {
            val firstDur = splitPositionMs - text.startTimeMs
            val secondDur = text.durationMs - firstDur
            val updatedFirst = text.copy(durationMs = firstDur)
            val newSecond = text.copy(id = UUID.randomUUID().toString(), startTimeMs = splitPositionMs, durationMs = secondDur)
            _projectState.value = p.copy(
                textClips = p.textClips.filter { it.id != clipId } + listOf(updatedFirst, newSecond)
            )
            autoSave()
            return
        }
    }

    fun duplicateClip(clipId: String) {
        recordSnapshot()
        val p = _projectState.value

        p.quranClips.find { it.id == clipId }?.let { original ->
            val duplicated = original.copy(
                id = UUID.randomUUID().toString(),
                startTimeMs = original.startTimeMs + original.durationMs
            )
            _projectState.value = p.copy(quranClips = p.quranClips + duplicated)
            autoSave()
            return
        }

        p.textClips.find { it.id == clipId }?.let { original ->
            val duplicated = original.copy(
                id = UUID.randomUUID().toString(),
                startTimeMs = original.startTimeMs + original.durationMs
            )
            _projectState.value = p.copy(textClips = p.textClips + duplicated)
            autoSave()
            return
        }

        p.stickerClips.find { it.id == clipId }?.let { original ->
            val duplicated = original.copy(
                id = UUID.randomUUID().toString(),
                startTimeMs = original.startTimeMs + original.durationMs
            )
            _projectState.value = p.copy(stickerClips = p.stickerClips + duplicated)
            autoSave()
            return
        }
    }

    fun deleteClip(clipId: String) {
        recordSnapshot()
        val p = _projectState.value
        _projectState.value = p.copy(
            quranClips = p.quranClips.filter { it.id != clipId },
            textClips = p.textClips.filter { it.id != clipId },
            imageClips = p.imageClips.filter { it.id != clipId },
            stickerClips = p.stickerClips.filter { it.id != clipId },
            audioClips = p.audioClips.filter { it.id != clipId }
        )
        _selectedTimelineItem.value = null
        autoSave()
    }

    fun updateExportSettings(newExportSettings: ExportSettings) {
        recordSnapshot()
        _projectState.value = _projectState.value.copy(exportSettings = newExportSettings)
        autoSave()
    }

    private fun sendMessage(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }
}
