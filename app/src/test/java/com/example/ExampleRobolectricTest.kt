package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CsvImportHelper
import com.example.data.RefuelEntry
import com.example.data.ReminderType
import com.example.data.VehicleReminder
import com.example.ui.RefuelStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Rifornimenti", appName)
  }

  @Test
  fun `test refuel calculations`() {
    val entry = RefuelEntry(
      distanceTraveledKm = 500.0,
      remainingDistanceKm = 100.0,
      tripConsumption = 5.0,
      pricePerLiter = 1.80,
      litersRefueled = 25.0,
      totalCost = 45.0
    )

    // Real consumption: 500 km / 25 L = 20 km/L
    assertEquals(20.0, entry.realConsumptionKmPerL ?: 0.0, 0.001)
    // Real L/100km: (25 / 500) * 100 = 5.0 L/100km
    assertEquals(5.0, entry.realConsumptionLPer100Km ?: 0.0, 0.001)
    // Cost per km: 45 / 500 = 0.09 €/km
    assertEquals(0.09, entry.costPerKm ?: 0.0, 0.001)
    // Total estimated range: 500 + 100 = 600 km
    assertEquals(600.0, entry.totalEstimatedRangeKm, 0.001)

    val stats = RefuelStats.calculate(listOf(entry))
    assertEquals(45.0, stats.totalSpent, 0.001)
    assertEquals(25.0, stats.totalLiters, 0.001)
    assertEquals(500.0, stats.totalDistanceKm, 0.001)
    assertEquals(1.80, stats.weightedAvgPricePerLiter, 0.001)
  }

  @Test
  fun `test csv import parsing`() {
    val csvContent = """
      Data,Km_Percorsi,Km_Rimanenti,Consumo_Cruscotto,Unita_Consumo,Prezzo_Litro,Litri,Costo_Totale,Carburante,Note
      10/09/2026,520.0,80.0,5.4,L_PER_100KM,1.78,30.0,53.4,Benzina,Eni
      20/09/2026,600.0,90.0,5.1,L_PER_100KM,1.75,32.0,56.0,Benzina,Q8
    """.trimIndent()

    val result = CsvImportHelper.parseCsv(csvContent)
    assertEquals(2, result.entries.size)
    assertEquals(520.0, result.entries[0].distanceTraveledKm, 0.01)
    assertEquals(30.0, result.entries[0].litersRefueled, 0.01)
    assertEquals(53.4, result.entries[0].totalCost, 0.01)
  }

  @Test
  fun `test vehicle reminder status`() {
    val now = System.currentTimeMillis()
    val day = 86400000L

    val bolloFuture = VehicleReminder(
      title = "Bollo Auto",
      type = ReminderType.BOLLO,
      dueDateTimestamp = now + day * 15 // in 15 days
    )
    assertTrue(bolloFuture.isExpiringSoon)
    assertFalse(bolloFuture.isExpired)

    val tagliandoPast = VehicleReminder(
      title = "Ultimo Tagliando",
      type = ReminderType.TAGLIANDO,
      dueDateTimestamp = now - day * 60,
      isCompleted = true
    )
    assertFalse(tagliandoPast.isExpired) // completed so not flagged as expired
    assertEquals("Completato", tagliandoPast.statusLabel())
  }
}
