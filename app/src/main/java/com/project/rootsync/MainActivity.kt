package com.project.rootsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.project.rootsync.data.UserPreferencesDataStore
import com.project.rootsync.data.repository.AuthState
import com.project.rootsync.ui.navigation.RootSyncNavGraph
import com.project.rootsync.ui.navigation.Screen
import com.project.rootsync.ui.theme.RootSyncTheme
import com.project.rootsync.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hardcode device and user IDs
        lifecycleScope.launch {
            val prefs = UserPreferencesDataStore(applicationContext)
            prefs.saveDeviceId("62e19bc1-2e15-43f9-a93b-ee68585a0e89")
            prefs.saveUserId("2cfca950-b279-4dd0-884e-5e50731d96ac")
        }

        setContent {
            RootSyncTheme {
                RootSyncApp()
            }
        }
    }
}

@Composable
fun RootSyncApp(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.sessionState.collectAsState()

    val startDestination = when (authState) {
        is AuthState.Authenticated -> Screen.Dashboard.route
        else -> Screen.Login.route
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        RootSyncNavGraph(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
