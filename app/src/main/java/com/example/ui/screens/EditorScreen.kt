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
import androidx.compose.foundation.gestures.detectTapGestures
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
                        tint = VenuslyBlue
                    )
                }
            }
        }

        // 2. Main Image Canvas with "Live" badge & Press to compare
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
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (displayBitmap != null) {
                Image(
                    bitmap = displayBitmap.asImageBitmap(),
                    contentDescription = "Editing Canvas",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
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
                        text = "Pick an image from your gallery to begin editing",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f)),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
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
                    text = if (isComparingBefore) "Showing Original" else "Hold to Compare",
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
                                                            AestheticFrame.FILM_35MM -> Color(0xFF0F0F12)
                                                            AestheticFrame.PASTEL_AURA -> Color(0xFFC084FC)
                                                            AestheticFrame.CLEAN_MAT -> Color(0xFFF1F5F9)
                                                            AestheticFrame.MINIMAL_KEYLINE -> Color(0xFF334155)
                                                            AestheticFrame.VINTAGE_STAMP -> Color(0xFFFDE68A)
                                                            AestheticFrame.PASTEL_CARD -> Color(0xFFBFDBFE)
                                                            AestheticFrame.RETRO_TV -> Color(0xFF1E293B)
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
                                text = "Aesthetic Overlays & Stamps",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

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
                                text = "Stickers & Sparkles",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                val stickers = listOf("✨", "♡", "🌸", "🦋", "📸", "☁️", "🌙", "🎞️")
                                items(stickers) { s ->
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { editorViewModel.addSticker(s) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = s, fontSize = 20.sp)
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
                    .shadow(10.dp, CircleShape, spotColor = VenuslyBlue.copy(alpha = 0.45f)),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = VenuslyBlue)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Export",
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

    // Export Bottom Sheet
    if (showExportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showExportSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Export Options",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Format Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Format",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !exportFormatPng,
                            onClick = { exportFormatPng = false },
                            label = { Text("JPEG (High)") },
                            shape = CircleShape
                        )
                        FilterChip(
                            selected = exportFormatPng,
                            onClick = { exportFormatPng = true },
                            label = { Text("PNG (Lossless)") },
                            shape = CircleShape
                        )
                    }
                }

                // Quality Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Quality",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "${exportQuality.toInt()}%",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = VenuslyBlue)
                        )
                    }
                    Slider(
                        value = exportQuality,
                        onValueChange = { exportQuality = it },
                        valueRange = 70f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = VenuslyBlue,
                            activeTrackColor = VenuslyBlue
                        )
                    )
                }

                // Action Buttons
                Button(
                    onClick = {
                        scope.launch {
                            val format = if (exportFormatPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                            editorViewModel.exportImage(format, exportQuality.toInt())
                            showExportSheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = VenuslyBlue)
                ) {
                    Text("Save to Gallery", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val intent = editorViewModel.getShareIntent()
                            showExportSheet = false
                            if (intent != null) {
                                context.startActivity(android.content.Intent.createChooser(intent, "Share Photo"))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = CircleShape
                ) {
                    Text("Share to Instagram / Socials", fontWeight = FontWeight.SemiBold)
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
                    colors = TextFieldDefaults.colors(focusedIndicatorColor = VenuslyBlue)
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
                    colors = ButtonDefaults.buttonColors(containerColor = VenuslyBlue)
                ) {
                    Text("Add")
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
