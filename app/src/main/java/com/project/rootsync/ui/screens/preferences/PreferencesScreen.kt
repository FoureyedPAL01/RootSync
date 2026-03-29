package com.project.rootsync.ui.screens.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WindPower
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.project.rootsync.ui.components.SettingsSection
import com.project.rootsync.viewmodel.PreferencesViewModel
import kotlinx.coroutines.launch

/**
 * Main preferences screen composable.
 */
@Composable
fun PreferencesScreen(
    navController: NavController,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle snackbar
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.onSnackbarShown()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- Units Section ---
            item {
                SettingsSection(title = "Units") {
                    // Temperature
                    PreferenceHeader(
                        title = "Temperature",
                        icon = {
                            Icon(
                                Icons.Rounded.Thermostat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    PreferenceRadioGroup(
                        options = listOf(
                            "celsius" to "Celsius (°C)",
                            "fahrenheit" to "Fahrenheit (°F)"
                        ),
                        selectedOption = uiState.tempUnit,
                        onOptionSelected = { viewModel.updateTempUnit(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Volume
                    PreferenceHeader(
                        title = "Volume",
                        icon = {
                            Icon(
                                Icons.Rounded.WaterDrop,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    PreferenceRadioGroup(
                        options = listOf(
                            "litres" to "Litres (L)",
                            "gallons" to "Gallons (gal)"
                        ),
                        selectedOption = uiState.volumeUnit,
                        onOptionSelected = { viewModel.updateVolumeUnit(it) }
                    )
                }
            }

            // --- Wind Speed Section ---
            item {
                SettingsSection(title = "Wind Speed") {
                    PreferenceRadioGroup(
                        options = listOf(
                            "km/h" to "Kilometres per hour",
                            "m/s" to "Metres per second",
                            "mph" to "Miles per hour",
                            "kn" to "Knots"
                        ),
                        selectedOption = uiState.windUnit,
                        onOptionSelected = { viewModel.updateWindUnit(it) },
                        showSubtitleAsValue = true
                    )
                }
            }

            // --- Precipitation Section ---
            item {
                SettingsSection(title = "Precipitation") {
                    PreferenceRadioGroup(
                        options = listOf(
                            "mm" to "Millimetres",
                            "inch" to "Inches"
                        ),
                        selectedOption = uiState.precipitationUnit,
                        onOptionSelected = { viewModel.updatePrecipitationUnit(it) },
                        showSubtitleAsValue = true
                    )
                }
            }

            // --- Air Quality Index Section ---
            item {
                SettingsSection(title = "Air Quality Index") {
                    PreferenceRadioGroup(
                        options = listOf(
                            "us" to "US AQI",
                            "eu" to "European AQI"
                        ),
                        selectedOption = uiState.aqiType,
                        onOptionSelected = { viewModel.updateAqiType(it) },
                        subtitles = mapOf(
                            "us" to "EPA standard, 0-500 scale",
                            "eu" to "EEA standard, 0-100 scale"
                        )
                    )
                }
            }

            // --- Time Section ---
            item {
                SettingsSection(title = "Time") {
                    ListItem(
                        headlineContent = { Text("Timezone") },
                        supportingContent = { Text(uiState.timezone) },
                        leadingContent = {
                            Icon(Icons.Rounded.Schedule, contentDescription = null)
                        }
                    )
                }
            }

            // --- Location Section ---
            item {
                SettingsSection(title = "Location") {
                    if (!uiState.isEditingLocation) {
                        ListItem(
                            headlineContent = { Text("Coordinates") },
                            supportingContent = {
                                if (uiState.locationLat.isNotEmpty() || uiState.locationLon.isNotEmpty()) {
                                    Text("${uiState.locationLat}, ${uiState.locationLon}")
                                } else {
                                    Text("Not set")
                                }
                            },
                            leadingContent = {
                                Icon(Icons.Rounded.Place, contentDescription = null)
                            },
                            trailingContent = {
                                IconButton(onClick = { viewModel.setEditingLocation(true) }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Edit Location")
                                }
                            }
                        )
                    } else {
                        LocationEditor(
                            lat = uiState.locationLat,
                            lon = uiState.locationLon,
                            latError = uiState.latError,
                            lonError = uiState.lonError,
                            isSaving = uiState.isSaving,
                            onLatChange = { viewModel.onLatChange(it) },
                            onLonChange = { viewModel.onLonChange(it) },
                            onSave = { viewModel.saveLocation() },
                            onCancel = { viewModel.cancelLocationEdit() }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Preference header with icon and title.
 */
@Composable
private fun PreferenceHeader(
    title: String,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Radio group for preference selection.
 */
@Composable
private fun PreferenceRadioGroup(
    options: List<Pair<String, String>>, // value to label
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    showSubtitleAsValue: Boolean = false,
    subtitles: Map<String, String>? = null
) {
    Column(modifier = Modifier.selectableGroup()) {
        options.forEach { (value, label) ->
            val subtitle = subtitles?.get(value) ?: if (showSubtitleAsValue) value else null

            ListItem(
                modifier = Modifier
                    .selectable(
                        selected = (value == selectedOption),
                        onClick = { onOptionSelected(value) },
                        role = Role.RadioButton
                    ),
                headlineContent = { Text(label) },
                supportingContent = subtitle?.let { { Text(it) } },
                trailingContent = {
                    RadioButton(
                        selected = (value == selectedOption),
                        onClick = null
                    )
                }
            )
        }
    }
}

/**
 * Location editor with lat/lon input fields.
 */
@Composable
private fun LocationEditor(
    lat: String,
    lon: String,
    latError: String?,
    lonError: String?,
    isSaving: Boolean,
    onLatChange: (String) -> Unit,
    onLonChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = lat,
            onValueChange = onLatChange,
            label = { Text("Latitude") },
            placeholder = { Text("e.g. 19.076") },
            isError = latError != null,
            supportingText = latError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = lon,
            onValueChange = onLonChange,
            label = { Text("Longitude") },
            placeholder = { Text("e.g. 72.877") },
            isError = lonError != null,
            supportingText = lonError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel, enabled = !isSaving) {
                Text("Cancel")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onSave, enabled = !isSaving) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (isSaving) "Saving..." else "Save")
            }
        }
    }
}
