package com.example.ui.screens

import android.net.Uri
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Upload
import com.example.ui.components.BatchProcessingSheet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.DefaultPresets
import com.example.data.ProjectEntity
import com.example.model.FilterCategory
import com.example.model.FilterPreset
import com.example.ui.components.AestheticFilterCard
import com.example.ui.components.PermissionsNotificationDialog
import com.example.ui.components.QuickActionCard
import com.example.ui.components.RealtimePermissionsBanner
import com.example.ui.components.ThemeTogglePill
import com.example.ui.components.PermissionUtils
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.LocalCompactMode
import com.example.ui.theme.VenuslyBlue
import com.example.ui.theme.VenuslyBlueContainer
import com.example.viewmodel.EditorViewModel
import com.example.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    editorViewModel: EditorViewModel,
    onNavigateToEdit: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToPresets: (FilterPreset) -> Unit,
    onSeeAllRecent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCategory by homeViewModel.selectedCategory.collectAsState()
    val isDarkMode by homeViewModel.isDarkMode.collectAsState()
    val favorites by homeViewModel.favoritePresetIds.collectAsState()
    val recentProjects by homeViewModel.recentProjects.collectAsState()
    val isCompactMode = LocalCompactMode.current
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    var showPermissionsDialog by remember { mutableStateOf(false) }
    var permissionsGranted by remember { mutableStateOf(PermissionUtils.isAllGranted(context)) }
    var showBatchSheet by remember { mutableStateOf(false) }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val nativeCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            editorViewModel.loadImageFromUri(tempCameraUri!!)
            onNavigateToEdit()
        }
    }

    fun launchNativeCameraApp() {
        try {
            val photoFile = File(context.cacheDir, "native_camera_${System.currentTimeMillis()}.jpg")
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempCameraUri = uri
            nativeCameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            onNavigateToCamera()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            editorViewModel.loadImageFromUri(uri)
            onNavigateToEdit()
        }
    }

    val rawPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            editorViewModel.loadImageFromUri(uri)
            onNavigateToEdit()
        }
    }

    val filteredPresets = if (selectedCategory == FilterCategory.ALL) {
        DefaultPresets.presets
    } else {
        DefaultPresets.presets.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = modifier
            .testTag("home_screen")
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = if (isCompactMode) 70.dp else 90.dp)
    ) {
        // 1. Top Header: Welcome back Creator + Permissions Shield + Dark Mode Pill
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = if (isCompactMode) 12.dp else 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = if (isCompactMode) 13.sp else 15.sp
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Creator",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = if (isCompactMode) 22.sp else 28.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (isCompactMode) 16.dp else 20.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Permission Notification Popup trigger button
                    IconButton(
                        onClick = { showPermissionsDialog = true },
                        modifier = Modifier
                            .testTag("permissions_popup_button")
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (permissionsGranted) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = if (permissionsGranted) Icons.Filled.VerifiedUser else Icons.Filled.Shield,
                            contentDescription = "Permissions Status",
                            tint = if (permissionsGranted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    ThemeTogglePill(
                        isDarkMode = isDarkMode,
                        onToggle = { homeViewModel.toggleDarkMode() }
                    )
                }
            }
        }

        // Real-Time Permissions & Access Banner (Tap to open full permissions popup)
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .clickable { showPermissionsDialog = true }
            ) {
                RealtimePermissionsBanner(
                    onPermissionsUpdated = {
                        permissionsGranted = PermissionUtils.isAllGranted(context)
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. Quick Actions Row (Import, Camera, Presets, Edit)
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "Import",
                        icon = Icons.Filled.Upload,
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "RAW DNG",
                        icon = Icons.Filled.MotionPhotosOn,
                        onClick = {
                            rawPickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Camera",
                        icon = Icons.Filled.CameraAlt,
                        onClick = { launchNativeCameraApp() },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Batch",
                        icon = Icons.Filled.Collections,
                        onClick = { showBatchSheet = true },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Presets",
                        icon = Icons.Filled.AutoAwesome,
                        onClick = {
                            onNavigateToPresets(DefaultPresets.presets.first())
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Aesthetic Filters Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column {
                Text(
                    text = "Aesthetic Filters",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Filter Categories Pill Row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FilterCategory.values()) { category ->
                        val isSelected = category == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { homeViewModel.selectCategory(category) },
                            label = {
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VenuslyBlue,
                                selectedLabelColor = Color.White,
                                containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = null,
                            modifier = Modifier.height(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Aesthetic Filter Cards Carousel
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredPresets) { preset ->
                        AestheticFilterCard(
                            preset = preset,
                            isSelected = false,
                            isFavorite = favorites.contains(preset.id),
                            onSelect = {
                                onNavigateToPresets(preset)
                            },
                            onToggleFavorite = {
                                homeViewModel.toggleFavorite(preset.id)
                            }
                        )
                    }
                }
            }
        }

        // 4. Recent Edits Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Edits",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (recentProjects.isNotEmpty()) {
                        TextButton(onClick = onSeeAllRecent) {
                            Text(
                                text = "See All (${recentProjects.size})",
                                style = MaterialTheme.typography.labelLarge.copy(color = VenuslyBlue)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (recentProjects.isEmpty()) {
                    // Clean real empty state with direct actions - No demo placeholder photos
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .border(
                                1.dp,
                                if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                RoundedCornerShape(22.dp)
                            ),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(VenuslyBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = null,
                                    tint = VenuslyBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Recent Edits",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pick a photo from your gallery or take a fresh shot to start editing.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = VenuslyBlue)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Upload,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = onNavigateToCamera,
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CameraAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = VenuslyBlue
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Take Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VenuslyBlue)
                                }
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recentProjects.take(6)) { project ->
                            ProjectRecentCard(
                                project = project,
                                onClick = {
                                    editorViewModel.loadProject(project)
                                    onNavigateToEdit()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPermissionsDialog) {
        PermissionsNotificationDialog(
            onDismiss = { showPermissionsDialog = false },
            onPermissionsGranted = {
                permissionsGranted = true
            }
        )
    }

    if (showBatchSheet) {
        BatchProcessingSheet(
            editorViewModel = editorViewModel,
            onDismiss = { showBatchSheet = false }
        )
    }
}

@Composable
private fun ProjectRecentCard(
    project: ProjectEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(105.dp)
            .aspectRatio(0.82f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (project.imageUri.startsWith("res://")) {
                val resId = project.imageUri.removePrefix("res://").toIntOrNull() ?: R.drawable.sample_fuji_arch
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = project.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = project.imageUri,
                    contentDescription = project.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xCCFFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit",
                    tint = VenuslyBlue,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
