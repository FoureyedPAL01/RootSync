package com.project.rootsync.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.project.rootsync.ui.components.DeleteAccountButton
import com.project.rootsync.ui.components.InlinePasswordTile
import com.project.rootsync.ui.components.ReadOnlyTile
import com.project.rootsync.ui.components.SettingsSection
import com.project.rootsync.ui.navigation.Screen
import com.project.rootsync.viewmodel.ProfileViewModel

/**
 * Main profile screen composable.
 */
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigation side-effects
    LaunchedEffect(uiState.isSignedOut, uiState.isDeleted) {
        if (uiState.isSignedOut || uiState.isDeleted) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Show save errors via Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.saveError) {
        uiState.saveError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Profile header
            item { ProfileHeader(uiState) }

            // Account Details section
            item {
                SettingsSection(
                    title = "Account Details",
                    leadingIcon = {
                        Icon(Icons.Rounded.Badge, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                ) {
                    UsernameRow(
                        username = uiState.username,
                        isSaving = uiState.isSaving,
                        onSave = { viewModel.updateUsername(it) }
                    )
                    ReadOnlyTile(
                        label = "Email",
                        value = uiState.email.ifEmpty { "Not logged in" },
                        icon = Icons.Rounded.Email
                    )
                }
            }

            // Security section
            item {
                SettingsSection(
                    title = "Security",
                    leadingIcon = {
                        Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                ) {
                    InlinePasswordTile(
                        title = "Change Password",
                        icon = Icons.Rounded.Lock,
                        isLoading = uiState.isSaving,
                        errorMessage = uiState.saveError,
                        onUpdate = { current, newPass ->
                            viewModel.updatePassword(current, newPass)
                        }
                    )
                }
            }

            // Sign Out button
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Button(
                        onClick = { viewModel.signOut() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Rounded.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Sign Out")
                    }
                }
            }

            // Danger Zone
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Danger Zone",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
                DeleteAccountButton(
                    isLoading = uiState.isSaving,
                    onConfirm = { viewModel.deleteAccount() }
                )
            }
        }
    }
}

/**
 * Profile header with avatar and user info.
 */
@Composable
private fun ProfileHeader(state: com.project.rootsync.viewmodel.ProfileUiState) {
    val colors = MaterialTheme.colorScheme

    // Compute initials
    val display = state.username.ifEmpty { state.email.ifEmpty { "?" } }
    val initials = display.trim()
        .split(Regex("""[\s_]+"""))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it[0].uppercaseChar().toString() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.primary.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = colors.outline.copy(alpha = 0.2f)
            )
            .padding(vertical = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 28.sp
                    )
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = state.username.ifEmpty { "No username set" },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = state.email,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = colors.onSurface.copy(alpha = 0.5f)
                )
            )
        }
    }
}

/**
 * Inline editable username row.
 */
@Composable
private fun UsernameRow(
    username: String,
    isSaving: Boolean,
    onSave: (String) -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var text by remember(username) { mutableStateOf(username) }
    var fieldError by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val colors = MaterialTheme.colorScheme

    // Auto-focus when editing starts
    LaunchedEffect(editing) {
        if (editing) focusRequester.requestFocus()
    }

    fun validate(value: String): String? = when {
        value.isEmpty() -> "Username is required"
        value.length < 3 -> "Username must be at least 3 characters"
        value.length > 30 -> "Username must be at most 30 characters"
        !value.matches(Regex("^[a-zA-Z0-9_]+\$")) -> "Letters, numbers and underscores only"
        else -> null
    }

    fun save() {
        val err = validate(text.trim())
        if (err != null) { fieldError = err; return }
        fieldError = null
        onSave(text.trim())
        editing = false
    }

    if (!editing) {
        ListItem(
            leadingContent = {
                Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(20.dp))
            },
            headlineContent = {
                Text("Username", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp))
            },
            supportingContent = {
                Text(username, style = MaterialTheme.typography.bodyLarge)
            },
            trailingContent = {
                IconButton(onClick = { text = username; fieldError = null; editing = true }) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit username", modifier = Modifier.size(20.dp))
                }
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it; fieldError = validate(it.trim()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text("Username") },
                isError = fieldError != null,
                supportingText = fieldError?.let { { Text(it, color = colors.error) } },
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(20.dp)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { save() }),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { text = username; fieldError = null; editing = false },
                    enabled = !isSaving
                ) {
                    Text("Cancel")
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = { save() },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = colors.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
}
