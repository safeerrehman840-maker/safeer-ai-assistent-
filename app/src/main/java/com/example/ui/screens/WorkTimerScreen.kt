package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.viewmodel.AssistantViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkTimerScreen(
    viewModel: AssistantViewModel,
    onBack: () -> Unit
) {
    val secondsLeft by viewModel.timerSecondsLeft.collectAsStateWithLifecycle()
    val isRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val currentMode by viewModel.timerMode.collectAsStateWithLifecycle()
    val workSessions by viewModel.workSessions.collectAsStateWithLifecycle()

    val totalWorkMinutes = remember(workSessions) {
        workSessions.filter { it.sessionType == "Work" }.sumOf { it.durationMinutes }
    }

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val maxSeconds = when (currentMode) {
        "Short Break" -> 5 * 60
        "Long Break" -> 15 * 60
        else -> 25 * 60
    }
    val progress = 1f - (secondsLeft.toFloat() / maxSeconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Work Timer & Pomodoro", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode selector chips
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    FilterChip(
                        selected = currentMode == "Work",
                        onClick = { viewModel.setTimerDuration("Work") },
                        label = { Text("Focus (25m)") }
                    )
                    FilterChip(
                        selected = currentMode == "Short Break",
                        onClick = { viewModel.setTimerDuration("Short Break") },
                        label = { Text("Short Break (5m)") }
                    )
                    FilterChip(
                        selected = currentMode == "Long Break",
                        onClick = { viewModel.setTimerDuration("Long Break") },
                        label = { Text("Long Break (15m)") }
                    )
                }
            }

            // Big Timer Display Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.size(190.dp),
                                strokeWidth = 10.dp,
                                color = if (currentMode == "Work") EmeraldPrimary else CyanAccent,
                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = timeFormatted,
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currentMode,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.toggleWorkTimer() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRunning) CoralAccent else EmeraldPrimary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(28.dp),
                                modifier = Modifier.height(52.dp).width(140.dp)
                            ) {
                                Icon(if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isRunning) "Pause" else "Start", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.resetWorkTimer() },
                                shape = RoundedCornerShape(28.dp),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reset")
                            }
                        }
                    }
                }
            }

            // Summary Card
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
                            Text("Total Focus Time Today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${totalWorkMinutes} minutes completed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            "${workSessions.size} Sessions",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Recent Sessions
            if (workSessions.isNotEmpty()) {
                item {
                    Text(
                        "Session History",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                items(workSessions.take(5), key = { it.id }) { session ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${session.sessionType} (${session.durationMinutes}m)", fontWeight = FontWeight.SemiBold)
                            val dateStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(session.timestamp)
                            Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
