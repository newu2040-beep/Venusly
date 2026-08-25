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
import com.example.engine.AiImageEngine
import com.example.engine.BitmapUtils
import com.example.engine.ImageProcessor
import com.example.engine.NotificationHelper
import com.example.engine.SmartCropEngine
import com.example.model.AdjustmentValues
import com.example.model.AestheticFrame
import com.example.model.BatchItemStatus
import com.example.model.BatchProcessingItem
import com.example.model.CropAspectRatio
import com.example.model.EditorTab
import com.example.model.ExportFormatOption
import com.example.model.ExportResolution
import com.example.model.FilterCategory
import com.example.model.FilterPreset
import com.example.model.GridOverlayMode
import com.example.model.LayerItem
import com.example.model.LayerType
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

    private val _gridOverlayMode = MutableStateFlow(GridOverlayMode.OFF)
    val gridOverlayMode: StateFlow<GridOverlayMode> = _gridOverlayMode.asStateFlow()

    fun cycleGridOverlayMode() {
        val modes = GridOverlayMode.values()
        val nextIndex = (_gridOverlayMode.value.ordinal + 1) % modes.size
        _gridOverlayMode.value = modes[nextIndex]
    }

    fun setGridOverlayMode(mode: GridOverlayMode) {
        _gridOverlayMode.value = mode
    }

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

    // Layers State
    private val _layers = MutableStateFlow<List<LayerItem>>(createDefaultLayers())
    val layers: StateFlow<List<LayerItem>> = _layers.asStateFlow()

    private val _selectedLayerId = MutableStateFlow<String?>(null)
    val selectedLayerId: StateFlow<String?> = _selectedLayerId.asStateFlow()

    // Batch Processing State
    private val _batchItems = MutableStateFlow<List<BatchProcessingItem>>(emptyList())
    val batchItems: StateFlow<List<BatchProcessingItem>> = _batchItems.asStateFlow()

    private val _isBatchProcessing = MutableStateFlow(false)
    val isBatchProcessing: StateFlow<Boolean> = _isBatchProcessing.asStateFlow()

    private val _batchProgress = MutableStateFlow(0f)
    val batchProgress: StateFlow<Float> = _batchProgress.asStateFlow()

    private val _batchStatusMessage = MutableStateFlow<String?>(null)
    val batchStatusMessage: StateFlow<String?> = _batchStatusMessage.asStateFlow()

    // AI Generation / Editing State
    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _aiErrorMessage = MutableStateFlow<String?>(null)
    val aiErrorMessage: StateFlow<String?> = _aiErrorMessage.asStateFlow()

    private val _aiSuccessMessage = MutableStateFlow<String?>(null)
    val aiSuccessMessage: StateFlow<String?> = _aiSuccessMessage.asStateFlow()

    fun clearAiMessages() {
        _aiErrorMessage.value = null
        _aiSuccessMessage.value = null
    }

    fun generateAiImage(prompt: String, isEditCurrentMode: Boolean) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _isAiGenerating.value = true
            _aiErrorMessage.value = null
            _aiSuccessMessage.value = null
            try {
                val baseBitmap = if (isEditCurrentMode) {
                    _processedBitmap.value ?: _originalBitmap.value
                } else null

                val result = AiImageEngine.generateOrEditImage(
                    prompt = prompt,
                    sourceBitmap = baseBitmap
                )

                result.fold(
                    onSuccess = { generatedBitmap ->
                        _originalBitmap.value = generatedBitmap
                        _processedBitmap.value = generatedBitmap
                        _aiSuccessMessage.value = if (isEditCurrentMode) "Photo transformed with AI! ✨" else "AI Image generated successfully! ✨"
                        triggerRender()
                    },
                    onFailure = { err ->
                        _aiErrorMessage.value = err.message ?: "AI Generation failed."
                    }
                )
            } catch (e: Throwable) {
                _aiErrorMessage.value = e.message ?: "AI Generation failed."
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

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

    fun performSmartAutoCrop(targetRatio: Float) {
        val current = _originalBitmap.value ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            pushUndoState()
            val cropped = SmartCropEngine.autoCropSubject(current, targetRatio)
            _originalBitmap.value = cropped
            triggerRender()
            _isProcessing.value = false
        }
    }

    private val _selectedTextOverlayId = MutableStateFlow<String?>(null)
    val selectedTextOverlayId: StateFlow<String?> = _selectedTextOverlayId.asStateFlow()

    fun selectTextOverlay(id: String?) {
        _selectedTextOverlayId.value = id
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
            fontStyle = "Monospace",
            hasBackgroundPill = false,
            isDateStamp = true
        )
        _textOverlays.value = _textOverlays.value + newOverlay
        _selectedTextOverlayId.value = newOverlay.id
        triggerRender()
    }

    fun addText(text: String = "VENUSLY", fontStyle: String = "Serif") {
        val newOverlay = TextOverlay(
            text = text,
            xPercent = 0.5f,
            yPercent = 0.82f,
            fontSizeSp = 22f,
            colorHex = "#FFFFFF",
            fontStyle = fontStyle,
            hasBackgroundPill = true,
            isDateStamp = false
        )
        _textOverlays.value = _textOverlays.value + newOverlay
        _selectedTextOverlayId.value = newOverlay.id
        triggerRender()
    }

    fun removeTextOverlay(id: String) {
        _textOverlays.value = _textOverlays.value.filter { it.id != id }
        if (_selectedTextOverlayId.value == id) {
            _selectedTextOverlayId.value = null
        }
        triggerRender()
    }

    fun updateTextPosition(id: String, xPercent: Float, yPercent: Float) {
        _textOverlays.value = _textOverlays.value.map {
            if (it.id == id) {
                it.copy(
                    xPercent = xPercent.coerceIn(0.05f, 0.95f),
                    yPercent = yPercent.coerceIn(0.05f, 0.95f)
                )
            } else it
        }
        triggerRender()
    }

    fun updateTextOverlayProperties(
        id: String,
        text: String? = null,
        colorHex: String? = null,
        fontStyle: String? = null,
        fontSizeSp: Float? = null,
        hasBackgroundPill: Boolean? = null,
        rotation: Float? = null,
        blendMode: String? = null
    ) {
        _textOverlays.value = _textOverlays.value.map {
            if (it.id == id) {
                it.copy(
                    text = text ?: it.text,
                    colorHex = colorHex ?: it.colorHex,
                    fontStyle = fontStyle ?: it.fontStyle,
                    fontSizeSp = fontSizeSp ?: it.fontSizeSp,
                    hasBackgroundPill = hasBackgroundPill ?: it.hasBackgroundPill,
                    rotation = rotation ?: it.rotation,
                    blendMode = blendMode ?: it.blendMode
                )
            } else it
        }
        triggerRender()
    }

    fun duplicateTextOverlay(id: String) {
        val existing = _textOverlays.value.find { it.id == id } ?: return
        val copied = existing.copy(
            id = java.util.UUID.randomUUID().toString(),
            xPercent = (existing.xPercent + 0.05f).coerceAtMost(0.9f),
            yPercent = (existing.yPercent + 0.05f).coerceAtMost(0.9f)
        )
        _textOverlays.value = _textOverlays.value + copied
        _selectedTextOverlayId.value = copied.id
        triggerRender()
    }

    private val _selectedStickerId = MutableStateFlow<String?>(null)
    val selectedStickerId: StateFlow<String?> = _selectedStickerId.asStateFlow()

    fun selectSticker(id: String?) {
        _selectedStickerId.value = id
    }

    fun addSticker(symbol: String) {
        val newSticker = StickerOverlay(
            symbol = symbol,
            xPercent = 0.5f,
            yPercent = 0.5f,
            sizeDp = 64f
        )
        _stickers.value = _stickers.value + newSticker
        _selectedStickerId.value = newSticker.id
        triggerRender()
    }

    fun addCustomStickerImage(uri: String) {
        val newSticker = StickerOverlay(
            symbol = "📷",
            customImageUri = uri,
            xPercent = 0.5f,
            yPercent = 0.5f,
            sizeDp = 110f
        )
        _stickers.value = _stickers.value + newSticker
        _selectedStickerId.value = newSticker.id
        triggerRender()
    }

    fun updateStickerProperties(
        id: String,
        sizeDp: Float? = null,
        rotation: Float? = null,
        alpha: Float? = null,
        blendMode: String? = null,
        tintColorHex: String? = null
    ) {
        _stickers.value = _stickers.value.map {
            if (it.id == id) {
                it.copy(
                    sizeDp = sizeDp ?: it.sizeDp,
                    rotation = rotation ?: it.rotation,
                    alpha = alpha ?: it.alpha,
                    blendMode = blendMode ?: it.blendMode,
                    tintColorHex = tintColorHex ?: it.tintColorHex
                )
            } else it
        }
        triggerRender()
    }

    fun removeSticker(id: String) {
        _stickers.value = _stickers.value.filter { it.id != id }
        if (_selectedStickerId.value == id) {
            _selectedStickerId.value = null
        }
        triggerRender()
    }

    fun duplicateSticker(id: String) {
        val existing = _stickers.value.find { it.id == id } ?: return
        val copied = existing.copy(
            id = java.util.UUID.randomUUID().toString(),
            xPercent = (existing.xPercent + 0.05f).coerceAtMost(0.9f),
            yPercent = (existing.yPercent + 0.05f).coerceAtMost(0.9f)
        )
        _stickers.value = _stickers.value + copied
        _selectedStickerId.value = copied.id
        triggerRender()
    }

    fun updateStickerPosition(id: String, xPercent: Float, yPercent: Float) {
        _stickers.value = _stickers.value.map {
            if (it.id == id) {
                it.copy(
                    xPercent = xPercent.coerceIn(0.05f, 0.95f),
                    yPercent = yPercent.coerceIn(0.05f, 0.95f)
                )
            } else it
        }
        triggerRender()
    }

    fun updateStickerScale(id: String, deltaScale: Float) {
        _stickers.value = _stickers.value.map {
            if (it.id == id) {
                val newSize = (it.sizeDp * deltaScale).coerceIn(24f, 200f)
                it.copy(sizeDp = newSize)
            } else it
        }
        triggerRender()
    }

    fun updateStickerRotation(id: String, deltaDegrees: Float) {
        _stickers.value = _stickers.value.map {
            if (it.id == id) {
                it.copy(rotation = (it.rotation + deltaDegrees) % 360f)
            } else it
        }
        triggerRender()
    }

    fun bringStickerToFront(id: String) {
        val target = _stickers.value.find { it.id == id } ?: return
        _stickers.value = _stickers.value.filter { it.id != id } + target
        triggerRender()
    }

    private fun applyCurrentPreset() {
        val preset = _selectedPreset.value ?: DefaultPresets.presets.first()
        _currentAdjustments.value = preset.adjustments
        triggerRender()
    }

    private fun createDefaultLayers(): List<LayerItem> {
        return listOf(
            LayerItem(id = "layer_base", type = LayerType.BASE_IMAGE, name = "Base Photo"),
            LayerItem(id = "layer_adj", type = LayerType.ADJUSTMENTS, name = "Color Grading & Preset"),
            LayerItem(id = "layer_grain", type = LayerType.GRAIN_LIGHT_LEAK, name = "Grain & Light Effects"),
            LayerItem(id = "layer_frame", type = LayerType.FRAME, name = "Frame & Borders")
        )
    }

    private fun syncLayersWithOverlays() {
        val currentList = _layers.value.toMutableList()
        val existingIds = currentList.mapNotNull { it.associatedId }.toSet()

        for (text in _textOverlays.value) {
            if (!existingIds.contains(text.id)) {
                val layerName = if (text.isDateStamp) "Date Stamp ('${text.text})" else "Text: \"${text.text.take(12)}\""
                currentList.add(
                    LayerItem(
                        id = "text_layer_${text.id}",
                        type = LayerType.TEXT_OVERLAY,
                        name = layerName,
                        associatedId = text.id
                    )
                )
            }
        }

        for (stk in _stickers.value) {
            if (!existingIds.contains(stk.id)) {
                val layerName = "Sticker ${stk.symbol}"
                currentList.add(
                    LayerItem(
                        id = "sticker_layer_${stk.id}",
                        type = LayerType.STICKER,
                        name = layerName,
                        associatedId = stk.id
                    )
                )
            }
        }

        val validTextIds = _textOverlays.value.map { it.id }.toSet()
        val validStickerIds = _stickers.value.map { it.id }.toSet()

        val filtered = currentList.filter { item ->
            when (item.type) {
                LayerType.TEXT_OVERLAY -> validTextIds.contains(item.associatedId)
                LayerType.STICKER -> validStickerIds.contains(item.associatedId)
                else -> true
            }
        }
        _layers.value = filtered
    }

    fun toggleLayerVisibility(layerId: String) {
        _layers.value = _layers.value.map {
            if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it
        }
        triggerRender()
    }

    fun setLayerOpacity(layerId: String, opacity: Float) {
        _layers.value = _layers.value.map {
            if (it.id == layerId) it.copy(opacity = opacity.coerceIn(0f, 1f)) else it
        }
        triggerRender()
    }

    fun toggleLayerLock(layerId: String) {
        _layers.value = _layers.value.map {
            if (it.id == layerId) it.copy(isLocked = !it.isLocked) else it
        }
    }

    fun moveLayerUp(index: Int) {
        if (index <= 0) return
        val list = _layers.value.toMutableList()
        val item = list.removeAt(index)
        list.add(index - 1, item)
        _layers.value = list
        triggerRender()
    }

    fun moveLayerDown(index: Int) {
        val list = _layers.value.toMutableList()
        if (index >= list.size - 1) return
        val item = list.removeAt(index)
        list.add(index + 1, item)
        _layers.value = list
        triggerRender()
    }

    fun selectLayer(layerId: String) {
        _selectedLayerId.value = layerId
        val target = _layers.value.find { it.id == layerId } ?: return
        if (target.associatedId != null) {
            if (target.type == LayerType.TEXT_OVERLAY) {
                _selectedTextOverlayId.value = target.associatedId
            } else if (target.type == LayerType.STICKER) {
                _selectedStickerId.value = target.associatedId
            }
        }
    }

    // Batch Processing Methods
    fun loadBatchPhotos(uris: List<Uri>) {
        viewModelScope.launch {
            val items = uris.map { uri ->
                val thumb = BitmapUtils.decodeSampledBitmapFromUri(getApplication(), uri, 300, 300)
                BatchProcessingItem(
                    uri = uri,
                    thumbnailBitmap = thumb,
                    status = BatchItemStatus.PENDING
                )
            }
            _batchItems.value = items
            _batchStatusMessage.value = "${items.size} photos ready for batch filter application."
        }
    }

    fun removeBatchPhoto(id: String) {
        _batchItems.value = _batchItems.value.filter { it.id != id }
    }

    fun clearBatch() {
        _batchItems.value = emptyList()
        _isBatchProcessing.value = false
        _batchProgress.value = 0f
        _batchStatusMessage.value = null
    }

    fun startBatchProcessing(customPreset: FilterPreset? = null) {
        if (_batchItems.value.isEmpty()) return
        viewModelScope.launch {
            _isBatchProcessing.value = true
            _batchProgress.value = 0f
            val targetAdjustments = customPreset?.adjustments ?: _currentAdjustments.value
            val targetPresetStrength = if (customPreset != null) 0.85f else _presetStrength.value
            val targetTexts = _textOverlays.value
            val targetStickers = _stickers.value
            val targetLayers = _layers.value

            val total = _batchItems.value.size
            var completedCount = 0

            val updatedList = _batchItems.value.toMutableList()

            for (i in updatedList.indices) {
                val item = updatedList[i]
                updatedList[i] = item.copy(status = BatchItemStatus.PROCESSING)
                _batchItems.value = updatedList.toList()
                _batchStatusMessage.value = "Processing photo ${i + 1} of $total..."

                try {
                    val fullBitmap = BitmapUtils.decodeSampledBitmapFromUri(getApplication(), item.uri, 2048, 2048)
                    if (fullBitmap != null) {
                        val processed = ImageProcessor.applyAdjustments(
                            source = fullBitmap,
                            adjustments = targetAdjustments,
                            strength = targetPresetStrength,
                            textOverlays = targetTexts,
                            stickers = targetStickers,
                            layers = targetLayers
                        )

                        val savedUri = BitmapUtils.saveBitmapToGallery(
                            context = getApplication(),
                            bitmap = processed,
                            title = "Venusly_Batch_${System.currentTimeMillis()}_${i + 1}"
                        )

                        completedCount++
                        updatedList[i] = updatedList[i].copy(
                            status = BatchItemStatus.COMPLETED,
                            resultUri = savedUri
                        )
                    } else {
                        updatedList[i] = updatedList[i].copy(
                            status = BatchItemStatus.FAILED,
                            errorMessage = "Failed to load image"
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    updatedList[i] = updatedList[i].copy(
                        status = BatchItemStatus.FAILED,
                        errorMessage = e.localizedMessage ?: "Processing error"
                    )
                }

                _batchItems.value = updatedList.toList()
                _batchProgress.value = (i + 1).toFloat() / total.toFloat()
            }

            _isBatchProcessing.value = false
            _batchStatusMessage.value = "Batch completed! Exported $completedCount of $total photos to gallery."
            NotificationHelper.showExportSuccessNotification(
                context = getApplication(),
                imageUri = updatedList.firstOrNull()?.resultUri,
                title = "Batch Filter Processing Complete ✨",
                message = "Applied filter stack and saved $completedCount photos to your gallery."
            )
        }
    }

    private fun triggerRender() {
        processJob?.cancel()
        processJob = viewModelScope.launch {
            delay(16) // Smooth 60fps debounce
            syncLayersWithOverlays()
            val source = _originalBitmap.value ?: return@launch
            val adjustments = _currentAdjustments.value
            val strength = _presetStrength.value
            val texts = _textOverlays.value
            val stks = _stickers.value
            val layerList = _layers.value

            val processed = ImageProcessor.applyAdjustments(
                source = source,
                adjustments = adjustments,
                strength = strength,
                textOverlays = texts,
                stickers = stks,
                layers = layerList
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

    private val _exportResolution = MutableStateFlow(ExportResolution.ORIGINAL)
    val exportResolution: StateFlow<ExportResolution> = _exportResolution.asStateFlow()

    private val _exportFormat = MutableStateFlow(ExportFormatOption.JPEG)
    val exportFormat: StateFlow<ExportFormatOption> = _exportFormat.asStateFlow()

    private val _exportQuality = MutableStateFlow(95)
    val exportQuality: StateFlow<Int> = _exportQuality.asStateFlow()

    fun setExportResolution(resolution: ExportResolution) {
        _exportResolution.value = resolution
    }

    fun setExportFormat(format: ExportFormatOption) {
        _exportFormat.value = format
    }

    fun setExportQuality(quality: Int) {
        _exportQuality.value = quality.coerceIn(50, 100)
    }

    suspend fun exportImage(
        format: Bitmap.CompressFormat = _exportFormat.value.format,
        quality: Int = _exportQuality.value,
        resolution: Int = _exportResolution.value.maxDimension
    ): Uri? {
        val bitmapToExport = _processedBitmap.value ?: return null
        val uri = BitmapUtils.saveBitmapToGallery(
            context = getApplication(),
            bitmap = bitmapToExport,
            format = format,
            quality = quality,
            targetResolution = resolution,
            title = "Venusly_${System.currentTimeMillis()}"
        )
        if (uri != null) {
            _exportStatusMessage.value = "Saved to Gallery in Ultra High Quality ✨"
            NotificationHelper.showExportSuccessNotification(
                context = getApplication(),
                imageUri = uri,
                title = "Photo Saved to Gallery ✨",
                message = "Your ultra high-resolution aesthetic photo is ready to share!"
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
