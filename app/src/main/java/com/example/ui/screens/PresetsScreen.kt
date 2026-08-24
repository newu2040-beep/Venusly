package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DefaultPresets
import com.example.engine.ImageProcessor
import com.example.model.FilterPreset
import com.example.ui.components.BeforeAfterSplitComparison
import com.example.ui.components.ToolParameterCard
import com.example.ui.theme.VenuslyBlue
import com.example.ui.theme.VenuslyBlueContainer
import com.example.viewmodel.EditorViewModel
import com.example.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetsScreen(
    initialPreset: FilterPreset?,
    homeViewModel: HomeViewModel,
    editorViewModel: EditorViewModel,
    onApply: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val favorites by homeViewModel.favoritePresetIds.collectAsState()
    val originalBmp by editorViewModel.originalBitmap.collectAsState()

    var currentPreset by remember {
        mutableStateOf(initialPreset ?: DefaultPresets.presets.first())
    }
    var strength by remember { mutableFloatStateOf(75f) }
    var presetFilteredBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val isFavorite = favorites.contains(currentPreset.id)

    // Render preset preview on current original bitmap
    LaunchedEffect(currentPreset, strength, originalBmp) {
        val orig = originalBmp ?: return@LaunchedEffect
        val processed = ImageProcessor.applyAdjustments(
            source = orig,
            adjustments = currentPreset.adjustments,
            strength = strength / 100f
        )
        presetFilteredBitmap = processed
    }

    LazyColumn(
        modifier = modifier
            .testTag("presets_screen")
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .testTag("preset_back_button")
                        .size(40.dp)
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
                    text = currentPreset.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { homeViewModel.toggleFavorite(currentPreset.id) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFFF4D6D) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Before / After Split Comparison View (Exact Mockup 3)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .aspectRatio(1.05f)
            ) {
                BeforeAfterSplitComparison(
                    beforeBitmap = originalBmp,
                    afterBitmap = presetFilteredBitmap ?: originalBmp,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Preset Details Header & Category Tag
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentPreset.name,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(VenuslyBlueContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currentPreset.category.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = VenuslyBlue
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentPreset.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Strength Slider
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Strength",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${strength.toInt()}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = VenuslyBlue
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = strength,
                    onValueChange = { strength = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = VenuslyBlue,
                        activeTrackColor = VenuslyBlue,
                        inactiveTrackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                    )
                )
            }
        }

        // 4 Parameter Cards (Exposure, Contrast, Saturation, Highlights)
        item {
            val adj = currentPreset.adjustments
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToolParameterCard(
                    title = "Exposure",
                    valueText = if (adj.exposure >= 0) "+%.2f".format(adj.exposure) else "%.2f".format(adj.exposure),
                    icon = Icons.Filled.Exposure,
                    modifier = Modifier.weight(1f)
                )
                ToolParameterCard(
                    title = "Contrast",
                    valueText = "%+.0f".format(adj.contrast),
                    icon = Icons.Filled.Contrast,
                    modifier = Modifier.weight(1f)
                )
                ToolParameterCard(
                    title = "Saturation",
                    valueText = "%+.0f".format(adj.saturation),
                    icon = Icons.Filled.InvertColors,
                    modifier = Modifier.weight(1f)
                )
                ToolParameterCard(
                    title = "Highlights",
                    valueText = "%+.0f".format(adj.highlights),
                    icon = Icons.Filled.Flare,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Preset Quick Carousel Selector
        item {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Text(
                    text = "Explore Presets",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DefaultPresets.presets) { preset ->
                        val isSelected = preset.id == currentPreset.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { currentPreset = preset },
                            label = { Text(preset.name) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VenuslyBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Bottom Action Buttons (Save Preset & Apply)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        homeViewModel.saveCustomPreset(
                            name = "${currentPreset.name} Custom",
                            category = currentPreset.category.displayName,
                            description = "Saved with strength ${strength.toInt()}%",
                            adjustments = currentPreset.adjustments
                        )
                    },
                    modifier = Modifier
                        .testTag("save_preset_button")
                        .weight(1f)
                        .height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "Save Preset",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = {
                        editorViewModel.selectPreset(currentPreset)
                        editorViewModel.setPresetStrength(strength / 100f)
                        onApply()
                    },
                    modifier = Modifier
                        .testTag("apply_preset_button")
                        .weight(1f)
                        .height(52.dp)
                        .shadow(8.dp, CircleShape, spotColor = VenuslyBlue.copy(alpha = 0.4f)),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = VenuslyBlue)
                ) {
                    Text(
                        text = "Apply",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}
