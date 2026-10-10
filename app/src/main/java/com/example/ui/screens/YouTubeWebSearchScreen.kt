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
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeWebSearchScreen(
    viewModel: AssistantViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var ytQuery by remember { mutableStateOf("English speaking practice lesson") }
    var webQuery by remember { mutableStateOf("Lahore weather 7 day forecast") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("YouTube & Web Assistant", fontWeight = FontWeight.Bold) },
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
            // YouTube Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SmartDisplay, contentDescription = null, tint = Color(0xFFFF0000))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("YouTube Search & Learning", fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = ytQuery,
                            onValueChange = { ytQuery = it },
                            label = { Text("Search YouTube Query or Video Link") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { PhoneActionExecutor.openYouTube(context, ytQuery) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000), contentColor = Color.White),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open in YouTube")
                            }

                            OutlinedButton(
                                onClick = { viewModel.saveBookmark(ytQuery, "https://youtube.com", "YOUTUBE") }
                            ) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                            }
                        }
                    }
                }
            }

            // Google Chrome & Web Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chrome & Google Web Search", fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = webQuery,
                            onValueChange = { webQuery = it },
                            label = { Text("Search Google or Enter URL (https://)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { PhoneActionExecutor.openWebBrowser(context, webQuery) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Search with Chrome")
                            }

                            OutlinedButton(
                                onClick = { viewModel.saveBookmark(webQuery, webQuery, "WEB") }
                            ) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}
