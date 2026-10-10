package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "General", // "Work", "Study", "Personal", "Health", "English Practice"
    val priority: String = "Medium", // "Low", "Medium", "High"
    val dueDate: Long = System.currentTimeMillis(),
    val estimatedMinutes: Int = 30,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val isRecurring: Boolean = false,
    val recurringPeriod: String = "Daily", // "Daily", "Weekly"
    val streak: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val triggerTime: Long,
    val isCompleted: Boolean = false,
    val isAlarm: Boolean = false, // alarm vs reminder notification
    val isRecurring: Boolean = false,
    val recurringIntervalMinutes: Int = 0, // e.g. every 45 mins
    val category: String = "Reminder", // "Alarm", "Water", "Break", "Task", "Meeting"
    val linkedGoalId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "user", "assistant", "system"
    val text: String,
    val language: String = "Roman Urdu", // "Urdu", "Roman Urdu", "English"
    val actionType: String? = null, // "WHATSAPP", "EMAIL", "ALARM", "GOAL", "TIMER", "WEB", "YOUTUBE"
    val actionPayload: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "work_sessions")
data class WorkSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionType: String = "Pomodoro", // "Focus", "ShortBreak", "LongBreak"
    val durationMinutes: Int,
    val completed: Boolean = true,
    val linkedTask: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "email_drafts")
data class EmailDraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipient: String = "",
    val subject: String = "",
    val body: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_bookmarks")
data class SavedBookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val type: String = "WEB", // "WEB", "YOUTUBE"
    val timestamp: Long = System.currentTimeMillis()
)
