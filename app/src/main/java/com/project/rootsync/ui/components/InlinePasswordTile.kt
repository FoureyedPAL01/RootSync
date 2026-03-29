package com.project.rootsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Inline password tile with expand/collapse behavior.
 * Shows current + new password fields when expanded.
 */
@Composable
fun InlinePasswordTile(
    title: String,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onUpdate: (current: String, newPassword: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    if (!expanded) {
        ListItem(
            leadingContent = icon?.let {
                { Icon(it, contentDescription = null, modifier = Modifier.size(20.dp)) }
            },
            headlineContent = { Text(title) },
            trailingContent = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = "Expand")
                }
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Current password field
            OutlinedTextField(
                value = currentPass,
                onValueChange = { currentPass = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Current Password") },
                singleLine = true,
                visualTransformation = if (showCurrent) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showCurrent = !showCurrent }) {
                        Icon(
                            imageVector = if (showCurrent) Icons.Rounded.Visibility
                            else Icons.Rounded.VisibilityOff,
                            contentDescription = null
                        )
                    }
                }
            )

            // New password field
            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New Password") },
                singleLine = true,
                isError = localError != null,
                supportingText = localError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                    ?: errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                visualTransformation = if (showNew) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showNew = !showNew }) {
                        Icon(
                            imageVector = if (showNew) Icons.Rounded.Visibility
                            else Icons.Rounded.VisibilityOff,
                            contentDescription = null
                        )
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { expanded = false; currentPass = ""; newPass = "" },
                    enabled = !isLoading
                ) { Text("Cancel") }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (newPass.length < 6) {
                            localError = "Password must be at least 6 characters"
                            return@Button
                        }
                        localError = null
                        onUpdate(currentPass, newPass)
                        if (errorMessage == null) {
                            expanded = false
                            currentPass = ""
                            newPass = ""
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Text("Save")
                    }
                }
            }
        }
    }
}
