package com.example.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import kotlinx.coroutines.flow.first

class ReminderCheckWorker(
  context: Context,
  params: WorkerParameters
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    return try {
      val dao = AppDatabase.getDatabase(applicationContext).reminderDao()
      val reminders = dao.getAllReminders().first()
      reminders
        .filter { !it.isCompleted && (it.isExpiringSoon || it.isExpired) }
        .forEach { reminder ->
          try {
            NotificationHelper.notifyReminder(applicationContext, reminder)
          } catch (e: SecurityException) {
            // Permesso notifiche non concesso: niente da fare, l'utente lo vedrà comunque nell'app
          }
        }
      Result.success()
    } catch (e: Exception) {
      Result.retry()
    }
  }
}
