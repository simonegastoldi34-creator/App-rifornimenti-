package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RefuelStats
import com.example.ui.components.ExportCsvDialog
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import java.util.Locale

@Composable
fun TripCalculatorScreen(
  stats: RefuelStats,
  onExportCsv: () -> String,
  onExportRemindersCsv: () -> String,
  onOpenImportCsv: () -> Unit,
  onClearAll: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  // Trip estimator inputs
  var tripKmStr by remember { mutableStateOf("300") }
  var budgetEuroStr by remember { mutableStateOf("50") }

  // Converter inputs
  var converterInputStr by remember { mutableStateOf("5.5") }
  var isConvertingL100ToKmL by remember { mutableStateOf(true) }

  var showClearConfirmDialog by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf(false) }

  val defaultConsumption = if (stats.avgRealLPer100Km > 0) stats.avgRealLPer100Km else 5.5
  val defaultPrice = if (stats.weightedAvgPricePerLiter > 0) stats.weightedAvgPricePerLiter else 1.78

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp)
      .testTag("calculator_screen")
  ) {
    Text(
      text = "Strumenti & Calcolatori",
      style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )
    Text(
      text = "Stime di viaggio e utilità basate sulla tua cronologia",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Tool 1: Stima Costo Viaggio
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("trip_estimator_card")
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.DirectionsCar,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier
                .padding(6.dp)
                .size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Stima Costo Viaggio",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = tripKmStr,
          onValueChange = { tripKmStr = it },
          label = { Text("Distanza prevista da percorrere") },
          suffix = { Text("km") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_trip_km")
        )

        val tripKm = tripKmStr.replace(',', '.').toDoubleOrNull() ?: 0.0
        val litersNeeded = (tripKm * defaultConsumption) / 100.0
        val costEstimated = litersNeeded * defaultPrice

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .padding(14.dp)
              .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Carburante stimato",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = String.format(Locale.ITALIAN, "%.1f Litri", litersNeeded),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Media: ${String.format(Locale.ITALIAN, "%.1f", defaultConsumption)} L/100km",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "Spesa stimata",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = String.format(Locale.ITALIAN, "€ %.2f", costEstimated),
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
              Text(
                text = "Prezzo: € ${String.format(Locale.ITALIAN, "%.3f", defaultPrice)}/L",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Tool 2: Convertitore Consumi Rapido (km/L <-> L/100km)
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("converter_card")
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = CyanAccent.copy(alpha = 0.2f),
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier
                  .padding(6.dp)
                  .size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Convertitore Consumi",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          TextButton(onClick = { isConvertingL100ToKmL = !isConvertingL100ToKmL }) {
            Text(if (isConvertingL100ToKmL) "L/100km → km/L" else "km/L → L/100km")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = converterInputStr,
          onValueChange = { converterInputStr = it },
          label = { Text(if (isConvertingL100ToKmL) "Valore in L/100km" else "Valore in km/L") },
          suffix = { Text(if (isConvertingL100ToKmL) "L/100km" else "km/L") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        val inputVal = converterInputStr.replace(',', '.').toDoubleOrNull() ?: 0.0
        val convertedVal = if (inputVal > 0) 100.0 / inputVal else 0.0

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .padding(14.dp)
              .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Risultato equivalente:",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = if (isConvertingL100ToKmL)
                String.format(Locale.ITALIAN, "%.2f km/L", convertedVal)
              else
                String.format(Locale.ITALIAN, "%.2f L/100km", convertedVal),
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = CyanAccent
              )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Tool 3: Gestione Dati (Esporta CSV e Resetta)
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("data_management_card")
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "Gestione Dati & Backup",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "Esporta la cronologia dei tuoi rifornimenti in formato CSV per consultarla su Excel o salvare un backup.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = {
              showExportDialog = true
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("export_csv_button")
          ) {
            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Esporta CSV")
          }

          Button(
            onClick = onOpenImportCsv,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer,
              contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("open_import_csv_button")
          ) {
            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Importa File")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
          onClick = { showClearConfirmDialog = true },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("clear_all_button")
        ) {
          Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Cancella Tutti i Dati")
        }
      }
    }

    Spacer(modifier = Modifier.height(80.dp))
  }

  // Export Dialog
  if (showExportDialog) {
    ExportCsvDialog(
      refuelsCsv = onExportCsv(),
      remindersCsv = onExportRemindersCsv(),
      onDismiss = { showExportDialog = false }
    )
  }

  // Clear Confirmation
  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = { Text("Cancellare tutti i rifornimenti?") },
      text = { Text("Questa azione cancellerà definitivamente tutti i record salvati.") },
      confirmButton = {
        TextButton(
          onClick = {
            onClearAll()
            showClearConfirmDialog = false
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Cancella Tutto")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("Annulla")
        }
      }
    )
  }
}
