package com.project.rootsync.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Delete account button with confirmation dialog and loading state.
 */
@Composable
fun DeleteAccountButton(
    isLoading: Boolean = false,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.padding(horizontal = 16.dp)) {
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onError
                )
                Spacer(Modifier.width(8.dp))
                Text("Deleting...")
            } else {
                Text("Delete Account")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Account") },
            text = {
                Text("This action is permanent and cannot be undone. All your data will be lost.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm()
                        showDialog = false
                    }
                ) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
