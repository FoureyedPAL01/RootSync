package com.project.rootsync.ui.screens.device

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.project.rootsync.ui.components.SettingsSection
import com.project.rootsync.util.DateHelpers
import com.project.rootsync.viewmodel.DeviceManagementUiState
import com.project.rootsync.viewmodel.DeviceUiState
import com.project.rootsync.viewmodel.DeviceViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

/**
 * Device management screen composable.
 * Displays device header, info section, connectivity, and danger zone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceManagementScreen(
    navController: NavController,
    viewModel: DeviceViewModel = hiltViewModel()
) {
    val deviceState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Local UI state for editing
    var editingName by remember { mutableStateOf(false) }
    var nameText by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var showUnlinkDialog by remember { mutableStateOf(false) }

    // Initialize name text when device state changes
    LaunchedEffect(deviceState.selectedDevice?.name) {
        nameText = deviceState.selectedDevice?.name ?: ""
    }

    // Load devices on screen entry
    LaunchedEffect(Unit) {
        // TODO: Get userId from auth state
        viewModel.loadDevices("current-user-id")
    }

    val device = deviceState.selectedDevice
    val isOnline = device?.isOnline ?: false
    val colors = MaterialTheme.colorScheme

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                DeviceHeader(
                    deviceName = device?.name ?: "Unknown Device",
                    isOnline = isOnline,
                    primaryColor = colors.primary
                )
            }

            // Device Info Section
            item {
                SettingsSection(title = "Device Info") {
                    // Device Name (editable)
                    if (!editingName) {
                        ListItem(
                            headlineContent = { Text("Name") },
                            supportingContent = { Text(device?.name ?: "Unknown") },
                            leadingContent = {
                                Icon(Icons.Rounded.Sell, contentDescription = null)
                            },
                            trailingContent = {
                                IconButton(onClick = {
                                    editingName = true
                                    nameText = device?.name ?: ""
                                }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                                }
                            }
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            OutlinedTextField(
                                value = nameText,
                                onValueChange = {
                                    nameText = it
                                    nameError = null
                                },
                                label = { Text("Device Name") },
                                isError = nameError != null,
                                supportingText = nameError?.let { { Text(it) } },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        nameText = device?.name ?: ""
                                        nameError = null
                                        editingName = false
                                    }
                                ) {
                                    Text("Cancel")
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        val trimmed = nameText.trim()
                                        if (trimmed.isEmpty()) {
                                            nameError = "Device name cannot be empty"
                                            return@Button
                                        }

                                        scope.launch {
                                            try {
                                                device?.let {
                                                    viewModel.updateDeviceName(it.id, trimmed)
                                                    editingName = false
                                                    snackbarHostState.showSnackbar(
                                                        message = "Device name updated.",
                                                        withDismissAction = true
                                                    )
                                                }
                                            } catch (e: Exception) {
                                                nameError = "Failed to update name."
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Rounded.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save")
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // Last Seen
                    ListItem(
                        headlineContent = { Text("Last Seen") },
                        supportingContent = {
                            Text(
                                device?.lastSeen?.let {
                                    DateHelpers.formatTimestamp(it)
                                } ?: "Unknown"
                            )
                        },
                        leadingContent = {
                            Icon(Icons.Rounded.Schedule, contentDescription = null)
                        },
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        device?.let { viewModel.selectDevice(it) }
                                    }
                                }
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                            }
                        }
                    )

                    HorizontalDivider()

                    // Device ID
                    ListItem(
                        headlineContent = { Text("Device ID") },
                        supportingContent = {
                            Text(
                                device?.id ?: "Not linked",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        },
                        leadingContent = {
                            Icon(Icons.Rounded.Fingerprint, contentDescription = null)
                        }
                    )
                }
            }

            // Connectivity Section
            item {
                SettingsSection(title = "Connectivity") {
                    ApiConnectivityTile(
                        serviceName = "Supabase",
                        isConnected = deviceState.errorMessage == null
                    )

                    HorizontalDivider()

                    ApiConnectivityTile(
                        serviceName = "Open-Meteo",
                        isConnected = true
                    )
                }
            }

            // Danger Zone
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Danger Zone",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        showUnlinkDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.error)
                ) {
                    Icon(Icons.Rounded.LinkOff, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlink Device")
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Unlink confirmation dialog
    if (showUnlinkDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showUnlinkDialog = false },
            title = { Text("Unlink Device") },
            text = { Text("Are you sure you want to unlink this device? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showUnlinkDialog = false
                        scope.launch {
                            device?.let {
                                try {
                                    viewModel.deleteDevice(it.id, "current-user-id")
                                    snackbarHostState.showSnackbar(
                                        message = "Device unlinked successfully.",
                                        withDismissAction = true
                                    )
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        message = "Failed to unlink: ${e.message}",
                                        withDismissAction = true
                                    )
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error
                    )
                ) {
                    Text("Unlink")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Device header composable showing device icon, name, and online status.
 */
@Composable
private fun DeviceHeader(
    deviceName: String,
    isOnline: Boolean,
    primaryColor: Color
) {
    val colors = MaterialTheme.colorScheme

    val backgroundColor = if (isOnline) {
        primaryColor.copy(alpha = 0.08f)
    } else {
        colors.onSurface.copy(alpha = 0.08f)
    }

    val iconColor = if (isOnline) primaryColor else colors.onSurface.copy(alpha = 0.25f)
    val statusColor = if (isOnline) primaryColor else colors.error

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Device icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(18.dp),
                    spotColor = iconColor.copy(alpha = 0.25f)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Memory,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Device name
        Text(
            text = deviceName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Online status badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = statusColor.copy(alpha = 0.12f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOnline) "Online" else "Offline",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }
        }
    }
}

/**
 * API connectivity status tile.
 */
@Composable
private fun ApiConnectivityTile(
    serviceName: String,
    isConnected: Boolean
) {
    ListItem(
        headlineContent = { Text(serviceName) },
        supportingContent = {
            Text(if (isConnected) "Connected" else "Disconnected")
        },
        trailingContent = {
            Icon(
                imageVector = if (isConnected) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                contentDescription = null,
                tint = if (isConnected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }
    )
}
