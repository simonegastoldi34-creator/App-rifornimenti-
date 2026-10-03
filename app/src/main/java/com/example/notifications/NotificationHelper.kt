package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.data.VehicleReminder

object NotificationHelper {
  const val CHANNEL_ID = "scadenze_veicolo"
  private const val CHANNEL_NAME = "Scadenze veicolo"
  private const val CHANNEL_DESC = "Avvisi per bollo, assicurazione, revisione e altre scadenze"

  fun createChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
      ).apply { description = CHANNEL_DESC }
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  fun notifyReminder(context: Context, reminder: VehicleReminder) {
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle(reminder.title)
      .setContentText("${reminder.type.displayName} - ${reminder.statusLabel()}")
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)

    NotificationManagerCompat.from(context).notify(reminder.id.toInt(), builder.build())
  }
}
