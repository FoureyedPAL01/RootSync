package com.project.rootsync.ui.screens.device

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowDownEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.project.rootsync.ui.navigation.Screen
import com.project.rootsync.viewmodel.DeviceChoiceViewModel
import kotlinx.coroutines.launch

/**
 * Device choice screen composable.
 * Shows when a device is already linked, allowing user to continue or relink.
 */
@Composable
fun DeviceChoiceScreen(
    navController: NavController,
    viewModel: DeviceChoiceViewModel = hiltViewModel()
) {
    val deviceId by viewModel.deviceId.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Entrance animation trigger
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        // Fade + Slide entrance animation
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowDownEasing)
            ) + slideInVertically(
                initialOffsetY = { (it * 0.12f).toInt() },
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowDownEasing)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(WindowInsets.statusBars.asPaddingValues()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 28.dp, vertical = 40.dp)
                        .widthIn(max = 440.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ── Icon Badge ───────────────────────────────────────
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Sensors,
                                contentDescription = "Device Icon",
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Heading ──────────────────────────────────────────
                    Text(
                        text = "Device Already Linked",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "This account has a linked device.\nContinue with it or link a different one.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 21.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── UUID Card ────────────────────────────────────────
                    val displayId = deviceId ?: "Unknown UUID"

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Memory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = displayId,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Copy UUID Button
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(displayId))
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "UUID copied",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "Copy UUID",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // ── Primary Button: Continue with this device ────────
                    Button(
                        onClick = {
                            viewModel.onContinue()
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircleOutline, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Continue with this device",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Secondary Button: Link a different device ────────
                    OutlinedButton(
                        onClick = {
                            viewModel.onLinkDifferent()
                            navController.navigate(Screen.LinkDevice.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(Icons.Rounded.SwapHoriz, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Link a different device",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
