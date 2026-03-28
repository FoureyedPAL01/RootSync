package com.project.rootsync.ui.screens.crop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.project.rootsync.viewmodel.CropProfileViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropProfilesScreen(
    navController: NavController,
    viewModel: CropProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // TODO: Get deviceId from DataStore
    val deviceId = "default-device"

    LaunchedEffect(Unit) {
        viewModel.loadProfiles(deviceId)
    }

    if (showAddDialog) {
        AddCropProfileDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, moistureThreshold, rainThreshold ->
                viewModel.addProfile(
                    com.project.rootsync.data.model.CropProfile(
                        id = UUID.randomUUID().toString(),
                        deviceId = deviceId,
                        name = name,
                        moistureThreshold = moistureThreshold,
                        rainProbabilityThreshold = rainThreshold
                    )
                )
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Crop Profiles") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Profile")
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else if (uiState.profiles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No crop profiles. Tap + to add one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.profiles) { profile ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(profile.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Moisture threshold: ${profile.moistureThreshold}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "Rain threshold: ${profile.rainProbabilityThreshold}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = { viewModel.deleteProfile(profile.id, deviceId) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCropProfileDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, moistureThreshold: Int, rainThreshold: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var moistureThreshold by remember { mutableStateOf(30f) }
    var rainThreshold by remember { mutableStateOf(50f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Crop Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Moisture threshold: ${moistureThreshold.toInt()}%")
                Slider(
                    value = moistureThreshold,
                    onValueChange = { moistureThreshold = it },
                    valueRange = 0f..100f
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Rain probability threshold: ${rainThreshold.toInt()}%")
                Slider(
                    value = rainThreshold,
                    onValueChange = { rainThreshold = it },
                    valueRange = 0f..100f
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, moistureThreshold.toInt(), rainThreshold.toInt()) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
