package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Safeer Assistant Reminder"
        val notes = intent.getStringExtra(EXTRA_NOTES) ?: "Time for your scheduled task!"
        val isAlarm = intent.getBooleanExtra(EXTRA_IS_ALARM, false)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannels(notificationManager)

        val channelId = if (isAlarm) CHANNEL_ALARM_ID else CHANNEL_REMINDER_ID

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_SCREEN", "alarms")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSound = RingtoneManager.getDefaultUri(
            if (isAlarm) RingtoneManager.TYPE_ALARM else RingtoneManager.TYPE_NOTIFICATION
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(notes)
            .setPriority(if (isAlarm) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setCategory(if (isAlarm) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            .setSound(alarmSound)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(reminderId.toInt().coerceAtLeast(100), notification)
    }

    private fun createChannels(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER_ID,
                "Reminders & Tasks",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for daily reminders, goal checks, and hydration"
                enableVibration(true)
            }

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM_ID,
                "Smart Alarms & Timers",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for smart waking and Pomodoro completion alarms"
                enableVibration(true)
            }

            manager.createNotificationChannel(reminderChannel)
            manager.createNotificationChannel(alarmChannel)
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_NOTES = "notes"
        const val EXTRA_IS_ALARM = "is_alarm"

        const val CHANNEL_REMINDER_ID = "safeer_reminders_channel"
        const val CHANNEL_ALARM_ID = "safeer_alarms_channel"
    }
}
