package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "refuel_entries")
data class RefuelEntry(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val dateTimestamp: Long = System.currentTimeMillis(),
  val distanceTraveledKm: Double,       // Distanza percorsa dall'ultimo rifornimento (km)
  val remainingDistanceKm: Double,      // Distanza rimanente / autonomia (km)
  val tripConsumption: Double,         // Consumo medio segnato dal computer di bordo
  val tripConsumptionUnit: String = "L_PER_100KM", // "L_PER_100KM" o "KM_PER_L"
  val pricePerLiter: Double,            // Prezzo al litro (€/L)
  val litersRefueled: Double,           // Litri riforniti (L)
  val totalCost: Double,                // Costo totale allo scontrino (€)
  val fuelType: String = "Benzina",     // Benzina, Diesel, GPL, Metano, Ibrido, Altro
  val notes: String = ""                // Note / Stazione di servizio
) {
  // Calcolo consumo effettivo reale basato sui litri riforniti e distanza percorsa
  val realConsumptionKmPerL: Double?
    get() = if (litersRefueled > 0.0 && distanceTraveledKm > 0.0) {
      distanceTraveledKm / litersRefueled
    } else null

  val realConsumptionLPer100Km: Double?
    get() = if (litersRefueled > 0.0 && distanceTraveledKm > 0.0) {
      (litersRefueled / distanceTraveledKm) * 100.0
    } else null

  // Costo per km (€/km)
  val costPerKm: Double?
    get() = if (distanceTraveledKm > 0.0 && totalCost > 0.0) {
      totalCost / distanceTraveledKm
    } else null

  // Autonomia totale stimata per questo pieno (km percorsi + rimanenti)
  val totalEstimatedRangeKm: Double
    get() = distanceTraveledKm + remainingDistanceKm

  // Differenza tra consumo effettivo e quello del computer di bordo (in L/100km)
  val consumptionDifferenceLPer100Km: Double?
    get() {
      val real = realConsumptionLPer100Km ?: return null
      val tripL100 = if (tripConsumptionUnit == "KM_PER_L") {
        if (tripConsumption > 0) 100.0 / tripConsumption else null
      } else {
        tripConsumption
      } ?: return null
      return real - tripL100
    }

  fun formattedDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ITALIAN)
    return sdf.format(Date(dateTimestamp))
  }

  fun formattedDateShort(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN)
    return sdf.format(Date(dateTimestamp))
  }
}
