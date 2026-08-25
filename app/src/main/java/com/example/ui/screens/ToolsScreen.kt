package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTab
import com.example.ui.theme.VenuslyBlue
import com.example.ui.theme.VenuslyBlueLight
import com.example.viewmodel.EditorViewModel

@Composable
fun ToolsScreen(
    editorViewModel: EditorViewModel,
    onNavigateToEditWithTab: (EditorTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    LazyColumn(
        modifier = modifier
            .testTag("tools_screen")
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Aesthetic Studio Tools",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Professional tactile editing modules crafted for analog film looks",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        item {
            ToolFeatureCard(
                title = "Dreamy Glow & Bloom",
                description = "Soft diffuse halation with dreamy pastel highlights and romantic mist",
                icon = Icons.Filled.Flare,
                accentColor = Color(0xFFF472B6),
                onClick = { onNavigateToEditWithTab(EditorTab.EFFECTS) }
            )
        }

        item {
            ToolFeatureCard(
                title = "Film Grain & Texture Lab",
                description = "Authentic organic 35mm silver-halide grain and sharpness calibration",
                icon = Icons.Filled.Grain,
                accentColor = Color(0xFFF59E0B),
                onClick = { onNavigateToEditWithTab(EditorTab.DETAILS) }
            )
        }

        item {
            ToolFeatureCard(
                title = "Chromatic Light Leaks",
                description = "Vintage film camera edge burns, warm amber flares, and prism leaks",
                icon = Icons.Filled.WbSunny,
                accentColor = Color(0xFFEF4444),
                onClick = { onNavigateToEditWithTab(EditorTab.EFFECTS) }
            )
        }

        item {
            ToolFeatureCard(
                title = "Retro 90s Date Stamper",
                description = "Iconic orange monospace date and time stamps with authentic CRT glow",
                icon = Icons.Filled.DateRange,
                accentColor = Color(0xFF10B981),
                onClick = { onNavigateToEditWithTab(EditorTab.OVERLAYS) }
            )
        }

        item {
            ToolFeatureCard(
                title = "HSL & Color Grading",
                description = "Precise temperature, magenta/green tint, and hue shift adjustments",
                icon = Icons.Filled.InvertColors,
                accentColor = VenuslyBlue,
                onClick = { onNavigateToEditWithTab(EditorTab.COLORS) }
            )
        }

        item {
            ToolFeatureCard(
                title = "Analog Dust & Scratches",
                description = "Organic vintage particles, dust specks, and nostalgic film artifacts",
                icon = Icons.Filled.Details,
                accentColor = Color(0xFF8B5CF6),
                onClick = { onNavigateToEditWithTab(EditorTab.EFFECTS) }
            )
        }
    }
}

@Composable
private fun ToolFeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = modifier
            .testTag("tool_card_${title.lowercase().take(8)}")
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, borderCol, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}
