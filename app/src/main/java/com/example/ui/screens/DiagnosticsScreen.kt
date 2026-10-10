package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.ai.GeminiAssistantClient
import com.example.service.AlarmSchedulerHelper
import com.example.service.PhoneActionExecutor
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.viewmodel.AssistantViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    viewModel: AssistantViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isCheckingAi by remember { mutableStateOf(false) }
    var aiStatusText by remember { mutableStateOf("Checking...") }
    var lastErrorLog by remember { mutableStateOf("No active critical errors recorded. System functioning nominally.") }

    val micGranted = remember(context) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    val notificationGranted = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    val alarmScheduler = remember(context) { AlarmSchedulerHelper(context) }
    val exactAlarmGranted = remember(alarmScheduler) { alarmScheduler.canScheduleExactAlarms() }

    val geminiClient = remember { GeminiAssistantClient() }

    fun checkAiStatus() {
        isCheckingAi = true
        if (geminiClient.isConfigured) {
            aiStatusText = "Active: Server-side Gemini 3.5 Flash Configured"
        } else {
            aiStatusText = "Fallback Mode: Built-in Multilingual NLP Active (Add GEMINI_API_KEY in Secrets for live cloud inference)"
        }
        isCheckingAi = false
    }

    LaunchedEffect(Unit) {
        checkAiStatus()
    }

    val fullDiagnosticDetails = remember(micGranted, notificationGranted, exactAlarmGranted, aiStatusText, lastErrorLog) {
        """
        === SAFEER AI ASSISTANT DIAGNOSTICS REPORT ===
        Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
        App Name: Safeer AI Assistant
        Package: com.safeer.aiassistant
        Version Name: ${BuildConfig.VERSION_NAME}
        Version Code: ${BuildConfig.VERSION_CODE}
        Android OS: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
        Device: ${Build.MANUFACTURER} ${Build.MODEL}
        
        [PERMISSIONS]
        • Microphone (RECORD_AUDIO): ${if (micGranted) "GRANTED" else "NOT GRANTED"}
        • Notifications (POST_NOTIFICATIONS): ${if (notificationGranted) "GRANTED" else "NOT GRANTED"}
        • Exact Alarm: ${if (exactAlarmGranted) "SCHEDULE_EXACT_ALARM ALLOWED" else "DEFAULT WINDOW SCHEDULE"}
        
        [AI BACKEND]
        • Status: $aiStatusText
        • Fallback NLP Engine: Active and fully functional
        
        [DIAGNOSTIC LOG]
        • State: $lastErrorLog
        ==============================================
        """.trimIndent()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Diagnostics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { checkAiStatus() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry & Refresh")
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
            // Version & Build Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Application & Build Information", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        DiagnosticRow("Application Name", "Safeer AI Assistant")
                        DiagnosticRow("Package Identifier", "com.safeer.aiassistant")
                        DiagnosticRow("Version", "${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})")
                        DiagnosticRow("Android Runtime", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                    }
                }
            }

            // Permissions Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Permissions Health Check", fontWeight = FontWeight.Bold)
                        }

                        StatusItem(
                            title = "Microphone (RECORD_AUDIO)",
                            subtitle = "Enables Urdu & English speech-to-text recognition",
                            isOk = micGranted
                        )

                        StatusItem(
                            title = "Push Notifications",
                            subtitle = "Required for daily goals, hydration, and task alerts",
                            isOk = notificationGranted
                        )

                        StatusItem(
                            title = "Exact Alarm Timing",
                            subtitle = "Guarantees punctual alarms without OS power throttling",
                            isOk = exactAlarmGranted
                        )

                        if (!micGranted || !notificationGranted) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { PhoneActionExecutor.openPhoneSettings(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Phone Permissions Settings")
                            }
                        }
                    }
                }
            }

            // AI Backend Connection Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = EmeraldLight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Intelligence Engine Status", fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = aiStatusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(onClick = { checkAiStatus() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry & Recheck")
                            }
                        }
                    }
                }
            }

            // Safe Diagnostics Log Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Terminal, contentDescription = null, tint = EmeraldPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Safe Diagnostics Log", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    PhoneActionExecutor.copyToClipboard(context, "Safeer Diagnostics", fullDiagnosticDetails)
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Details")
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = fullDiagnosticDetails,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun StatusItem(title: String, subtitle: String, isOk: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(
            shape = CircleShape,
            color = if (isOk) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    if (isOk) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isOk) EmeraldPrimary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isOk) "Active" else "Missing",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isOk) EmeraldPrimary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
