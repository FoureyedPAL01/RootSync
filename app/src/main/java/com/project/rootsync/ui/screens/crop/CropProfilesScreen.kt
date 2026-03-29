package com.project.rootsync.ui.screens.crop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.project.rootsync.data.model.CropProfile
import com.project.rootsync.util.DateHelpers
import com.project.rootsync.viewmodel.CropProfileViewModel
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Crop profiles screen composable.
 * Displays a list of crop profiles with create/edit/delete functionality.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CropProfilesScreen(
    navController: NavController,
    viewModel: CropProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<CropProfile?>(null) }
    var showDeleteDialog by remember { mutableStateOf<CropProfile?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingProfile = null
                    showBottomSheet = true
                }
            ) {
                Icon(Icons.Rounded.Add, "Add Profile")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (uiState.profiles.isEmpty()) {
                EmptyState(
                    onAdd = {
                        editingProfile = null
                        showBottomSheet = true
                    },
                    modifier = Modifier.padding(padding)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.profiles) { profile ->
                        ProfileCard(
                            profile = profile,
                            isActive = profile.id == uiState.activeProfileId,
                            isFetching = profile.id in uiState.fetchingIds,
                            onSetActive = {
                                viewModel.setActiveProfile(profile.id)
                            },
                            onEdit = {
                                editingProfile = profile
                                showBottomSheet = true
                            },
                            onDelete = {
                                showDeleteDialog = profile
                            },
                            onFetchData = {
                                viewModel.fetchPerenualData(profile)
                            }
                        )
                    }

                    // Bottom padding for FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Bottom Sheet for create/edit
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                editingProfile = null
            },
            sheetState = rememberModalBottomSheetState()
        ) {
            ProfileSheet(
                existing = editingProfile,
                onSave = { name, plantName, threshold ->
                    viewModel.saveProfile(
                        profileId = editingProfile?.id,
                        name = name,
                        plantName = plantName,
                        threshold = threshold,
                        onComplete = {
                            showBottomSheet = false
                            editingProfile = null
                        }
                    )
                }
            )
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { profile ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Profile") },
            text = { Text("Are you sure you want to delete \"${profile.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProfile(profile)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Empty state composable when no profiles exist.
 */
@Composable
private fun EmptyState(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.Eco,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No profiles yet",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Tap the + button to create your first crop profile.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onAdd) {
            Icon(Icons.Rounded.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Profile")
        }
    }
}

/**
 * Profile card composable showing crop profile details.
 */
@Composable
private fun ProfileCard(
    profile: CropProfile,
    isActive: Boolean,
    isFetching: Boolean,
    onSetActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFetchData: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isActive) 2.dp else 0.5.dp,
            color = if (isActive) colors.primary else colors.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Eco,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = colors.primary,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, "Edit", modifier = Modifier.size(18.dp))
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Rounded.Delete,
                        "Delete",
                        modifier = Modifier.size(18.dp),
                        tint = colors.error
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Plant info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailChip(
                    icon = Icons.Rounded.Grass,
                    label = profile.plantName.ifEmpty { "No plant set" },
                    faded = profile.plantName.isEmpty(),
                    modifier = Modifier.weight(1f)
                )

                DetailChip(
                    icon = Icons.Rounded.WaterDrop,
                    label = "${profile.minMoisture}% threshold"
                )
            }

            // Perenual section
            if (profile.plantName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val cached = profile.perenualCachedAt

                    if (cached != null) {
                        DetailChip(
                            icon = Icons.Rounded.CheckCircle,
                            label = "Cached",
                            color = colors.primary,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        DetailChip(
                            icon = Icons.Rounded.Warning,
                            label = "No plant data yet",
                            color = Color(0xFFB45309),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (isFetching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        OutlinedButton(
                            onClick = onFetchData,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(
                                if (cached != null) Icons.Rounded.Refresh
                                else Icons.Rounded.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (cached != null) "Refresh" else "Fetch Data",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                // Plant data panel
                profile.perenualData?.let { data ->
                    if (data is JsonObject) {
                        Spacer(modifier = Modifier.height(10.dp))
                        PlantDataPanel(data = data)
                    }
                }
            }

            // Set active button
            if (!isActive) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onSetActive,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, colors.primary)
                ) {
                    Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set as Active")
                }
            }
        }
    }
}

/**
 * Detail chip composable for small info badges.
 */
@Composable
private fun DetailChip(
    icon: ImageVector,
    label: String,
    faded: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = if (faded) color.copy(alpha = 0.35f) else color
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (faded) color.copy(alpha = 0.35f) else color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Plant data panel showing Perenual data.
 */
@Composable
private fun PlantDataPanel(data: JsonObject) {
    val colors = MaterialTheme.colorScheme

    val scientificName = data["scientific_name"]?.let {
        if (it is JsonArray) it.firstOrNull()?.jsonPrimitive?.contentOrNull
        else it.jsonPrimitive?.contentOrNull
    }
    val watering = data["watering"]?.jsonPrimitive?.contentOrNull
    val sunlight = data["sunlight"]?.let { element ->
        when (element) {
            is JsonArray -> element.joinToString(", ") { it.jsonPrimitive.content }
            is JsonPrimitive -> element.contentOrNull
            else -> null
        }
    }
    val cycle = data["cycle"]?.jsonPrimitive?.contentOrNull
    val description = data["description"]?.jsonPrimitive?.contentOrNull

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colors.primary.copy(alpha = 0.07f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = colors.primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Rounded.Eco,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = colors.primary
            )

            Text(
                text = "Plant Info",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!scientificName.isNullOrEmpty()) {
            InfoRow(Icons.Rounded.MenuBook, "Scientific name", scientificName)
        }

        if (!watering.isNullOrEmpty()) {
            InfoRow(Icons.Rounded.WaterDrop, "Watering", watering)
        }

        if (!sunlight.isNullOrEmpty()) {
            InfoRow(Icons.Rounded.WbSunny, "Sunlight", sunlight)
        }

        if (!cycle.isNullOrEmpty()) {
            InfoRow(Icons.Rounded.Refresh, "Cycle", cycle)
        }

        if (!description.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.6f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Info row composable for displaying label-value pairs.
 */
@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.padding(bottom = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = colors.primary
        )

        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.75f)
        )
    }
}

