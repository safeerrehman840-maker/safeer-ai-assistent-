package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.local.AppDatabase
import com.example.data.repository.AssistantRepository
import com.example.service.ReminderBroadcastReceiver

class SafeerApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { AssistantRepository(database) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val reminderChannel = NotificationChannel(
                ReminderBroadcastReceiver.CHANNEL_REMINDER_ID,
                "Reminders & Tasks",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Safeer daily tasks and reminder notifications"
                enableVibration(true)
            }

            val alarmChannel = NotificationChannel(
                ReminderBroadcastReceiver.CHANNEL_ALARM_ID,
                "Smart Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Safeer smart alarms and timer alerts"
                enableVibration(true)
            }

            notificationManager?.createNotificationChannel(reminderChannel)
            notificationManager?.createNotificationChannel(alarmChannel)
        }
    }
}
