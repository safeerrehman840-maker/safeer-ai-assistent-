package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AssistantViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val assistantViewModel: AssistantViewModel = viewModel()
                MainAppScaffold(assistantViewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: AssistantViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    // Permission Request Launcher for Audio & Notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle results if needed
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    // Intercept hardware/system back button
    BackHandler(enabled = currentScreen != "dashboard") {
        when (currentScreen) {
            "about", "diagnostics" -> viewModel.navigateTo("settings")
            else -> viewModel.navigateTo("dashboard")
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                NavigationBarItem(
                    selected = currentScreen == "dashboard",
                    onClick = { viewModel.navigateTo("dashboard") },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.25f),
                        selectedIconColor = EmeraldPrimary
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == "chat",
                    onClick = { viewModel.navigateTo("chat") },
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI Voice") },
                    label = { Text("Safeer AI") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.25f),
                        selectedIconColor = EmeraldPrimary
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == "goals",
                    onClick = { viewModel.navigateTo("goals") },
                    icon = { Icon(Icons.Default.CheckCircleOutline, contentDescription = "Goals") },
                    label = { Text("Goals") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.25f),
                        selectedIconColor = EmeraldPrimary
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == "timer",
                    onClick = { viewModel.navigateTo("timer") },
                    icon = { Icon(Icons.Default.Timer, contentDescription = "Work Timer") },
                    label = { Text("Timer") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.25f),
                        selectedIconColor = EmeraldPrimary
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == "alarms",
                    onClick = { viewModel.navigateTo("alarms") },
                    icon = { Icon(Icons.Default.Alarm, contentDescription = "Alarms") },
                    label = { Text("Alarms") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.25f),
                        selectedIconColor = EmeraldPrimary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "dashboard" -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigate = { screenId -> viewModel.navigateTo(screenId) }
                )
                "chat" -> AssistantChatScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo("dashboard") }
                )
                "goals" -> DailyGoalsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo("dashboard") }
                )
                "alarms" -> AlarmsRemindersScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo("dashboard") }
                )
                "timer" -> WorkTimerScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo("dashboard") }
                )
                "whatsapp" -> WhatsAppHelperScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo("dashboard") }
                )
                "email" -> EmailAssistantScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo("dashboard") }
                )
                "youtube_web" -> YouTubeWebSearchScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo("dashboard") }
                )
                "settings" -> SettingsPrivacyScreen(
                    viewModel = viewModel,
                    onNavigate = { screenId -> viewModel.navigateTo(screenId) },
                    onBack = { viewModel.navigateTo("dashboard") }
                )
                "about" -> AboutOwnerScreen(
                    onBack = { viewModel.navigateTo("settings") }
                )
                "diagnostics" -> DiagnosticsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo("settings") }
                )
                else -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigate = { screenId -> viewModel.navigateTo(screenId) }
                )
            }
        }
    }
}
