package com.project.rootsync.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Read-only settings tile with optional icon.
 */
@Composable
fun ReadOnlyTile(
    label: String,
    value: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier,
        leadingContent = icon?.let {
            { Icon(it, contentDescription = null, modifier = Modifier.size(20.dp)) }
        },
        headlineContent = { Text(label) },
        trailingContent = {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}
