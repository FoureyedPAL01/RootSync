package com.project.rootsync.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Settings section component with optional leading icon.
 */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = modifier.padding(bottom = 16.dp)) {
        // Section header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
            leadingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.primary
            )
        }

        // Card container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(content = content)
        }
    }
}
