package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvImportHelper {

  data class ParseResult(
    val entries: List<RefuelEntry>,
    val skippedRowsCount: Int,
    val errors: List<String>
  )

  private val dateFormats = listOf(
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALIAN),
    SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN),
    SimpleDateFormat("dd-MM-yyyy", Locale.ITALIAN),
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ITALIAN),
    SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN)
  )

  fun parseCsv(content: String): ParseResult {
    val lines = content.lines().filter { it.isNotBlank() }
    if (lines.isEmpty()) {
      return ParseResult(emptyList(), 0, listOf("Il file è vuoto"))
    }

    val entries = mutableListOf<RefuelEntry>()
    val errors = mutableListOf<String>()
    var skipped = 0

    // Detect delimiter: check if semicolon is more prevalent in the first non-empty line
    val firstLine = lines.first()
    val delimiter = if (firstLine.count { it == ';' } > firstLine.count { it == ',' }) ';' else ','

    for ((index, line) in lines.withIndex()) {
      val trimmed = line.trim()
      if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

      val tokens = splitCsvLine(trimmed, delimiter)
      if (tokens.isEmpty()) continue

      // Check if this is the header row
      val firstToken = tokens[0].trim().lowercase(Locale.ITALIAN)
      if (firstToken in listOf("id", "data", "date", "km", "km_percorsi", "giorno", "data rifornimento")) {
        // Skip header
        continue
      }

      val entry = parseLineToEntry(tokens, index + 1)
      if (entry != null) {
        entries.add(entry)
      } else {
        skipped++
        if (errors.size < 5) {
          errors.add("Riga ${index + 1}: dati numerici non validi o mancanti")
        }
      }
    }

    return ParseResult(entries, skipped, errors)
  }

  private fun splitCsvLine(line: String, delimiter: Char): List<String> {
    val tokens = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false

    for (char in line) {
      when {
        char == '"' -> inQuotes = !inQuotes
        char == delimiter && !inQuotes -> {
          tokens.add(sb.toString().trim())
          sb.clear()
        }
        else -> sb.append(char)
      }
    }
    tokens.add(sb.toString().trim())
    return tokens.map { it.removeSurrounding("\"").trim() }
  }

  private fun parseLineToEntry(tokens: List<String>, rowNum: Int): RefuelEntry? {
    try {
      // Determine columns based on count
      // Supported layout A (Full format - 11 or 10 columns):
      // ID (opt), Data, Km_Percorsi, Km_Rimanenti, Consumo_Cruscotto, Unita, Prezzo_Litro, Litri, Costo_Totale, Carburante, Note
      // Supported layout B (Standard format - 7 columns):
      // Data, Km_Percorsi, Km_Rimanenti, Prezzo_Litro, Litri, Costo_Totale, Consumo_Cruscotto

      val offset = if (tokens.size >= 11 && tokens[0].toLongOrNull() != null) 1 else 0

      val dateStr = tokens.getOrNull(offset) ?: return null
      val dateTimestamp = parseDate(dateStr) ?: System.currentTimeMillis()

      val distanceTraveledKm = parseDouble(tokens.getOrNull(offset + 1)) ?: return null
      val remainingDistanceKm = parseDouble(tokens.getOrNull(offset + 2)) ?: 0.0

      // Next might be consumption or price depending on schema
      val token3 = tokens.getOrNull(offset + 3)
      val token4 = tokens.getOrNull(offset + 4)
      val token5 = tokens.getOrNull(offset + 5)
      val token6 = tokens.getOrNull(offset + 6)
      val token7 = tokens.getOrNull(offset + 7)
      val token8 = tokens.getOrNull(offset + 8)
      val token9 = tokens.getOrNull(offset + 9)

      var tripConsumption = 0.0
      var tripUnit = "L_PER_100KM"
      var pricePerLiter = 0.0
      var litersRefueled = 0.0
      var totalCost = 0.0
      var fuelType = "Benzina"
      var notes = ""

      if (tokens.size >= offset + 8) {
        // Full standard export format
        tripConsumption = parseDouble(token3) ?: 0.0
        tripUnit = if (token4?.contains("km", ignoreCase = true) == true) "KM_PER_L" else "L_PER_100KM"
        pricePerLiter = parseDouble(token5) ?: 0.0
        litersRefueled = parseDouble(token6) ?: 0.0
        totalCost = parseDouble(token7) ?: (if (pricePerLiter > 0 && litersRefueled > 0) pricePerLiter * litersRefueled else 0.0)
        fuelType = token8?.ifBlank { "Benzina" } ?: "Benzina"
        notes = token9 ?: ""
      } else {
        // Minimal format: Data, Km, Prezzo, Litri, Totale
        pricePerLiter = parseDouble(token3) ?: 0.0
        litersRefueled = parseDouble(token4) ?: 0.0
        totalCost = parseDouble(token5) ?: (pricePerLiter * litersRefueled)
        tripConsumption = parseDouble(token6) ?: 0.0
      }

      if (distanceTraveledKm <= 0.0 && litersRefueled <= 0.0 && totalCost <= 0.0) {
        return null
      }

      return RefuelEntry(
        id = 0,
        dateTimestamp = dateTimestamp,
        distanceTraveledKm = distanceTraveledKm,
        remainingDistanceKm = remainingDistanceKm,
        tripConsumption = tripConsumption,
        tripConsumptionUnit = tripUnit,
        pricePerLiter = pricePerLiter,
        litersRefueled = litersRefueled,
        totalCost = totalCost,
        fuelType = fuelType,
        notes = notes
      )
    } catch (_: Exception) {
      return null
    }
  }

  private fun parseDouble(str: String?): Double? {
    if (str.isNullOrBlank()) return null
    return str.replace(',', '.').replace("€", "").replace("L", "").replace("km", "").trim().toDoubleOrNull()
  }

  private fun parseDate(str: String): Long? {
    val trimmed = str.trim()
    val asLong = trimmed.toLongOrNull()
    if (asLong != null && asLong > 100000000000L) {
      return asLong
    }
    for (sdf in dateFormats) {
      try {
        val parsed: Date? = sdf.parse(trimmed)
        if (parsed != null) return parsed.time
      } catch (_: Exception) {
      }
    }
    return null
  }
}
