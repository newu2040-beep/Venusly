package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import com.example.model.StickerOverlay
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdjustmentValues
import com.example.model.AestheticFrame
import com.example.model.EditorTab
import com.example.model.ExportFormatOption
import com.example.model.ExportResolution
import com.example.model.GridOverlayMode
import com.example.ui.components.PillSliderControl
import com.example.ui.theme.LocalCompactMode
import com.example.ui.theme.VenuslyBlue
import com.example.ui.theme.VenuslyBlueLight
import com.example.viewmodel.EditorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    editorViewModel: EditorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val originalBitmap by editorViewModel.originalBitmap.collectAsState()
    val processedBitmap by editorViewModel.processedBitmap.collectAsState()
    val adjustments by editorViewModel.currentAdjustments.collectAsState()
    val activeTab by editorViewModel.activeTab.collectAsState()
    val isComparingBefore by editorViewModel.isComparingBefore.collectAsState()
    val canUndo by editorViewModel.canUndo.collectAsState()
    val canRedo by editorViewModel.canRedo.collectAsState()
    val isProcessing by editorViewModel.isProcessing.collectAsState()
    val exportMsg by editorViewModel.exportStatusMessage.collectAsState()
    val stickers by editorViewModel.stickers.collectAsState()
    val selectedStickerId by editorViewModel.selectedStickerId.collectAsState()
    val gridOverlayMode by editorViewModel.gridOverlayMode.collectAsState()
    val isCompactMode = LocalCompactMode.current

    var showExportSheet by remember { mutableStateOf(false) }
    var exportQuality by remember { mutableFloatStateOf(95f) }
    var exportFormatPng by remember { mutableStateOf(false) }
    var showTextDialog by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("VENUSLY ✨") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            editorViewModel.loadImageFromUri(uri)
        }
    }

    val rawPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            editorViewModel.loadImageFromUri(uri)
        }
    }

    LaunchedEffect(exportMsg) {
        exportMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            editorViewModel.clearExportMessage()
        }
    }

    val displayBitmap = if (isComparingBefore) originalBitmap else (processedBitmap ?: originalBitmap)

    Column(
        modifier = modifier
            .testTag("editor_screen")
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Top Bar: Back, "Edit", Undo, Redo, Share
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .testTag("editor_back_button")
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Edit",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { editorViewModel.undo() },
                    enabled = canUndo,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }

                IconButton(
                    onClick = { editorViewModel.redo() },
                    enabled = canRedo,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }

                IconButton(
                    onClick = {
                        scope.launch {
                            val intent = editorViewModel.getShareIntent()
                            if (intent != null) {
                                context.startActivity(android.content.Intent.createChooser(intent, "Share via Venusly"))
                            }
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 2. Main Image Canvas with "Live" badge & Interactive Touch Stickers
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .aspectRatio(1.15f)
                .clip(RoundedCornerShape(26.dp))
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            editorViewModel.setComparingBefore(true)
                            tryAwaitRelease()
                            editorViewModel.setComparingBefore(false)
                        },
                        onTap = {
                            editorViewModel.selectSticker(null)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (displayBitmap != null) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val containerWidthPx = constraints.maxWidth.toFloat()
                    val containerHeightPx = constraints.maxHeight.toFloat()

                    Image(
                        bitmap = displayBitmap.asImageBitmap(),
                        contentDescription = "Editing Canvas",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Composition Grid Overlay (Rule of Thirds, Golden Ratio, Center Grid)
                    CompositionGridOverlay(
                        mode = gridOverlayMode,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Interactive Sticker Overlay Elements on Canvas
                    stickers.forEach { sticker ->
                        val isSelected = sticker.id == selectedStickerId
                        val xPx = sticker.xPercent * containerWidthPx
                        val yPx = sticker.yPercent * containerHeightPx
                        val stkSizePx = sticker.sizeDp * (containerWidthPx / 360f)

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (xPx - stkSizePx / 2f).toInt(),
                                        (yPx - stkSizePx / 2f).toInt()
                                    )
                                }
                                .graphicsLayer { rotationZ = sticker.rotation }
                                .pointerInput(sticker.id) {
                                    detectTapGestures {
                                        editorViewModel.selectSticker(sticker.id)
                                    }
                                }
                                .pointerInput(sticker.id) {
                                    detectDragGestures(
                                        onDragStart = { editorViewModel.selectSticker(sticker.id) },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val newX = (xPx + dragAmount.x) / containerWidthPx
                                            val newY = (yPx + dragAmount.y) / containerHeightPx
                                            editorViewModel.updateStickerPosition(sticker.id, newX, newY)
                                        }
                                    )
                                }
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .border(2.dp, Color(0xFFEC4899), RoundedCornerShape(12.dp))
                                            .background(Color(0x33EC4899), RoundedCornerShape(12.dp))
                                    } else Modifier
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sticker.symbol,
                                fontSize = (sticker.sizeDp * 0.7f).sp
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        tint = VenuslyBlue,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No Photo Loaded",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pick an image or RAW photo to begin editing",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f)),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VenuslyBlue),
                            shape = CircleShape
                        ) {
                            Icon(imageVector = Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { rawPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = CircleShape
                        ) {
                            Icon(imageVector = Icons.Filled.MotionPhotosOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import RAW", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Top-left Live badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isComparingBefore) "ORIGINAL" else "LIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // Top-Right Composition Grid Toggle Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (gridOverlayMode != GridOverlayMode.OFF) Color(0xDD0284C7) else Color(0xCC000000))
                    .clickable { editorViewModel.cycleGridOverlayMode() }
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.GridOn,
                        contentDescription = "Composition Grid Overlay",
                        tint = if (gridOverlayMode != GridOverlayMode.OFF) Color.White else Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = gridOverlayMode.badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // Bottom hint: Hold to compare
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x77000000))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isComparingBefore) "Showing Original" else "Hold to Compare • Tap Sticker to Edit",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                )
            }
        }

        // 3. Tab Bar (Adjust, Colors, Effects, Frames, Details, Light, Overlays)
        ScrollableTabRow(
            selectedTabIndex = activeTab.ordinal,
            edgePadding = if (isCompactMode) 12.dp else 20.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {},
            indicator = { tabPositions ->
                if (activeTab.ordinal < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                        height = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            EditorTab.values().forEach { tab ->
                val isSelected = tab == activeTab
                Tab(
                    selected = isSelected,
                    onClick = { editorViewModel.setActiveTab(tab) },
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = if (isCompactMode) 13.sp else 14.sp
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 4. Sliders List for Active Tab
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            when (activeTab) {
                EditorTab.ADJUST -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Composition Grid Overlay",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = gridOverlayMode.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(GridOverlayMode.values()) { mode ->
                                    FilterChip(
                                        selected = (gridOverlayMode == mode),
                                        onClick = { editorViewModel.setGridOverlayMode(mode) },
                                        label = { Text(mode.displayName, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = VenuslyBlue,
                                            selectedLabelColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }
                    item {
                        PillSliderControl(
                            label = "Exposure",
                            value = adjustments.exposure,
                            valueRange = -1.0f..1.0f,
                            formatValue = { if (it >= 0) "+%.2f".format(it) else "%.2f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(exposure = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Contrast",
                            value = adjustments.contrast,
                            valueRange = -100f..100f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(contrast = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Clarity / Structure",
                            value = adjustments.clarity,
                            valueRange = -50f..50f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(clarity = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Noise Reduction (Denoise)",
                            value = adjustments.noiseReduction,
                            valueRange = 0f..100f,
                            formatValue = { "%.0f%%".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(noiseReduction = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Highlights",
                            value = adjustments.highlights,
                            valueRange = -100f..100f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(highlights = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Shadows",
                            value = adjustments.shadows,
                            valueRange = -100f..100f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(shadows = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Saturation",
                            value = adjustments.saturation,
                            valueRange = -100f..100f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(saturation = v) } }
                        )
                    }
                }

                EditorTab.COLORS -> {
                    // Quick Cinematic Color Grading LUT Presets
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Cinematic Color Grading Looks",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            editorViewModel.updateAdjustmentContinuous {
                                                it.copy(
                                                    shadowHue = -110f, shadowSaturation = 50f,
                                                    highlightHue = 35f, highlightSaturation = 60f,
                                                    skyBlueBoost = 30f, skinToneWarmth = 20f
                                                )
                                            }
                                        },
                                        label = { Text("Teal & Orange", fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Color(0xFF0F172A),
                                            labelColor = Color(0xFF38BDF8)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            editorViewModel.updateAdjustmentContinuous {
                                                it.copy(
                                                    liftedBlacks = 40f, highlightCompress = 25f,
                                                    skinToneWarmth = 35f, temperature = 15f
                                                )
                                            }
                                        },
                                        label = { Text("Portra Gold", fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Color(0xFF1C1917),
                                            labelColor = Color(0xFFFBBF24)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            editorViewModel.updateAdjustmentContinuous {
                                                it.copy(
                                                    shadowHue = 120f, shadowSaturation = 45f,
                                                    highlightHue = -40f, highlightSaturation = 55f,
                                                    liftedBlacks = 20f
                                                )
                                            }
                                        },
                                        label = { Text("Cyberpunk Neon", fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Color(0xFF18181B),
                                            labelColor = Color(0xFFF472B6)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            editorViewModel.updateAdjustmentContinuous {
                                                it.copy(
                                                    temperature = -30f, skyBlueBoost = 40f,
                                                    liftedBlacks = 25f, shadowHue = -120f, shadowSaturation = 30f
                                                )
                                            }
                                        },
                                        label = { Text("Nordic Chill", fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Color(0xFF0F172A),
                                            labelColor = Color(0xFF60A5FA)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            editorViewModel.updateAdjustmentContinuous {
                                                it.copy(
                                                    contrast = 40f, saturation = -65f,
                                                    liftedBlacks = 35f, highlightCompress = 40f
                                                )
                                            }
                                        },
                                        label = { Text("Bleach Bypass", fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Color(0xFF27272A),
                                            labelColor = Color(0xFFE4E4E7)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            editorViewModel.updateAdjustmentContinuous {
                                                it.copy(
                                                    shadowHue = 0f, shadowSaturation = 0f,
                                                    midtoneHue = 0f, midtoneSaturation = 0f,
                                                    highlightHue = 0f, highlightSaturation = 0f,
                                                    liftedBlacks = 0f, highlightCompress = 0f,
                                                    skinToneWarmth = 0f, skyBlueBoost = 0f, foliageGreenBoost = 0f
                                                )
                                            }
                                        },
                                        label = { Text("Reset Colors", fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Color(0xFF3F3F46),
                                            labelColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Section 1: 3-Way Color Wheels / Tone Grading
                    item {
                        Text(
                            text = "3-Way Tone Color Wheels",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Shadow Tone Hue",
                            value = adjustments.shadowHue,
                            valueRange = -180f..180f,
                            formatValue = { "%.0f°".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(shadowHue = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Shadow Tone Saturation",
                            value = adjustments.shadowSaturation,
                            valueRange = 0f..100f,
                            formatValue = { "%.0f%%".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(shadowSaturation = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Midtone Color Shift",
                            value = adjustments.midtoneHue,
                            valueRange = -180f..180f,
                            formatValue = { "%.0f°".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(midtoneHue = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Midtone Intensity",
                            value = adjustments.midtoneSaturation,
                            valueRange = 0f..100f,
                            formatValue = { "%.0f%%".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(midtoneSaturation = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Highlight Tone Hue",
                            value = adjustments.highlightHue,
                            valueRange = -180f..180f,
                            formatValue = { "%.0f°".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(highlightHue = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Highlight Tone Saturation",
                            value = adjustments.highlightSaturation,
                            valueRange = 0f..100f,
                            formatValue = { "%.0f%%".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(highlightSaturation = v) } }
                        )
                    }

                    // Section 2: Filmic Tone Curve Pedestal
                    item {
                        Text(
                            text = "Tone Curve & Pedestal",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Lifted Blacks (Filmic Matte Fade)",
                            value = adjustments.liftedBlacks,
                            valueRange = 0f..100f,
                            formatValue = { "%.0f%%".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(liftedBlacks = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Soft Highlight Recovery",
                            value = adjustments.highlightCompress,
                            valueRange = 0f..100f,
                            formatValue = { "%.0f%%".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(highlightCompress = v) } }
                        )
                    }

                    // Section 3: Selective Target HSL
                    item {
                        Text(
                            text = "Selective Target Color HSL",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Skin Tone Warmth",
                            value = adjustments.skinToneWarmth,
                            valueRange = -50f..50f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(skinToneWarmth = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Sky & Ocean Blue Vibrance",
                            value = adjustments.skyBlueBoost,
                            valueRange = -50f..50f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(skyBlueBoost = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Foliage & Nature Emerald",
                            value = adjustments.foliageGreenBoost,
                            valueRange = -50f..50f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(foliageGreenBoost = v) } }
                        )
                    }

                    // Section 4: White Balance & Channels
                    item {
                        Text(
                            text = "White Balance & Primary Channels",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Temperature (Warmth)",
                            value = adjustments.temperature,
                            valueRange = -100f..100f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(temperature = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Tint (Magenta / Green)",
                            value = adjustments.tint,
                            valueRange = -100f..100f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(tint = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Vibrance",
                            value = adjustments.vibrance,
                            valueRange = -100f..100f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(vibrance = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Red Channel Balance",
                            value = adjustments.redChannel,
                            valueRange = -100f..100f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(redChannel = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Green Channel Balance",
                            value = adjustments.greenChannel,
                            valueRange = -100f..100f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(greenChannel = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Blue Channel Balance",
                            value = adjustments.blueChannel,
                            valueRange = -100f..100f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(blueChannel = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Hue Shift",
                            value = adjustments.hueShift,
                            valueRange = -180f..180f,
                            formatValue = { "%.0f°".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(hueShift = v) } }
                        )
                    }
                }

                EditorTab.EFFECTS -> {
                    item {
                        PillSliderControl(
                            label = "Dreamy Glow / Halation",
                            value = adjustments.glow,
                            valueRange = 0f..100f,
                            formatValue = { "%.0f%%".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(glow = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Light Leak",
                            value = adjustments.lightLeak,
                            valueRange = 0f..100f,
                            formatValue = { "%.0f%%".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(lightLeak = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Retro Film Dust & Scratches",
                            value = adjustments.dustEffect,
                            valueRange = 0f..100f,
                            formatValue = { "%.0f%%".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(dustEffect = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Vignette",
                            value = adjustments.vignette,
                            valueRange = 0f..100f,
                            formatValue = { "%.0f%%".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(vignette = v) } }
                        )
                    }
                }

                EditorTab.FRAMES -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(if (isCompactMode) 14.dp else 20.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(if (isCompactMode) 12.dp else 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = "Photo Corner Rounding & Frame Style",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Smooth out photo corners and set perfectly in framed matte borders (v2.5.0)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    item {
                                        FilterChip(
                                            selected = false,
                                            onClick = {
                                                editorViewModel.updateAdjustmentContinuous {
                                                    it.copy(photoCornerRadius = 18f, frameMatteWidth = 0f)
                                                }
                                            },
                                            label = { Text("Soft Rounded", fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = Color(0xFF1E293B),
                                                labelColor = Color(0xFF38BDF8)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                    item {
                                        FilterChip(
                                            selected = false,
                                            onClick = {
                                                editorViewModel.updateAdjustmentContinuous {
                                                    it.copy(photoCornerRadius = 35f, frameMatteWidth = 30f, frameMatteColor = 0xFFFFFFFF)
                                                }
                                            },
                                            label = { Text("Squircle Card", fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = Color(0xFFF8FAFC),
                                                labelColor = Color(0xFF0F172A)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                    item {
                                        FilterChip(
                                            selected = false,
                                            onClick = {
                                                editorViewModel.updateAdjustmentContinuous {
                                                    it.copy(photoCornerRadius = 100f, frameMatteWidth = 35f, frameMatteColor = 0xFF18181B)
                                                }
                                            },
                                            label = { Text("Pill Oval", fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = Color(0xFF18181B),
                                                labelColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                    item {
                                        FilterChip(
                                            selected = false,
                                            onClick = {
                                                editorViewModel.updateAdjustmentContinuous {
                                                    it.copy(photoCornerRadius = 25f, frameMatteWidth = 30f, frameMatteColor = 0xFFFCE7F3)
                                                }
                                            },
                                            label = { Text("Pastel Frame", fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = Color(0xFFFCE7F3),
                                                labelColor = Color(0xFFBE185D)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                    item {
                                        FilterChip(
                                            selected = false,
                                            onClick = {
                                                editorViewModel.updateAdjustmentContinuous {
                                                    it.copy(photoCornerRadius = 12f, frameMatteWidth = 45f, frameMatteColor = 0xFFFEF3C7)
                                                }
                                            },
                                            label = { Text("Gallery Mat", fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = Color(0xFFFEF3C7),
                                                labelColor = Color(0xFF78350F)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                    item {
                                        FilterChip(
                                            selected = false,
                                            onClick = {
                                                editorViewModel.updateAdjustmentContinuous {
                                                    it.copy(photoCornerRadius = 0f, frameMatteWidth = 0f)
                                                }
                                            },
                                            label = { Text("Reset Corners", fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = Color(0xFF3F3F46),
                                                labelColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                }
                            }

                            PillSliderControl(
                                label = "Photo Corner Radius",
                                value = adjustments.photoCornerRadius,
                                valueRange = 0f..100f,
                                formatValue = { "%.0f%%".format(it) },
                                onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(photoCornerRadius = v) } }
                            )

                            PillSliderControl(
                                label = "Frame Matte Margin",
                                value = adjustments.frameMatteWidth,
                                valueRange = 0f..100f,
                                formatValue = { "%.0f%%".format(it) },
                                onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(frameMatteWidth = v) } }
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Matte Frame Color",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                val colors = listOf(
                                    "Studio White" to 0xFFFFFFFFL,
                                    "Dark Obsidian" to 0xFF18181BL,
                                    "Warm Cream" to 0xFFFEF3C7L,
                                    "Soft Pink" to 0xFFFCE7F3L,
                                    "Sky Blue" to 0xFFE0F2FEL,
                                    "Dark Slate" to 0xFF1E293BL,
                                    "Cutout Clear" to 0x00000000L
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(colors) { (name, hex) ->
                                        val isSelected = adjustments.frameMatteColor == hex
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                editorViewModel.updateAdjustmentContinuous { it.copy(frameMatteColor = hex) }
                                            },
                                            label = { Text(name, fontSize = 11.sp) },
                                            leadingIcon = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(hex))
                                                        .border(1.dp, Color.Gray, CircleShape)
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Aesthetic Film & Art Frames",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Apply analog borders, polaroids, and minimal mats",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                if (adjustments.frame != AestheticFrame.NONE) {
                                    TextButton(
                                        onClick = { editorViewModel.updateFrame(AestheticFrame.NONE) },
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("Remove", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            AestheticFrame.values().forEach { frameOption ->
                                val isSelected = adjustments.frame == frameOption
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { editorViewModel.updateFrame(frameOption) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    ),
                                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = if (isCompactMode) 8.dp else 11.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(if (isCompactMode) 32.dp else 38.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        when (frameOption) {
                                                            AestheticFrame.NONE -> Color(0xFF94A3B8)
                                                            AestheticFrame.POLAROID_WHITE -> Color(0xFFF8FAFC)
                                                            AestheticFrame.POLAROID_DARK -> Color(0xFF18181B)
                                                            AestheticFrame.POLAROID_PASTEL -> Color(0xFFFCE7F3)
                                                            AestheticFrame.FILM_35MM -> Color(0xFF0F0F12)
                                                            AestheticFrame.DIGICAM_OSD -> Color(0xFF0284C7)
                                                            AestheticFrame.PASTEL_AURA -> Color(0xFFC084FC)
                                                            AestheticFrame.CLEAN_MAT -> Color(0xFFF1F5F9)
                                                            AestheticFrame.MINIMAL_KEYLINE -> Color(0xFF334155)
                                                            AestheticFrame.VINTAGE_STAMP -> Color(0xFFFDE68A)
                                                            AestheticFrame.PASTEL_CARD -> Color(0xFFBFDBFE)
                                                            AestheticFrame.RETRO_TV -> Color(0xFF1E293B)
                                                            AestheticFrame.Y2K_STICKER_FRAME -> Color(0xFFF472B6)
                                                            AestheticFrame.NEON_CYBER_BORDER -> Color(0xFFD946EF)
                                                            AestheticFrame.SCALLOPED_LACE -> Color(0xFFFB7185)
                                                            AestheticFrame.FILM_SLIDE_MOUNT -> Color(0xFFF1F5F9)
                                                            AestheticFrame.GOLD_GLITTER_BORDER -> Color(0xFFF59E0B)
                                                            AestheticFrame.FLORAL_PASTEL_RIBBON -> Color(0xFFF472B6)
                                                            AestheticFrame.PAPER_TEAR_SCRAPBOOK -> Color(0xFFFEF9C3)
                                                        }
                                                    )
                                                    .border(1.dp, Color(0x22000000), RoundedCornerShape(8.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (frameOption == AestheticFrame.NONE) Icons.Filled.CropFree else Icons.Filled.CropPortrait,
                                                    contentDescription = frameOption.displayName,
                                                    tint = if (frameOption == AestheticFrame.POLAROID_WHITE || frameOption == AestheticFrame.CLEAN_MAT || frameOption == AestheticFrame.PASTEL_CARD) Color(0xFF0F172A) else Color.White,
                                                    modifier = Modifier.size(if (isCompactMode) 16.dp else 20.dp)
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = frameOption.displayName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = if (isCompactMode) 13.sp else 14.sp
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = frameOption.subtitle,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = if (isCompactMode) 10.sp else 11.sp
                                                    )
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                EditorTab.DETAILS -> {
                    item {
                        PillSliderControl(
                            label = "Film Grain",
                            value = adjustments.grain,
                            valueRange = 0f..100f,
                            formatValue = { "%.0f%%".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(grain = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Sharpen",
                            value = adjustments.sharpen,
                            valueRange = 0f..100f,
                            formatValue = { "%.0f%%".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(sharpen = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Soft Blur",
                            value = adjustments.blur,
                            valueRange = 0f..50f,
                            formatValue = { "%.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(blur = v) } }
                        )
                    }
                }

                EditorTab.LIGHT -> {
                    item {
                        PillSliderControl(
                            label = "Brightness",
                            value = adjustments.brightness,
                            valueRange = -100f..100f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(brightness = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Highlights Recovery",
                            value = adjustments.highlights,
                            valueRange = -100f..100f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(highlights = v) } }
                        )
                    }
                    item {
                        PillSliderControl(
                            label = "Shadow Boost",
                            value = adjustments.shadows,
                            valueRange = -100f..100f,
                            formatValue = { "%+.0f".format(it) },
                            onValueChange = { v -> editorViewModel.updateAdjustmentContinuous { it.copy(shadows = v) } }
                        )
                    }
                }

                EditorTab.OVERLAYS -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Aesthetic Overlays & Interactive Stickers",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Active Selected Sticker Action Toolbar
                            if (selectedStickerId != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Active Sticker Controls",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { editorViewModel.duplicateSticker(selectedStickerId!!) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate / Copy", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(
                                                onClick = { editorViewModel.updateStickerScale(selectedStickerId!!, 1.2f) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Filled.ZoomIn, contentDescription = "Scale Up", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(
                                                onClick = { editorViewModel.updateStickerScale(selectedStickerId!!, 0.8f) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Filled.ZoomOut, contentDescription = "Scale Down", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(
                                                onClick = { editorViewModel.updateStickerRotation(selectedStickerId!!, 30f) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Filled.Rotate90DegreesCw, contentDescription = "Rotate", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(
                                                onClick = { editorViewModel.bringStickerToFront(selectedStickerId!!) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Filled.VerticalAlignTop, contentDescription = "Bring to Front", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(
                                                onClick = { editorViewModel.removeSticker(selectedStickerId!!) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { editorViewModel.addDateStamp() },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = VenuslyBlue),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("'98 Date", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { showTextDialog = true },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = VenuslyBlueLight),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Text", fontSize = 12.sp)
                                }
                            }

                            Text(
                                text = "Cute & Aesthetic Stickers Collection (Tap to add)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                val cuteStickers = listOf(
                                    "✨", "🎀", "🍒", "🌸", "🐰", "🐻", "💖", "💫", "🦋", "☁️",
                                    "🍓", "🍭", "👑", "💌", "🎧", "💿", "📷", "🌟", "🍀", "🌼",
                                    "👼", "🔮", "🕊️", "🍦", "🎨", "💎", "🧸", "🦄", "🎞️", "🌙", "♡"
                                )
                                items(cuteStickers) { s ->
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { editorViewModel.addSticker(s) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = s, fontSize = 22.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Quick Tools Icon Row (Crop, Rotate, Flip, Sharpen, Vignette, Reset)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EditorToolIconButton(
                icon = Icons.Filled.Rotate90DegreesCw,
                label = "Rotate",
                onClick = { editorViewModel.rotate90() }
            )
            EditorToolIconButton(
                icon = Icons.Filled.Flip,
                label = "Flip",
                onClick = { editorViewModel.flipHorizontal() }
            )
            EditorToolIconButton(
                icon = Icons.Filled.Grain,
                label = "Grain",
                onClick = {
                    editorViewModel.setActiveTab(EditorTab.DETAILS)
                    editorViewModel.updateAdjustment { it.copy(grain = if (it.grain > 0) 0f else 25f) }
                }
            )
            EditorToolIconButton(
                icon = Icons.Filled.Flare,
                label = "Glow",
                onClick = {
                    editorViewModel.setActiveTab(EditorTab.EFFECTS)
                    editorViewModel.updateAdjustment { it.copy(glow = if (it.glow > 0) 0f else 35f) }
                }
            )
            EditorToolIconButton(
                icon = Icons.Filled.Refresh,
                label = "Reset",
                onClick = { editorViewModel.resetAdjustments() }
            )
        }

        // 6. Prominent Blue "Export" Pill Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Button(
                onClick = { showExportSheet = true },
                modifier = Modifier
                    .testTag("export_button")
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(10.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Export Photo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Upload,
                        contentDescription = "Export",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // High Quality Custom Photo Export Bottom Sheet
    if (showExportSheet) {
        val currentResolution by editorViewModel.exportResolution.collectAsState()
        val currentFormat by editorViewModel.exportFormat.collectAsState()
        val currentQuality by editorViewModel.exportQuality.collectAsState()

        ModalBottomSheet(
            onDismissRequest = { showExportSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ultra High-Res Export",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Studio master rendering & color profile",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "HQ Studio",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // 1. Resolution Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Output Resolution",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ExportResolution.values().forEach { res ->
                            val isSelected = currentResolution == res
                            FilterChip(
                                selected = isSelected,
                                onClick = { editorViewModel.setExportResolution(res) },
                                label = {
                                    Text(
                                        text = when (res) {
                                            ExportResolution.ORIGINAL -> "4K Ultra"
                                            ExportResolution.QHD_2K -> "2K QHD"
                                            ExportResolution.FHD_1080P -> "1080p FHD"
                                            ExportResolution.HD_720P -> "720p HD"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = CircleShape,
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // 2. Format Selection (JPEG, PNG, WEBP)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Image Format",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExportFormatOption.values().forEach { fmt ->
                            val isSelected = currentFormat == fmt
                            FilterChip(
                                selected = isSelected,
                                onClick = { editorViewModel.setExportFormat(fmt) },
                                label = {
                                    Text(
                                        text = fmt.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = CircleShape,
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // 3. Quality Slider (if format != PNG)
                if (currentFormat != ExportFormatOption.PNG) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Compression Quality",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$currentQuality%",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Slider(
                            value = currentQuality.toFloat(),
                            onValueChange = { editorViewModel.setExportQuality(it.toInt()) },
                            valueRange = 60f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // 4. Action Buttons
                Button(
                    onClick = {
                        scope.launch {
                            editorViewModel.exportImage()
                            showExportSheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save to Gallery in Full Quality ✨", fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val intent = editorViewModel.getShareIntent()
                            showExportSheet = false
                            if (intent != null) {
                                context.startActivity(android.content.Intent.createChooser(intent, "Share High-Res Photo"))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = CircleShape
                ) {
                    Text("Share to Instagram / Socials", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Add Text Dialog
    if (showTextDialog) {
        AlertDialog(
            onDismissRequest = { showTextDialog = false },
            title = { Text("Add Text Caption") },
            text = {
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("e.g. VENUSLY, SUMMER '24") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(focusedIndicatorColor = MaterialTheme.colorScheme.primary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            editorViewModel.addText(textInput)
                        }
                        showTextDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EditorToolIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag("tool_${label.lowercase()}")
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CompositionGridOverlay(
    mode: GridOverlayMode,
    modifier: Modifier = Modifier
) {
    if (mode == GridOverlayMode.OFF) return

    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val gridColor = Color.White.copy(alpha = 0.65f)
        val shadowColor = Color.Black.copy(alpha = 0.45f)
        val strokeWidth = 1.5.dp.toPx()
        val shadowWidth = 3.dp.toPx()

        when (mode) {
            GridOverlayMode.RULE_OF_THIRDS -> {
                val x1 = width / 3f
                val x2 = (width * 2f) / 3f
                val y1 = height / 3f
                val y2 = (height * 2f) / 3f

                drawLine(shadowColor, start = androidx.compose.ui.geometry.Offset(x1, 0f), end = androidx.compose.ui.geometry.Offset(x1, height), strokeWidth = shadowWidth)
                drawLine(shadowColor, start = androidx.compose.ui.geometry.Offset(x2, 0f), end = androidx.compose.ui.geometry.Offset(x2, height), strokeWidth = shadowWidth)
                drawLine(shadowColor, start = androidx.compose.ui.geometry.Offset(0f, y1), end = androidx.compose.ui.geometry.Offset(width, y1), strokeWidth = shadowWidth)
                drawLine(shadowColor, start = androidx.compose.ui.geometry.Offset(0f, y2), end = androidx.compose.ui.geometry.Offset(width, y2), strokeWidth = shadowWidth)

                drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(x1, 0f), end = androidx.compose.ui.geometry.Offset(x1, height), strokeWidth = strokeWidth)
                drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(x2, 0f), end = androidx.compose.ui.geometry.Offset(x2, height), strokeWidth = strokeWidth)
                drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(0f, y1), end = androidx.compose.ui.geometry.Offset(width, y1), strokeWidth = strokeWidth)
                drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(0f, y2), end = androidx.compose.ui.geometry.Offset(width, y2), strokeWidth = strokeWidth)

                val dotRadius = 4.5.dp.toPx()
                val goldColor = Color(0xFFFBBF24)
                drawCircle(goldColor, radius = dotRadius, center = androidx.compose.ui.geometry.Offset(x1, y1))
                drawCircle(goldColor, radius = dotRadius, center = androidx.compose.ui.geometry.Offset(x2, y1))
                drawCircle(goldColor, radius = dotRadius, center = androidx.compose.ui.geometry.Offset(x1, y2))
                drawCircle(goldColor, radius = dotRadius, center = androidx.compose.ui.geometry.Offset(x2, y2))
            }

            GridOverlayMode.GOLDEN_RATIO -> {
                val phi = 0.381966f
                val x1 = width * phi
                val x2 = width * (1f - phi)
                val y1 = height * phi
                val y2 = height * (1f - phi)

                drawLine(shadowColor, start = androidx.compose.ui.geometry.Offset(x1, 0f), end = androidx.compose.ui.geometry.Offset(x1, height), strokeWidth = shadowWidth)
                drawLine(shadowColor, start = androidx.compose.ui.geometry.Offset(x2, 0f), end = androidx.compose.ui.geometry.Offset(x2, height), strokeWidth = shadowWidth)
                drawLine(shadowColor, start = androidx.compose.ui.geometry.Offset(0f, y1), end = androidx.compose.ui.geometry.Offset(width, y1), strokeWidth = shadowWidth)
                drawLine(shadowColor, start = androidx.compose.ui.geometry.Offset(0f, y2), end = androidx.compose.ui.geometry.Offset(width, y2), strokeWidth = shadowWidth)

                val phiColor = Color(0xFFF472B6).copy(alpha = 0.85f)
                drawLine(phiColor, start = androidx.compose.ui.geometry.Offset(x1, 0f), end = androidx.compose.ui.geometry.Offset(x1, height), strokeWidth = strokeWidth)
                drawLine(phiColor, start = androidx.compose.ui.geometry.Offset(x2, 0f), end = androidx.compose.ui.geometry.Offset(x2, height), strokeWidth = strokeWidth)
                drawLine(phiColor, start = androidx.compose.ui.geometry.Offset(0f, y1), end = androidx.compose.ui.geometry.Offset(width, y1), strokeWidth = strokeWidth)
                drawLine(phiColor, start = androidx.compose.ui.geometry.Offset(0f, y2), end = androidx.compose.ui.geometry.Offset(width, y2), strokeWidth = strokeWidth)

                val dotRadius = 4.5.dp.toPx()
                drawCircle(Color(0xFFF472B6), radius = dotRadius, center = androidx.compose.ui.geometry.Offset(x1, y1))
                drawCircle(Color(0xFFF472B6), radius = dotRadius, center = androidx.compose.ui.geometry.Offset(x2, y1))
                drawCircle(Color(0xFFF472B6), radius = dotRadius, center = androidx.compose.ui.geometry.Offset(x1, y2))
                drawCircle(Color(0xFFF472B6), radius = dotRadius, center = androidx.compose.ui.geometry.Offset(x2, y2))
            }

            GridOverlayMode.SQUARE_GRID -> {
                val cyanColor = Color(0xFF38BDF8).copy(alpha = 0.75f)
                for (i in 1..3) {
                    val x = (width * i) / 4f
                    val y = (height * i) / 4f

                    drawLine(shadowColor, start = androidx.compose.ui.geometry.Offset(x, 0f), end = androidx.compose.ui.geometry.Offset(x, height), strokeWidth = shadowWidth)
                    drawLine(shadowColor, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(width, y), strokeWidth = shadowWidth)

                    drawLine(cyanColor, start = androidx.compose.ui.geometry.Offset(x, 0f), end = androidx.compose.ui.geometry.Offset(x, height), strokeWidth = strokeWidth)
                    drawLine(cyanColor, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(width, y), strokeWidth = strokeWidth)
                }

                val cx = width / 2f
                val cy = height / 2f
                val chSize = 14.dp.toPx()
                drawLine(Color(0xFF38BDF8), start = androidx.compose.ui.geometry.Offset(cx - chSize, cy), end = androidx.compose.ui.geometry.Offset(cx + chSize, cy), strokeWidth = strokeWidth * 1.8f)
                drawLine(Color(0xFF38BDF8), start = androidx.compose.ui.geometry.Offset(cx, cy - chSize), end = androidx.compose.ui.geometry.Offset(cx, cy + chSize), strokeWidth = strokeWidth * 1.8f)
            }

            else -> {}
        }
    }
}
