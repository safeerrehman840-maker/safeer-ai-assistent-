package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.PhoneActionExecutor
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPrivacyScreen(
    viewModel: AssistantViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by viewModel.preferredLanguage.collectAsStateWithLifecycle()
    val ttsEnabled by viewModel.ttsEnabled.collectAsStateWithLifecycle()

    var showPrivacyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Information", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // About Safeer AI Assistant & Developer Info Card (Clickable Entry)
            item {
                Card(
                    onClick = { onNavigate("about") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "About Safeer AI Assistant",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Owner: Safeer Ur Rehman • Contact: 03293408114",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldLight
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Diagnostics & System Health (Clickable Entry)
            item {
                Card(
                    onClick = { onNavigate("diagnostics") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Troubleshoot, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Diagnostics & Error Reporting",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Inspect build info, permissions, AI status & logs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Language Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Voice Assistant Preferred Language", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = currentLang == "Roman Urdu",
                                onClick = { viewModel.preferredLanguage.value = "Roman Urdu" },
                                label = { Text("Roman Urdu") }
                            )
                            FilterChip(
                                selected = currentLang == "Urdu",
                                onClick = { viewModel.preferredLanguage.value = "Urdu" },
                                label = { Text("اردو (Urdu)") }
                            )
                            FilterChip(
                                selected = currentLang == "English",
                                onClick = { viewModel.preferredLanguage.value = "English" },
                                label = { Text("English") }
                            )
                        }
                    }
                }
            }

            // Speech Settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Text-to-Speech (TTS) Voice Replies", fontWeight = FontWeight.SemiBold)
                            Text("Speak responses out loud in Urdu/English", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = ttsEnabled,
                            onCheckedChange = { viewModel.ttsEnabled.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }
                }
            }

            // Permissions Status & Device Settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Android Permissions & Device Settings", fontWeight = FontWeight.Bold)
                        Text(
                            "• Microphone (RECORD_AUDIO): Used solely for voice speech recognition when you tap the mic.\n• Notifications: Used for smart alarms & goal reminder alerts.\n• Exact Alarms: Used for precise schedule delivery.\n• WhatsApp & Email: Dispatched via standard Android sharing intents.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { PhoneActionExecutor.openPhoneSettings(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open App System Settings")
                        }
                    }
                }
            }

            // Privacy & Data Control
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Privacy & Security Policy", fontWeight = FontWeight.Bold)
                        Text(
                            "Safeer stores all your goals, reminders, and conversation history on your device using a local Room SQLite database. Your personal info is never sold or shared.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showPrivacyDialog = true }) {
                                Text("Read Full Policy")
                            }
                            OutlinedButton(
                                onClick = { viewModel.clearChat() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Clear History")
                            }
                        }
                    }
                }
            }

            // Footer branding
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Safeer AI Assistant", fontWeight = FontWeight.Bold, color = EmeraldLight)
                    Text("Personal AI Assistant • Owner: Safeer Ur Rehman", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Safeer AI Assistant — Privacy Policy") },
            text = {
                Text(
                    "1. Voice Data: Audio captured via the microphone is used strictly during active recognition and is never stored permanently or uploaded secretly.\n\n" +
                    "2. Device Integrity: Actions such as sending WhatsApp messages or drafting emails always present the message for your review before dispatch.\n\n" +
                    "3. Local Data: Goals, alarms, reminders, and local history reside locally on your device.\n\n" +
                    "4. Security: Gemini API calls are performed via secure SSL endpoints without leaking private contacts or device storage."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
                ) {
                    Text("I Understand")
                }
            }
        )
    }
}
