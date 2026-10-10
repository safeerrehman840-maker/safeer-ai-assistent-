package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.local.EmailDraftEntity
import com.example.service.PhoneActionExecutor
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailAssistantScreen(
    viewModel: AssistantViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val drafts by viewModel.emailDrafts.collectAsStateWithLifecycle()

    var recipient by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Leave Application / Class Absence") }
    var body by remember {
        mutableStateOf("Respected Teacher / Manager,\n\nI am writing to inform you that I will be unable to attend tomorrow's class/session due to an urgent personal commitment. I will catch up with the missed coursework.\n\nBest regards,\nSafeer User")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Email Composer & Drafts", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    label = { Text("Recipient Email (e.g. teacher@university.edu)") },
                    leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    leadingIcon = { Icon(Icons.Default.Subject, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Email Body (Drafted by Safeer)") },
                    minLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            PhoneActionExecutor.openEmailComposer(context, recipient, subject, body)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Email Client")
                    }
                }
            }

            if (drafts.isNotEmpty()) {
                item {
                    Text(
                        "Saved Drafts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(drafts, key = { it.id }) { draft ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(draft.subject, fontWeight = FontWeight.Bold)
                            Text(draft.recipient, style = MaterialTheme.typography.bodySmall, color = CyanAccent)
                            Text(draft.body.take(80) + "...", style = MaterialTheme.typography.bodySmall)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = {
                                    recipient = draft.recipient
                                    subject = draft.subject
                                    body = draft.body
                                }) {
                                    Text("Load Draft")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
