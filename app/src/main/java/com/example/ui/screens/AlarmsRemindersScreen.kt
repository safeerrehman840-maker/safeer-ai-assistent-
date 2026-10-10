package com.example.ui.screens

import android.app.TimePickerDialog
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ReminderEntity
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.viewmodel.AssistantViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsRemindersScreen(
    viewModel: AssistantViewModel,
    onBack: () -> Unit
) {
    val reminders by viewModel.allReminders.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Alarms & Reminders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = CyanAccent,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_reminder_fab")
            ) {
                Icon(Icons.Default.AddAlarm, contentDescription = "Add Alarm/Reminder")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Alarm Reliability Notice", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Safeer schedules reminders via Android AlarmManager. For exact timing, ensure Alarm permission is granted in Android Settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (reminders.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No alarms or reminders scheduled. Tap + to add!")
                    }
                }
            }

            items(reminders, key = { it.id }) { reminder ->
                ReminderItemCard(
                    reminder = reminder,
                    onDelete = { viewModel.deleteReminder(reminder) }
                )
            }
        }
    }

    if (showDialog) {
        AddReminderDialog(
            onDismiss = { showDialog = false },
            onAdd = { title, notes, timeMillis, isAlarm, cat ->
                viewModel.addReminder(title, notes, timeMillis, isAlarm, cat)
                showDialog = false
            }
        )
    }
}

@Composable
fun ReminderItemCard(
    reminder: ReminderEntity,
    onDelete: () -> Unit
) {
    val timeFormatted = remember(reminder.triggerTime) {
        SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(reminder.triggerTime)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (reminder.isAlarm) Icons.Default.Alarm else Icons.Default.Notifications,
                contentDescription = null,
                tint = if (reminder.isAlarm) CyanAccent else EmeraldPrimary
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyanAccent
                )
                if (reminder.notes.isNotBlank()) {
                    Text(
                        text = reminder.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Long, Boolean, String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isAlarm by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf("Task") }

    val cal = remember { Calendar.getInstance().apply { add(Calendar.MINUTE, 30) } }
    var selectedTimeText by remember {
        mutableStateOf(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Alarm or Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reminder Title (e.g. Drink Water)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        val c = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                cal.set(Calendar.HOUR_OF_DAY, h)
                                cal.set(Calendar.MINUTE, m)
                                cal.set(Calendar.SECOND, 0)
                                if (cal.timeInMillis <= System.currentTimeMillis()) {
                                    cal.add(Calendar.DAY_OF_YEAR, 1)
                                }
                                selectedTimeText = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
                            },
                            c.get(Calendar.HOUR_OF_DAY),
                            c.get(Calendar.MINUTE),
                            false
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Time: $selectedTimeText")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isAlarm, onCheckedChange = { isAlarm = it })
                    Text("Treat as Full Alarm (Loud sound)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, notes, cal.timeInMillis, isAlarm, category)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
            ) {
                Text("Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
