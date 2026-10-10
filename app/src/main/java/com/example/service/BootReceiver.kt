package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val db = AppDatabase.getInstance(context)
            val scheduler = AlarmSchedulerHelper(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeReminders = db.reminderDao().getActiveReminders().first()
                    val now = System.currentTimeMillis()
                    for (reminder in activeReminders) {
                        if (reminder.triggerTime > now) {
                            scheduler.scheduleReminder(reminder)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
