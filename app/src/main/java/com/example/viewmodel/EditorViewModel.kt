package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.R
import com.example.data.AppDatabase
import com.example.data.DefaultPresets
import com.example.data.ProjectEntity
import com.example.engine.BitmapUtils
import com.example.engine.ImageProcessor
import com.example.engine.NotificationHelper
import com.example.model.AdjustmentValues
import com.example.model.AestheticFrame
import com.example.model.CropAspectRatio
import com.example.model.EditorTab
import com.example.model.FilterCategory
import com.example.model.FilterPreset
import com.example.model.StickerOverlay
import com.example.model.TextOverlay
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "venusly_db"
    ).fallbackToDestructiveMigration().build()

    private val projectDao = db.projectDao()
    private val customPresetDao = db.customPresetDao()

    private val _originalBitmap = MutableStateFlow<Bitmap?>(null)
    val originalBitmap: StateFlow<Bitmap?> = _originalBitmap.asStateFlow()

    private val _processedBitmap = MutableStateFlow<Bitmap?>(null)
    val processedBitmap: StateFlow<Bitmap?> = _processedBitmap.asStateFlow()

    private val _currentAdjustments = MutableStateFlow(AdjustmentValues())
    val currentAdjustments: StateFlow<AdjustmentValues> = _currentAdjustments.asStateFlow()

    private val _selectedPreset = MutableStateFlow<FilterPreset?>(DefaultPresets.presets.first())
    val selectedPreset: StateFlow<FilterPreset?> = _selectedPreset.asStateFlow()

    private val _presetStrength = MutableStateFlow(0.75f)
    val presetStrength: StateFlow<Float> = _presetStrength.asStateFlow()

    private val _activeTab = MutableStateFlow(EditorTab.ADJUST)
    val activeTab: StateFlow<EditorTab> = _activeTab.asStateFlow()

    private val _isComparingBefore = MutableStateFlow(false)
    val isComparingBefore: StateFlow<Boolean> = _isComparingBefore.asStateFlow()

    private val _compareSplitProgress = MutableStateFlow(0.5f)
    val compareSplitProgress: StateFlow<Float> = _compareSplitProgress.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _textOverlays = MutableStateFlow<List<TextOverlay>>(emptyList())
    val textOverlays: StateFlow<List<TextOverlay>> = _textOverlays.asStateFlow()

    private val _stickers = MutableStateFlow<List<StickerOverlay>>(emptyList())
    val stickers: StateFlow<List<StickerOverlay>> = _stickers.asStateFlow()

    private val _currentImageUri = MutableStateFlow<String?>(null)
    val currentImageUri: StateFlow<String?> = _currentImageUri.asStateFlow()

    private val _currentProjectId = MutableStateFlow<Long?>(null)
    val currentProjectId: StateFlow<Long?> = _currentProjectId.asStateFlow()

    // Undo / Redo stacks
    private val undoStack = ArrayDeque<AdjustmentValues>()
    private val redoStack = ArrayDeque<AdjustmentValues>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _exportStatusMessage = MutableStateFlow<String?>(null)
    val exportStatusMessage: StateFlow<String?> = _exportStatusMessage.asStateFlow()

    private var processJob: Job? = null

    init {
        // Clean start - no demo data auto-loaded
    }

    fun loadDefaultSampleImage(resId: Int) {
        viewModelScope.launch {
            _isProcessing.value = true
            val bmp = BitmapUtils.decodeResource(getApplication(), resId)
            if (bmp != null) {
                _originalBitmap.value = bmp
                _currentImageUri.value = "res://$resId"
                applyCurrentPreset()
            }
            _isProcessing.value = false
        }
    }

    fun loadImageFromUri(uri: Uri) {
        viewModelScope.launch {
            _isProcessing.value = true
            val bmp = BitmapUtils.decodeSampledBitmapFromUri(getApplication(), uri)
            if (bmp != null) {
                _originalBitmap.value = bmp
                _currentImageUri.value = uri.toString()
                _currentProjectId.value = null
                undoStack.clear()
                redoStack.clear()
                updateUndoRedoStates()
                triggerRender()
            }
            _isProcessing.value = false
        }
    }

    fun setComparingBefore(comparing: Boolean) {
        _isComparingBefore.value = comparing
    }

    fun setCompareSplitProgress(progress: Float) {
        _compareSplitProgress.value = progress.coerceIn(0f, 1f)
    }

    fun setActiveTab(tab: EditorTab) {
        _activeTab.value = tab
    }

    fun selectPreset(preset: FilterPreset?) {
        _selectedPreset.value = preset
        if (preset != null) {
            pushUndoState()
            _currentAdjustments.value = preset.adjustments
            triggerRender()
        }
    }

    fun setPresetStrength(strength: Float) {
        _presetStrength.value = strength
        triggerRender()
    }

    fun updateAdjustment(transform: (AdjustmentValues) -> AdjustmentValues) {
        pushUndoState()
        val updated = transform(_currentAdjustments.value)
        _currentAdjustments.value = updated
        triggerRender()
    }

    fun updateAdjustmentContinuous(transform: (AdjustmentValues) -> AdjustmentValues) {
        // For continuous slider movement without flooding undo stack
        val updated = transform(_currentAdjustments.value)
        _currentAdjustments.value = updated
        triggerRender()
    }

    fun updateFrame(frame: AestheticFrame) {
        pushUndoState()
        _currentAdjustments.value = _currentAdjustments.value.copy(frame = frame)
        triggerRender()
    }

    fun pushUndoState() {
        undoStack.addLast(_currentAdjustments.value)
        if (undoStack.size > 30) undoStack.removeFirst()
        redoStack.clear()
        updateUndoRedoStates()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeLast()
            redoStack.addLast(_currentAdjustments.value)
            _currentAdjustments.value = previous
            updateUndoRedoStates()
            triggerRender()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeLast()
            undoStack.addLast(_currentAdjustments.value)
            _currentAdjustments.value = next
            updateUndoRedoStates()
            triggerRender()
        }
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun resetAdjustments() {
        pushUndoState()
        _currentAdjustments.value = AdjustmentValues()
        _selectedPreset.value = null
        _textOverlays.value = emptyList()
        _stickers.value = emptyList()
        triggerRender()
    }

    fun rotate90() {
        pushUndoState()
        val currentRot = _currentAdjustments.value.rotationDegrees
        val newRot = (currentRot + 90f) % 360f
        _currentAdjustments.value = _currentAdjustments.value.copy(rotationDegrees = newRot)
        triggerRender()
    }

    fun flipHorizontal() {
        pushUndoState()
        _currentAdjustments.value = _currentAdjustments.value.copy(
            flipHorizontal = !_currentAdjustments.value.flipHorizontal
        )
        triggerRender()
    }

    fun flipVertical() {
        pushUndoState()
        _currentAdjustments.value = _currentAdjustments.value.copy(
            flipVertical = !_currentAdjustments.value.flipVertical
        )
        triggerRender()
    }

    fun addDateStamp() {
        val dateFormat = java.text.SimpleDateFormat("''yy MM dd", java.util.Locale.US)
        val dateString = dateFormat.format(java.util.Date())
        val newOverlay = TextOverlay(
            text = dateString,
            xPercent = 0.82f,
            yPercent = 0.92f,
            fontSizeSp = 16f,
            colorHex = "#FF9500",
            hasBackgroundPill = false,
            isDateStamp = true
        )
        _textOverlays.value = _textOverlays.value + newOverlay
        triggerRender()
    }

    fun addText(text: String = "VENUSLY") {
        val newOverlay = TextOverlay(
            text = text,
            xPercent = 0.5f,
            yPercent = 0.82f,
            fontSizeSp = 20f,
            colorHex = "#FFFFFF",
            hasBackgroundPill = true,
            isDateStamp = false
        )
        _textOverlays.value = _textOverlays.value + newOverlay
        triggerRender()
    }

    fun removeTextOverlay(id: String) {
        _textOverlays.value = _textOverlays.value.filter { it.id != id }
        triggerRender()
    }

    fun addSticker(symbol: String) {
        val newSticker = StickerOverlay(
            symbol = symbol,
            xPercent = 0.5f,
            yPercent = 0.5f
        )
        _stickers.value = _stickers.value + newSticker
        triggerRender()
    }

    fun removeSticker(id: String) {
        _stickers.value = _stickers.value.filter { it.id != id }
        triggerRender()
    }

    private fun applyCurrentPreset() {
        val preset = _selectedPreset.value ?: DefaultPresets.presets.first()
        _currentAdjustments.value = preset.adjustments
        triggerRender()
    }

    private fun triggerRender() {
        processJob?.cancel()
        processJob = viewModelScope.launch {
            delay(16) // Smooth 60fps debounce
            val source = _originalBitmap.value ?: return@launch
            val adjustments = _currentAdjustments.value
            val strength = _presetStrength.value
            val texts = _textOverlays.value
            val stks = _stickers.value

            val processed = ImageProcessor.applyAdjustments(
                source = source,
                adjustments = adjustments,
                strength = strength,
                textOverlays = texts,
                stickers = stks
            )
            _processedBitmap.value = processed
            autoSaveDraft()
        }
    }

    private fun autoSaveDraft() {
        viewModelScope.launch {
            val uri = _currentImageUri.value ?: return@launch
            val adj = _currentAdjustments.value
            val entity = ProjectEntity(
                id = _currentProjectId.value ?: 0,
                title = "Edit ${java.text.SimpleDateFormat("MMM d, HH:mm", java.util.Locale.US).format(java.util.Date())}",
                imageUri = uri,
                filterPresetId = _selectedPreset.value?.id,
                filterStrength = _presetStrength.value,
                exposure = adj.exposure,
                contrast = adj.contrast,
                highlights = adj.highlights,
                shadows = adj.shadows,
                saturation = adj.saturation,
                temperature = adj.temperature,
                tint = adj.tint,
                vibrance = adj.vibrance,
                sharpen = adj.sharpen,
                grain = adj.grain,
                vignette = adj.vignette,
                glow = adj.glow,
                blur = adj.blur,
                lightLeak = adj.lightLeak,
                dustEffect = adj.dustEffect,
                hueShift = adj.hueShift,
                rotationDegrees = adj.rotationDegrees,
                flipHorizontal = adj.flipHorizontal,
                flipVertical = adj.flipVertical,
                lastModified = System.currentTimeMillis()
            )
            val newId = projectDao.insertOrUpdate(entity)
            if (_currentProjectId.value == null) {
                _currentProjectId.value = newId
            }
        }
    }

    fun loadProject(project: ProjectEntity) {
        viewModelScope.launch {
            _currentProjectId.value = project.id
            _currentImageUri.value = project.imageUri
            _presetStrength.value = project.filterStrength
            _selectedPreset.value = project.filterPresetId?.let { DefaultPresets.getPresetById(it) }

            val adj = AdjustmentValues(
                exposure = project.exposure,
                contrast = project.contrast,
                highlights = project.highlights,
                shadows = project.shadows,
                saturation = project.saturation,
                temperature = project.temperature,
                tint = project.tint,
                vibrance = project.vibrance,
                sharpen = project.sharpen,
                grain = project.grain,
                vignette = project.vignette,
                glow = project.glow,
                blur = project.blur,
                lightLeak = project.lightLeak,
                dustEffect = project.dustEffect,
                hueShift = project.hueShift,
                rotationDegrees = project.rotationDegrees,
                flipHorizontal = project.flipHorizontal,
                flipVertical = project.flipVertical
            )
            _currentAdjustments.value = adj

            // Load source image
            if (project.imageUri.startsWith("res://")) {
                val resId = project.imageUri.removePrefix("res://").toIntOrNull() ?: R.drawable.sample_fuji_arch
                loadDefaultSampleImage(resId)
            } else {
                val uri = Uri.parse(project.imageUri)
                val bmp = BitmapUtils.decodeSampledBitmapFromUri(getApplication(), uri)
                if (bmp != null) {
                    _originalBitmap.value = bmp
                    triggerRender()
                }
            }
        }
    }

    suspend fun exportImage(
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 95
    ): Uri? {
        val bitmapToExport = _processedBitmap.value ?: return null
        val uri = BitmapUtils.saveBitmapToGallery(
            context = getApplication(),
            bitmap = bitmapToExport,
            format = format,
            quality = quality,
            title = "Venusly_${System.currentTimeMillis()}"
        )
        if (uri != null) {
            _exportStatusMessage.value = "Saved to Gallery in High Quality ✨"
            NotificationHelper.showExportSuccessNotification(
                context = getApplication(),
                imageUri = uri,
                title = "Photo Saved to Gallery ✨",
                message = "Your high-resolution aesthetic photo is ready to share!"
            )
        } else {
            _exportStatusMessage.value = "Export failed. Check storage permissions."
        }
        return uri
    }

    suspend fun getShareIntent(): Intent? {
        val bitmap = _processedBitmap.value ?: return null
        return BitmapUtils.createShareIntent(getApplication(), bitmap)
    }

    fun clearExportMessage() {
        _exportStatusMessage.value = null
    }
}
