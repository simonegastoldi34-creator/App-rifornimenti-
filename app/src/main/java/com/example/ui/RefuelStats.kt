package com.example.ui

import com.example.data.RefuelEntry

data class RefuelStats(
  val totalSpent: Double = 0.0,
  val totalLiters: Double = 0.0,
  val totalDistanceKm: Double = 0.0,
  val refuelsCount: Int = 0,
  val weightedAvgPricePerLiter: Double = 0.0,
  val avgRealLPer100Km: Double = 0.0,
  val avgRealKmPerL: Double = 0.0,
  val avgTripConsumption: Double = 0.0,
  val avgCostPerKm: Double = 0.0,
  val avgEstimatedTotalRangeKm: Double = 0.0,
  val minPricePerLiter: Double = 0.0,
  val maxPricePerLiter: Double = 0.0
) {
  companion object {
    fun calculate(entries: List<RefuelEntry>): RefuelStats {
      if (entries.isEmpty()) return RefuelStats()

      val totalSpent = entries.sumOf { it.totalCost }
      val totalLiters = entries.sumOf { it.litersRefueled }
      val totalDistance = entries.sumOf { it.distanceTraveledKm }
      val count = entries.size

      val weightedPrice = if (totalLiters > 0) totalSpent / totalLiters else 0.0

      val realL100 = if (totalDistance > 0 && totalLiters > 0) {
        (totalLiters / totalDistance) * 100.0
      } else 0.0

      val realKmL = if (totalLiters > 0) {
        totalDistance / totalLiters
      } else 0.0

      val avgTrip = if (count > 0) {
        entries.map {
          if (it.tripConsumptionUnit == "KM_PER_L" && it.tripConsumption > 0) {
            100.0 / it.tripConsumption
          } else {
            it.tripConsumption
          }
        }.average()
      } else 0.0

      val costPerKm = if (totalDistance > 0) totalSpent / totalDistance else 0.0

      val avgRange = if (count > 0) {
        entries.map { it.totalEstimatedRangeKm }.average()
      } else 0.0

      val prices = entries.map { it.pricePerLiter }.filter { it > 0.0 }
      val minPrice = prices.minOrNull() ?: 0.0
      val maxPrice = prices.maxOrNull() ?: 0.0

      return RefuelStats(
        totalSpent = totalSpent,
        totalLiters = totalLiters,
        totalDistanceKm = totalDistance,
        refuelsCount = count,
        weightedAvgPricePerLiter = weightedPrice,
        avgRealLPer100Km = realL100,
        avgRealKmPerL = realKmL,
        avgTripConsumption = avgTrip,
        avgCostPerKm = costPerKm,
        avgEstimatedTotalRangeKm = avgRange,
        minPricePerLiter = minPrice,
        maxPricePerLiter = maxPrice
      )
    }
  }
}
