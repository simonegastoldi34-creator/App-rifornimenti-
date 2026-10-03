package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class ReminderType(val displayName: String) {
  BOLLO("Scadenza Bollo"),
  ASSICURAZIONE("Scadenza Assicurazione"),
  TAGLIANDO("Ultimo / Prossimo Tagliando"),
  REVISIONE("Revisione Ministeriale"),
  GOMME("Cambio Gomme"),
  ALTRO("Altro Promemoria")
}

@Entity(tableName = "vehicle_reminders")
data class VehicleReminder(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val type: ReminderType = ReminderType.BOLLO,
  val dueDateTimestamp: Long,
  val kmThreshold: Double? = null,
  val cost: Double? = null,
  val notes: String = "",
  val isCompleted: Boolean = false
) {
  val daysRemaining: Long
    get() {
      val diff = dueDateTimestamp - System.currentTimeMillis()
      return TimeUnit.MILLISECONDS.toDays(diff)
    }

  val isExpired: Boolean
    get() = daysRemaining < 0 && !isCompleted

  val isExpiringSoon: Boolean
    get() = daysRemaining in 0..30 && !isCompleted

  fun formattedDueDate(): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.ITALIAN)
    return sdf.format(Date(dueDateTimestamp))
  }

  fun statusLabel(): String {
    if (isCompleted) return "Completato"
    val days = daysRemaining
    return when {
      days < 0 -> "Scaduto da ${-days} giorni"
      days == 0L -> "Scade oggi!"
      days == 1L -> "Scade domani!"
      else -> "Tra $days giorni"
    }
  }
}
