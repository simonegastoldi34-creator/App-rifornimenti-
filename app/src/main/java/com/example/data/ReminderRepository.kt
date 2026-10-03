package com.example.data

import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val dao: ReminderDao) {
  val allReminders: Flow<List<VehicleReminder>> = dao.getAllReminders()

  suspend fun getById(id: Long): VehicleReminder? = dao.getReminderById(id)

  suspend fun insert(reminder: VehicleReminder): Long = dao.insertReminder(reminder)

  suspend fun insertAll(reminders: List<VehicleReminder>) = dao.insertReminders(reminders)

  suspend fun update(reminder: VehicleReminder) = dao.updateReminder(reminder)

  suspend fun delete(reminder: VehicleReminder) = dao.deleteReminder(reminder)

  suspend fun deleteById(id: Long) = dao.deleteReminderById(id)

  suspend fun clearAll() = dao.clearAll()
}
