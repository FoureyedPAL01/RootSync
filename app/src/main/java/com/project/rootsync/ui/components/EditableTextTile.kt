package com.project.rootsync.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun EditableTextTile(
    title: String,
    value: String,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var editing by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(value) }

    ListItem(
        modifier = modifier.fillMaxWidth(),
        headlineContent = { Text(title) },
        supportingContent = {
            if (editing) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onSave(text)
                            editing = false
                        }
                    ),
                    singleLine = true
                )
            } else {
                Text(value)
            }
        },
        trailingContent = {
            IconButton(onClick = {
                if (editing) {
                    onSave(text)
                }
                editing = !editing
            }) {
                Icon(Icons.Rounded.Edit, contentDescription = "Edit")
            }
        }
    )
}
