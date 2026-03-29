package com.project.rootsync.ui.screens.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.LockPerson
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.project.rootsync.ui.navigation.Screen
import com.project.rootsync.viewmodel.AuthState
import com.project.rootsync.viewmodel.AuthViewModel

/**
 * Main login screen composable.
 */
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()

    // Tab state (replaces Flutter's TabController)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Text field states (replaces Flutter's TextEditingController)
    var signInIdentifier by remember { mutableStateOf("") }
    var signInPassword by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpUsername by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    // Navigate when authenticated (replaces Flutter's auth state listener)
    LaunchedEffect(sessionState) {
        if (sessionState is AuthState.Authenticated) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    // Clear error when switching tabs (replaces Flutter's TabController listener)
    LaunchedEffect(selectedTab) {
        viewModel.clearError()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App icon
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Constrained width card
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Tab bar
                        LoginTabBar(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )

                        // Tab content with animation
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "TabContent"
                        ) { tabIndex ->
                            when (tabIndex) {
                                0 -> SignInTabContent(
                                    identifier = signInIdentifier,
                                    onIdentifierChange = { signInIdentifier = it },
                                    password = signInPassword,
                                    onPasswordChange = { signInPassword = it },
                                    obscurePassword = uiState.obscureSignIn,
                                    onToggleObscure = { viewModel.toggleSignInObscure() },
                                    errorMessage = uiState.errorMessage,
                                    isLoading = uiState.isLoading,
                                    onSignIn = {
                                        viewModel.signIn(signInIdentifier, signInPassword)
                                    }
                                )
                                1 -> SignUpTabContent(
                                    email = signUpEmail,
                                    onEmailChange = { signUpEmail = it },
                                    username = signUpUsername,
                                    onUsernameChange = { signUpUsername = it },
                                    password = signUpPassword,
                                    onPasswordChange = { signUpPassword = it },
                                    obscurePassword = uiState.obscureSignUp,
                                    onToggleObscure = { viewModel.toggleSignUpObscure() },
                                    errorMessage = uiState.errorMessage,
                                    isLoading = uiState.isLoading,
                                    onSignUp = {
                                        viewModel.signUp(
                                            signUpEmail,
                                            signUpUsername,
                                            signUpPassword
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab bar matching Flutter's TabBar styling.
 */
@Composable
private fun LoginTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Transparent,
        contentColor = colors.onSurface,
        divider = {
            TabRowDefaults.SecondaryIndicator(
                color = colors.onSurface.copy(alpha = 0.08f)
            )
        },
        indicator = { tabPositions ->
            if (selectedTab < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = colors.primary,
                    height = 2.5.dp
                )
            }
        }
    ) {
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            selectedContentColor = colors.onSurface,
            unselectedContentColor = colors.onSurface.copy(alpha = 0.35f)
        ) {
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        Tab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            selectedContentColor = colors.onSurface,
            unselectedContentColor = colors.onSurface.copy(alpha = 0.35f)
        ) {
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }
}

/**
 * Sign In tab content.
 */
@Composable
private fun SignInTabContent(
    identifier: String,
    onIdentifierChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    obscurePassword: Boolean,
    onToggleObscure: () -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    onSignIn: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp)
    ) {
        LoginTextField(
            value = identifier,
            onValueChange = onIdentifierChange,
            label = "Email or Username",
            hint = "you@example.com  or  your_username",
            icon = Icons.Outlined.AccountCircle,
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(14.dp))

        LoginTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            hint = "Enter your password",
            icon = Icons.Outlined.LockPerson,
            isPassword = true,
            obscureText = obscurePassword,
            onToggleObscure = onToggleObscure
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            ErrorBox(message = errorMessage)
        }

        Spacer(modifier = Modifier.height(22.dp))

        PrimaryButton(
            label = "Sign In",
            icon = Icons.Rounded.Login,
            isLoading = isLoading,
            onClick = if (!isLoading) onSignIn else null
        )
    }
}

/**
 * Sign Up tab content.
 */
@Composable
private fun SignUpTabContent(
    email: String,
    onEmailChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    obscurePassword: Boolean,
    onToggleObscure: () -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    onSignUp: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp)
    ) {
        LoginTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email address",
            hint = "you@example.com",
            icon = Icons.Outlined.Mail
        )

        Spacer(modifier = Modifier.height(14.dp))

        LoginTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = "Username",
            hint = "e.g. john_farmer",
            icon = Icons.Outlined.Badge,
            keyboardType = KeyboardType.Text
        )

        Spacer(modifier = Modifier.height(14.dp))

        LoginTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            hint = "Min. 8 characters",
            icon = Icons.Outlined.LockPerson,
            isPassword = true,
            obscureText = obscurePassword,
            onToggleObscure = onToggleObscure
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            ErrorBox(message = errorMessage)
        }

        Spacer(modifier = Modifier.height(22.dp))

        PrimaryButton(
            label = "Create Account",
            icon = Icons.Rounded.PersonAdd,
            isLoading = isLoading,
            onClick = if (!isLoading) onSignUp else null
        )
    }
}
