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
import com.example.data.VehicleReminder
import java.util.Calendar

object NotificationHelper {
  const val CHANNEL_ID = "scadenze_veicolo"
  private const val CHANNEL_NAME = "Scadenze veicolo"
  private const val CHANNEL_DESC = "Avvisi per bollo, assicurazione, revisione e altre scadenze"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
      ).apply { description = CHANNEL_DESC }
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  private fun monthsBefore(dueDateTimestamp: Long, months: Int): Long {
    val cal = Calendar.getInstance().apply {
      timeInMillis = dueDateTimestamp
      add(Calendar.MONTH, -months)
    }
    return cal.timeInMillis
  }

  /** Programma gli avvisi per una scadenza: uno due mesi prima e uno un mese prima. */
  fun scheduleReminderNotifications(context: Context, reminder: VehicleReminder) {
    if (reminder.isCompleted) {
      cancelReminderNotifications(context, reminder)
      return
    }
    val now = System.currentTimeMillis()
    val label = reminder.type.displayName.lowercase()

    val twoMonthsBefore = monthsBefore(reminder.dueDateTimestamp, 2)
    if (twoMonthsBefore > now) {
      schedule(
        context, reminder,
        offsetCode = 1,
        triggerAt = twoMonthsBefore,
        message = "Tra due mesi: $label."
      )
    }

    val oneMonthBefore = monthsBefore(reminder.dueDateTimestamp, 1)
    if (oneMonthBefore > now) {
      schedule(
        context, reminder,
        offsetCode = 2,
        triggerAt = oneMonthBefore,
        message = "Tra un mese: $label."
      )
    }
  }

  fun cancelReminderNotifications(context: Context, reminder: VehicleReminder) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    listOf(1, 2).forEach { offsetCode ->
      val pendingIntent = buildPendingIntent(context, reminder, offsetCode, create = false)
      pendingIntent?.let { alarmManager.cancel(it) }
    }
  }

  private fun schedule(
    context: Context,
    reminder: VehicleReminder,
    offsetCode: Int,
    triggerAt: Long,
    message: String
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pendingIntent = buildPendingIntent(context, reminder, offsetCode, create = true, message = message)
      ?: return

    val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    try {
      if (canExact) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
      } else {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
      }
    } catch (e: SecurityException) {
      alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }
  }

  private fun buildPendingIntent(
    context: Context,
    reminder: VehicleReminder,
    offsetCode: Int,
    create: Boolean,
    message: String = ""
  ): PendingIntent? {
    val requestCode = (reminder.id.toInt() * 10) + offsetCode
    val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
      putExtra("NOTIFICATION_ID", requestCode)
      putExtra("TITLE", reminder.title)
      putExtra("MESSAGE", message)
    }
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE or
      if (!create) PendingIntent.FLAG_NO_CREATE else 0
    return PendingIntent.getBroadcast(context, requestCode, intent, flags)
  }

  fun showNotification(context: Context, notificationId: Int, title: String, message: String) {
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle(title)
      .setContentText(message)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)

    try {
      NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    } catch (e: SecurityException) {
      // Permesso notifiche non concesso
    }
  }
}
