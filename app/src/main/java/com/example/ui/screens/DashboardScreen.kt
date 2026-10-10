package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AnimatedAssistantOrb
import com.example.ui.components.AssistantStatus
import com.example.ui.theme.*
import com.example.viewmodel.AssistantViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: AssistantViewModel,
    onNavigate: (String) -> Unit
) {
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()
    val reminders by viewModel.allReminders.collectAsStateWithLifecycle()
    val status by viewModel.assistantStatus.collectAsStateWithLifecycle()
    val statusMsg by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isListening by viewModel.speechManager.isListening.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.timerSecondsLeft.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val timerMode by viewModel.timerMode.collectAsStateWithLifecycle()

    val pendingGoals = goals.filter { !it.isCompleted }
    val completedCount = goals.count { it.isCompleted }
    val progress = if (goals.isNotEmpty()) completedCount.toFloat() / goals.size else 0f

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            // Header Hero Banner with Emerald Neon Glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                CardNavyLight.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Safeer AI Assistant",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )
                            )
                            Text(
                                text = todayFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onNavigate("settings") },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = EmeraldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Animated Assistant Orb Centerpiece
                    AnimatedAssistantOrb(
                        status = status,
                        size = 170.dp,
                        onClick = {
                            if (isListening) viewModel.stopListening() else viewModel.startListening()
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when (status) {
                            AssistantStatus.LISTENING -> EmeraldPrimary.copy(alpha = 0.2f)
                            AssistantStatus.THINKING -> CyanAccent.copy(alpha = 0.2f)
                            AssistantStatus.WORKING -> AmberAccent.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (status) {
                                            AssistantStatus.LISTENING -> EmeraldPrimary
                                            AssistantStatus.THINKING -> CyanAccent
                                            AssistantStatus.WORKING -> AmberAccent
                                            else -> EmeraldLight
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusMsg,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Action Speech/Text row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (isListening) viewModel.stopListening() else viewModel.startListening()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isListening) CoralAccent else EmeraldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.testTag("voice_listen_button")
                        ) {
                            Icon(
                                if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Voice Input"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isListening) "Listening..." else "Tap to Speak (بولیں)")
                        }

                        OutlinedButton(
                            onClick = { onNavigate("chat") },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = EmeraldLight
                            )
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "Chat Assistant")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Chat")
                        }
                    }
                }
            }
        }

        // Section: Today's Goals Progress
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrackChanges, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Today's Goals Progress",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Text(
                            "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldPrimary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${completedCount} of ${goals.size} tasks finished",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { onNavigate("goals") }) {
                            Text("Manage Goals", color = CyanAccent)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = CyanAccent)
                        }
                    }
                }
            }
        }

        // Section: Work Timer & Pomodoro Quick Widget
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Work & Focus Mode",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        val mins = timerSeconds / 60
                        val secs = timerSeconds % 60
                        Text(
                            text = String.format("%02d:%02d (%s)", mins, secs, timerMode),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.toggleWorkTimer() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isTimerRunning) CoralAccent else EmeraldPrimary)
                        ) {
                            Icon(
                                if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Toggle Timer",
                                tint = Color.Black
                            )
                        }
                        IconButton(
                            onClick = { onNavigate("timer") },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Open Timer", tint = CyanAccent)
                        }
                    }
                }
            }
        }

        // Section: Quick Assistant Actions Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    text = "Quick Assistant Actions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "WhatsApp",
                        subtitle = "Send & Draft",
                        icon = Icons.Default.Message,
                        accentColor = Color(0xFF25D366),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("whatsapp") }
                    )
                    QuickActionCard(
                        title = "Email",
                        subtitle = "Compose Draft",
                        icon = Icons.Default.Email,
                        accentColor = Color(0xFFEA4335),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("email") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "YouTube",
                        subtitle = "Search Lessons",
                        icon = Icons.Default.SmartDisplay,
                        accentColor = Color(0xFFFF0000),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("youtube_web") }
                    )
                    QuickActionCard(
                        title = "Alarms",
                        subtitle = "Smart Reminders",
                        icon = Icons.Default.Alarm,
                        accentColor = AmberAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("alarms") }
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
