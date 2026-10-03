package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RefuelDao {
  @Query("SELECT * FROM refuel_entries ORDER BY dateTimestamp DESC")
  fun getAllEntries(): Flow<List<RefuelEntry>>

  @Query("SELECT * FROM refuel_entries WHERE id = :id LIMIT 1")
  suspend fun getEntryById(id: Long): RefuelEntry?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEntry(entry: RefuelEntry): Long

  @Update
  suspend fun updateEntry(entry: RefuelEntry)

  @Delete
  suspend fun deleteEntry(entry: RefuelEntry)

  @Query("DELETE FROM refuel_entries WHERE id = :id")
  suspend fun deleteEntryById(id: Long)

  @Query("DELETE FROM refuel_entries")
  suspend fun clearAll()
}
