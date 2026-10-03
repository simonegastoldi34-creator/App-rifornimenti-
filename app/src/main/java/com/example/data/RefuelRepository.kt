package com.example.data

import kotlinx.coroutines.flow.Flow

class RefuelRepository(private val dao: RefuelDao) {
  val allEntries: Flow<List<RefuelEntry>> = dao.getAllEntries()

  suspend fun getEntryById(id: Long): RefuelEntry? = dao.getEntryById(id)

  suspend fun insert(entry: RefuelEntry): Long = dao.insertEntry(entry)

  suspend fun update(entry: RefuelEntry) = dao.updateEntry(entry)

  suspend fun delete(entry: RefuelEntry) = dao.deleteEntry(entry)

  suspend fun deleteById(id: Long) = dao.deleteEntryById(id)

  suspend fun clearAll() = dao.clearAll()
}
