package com.project.rootsync.ui.screens.alerts

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.FilterAltOff
import androidx.compose.material.icons.rounded.FilterListOff
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.project.rootsync.data.model.Alert
import com.project.rootsync.util.AlertDateHelpers
import com.project.rootsync.util.Impact
import com.project.rootsync.util.classifyImpact
import com.project.rootsync.viewmodel.AlertViewModel

/**
 * Main alerts screen composable.
 */
@Composable
fun AlertsScreen(
    navController: NavController,
    viewModel: AlertViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    ErrorState(
                        message = "Could not load alerts.",
                        onRetry = { viewModel.refresh() }
                    )
                }
                else -> {
                    AlertsContent(
                        alerts = state.alerts,
                        selectedType = state.selectedAlertType,
                        onFilterChange = { viewModel.setFilter(it) },
                        onResolve = { viewModel.resolveAlert(it) },
                        onDeleteAll = { viewModel.deleteAll("current-device-id") },
                        onRefresh = { viewModel.refresh() }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertsContent(
    alerts: List<Alert>,
    selectedType: String?,
    onFilterChange: (String?) -> Unit,
    onResolve: (Long) -> Unit,
    onDeleteAll: () -> Unit,
    onRefresh: () -> Unit
) {
    val unreadCount = alerts.count { !it.resolved }

    // Filter logic
    val availableTypes = alerts.map { it.alertType }.distinct().sorted()
    val visibleAlerts = if (selectedType == null) alerts else alerts.filter { it.alertType == selectedType }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (unreadCount > 0) "$unreadCount unresolved" else "All clear",
                fontSize = 13.sp,
                color = if (unreadCount > 0) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onRefresh) {
                Icon(
                    Icons.Rounded.Refresh, contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            if (alerts.isNotEmpty()) {
                TextButton(
                    onClick = onDeleteAll,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Rounded.DeleteSweep, modifier = Modifier.size(18.dp), contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Clear all")
                }
            }
        }

        // Legend
        if (alerts.isNotEmpty()) {
            Row(modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 4.dp)) {
                LegendDot(Impact.CRITICAL, "Critical")
                Spacer(Modifier.width(14.dp))
                LegendDot(Impact.WARNING, "Warning")
                Spacer(Modifier.width(14.dp))
                LegendDot(Impact.INFO, "Info")
            }
        }

        // Filter chips
        if (alerts.isNotEmpty()) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { onFilterChange(null) },
                    label = { Text("All") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                availableTypes.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { onFilterChange(if (selectedType == type) null else type) },
                        label = {
                            Text(
                                type.replace("_", " ").lowercase()
                                    .replaceFirstChar { it.uppercase() }
                            )
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.height(1.dp))

        // List
        if (alerts.isEmpty()) {
            EmptyState()
        } else if (visibleAlerts.isEmpty()) {
            FilteredEmptyState(
                selectedLabel = selectedType!!.replace("_", " "),
                onClearFilter = { onFilterChange(null) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(visibleAlerts, key = { it.id }) { alert ->
                    AlertTile(
                        alert = alert,
                        onResolve = { onResolve(alert.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertTile(alert: Alert, onResolve: () -> Unit) {
    val impact = classifyImpact(alert.alertType)
    val accent = if (alert.resolved) Color.Gray.copy(alpha = 0.25f) else getImpactColor(impact)

    val bgColor by animateColorAsState(
        targetValue = if (alert.resolved) Color.LightGray.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        label = "bgColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (alert.resolved) Icons.Rounded.CheckCircleOutline else getImpactIcon(impact),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = accent.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = impact.name,
                        color = accent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = alert.alertType.replace("_", " "),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = alert.message,
                fontSize = 14.sp,
                color = if (alert.resolved) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(5.dp))
            val timeAgo = AlertDateHelpers.timeAgoShort(alert.createdAt)
            Text(
                text = if (alert.resolved) "Resolved · $timeAgo" else timeAgo,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            )
        }

        // Resolve button
        if (!alert.resolved) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(getImpactColor(impact).copy(alpha = 0.1f))
                    .border(1.dp, getImpactColor(impact).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .clickable { onResolve() }
                    .padding(7.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = "Resolve",
                    tint = getImpactColor(impact),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun LegendDot(impact: Impact, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(getImpactColor(impact), CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

@Composable
private fun EmptyState() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.NotificationsNone, modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f), contentDescription = null
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No alerts", fontSize = 18.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Everything looks good.", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun FilteredEmptyState(selectedLabel: String, onClearFilter: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.FilterListOff, modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), contentDescription = null
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No $selectedLabel alerts", fontSize = 18.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Try another alert type or clear the filter.", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        )
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onClearFilter) {
            Icon(Icons.Rounded.FilterAltOff, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Clear filter")
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.CloudOff, modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), contentDescription = null
        )
        Spacer(Modifier.height(16.dp))
        Text(
            message, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRetry) {
            Icon(Icons.Rounded.Refresh, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Retry")
        }
    }
}

// Extension helpers for Theme/Color
private fun getImpactColor(impact: Impact): Color {
    return when (impact) {
        Impact.CRITICAL -> Color(0xFFEF4444)
        Impact.WARNING -> Color(0xFFF59E0B)
        Impact.INFO -> Color(0xFF3B82F6)
    }
}

private fun getImpactIcon(impact: Impact): ImageVector = when (impact) {
    Impact.CRITICAL -> Icons.Rounded.Error
    Impact.WARNING -> Icons.Rounded.WarningAmber
    Impact.INFO -> Icons.Rounded.Info
}
