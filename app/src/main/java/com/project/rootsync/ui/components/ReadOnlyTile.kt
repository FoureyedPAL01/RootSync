package com.project.rootsync.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ReadOnlyTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.fillMaxWidth(),
        headlineContent = { Text(label) },
        trailingContent = {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
