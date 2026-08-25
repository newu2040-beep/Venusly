package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FilterCategory
import com.example.model.FilterPreset
import com.example.ui.theme.LocalCompactMode
import com.example.ui.theme.VenuslyBlue
import com.example.ui.theme.VenuslyBlueLight
import kotlin.math.roundToInt

@Composable
fun GlassmorphicSurface(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    Surface(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
            .border(width = 1.dp, color = borderColor, shape = shape),
        shape = shape,
        color = bgColor
    ) {
        content()
    }
}

@Composable
fun ThemeTogglePill(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillBg = MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = modifier
            .testTag("theme_toggle")
            .height(36.dp)
            .width(68.dp)
            .clip(CircleShape)
            .background(pillBg)
            .clickable(onClick = onToggle)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (!isDarkMode) MaterialTheme.colorScheme.primary else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LightMode,
                    contentDescription = "Light Mode",
                    tint = if (!isDarkMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isDarkMode) MaterialTheme.colorScheme.primary else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DarkMode,
                    contentDescription = "Dark Mode",
                    tint = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompactMode = LocalCompactMode.current
    val bg = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier
            .testTag("quick_action_${title.lowercase()}")
            .clip(RoundedCornerShape(if (isCompactMode) 14.dp else 20.dp))
            .background(bg)
            .border(1.2.dp, borderCol, RoundedCornerShape(if (isCompactMode) 14.dp else 20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = if (isCompactMode) 10.dp else 16.dp, horizontal = if (isCompactMode) 8.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (isCompactMode) 36.dp else 44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (isCompactMode) 18.dp else 22.dp)
            )
        }
        Spacer(modifier = Modifier.height(if (isCompactMode) 4.dp else 8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = if (isCompactMode) 11.sp else 12.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AestheticFilterCard(
    preset: FilterPreset,
    isSelected: Boolean,
    isFavorite: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = modifier
            .testTag("filter_card_${preset.id}")
            .width(130.dp)
            .height(185.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(if (isSelected) 2.5.dp else 1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (preset.previewDrawableRes != null) {
                Image(
                    painter = painterResource(id = preset.previewDrawableRes),
                    contentDescription = preset.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFE0F2FE), Color(0xFFFCE7F3))
                            )
                        )
                )
            }

            // Bottom gradient overlay for legible text
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(65.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC000000))
                        )
                    )
            )

            // Title & Favorite button
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1
                )
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFFF4D6D) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Top Category badge
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xAA000000))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = preset.category.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PillSliderControl(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    formatValue: (Float) -> String = { "%.0f".format(it) },
    modifier: Modifier = Modifier
) {
    val isCompactMode = LocalCompactMode.current
    val bg = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier
            .testTag("slider_${label.lowercase()}")
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (isCompactMode) 12.dp else 18.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(if (isCompactMode) 12.dp else 18.dp))
            .padding(
                horizontal = if (isCompactMode) 12.dp else 16.dp,
                vertical = if (isCompactMode) 8.dp else 12.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = if (isCompactMode) 12.sp else 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatValue(value),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isCompactMode) 12.sp else 14.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(if (isCompactMode) 2.dp else 4.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ToolParameterCard(
    title: String,
    valueText: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val bg = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BeforeAfterSplitComparison(
    beforeBitmap: android.graphics.Bitmap?,
    afterBitmap: android.graphics.Bitmap?,
    modifier: Modifier = Modifier
) {
    var splitFraction by remember { mutableFloatStateOf(0.5f) }

    BoxWithConstraints(
        modifier = modifier
            .testTag("before_after_split")
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black)
    ) {
        val containerMaxWidth = maxWidth
        val totalWidth = constraints.maxWidth.toFloat()
        val splitPx = totalWidth * splitFraction

        // Layer 1: After Image (Full background)
        if (afterBitmap != null) {
            Image(
                bitmap = afterBitmap.asImageBitmap(),
                contentDescription = "After Filter",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Layer 2: Before Image (Clipped to split fraction)
        if (beforeBitmap != null) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(containerMaxWidth * splitFraction)
                    .clip(RoundedCornerShape(0.dp))
            ) {
                Image(
                    bitmap = beforeBitmap.asImageBitmap(),
                    contentDescription = "Before Filter",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Draggable Split Divider Line & Knob
        Box(
            modifier = Modifier
                .offset { IntOffset(splitPx.roundToInt() - 2, 0) }
                .fillMaxHeight()
                .width(4.dp)
                .background(Color.White)
        )

        // Center Split Knob Pill (< >)
        Box(
            modifier = Modifier
                .offset { IntOffset(splitPx.roundToInt() - 20, (constraints.maxHeight / 2) - 20) }
                .size(40.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newFraction = (splitFraction + (dragAmount.x / totalWidth)).coerceIn(0.05f, 0.95f)
                        splitFraction = newFraction
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "‹ ›",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = VenuslyBlue
            )
        }

        // Before & After Label Pills
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xAA000000))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(text = "Before", style = MaterialTheme.typography.labelSmall, color = Color.White)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xAA000000))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(text = "After", style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}
