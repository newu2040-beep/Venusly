package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.VenuslyBlue
import com.example.ui.theme.VenuslyBlueContainer

object PermissionUtils {

    fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf(Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
            permissions.add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        return permissions.toTypedArray()
    }

    fun isCameraGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    fun isStorageGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isNotificationGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isAllGranted(context: Context): Boolean {
        return isCameraGranted(context) && isStorageGranted(context) && isNotificationGranted(context)
    }
}

@Composable
fun RealtimePermissionsBanner(
    modifier: Modifier = Modifier,
    onPermissionsUpdated: () -> Unit = {}
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    var cameraGranted by remember { mutableStateOf(PermissionUtils.isCameraGranted(context)) }
    var storageGranted by remember { mutableStateOf(PermissionUtils.isStorageGranted(context)) }
    var notifGranted by remember { mutableStateOf(PermissionUtils.isNotificationGranted(context)) }

    fun refreshState() {
        cameraGranted = PermissionUtils.isCameraGranted(context)
        storageGranted = PermissionUtils.isStorageGranted(context)
        notifGranted = PermissionUtils.isNotificationGranted(context)
        onPermissionsUpdated()
    }

    val requestMultipleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshState()
    }

    val allGranted = cameraGranted && storageGranted && notifGranted

    if (allGranted) {
        return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("realtime_permissions_card")
            .clip(RoundedCornerShape(22.dp))
            .border(
                1.dp,
                if (allGranted) Color(0x3310B981) else if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (allGranted) {
                if (isDark) Color(0xFF0F2620) else Color(0xFFECFDF5)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (allGranted) Color(0xFF10B981) else VenuslyBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (allGranted) Icons.Filled.VerifiedUser else Icons.Filled.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (allGranted) "Real-Time Access Active" else "Real-Time Full Access",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (allGranted) "Camera, Gallery & Notifications enabled" else "Grant all permissions for full photo studio features",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Permission Status Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PermissionStatusPill(
                    icon = Icons.Filled.CameraAlt,
                    label = "Camera",
                    isGranted = cameraGranted,
                    modifier = Modifier.weight(1f)
                )
                PermissionStatusPill(
                    icon = Icons.Filled.PhotoLibrary,
                    label = "Gallery",
                    isGranted = storageGranted,
                    modifier = Modifier.weight(1f)
                )
                PermissionStatusPill(
                    icon = Icons.Filled.Notifications,
                    label = "Notifications",
                    isGranted = notifGranted,
                    modifier = Modifier.weight(1f)
                )
            }

            if (!allGranted) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        requestMultipleLauncher.launch(PermissionUtils.getRequiredPermissions())
                    },
                    modifier = Modifier
                        .testTag("allow_all_permissions_button")
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = VenuslyBlue)
                ) {
                    Text(
                        text = "Allow All Permissions ✨",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionsNotificationDialog(
    onDismiss: () -> Unit,
    onPermissionsGranted: () -> Unit = {}
) {
    val context = LocalContext.current
    var cameraGranted by remember { mutableStateOf(PermissionUtils.isCameraGranted(context)) }
    var storageGranted by remember { mutableStateOf(PermissionUtils.isStorageGranted(context)) }
    var notifGranted by remember { mutableStateOf(PermissionUtils.isNotificationGranted(context)) }

    fun refresh() {
        cameraGranted = PermissionUtils.isCameraGranted(context)
        storageGranted = PermissionUtils.isStorageGranted(context)
        notifGranted = PermissionUtils.isNotificationGranted(context)
        if (cameraGranted && storageGranted && notifGranted) {
            onPermissionsGranted()
            onDismiss()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refresh()
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("permissions_notification_dialog"),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Permissions Required",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "To enable all studio & filter features",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Please allow permissions for Venusly to capture, edit, save photos, and notify you when exports complete:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PermissionItemRow(
                    icon = Icons.Filled.CameraAlt,
                    title = "Camera Access",
                    description = "Take photos directly inside the real-time pastel camera viewfinder.",
                    isGranted = cameraGranted
                )

                PermissionItemRow(
                    icon = Icons.Filled.PhotoLibrary,
                    title = "Photos & Storage",
                    description = "Import photos from your gallery and export high-res edited creations.",
                    isGranted = storageGranted
                )

                PermissionItemRow(
                    icon = Icons.Filled.Notifications,
                    title = "Notifications",
                    description = "Receive instant alerts when batch edits and preset exports finish.",
                    isGranted = notifGranted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    launcher.launch(PermissionUtils.getRequiredPermissions())
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Allow Permissions", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}

@Composable
private fun PermissionItemRow(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean
) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isGranted) Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Filled.CheckCircle else icon,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isGranted) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Granted",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    )
                }
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PermissionStatusPill(
    icon: ImageVector,
    label: String,
    isGranted: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isGranted) {
        if (isDark) Color(0xFF14532D).copy(alpha = 0.6f) else Color(0xFFDCFCE7)
    } else {
        if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    }
    val contentColor = if (isGranted) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Filled.CheckCircle else icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isGranted) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = contentColor
        )
    }
}
