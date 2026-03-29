package com.project.rootsync.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.project.rootsync.viewmodel.ThemePref

/**
 * Theme radio group component for selecting light/dark/system theme.
 */
@Composable
fun ThemeRadioGroup(
    selected: ThemePref,
    onSelected: (ThemePref) -> Unit
) {
    val options = listOf(
        Triple(ThemePref.LIGHT, Icons.Outlined.LightMode, "Light" to "Always use light theme"),
        Triple(ThemePref.DARK, Icons.Outlined.DarkMode, "Dark" to "Always use dark theme"),
        Triple(ThemePref.SYSTEM, Icons.Outlined.PhoneAndroid, "System Default" to "Follow device theme setting"),
    )

    options.forEach { (pref, icon, labels) ->
        val (title, subtitle) = labels
        ThemeRadioOption(
            title = title,
            subtitle = subtitle,
            icon = icon,
            selected = selected == pref,
            onClick = { onSelected(pref) }
        )
    }
}

@Composable
private fun ThemeRadioOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        leadingContent = {
            Icon(imageVector = icon, contentDescription = null)
        },
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            RadioButton(
                selected = selected,
                onClick = onClick
            )
        }
    )
}
