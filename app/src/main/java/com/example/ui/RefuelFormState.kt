package com.example.ui

import com.example.data.RefuelEntry
import java.util.Locale

data class RefuelFormState(
  val id: Long? = null,
  val dateTimestamp: Long = System.currentTimeMillis(),
  val distanceTraveledKmStr: String = "",
  val remainingDistanceKmStr: String = "",
  val tripConsumptionStr: String = "",
  val tripConsumptionUnit: String = "L_PER_100KM",
  val pricePerLiterStr: String = "",
  val litersRefueledStr: String = "",
  val totalCostStr: String = "",
  val fuelType: String = "Benzina",
  val notes: String = "",
  val validationError: String? = null
) {
  // Live previews
  val computedCostPreview: Double?
    get() {
      val liters = litersRefueledStr.toDoubleOrNull() ?: return null
      val price = pricePerLiterStr.toDoubleOrNull() ?: return null
      return liters * price
    }

  val computedRealConsumptionKmPerL: Double?
    get() {
      val km = distanceTraveledKmStr.toDoubleOrNull() ?: return null
      val liters = litersRefueledStr.toDoubleOrNull() ?: return null
      if (km > 0 && liters > 0) return km / liters
      return null
    }

  val computedRealConsumptionLPer100Km: Double?
    get() {
      val km = distanceTraveledKmStr.toDoubleOrNull() ?: return null
      val liters = litersRefueledStr.toDoubleOrNull() ?: return null
      if (km > 0 && liters > 0) return (liters / km) * 100.0
      return null
    }

  val computedCostPerKm: Double?
    get() {
      val km = distanceTraveledKmStr.toDoubleOrNull() ?: return null
      val cost = totalCostStr.toDoubleOrNull() ?: computedCostPreview ?: return null
      if (km > 0 && cost > 0) return cost / km
      return null
    }

  fun toEntity(): RefuelEntry? {
    val distance = distanceTraveledKmStr.replace(',', '.').toDoubleOrNull() ?: return null
    val remaining = remainingDistanceKmStr.replace(',', '.').toDoubleOrNull() ?: 0.0
    val trip = tripConsumptionStr.replace(',', '.').toDoubleOrNull() ?: 0.0
    val price = pricePerLiterStr.replace(',', '.').toDoubleOrNull() ?: return null
    val liters = litersRefueledStr.replace(',', '.').toDoubleOrNull() ?: return null
    val cost = totalCostStr.replace(',', '.').toDoubleOrNull() ?: (liters * price)

    return RefuelEntry(
      id = id ?: 0,
      dateTimestamp = dateTimestamp,
      distanceTraveledKm = distance,
      remainingDistanceKm = remaining,
      tripConsumption = trip,
      tripConsumptionUnit = tripConsumptionUnit,
      pricePerLiter = price,
      litersRefueled = liters,
      totalCost = cost,
      fuelType = fuelType,
      notes = notes.trim()
    )
  }

  companion object {
    fun fromEntity(entry: RefuelEntry): RefuelFormState {
      return RefuelFormState(
        id = entry.id,
        dateTimestamp = entry.dateTimestamp,
        distanceTraveledKmStr = formatDouble(entry.distanceTraveledKm),
        remainingDistanceKmStr = formatDouble(entry.remainingDistanceKm),
        tripConsumptionStr = formatDouble(entry.tripConsumption),
        tripConsumptionUnit = entry.tripConsumptionUnit,
        pricePerLiterStr = formatDouble(entry.pricePerLiter),
        litersRefueledStr = formatDouble(entry.litersRefueled),
        totalCostStr = formatDouble(entry.totalCost),
        fuelType = entry.fuelType,
        notes = entry.notes
      )
    }

    private fun formatDouble(value: Double): String {
      return if (value == 0.0) "" else String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }
  }
}
