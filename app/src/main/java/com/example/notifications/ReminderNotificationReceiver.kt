package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderNotificationReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val action = intent.action

    if (action == Intent.ACTION_BOOT_COMPLETED) {
      // Re-schedule all active reminders on device boot
      val pendingResult = goAsync()
      CoroutineScope(Dispatchers.IO).launch {
        try {
          val db = AppDatabase.getDatabase(context)
          val allReminders = db.reminderDao().getAllReminders().first()
          val active = allReminders.filter { !it.isCompleted }
          for (reminder in active) {
            NotificationHelper.scheduleReminderNotifications(context, reminder)
          }
        } finally {
          pendingResult.finish()
        }
      }
      return
    }

    // Alarm triggered for specific reminder notification
    val notificationId = intent.getIntExtra("NOTIFICATION_ID", 1001)
    val title = intent.getStringExtra("TITLE") ?: "Scadenza Veicolo"
    val message = intent.getStringExtra("MESSAGE") ?: "Hai una scadenza imminente per la tua auto."

    NotificationHelper.showNotification(
      context = context,
      notificationId = notificationId,
      title = title,
      message = message
    )
  }
}