/**
 * Bottom sheet composable for creating/editing profiles.
 */
@Composable
private fun ProfileSheet(
    existing: CropProfile? = null,
    onSave: (name: String, plantName: String, threshold: Int) -> Unit
) {
    val isEdit = existing != null
    val colors = MaterialTheme.colorScheme

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var plantName by remember { mutableStateOf(existing?.plantName ?: "") }
    var threshold by remember { mutableFloatStateOf(existing?.minMoisture?.toFloat() ?: 30f) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    // Clear name error when user types
    LaunchedEffect(name) {
        if (nameError != null && name.trim().isNotEmpty()) {
            nameError = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Rounded.Eco,
                contentDescription = null,
                tint = colors.primary
            )

            Text(
                text = if (isEdit) "Edit Profile" else "New Profile",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Profile name field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Profile Name") },
            placeholder = { Text("e.g. Summer Tomato") },
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Plant name field
        OutlinedTextField(
            value = plantName,
            onValueChange = { plantName = it },
            label = { Text("Plant Name") },
            placeholder = { Text("e.g. Tomato, Wheat, Basil") },
            supportingText = { Text("Used to fetch nutrient & care data from Perenual.") },
            leadingIcon = {
                Icon(
                    Icons.Rounded.Grass,
                    contentDescription = null,
                    tint = colors.primary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Threshold slider card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.WaterDrop,
                        contentDescription = null,
                        tint = colors.primary
                    )

                    Text(
                        text = "Dry Threshold (%)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Pump turns on when moisture drops below this.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = threshold,
                    onValueChange = { threshold = it },
                    valueRange = 0f..100f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${threshold.toInt()}%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save button
        Button(
            onClick = {
                if (name.trim().isEmpty()) {
                    nameError = "Please enter a profile name"
                    return@Button
                }

                saving = true
                onSave(name.trim(), plantName.trim(), threshold.toInt())
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !saving
        ) {
            if (saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saving…")
            } else {
                Icon(Icons.Rounded.Save, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEdit) "Save Changes" else "Create Profile")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
