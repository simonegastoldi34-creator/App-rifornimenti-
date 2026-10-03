package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
  @Query("SELECT * FROM vehicle_reminders ORDER BY isCompleted ASC, dueDateTimestamp ASC")
  fun getAllReminders(): Flow<List<VehicleReminder>>

  @Query("SELECT * FROM vehicle_reminders WHERE id = :id LIMIT 1")
  suspend fun getReminderById(id: Long): VehicleReminder?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReminder(reminder: VehicleReminder): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReminders(reminders: List<VehicleReminder>)

  @Update
  suspend fun updateReminder(reminder: VehicleReminder)

  @Delete
  suspend fun deleteReminder(reminder: VehicleReminder)

  @Query("DELETE FROM vehicle_reminders WHERE id = :id")
  suspend fun deleteReminderById(id: Long)

  @Query("DELETE FROM vehicle_reminders")
  suspend fun clearAll()
}
