package com.project.rootsync.ui.screens.device

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.project.rootsync.viewmodel.LinkDeviceViewModel

/**
 * Link device screen composable.
 * Allows users to link an ESP32 device by entering its UUID.
 */
@Composable
fun LinkDeviceScreen(
    navController: NavController,
    viewModel: LinkDeviceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var uuidInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }

    // Handle navigation on success
    LaunchedEffect(uiState.isLinked) {
        if (uiState.isLinked) {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Icon
            Icon(
                imageVector = Icons.Rounded.Sensors,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Link Your Device",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = "Enter the Device UUID printed on the sticker\n" +
                        "on your SISS hardware unit.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // UUID Input Field
            OutlinedTextField(
                value = uuidInput,
                onValueChange = {
                    uuidInput = it
                    if (uiState.error != null) viewModel.resetError()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Device UUID") },
                placeholder = { Text("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx") },
                isError = uiState.error != null,
                supportingText = {
                    if (uiState.error != null) {
                        Text(uiState.error!!)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.None
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Device Name Input Field
            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                    if (uiState.error != null) viewModel.resetError()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Device Name") },
                placeholder = { Text("e.g. Garden Sensor, Greenhouse 1") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Link Button
            Button(
                onClick = { viewModel.linkDevice(uuidInput, nameInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uuidInput.isNotBlank() && nameInput.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Link Device")
                }
            }
        }
    }
}
