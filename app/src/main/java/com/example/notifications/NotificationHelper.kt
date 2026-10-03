package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.VehicleReminder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NotificationHelper {
  const val CHANNEL_ID = "vehicle_reminders_channel"
  private const val CHANNEL_NAME = "Scadenze e Manutenzione Auto"
  private const val CHANNEL_DESC = "Avvisi per scadenza bollo, assicurazione, tagliandi e revisione"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
        description = CHANNEL_DESC
        enableLights(true)
        enableVibration(true)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun showNotification(
    context: Context,
    notificationId: Int,
    title: String,
    message: String
  ) {
    createNotificationChannel(context)

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)

    val manager = NotificationManagerCompat.from(context)
    try {
      manager.notify(notificationId, builder.build())
    } catch (_: SecurityException) {
      // Catch missing POST_NOTIFICATIONS permission gracefully
    }
  }

  fun showInstantTestNotification(context: Context) {
    showNotification(
      context = context,
      notificationId = 99999,
      title = "🚗 Test Notifiche Scadenze Auto",
      message = "Il sistema di avvisi è attivo! Riceverai un promemoria 2 mesi prima e 1 mese prima di ogni scadenza (bollo, assicurazione, tagliando)."
    )
  }

  /**
   * Schedules notifications for a reminder:
   * - 60 days before (approx. 2 months)
   * - 30 days before (approx. 1 month)
   * - 7 days before (1 week)
   * - Day of deadline at 09:00
   */
  fun scheduleReminderNotifications(context: Context, reminder: VehicleReminder) {
    if (reminder.isCompleted) {
      cancelReminderNotifications(context, reminder.id)
      return
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val now = System.currentTimeMillis()
    val due = reminder.dueDateTimestamp
    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN).format(Date(due))

    // Interval definitions: Pair(offsetDays, label)
    val intervals = listOf(
      60L to "Mancano 2 mesi alla scadenza di",
      30L to "Manca 1 mese alla scadenza di",
      7L to "Manca solo 1 settimana alla scadenza di",
      0L to "Oggi scade"
    )

    intervals.forEachIndexed { index, (daysBefore, label) ->
      val triggerAtMillis = due - (daysBefore * 86400000L)

      if (triggerAtMillis > now) {
        val requestCode = generateRequestCode(reminder.id, index)
        val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
          putExtra("NOTIFICATION_ID", requestCode)
          putExtra("TITLE", "Promemoria: ${reminder.title}")
          putExtra(
            "MESSAGE",
            "$label ${reminder.title} (data prevista: $dateStr). ${if (reminder.notes.isNotBlank()) "Note: ${reminder.notes}" else ""}".trim()
          )
        }

        val pendingIntent = PendingIntent.getBroadcast(
          context,
          requestCode,
          intent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
          } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
          }
        } catch (_: SecurityException) {
          // In case exact alarm permission is not available, fallback to inexact
          alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
      }
    }
  }

  fun cancelReminderNotifications(context: Context, reminderId: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    for (index in 0..3) {
      val requestCode = generateRequestCode(reminderId, index)
      val intent = Intent(context, ReminderNotificationReceiver::class.java)
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
      )
      if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
      }
    }
  }

  private fun generateRequestCode(reminderId: Long, offsetIndex: Int): Int {
    return ((reminderId % 100000) * 10 + offsetIndex).toInt()
  }
}
