package com.project.rootsync.ui.screens.fertigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.rootsync.ui.components.SettingsSection
import com.project.rootsync.util.DateHelpers
import com.project.rootsync.viewmodel.FertigationUiState
import com.project.rootsync.viewmodel.FertigationViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// Theme color for fertigation
private val Purple = Color(0xFF7C3AED)
private val Green = Color(0xFF2D9D5C)
private val Amber = Color(0xFFF59E0B)

/**
 * Main fertigation screen composable.
 */
@Composable
fun FertigationScreen(
    viewModel: FertigationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLogDialog by remember { mutableStateOf(false) }

    // Show messages
    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.profile == null -> {
                NoProfileState()
            }

            else -> {
                FertigationContent(
                    uiState = uiState,
                    onRefresh = { viewModel.load() },
                    onFetchPlantData = { viewModel.fetchPlantData() },
                    onLogApplication = { showLogDialog = true }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Log application dialog
    if (showLogDialog) {
        LogApplicationDialog(
            onDismiss = { showLogDialog = false },
            onConfirm = { notes ->
                viewModel.logFertilizerApplication(notes)
                showLogDialog = false
            }
        )
    }
}

@Composable
private fun FertigationContent(
    uiState: FertigationUiState,
    onRefresh: () -> Unit,
    onFetchPlantData: () -> Unit,
    onLogApplication: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = uiState.profile!!
    val plantName = profile.plantName ?: ""

    val daysSinceLast = remember(uiState.logs) {
        if (uiState.logs.isEmpty()) -1
        else {
            try {
                val last = Instant.parse(uiState.logs.first().fertilizedAt)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                (now.date - last.date).days
            } catch (e: Exception) {
                -1
            }
        }
    }

    val careData = uiState.careData as? JsonObject
    val fertDescription = remember(careData) {
        careData?.get("fertilizer")?.jsonObject?.get("description")?.jsonPrimitive?.content
    }
    val waterDescription = remember(careData) {
        careData?.get("watering")?.jsonObject?.get("description")?.jsonPrimitive?.content
    }
    val hasCareData = careData != null && fertDescription != null

    val plantData = uiState.plantData as? JsonObject
    val imageUrl = plantData?.get("image_url")?.jsonPrimitive?.content
    val scientificName = plantData?.let {
        it["scientific_name"]?.let { sn ->
            if (sn.jsonArray.isNotEmpty()) sn.jsonArray[0].jsonPrimitive.content
            else null
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Plant hero section
            item {
                PlantHeroSection(
                    plantName = plantName,
                    profileName = profile.name,
                    scientificName = scientificName,
                    imageUrl = imageUrl,
                    isFetching = uiState.isFetching,
                    onFetchPlantData = onFetchPlantData
                )
            }

            // Stats row
            item {
                StatsRow(
                    daysSinceLast = daysSinceLast,
                    totalApplications = uiState.logs.size,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            // Log button
            item {
                LogButton(
                    isLogging = uiState.isLogging,
                    onLogApplication = onLogApplication,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // No care data prompt
            if (!hasCareData && plantName.isNotEmpty()) {
                item {
                    NoCareDataPrompt(
                        plantName = plantName,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }

            // Fertilization guide
            if (hasCareData) {
                item {
                    SettingsSection(
                        title = "Fertilization Guide",
                        content = {
                            CareSection(
                                icon = Icons.Rounded.Science,
                                title = "Fertilizer",
                                description = fertDescription!!,
                                color = Purple
                            )
                            if (waterDescription != null) {
                                HorizontalDivider()
                                CareSection(
                                    icon = Icons.Rounded.WaterDrop,
                                    title = "Watering",
                                    description = waterDescription,
                                    color = Color(0xFF2196F3)
                                )
                            }
                        }
                    )
                }
            }

            // Plant info
            if (plantData != null) {
                item {
                    PlantInfoSection(
                        plantData = plantData,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Application history
            item {
                SettingsSection(
                    title = "Application History",
                    content = {
                        if (uiState.logs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No applications logged yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        } else {
                            uiState.logs.forEachIndexed { index, log ->
                                LogRow(log = log)
                                if (index < uiState.logs.size - 1) {
                                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                                }
                            }
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(36.dp)) }
        }
    }
}

@Composable
private fun PlantHeroSection(
    plantName: String,
    profileName: String,
    scientificName: String?,
    imageUrl: String?,
    isFetching: Boolean,
    onFetchPlantData: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plant image placeholder
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFlorist,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Profile info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plantName.ifEmpty { profileName },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (plantName.isNotEmpty()) {
                    Text(
                        text = profileName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                if (scientificName != null) {
                    Text(
                        text = scientificName,
                        style = MaterialTheme.typography.labelSmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Refresh button
            IconButton(
                onClick = onFetchPlantData,
                enabled = !isFetching
            ) {
                if (isFetching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh plant data"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(
    daysSinceLast: Int,
    totalApplications: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        StatBox(
            icon = Icons.Rounded.Event,
            label = "Last Applied",
            value = when {
                daysSinceLast < 0 -> "Never"
                daysSinceLast == 0 -> "Today"
                else -> "$daysSinceLast days ago"
            },
            color = when {
                daysSinceLast < 0 -> MaterialTheme.colorScheme.error
                daysSinceLast <= 7 -> Green
                else -> Amber
            },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        StatBox(
            icon = Icons.Rounded.FormatListNumbered,
            label = "Total Applications",
            value = totalApplications.toString(),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatBox(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun LogButton(
    isLogging: Boolean,
    onLogApplication: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onLogApplication,
        enabled = !isLogging,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Purple,
            contentColor = Color.White
        )
    ) {
        if (isLogging) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            Icon(
                imageVector = Icons.Rounded.Science,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = if (isLogging) "Logging…" else "Log Fertilizer Application")
    }
}

@Composable
private fun NoCareDataPrompt(
    plantName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Tap the refresh button above to fetch fertilization recommendations for $plantName.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun CareSection(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        ListItem(
            headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            trailingContent = {
                Icon(
                    imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp
                    else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            },
            modifier = Modifier.clickable { expanded = !expanded }
        )

        if (expanded) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 68.dp, end = 16.dp, bottom = 16.dp),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun PlantInfoSection(
    plantData: JsonObject,
    modifier: Modifier = Modifier
) {
    SettingsSection(
        title = "Plant Info",
        modifier = modifier,
        content = {
            plantData["watering"]?.jsonPrimitive?.content?.let { watering ->
                InfoRow(
                    icon = Icons.Rounded.WaterDrop,
                    label = "Watering Needs",
                    value = watering
                )
            }

            plantData["cycle"]?.jsonPrimitive?.content?.let { cycle ->
                HorizontalDivider()
                InfoRow(
                    icon = Icons.Rounded.Autorenew,
                    label = "Growth Cycle",
                    value = cycle
                )
            }

            plantData["sunlight"]?.jsonArray?.let { sunlight ->
                HorizontalDivider()
                InfoRow(
                    icon = Icons.Rounded.WbSunny,
                    label = "Sunlight",
                    value = sunlight.joinToString(", ") { it.jsonPrimitive.content }
                )
            }

            plantData["description"]?.jsonPrimitive?.content?.let { description ->
                HorizontalDivider()
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 20.sp
                )
            }
        }
    )
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .widthIn(max = 160.dp)
                    .then(modifier)
            )
        }
    )
}

@Composable
private fun LogRow(
    log: com.project.rootsync.data.model.FertigationLog,
    modifier: Modifier = Modifier
) {
    val timeAgo = remember(log.fertilizedAt) {
        try {
            val instant = Instant.parse(log.fertilizedAt)
            DateHelpers.formatRelative(instant)
        } catch (e: Exception) {
            log.fertilizedAt
        }
    }

    ListItem(
        headlineContent = {
            Text(
                text = timeAgo,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = if (!log.notes.isNullOrEmpty()) {
            {
                Text(
                    text = log.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
        } else null,
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Purple.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Science,
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        modifier = modifier
    )
}

@Composable
private fun NoProfileState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalFlorist,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Active Crop Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Go to Crop Profiles, create a profile with a plant name, and set it as Active.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun LogApplicationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Fertilizer Application") },
        text = {
            Column {
                Text(
                    text = "Record a fertilizer application for today.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("e.g. Used NPK 10-10-10") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(notes) }
            ) {
                Text("Log")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}
