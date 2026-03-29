package com.project.rootsync.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.project.rootsync.util.SaveStatus

/**
 * Toggle setting tile component.
 */
@Composable
fun ToggleSettingTile(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    checked: Boolean,
    saveStatus: SaveStatus = SaveStatus.IDLE,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier,
        leadingContent = icon?.let {
            { Icon(imageVector = it, contentDescription = null) }
        },
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = saveStatus != SaveStatus.SAVING
            )
        }
    )
}
