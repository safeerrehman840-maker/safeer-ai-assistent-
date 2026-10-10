package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AssistantAction
import com.example.data.local.ChatMessageEntity
import com.example.ui.components.AnimatedAssistantOrb
import com.example.ui.components.AssistantStatus
import com.example.ui.theme.*
import com.example.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantChatScreen(
    viewModel: AssistantViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val status by viewModel.assistantStatus.collectAsStateWithLifecycle()
    val isListening by viewModel.speechManager.isListening.collectAsStateWithLifecycle()
    val pendingAction by viewModel.pendingAction.collectAsStateWithLifecycle()
    val currentLang by viewModel.preferredLanguage.collectAsStateWithLifecycle()
    val ttsEnabled by viewModel.ttsEnabled.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedAssistantOrb(status = status, size = 36.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Safeer AI Voice & Chat", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "Urdu • Roman Urdu • English",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.ttsEnabled.value = !ttsEnabled }) {
                        Icon(
                            if (ttsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle TTS",
                            tint = if (ttsEnabled) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Clear Chat")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Pending Action Confirmation Banner
                AnimatedVisibility(visible = pendingAction != null) {
                    pendingAction?.let { action ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardNavyLight)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Proposed Action: ${action::class.simpleName}",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = EmeraldLight)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatActionSummary(action),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimaryDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { viewModel.dismissPendingAction() }) {
                                        Text("Cancel", color = CoralAccent)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.executePendingAction(action) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
                                    ) {
                                        Text("Review & Execute")
                                    }
                                }
                            }
                        }
                    }
                }

                // Chat Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (isListening) viewModel.stopListening() else viewModel.startListening()
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isListening) CoralAccent else EmeraldPrimary)
                            .testTag("chat_mic_button")
                    ) {
                        Icon(
                            if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_text_input"),
                        placeholder = { Text("Ask Safeer in Urdu or English...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val msg = inputText
                                inputText = ""
                                viewModel.processUserInput(msg)
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) CyanAccent else MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedAssistantOrb(status = status, size = 120.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Assalam-o-Alaikum! Main Safeer Assistant hoon.",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldLight)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try saying:\n• \"Kal subah 7 baje alarm lagao\"\n• \"WhatsApp par message likh do\"\n• \"YouTube par Python tutorial chalao\"\n• \"Mere aaj ke goals complete hue ya nahi?\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(message = msg)
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessageEntity) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text("S", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.Black else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

fun formatActionSummary(action: AssistantAction): String {
    return when (action) {
        is AssistantAction.SendWhatsApp -> "Prepare WhatsApp message for ${action.contactName ?: "recipient"}: \"${action.message}\""
        is AssistantAction.DraftEmail -> "Draft Email to: ${action.recipient ?: "specified contact"} with subject \"${action.subject ?: "Draft"}\""
        is AssistantAction.SetAlarm -> "Set system alarm for ${action.hour}:${String.format("%02d", action.minute)} (${action.label})"
        is AssistantAction.SetReminder -> "Set reminder: \"${action.title}\" in ${action.delayMinutes} minutes"
        is AssistantAction.CreateGoal -> "Add daily goal: \"${action.title}\" (${action.category})"
        is AssistantAction.SearchYouTube -> "Open YouTube and search: \"${action.query}\""
        is AssistantAction.SearchWeb -> "Open Google / Chrome search for: \"${action.query}\""
        is AssistantAction.OpenApp -> "Launch application: ${action.appTarget}"
    }
}
