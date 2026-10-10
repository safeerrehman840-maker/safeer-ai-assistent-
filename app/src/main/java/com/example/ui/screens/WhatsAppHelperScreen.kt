package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.service.PhoneActionExecutor
import com.example.ui.theme.EmeraldPrimary
import com.example.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppHelperScreen(
    viewModel: AssistantViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var contactNameOrNumber by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("Assalam-o-Alaikum, main 10 minute mein aa raha hoon.") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsApp Message Helper", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF25D366))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Safe Android Intent Integration", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Safeer prepares your draft cleanly and safely opens WhatsApp. You will review and tap Send on WhatsApp yourself. No automated bypass or background spying.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = contactNameOrNumber,
                    onValueChange = { contactNameOrNumber = it },
                    label = { Text("Phone Number or Contact Name (Optional)") },
                    placeholder = { Text("e.g. +923001234567 or Ali") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Message Text (پیغام)") },
                    minLines = 4,
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
                            PhoneActionExecutor.openWhatsApp(context, contactNameOrNumber, messageText)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open & Send")
                    }

                    OutlinedButton(
                        onClick = {
                            PhoneActionExecutor.openWhatsApp(context)
                        },
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Open App")
                    }
                }
            }

            item {
                Text(
                    "Quick Voice Templates",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TemplateChip("Main 10 minute mein aa raha hoon.") { messageText = it }
                    TemplateChip("Meeting ke liye ready hain?") { messageText = it }
                    TemplateChip("Assalam-o-Alaikum, kahan hain aap?") { messageText = it }
                }
            }
        }
    }
}

@Composable
fun TemplateChip(text: String, onSelect: (String) -> Unit) {
    SuggestionChip(
        onClick = { onSelect(text) },
        label = { Text(text) },
        modifier = Modifier.fillMaxWidth()
    )
}
