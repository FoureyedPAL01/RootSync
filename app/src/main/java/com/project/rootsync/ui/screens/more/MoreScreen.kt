package com.project.rootsync.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.project.rootsync.ui.navigation.Screen
import com.project.rootsync.ui.theme.ManropeFont

/**
 * Data class representing a menu item in the More screen.
 */
data class MoreItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

/**
 * More screen composable.
 * Displays a header with profile info and a list of navigation items.
 */
@Composable
fun MoreScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val backgroundColor = colors.surface

    val items = listOf(
        MoreItem(Icons.Rounded.Person, "Profile", Screen.Profile.route),
        MoreItem(Icons.Rounded.WaterDrop, "Irrigation History", Screen.Irrigation.route),
        MoreItem(Icons.Rounded.Notifications, "Alerts", Screen.Alerts.route),
        MoreItem(Icons.Rounded.BarChart, "Water Usage", Screen.WaterUsage.route),
        MoreItem(Icons.Rounded.Science, "Fertigation", Screen.Fertigation.route),
        MoreItem(Icons.Rounded.Devices, "Device Management", Screen.DeviceManagement.route),
        MoreItem(Icons.Rounded.Tune, "Preferences", Screen.Preferences.route),
        MoreItem(Icons.Rounded.Settings, "Settings", Screen.Settings.route)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Header with profile image and name
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerHighest)
                    .padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile image placeholder
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(48.dp),
                        tint = colors.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "RootSync",
                    fontFamily = ManropeFont,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }
        }

        // Divider after header
        item {
            HorizontalDivider(
                thickness = 1.dp,
                color = colors.outline.copy(alpha = 0.3f)
            )
        }

        // Menu items with dividers
        itemsIndexed(items) { index, item ->
            MoreMenuItem(
                item = item,
                onClick = { navController.navigate(item.route) },
                showDivider = index < items.lastIndex
            )
        }
    }
}

/**
 * A single menu item in the More screen.
 */
@Composable
private fun MoreMenuItem(
    item: MoreItem,
    onClick: () -> Unit,
    showDivider: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container with rounded background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = colors.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.w600,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.onSurface.copy(alpha = 0.35f)
            )
        }

        // Divider (only if not last item)
        if (showDivider) {
            HorizontalDivider(
                thickness = 1.dp,
                indent = 72.dp,  // 40.dp icon + 16.dp padding + 16.dp spacing
                color = colors.outline.copy(alpha = 0.3f)
            )
        }
    }
}
