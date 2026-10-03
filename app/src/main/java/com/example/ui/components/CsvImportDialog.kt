package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CsvImportHelper
import com.example.data.RefuelEntry
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvImportDialog(
  onParseContent: (String) -> CsvImportHelper.ParseResult,
  onParseUri: (Uri) -> CsvImportHelper.ParseResult,
  onConfirmImport: (List<RefuelEntry>) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var rawCsvText by remember { mutableStateOf("") }
  var parseResult by remember { mutableStateOf<CsvImportHelper.ParseResult?>(null) }
  var selectedFileName by remember { mutableStateOf<String?>(null) }

  // File picker launcher
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri: Uri? ->
    uri?.let {
      selectedFileName = it.lastPathSegment ?: "file_selezionato.csv"
      val result = onParseUri(it)
      parseResult = result
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Scaffold(
      modifier = Modifier
        .fillMaxSize()
        .testTag("csv_import_dialog"),
      topBar = {
        TopAppBar(
          title = {
            Text("Importa Rifornimenti da File", fontWeight = FontWeight.Bold)
          },
          navigationIcon = {
            IconButton(onClick = onDismiss) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Chiudi")
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
          )
        )
      }
    ) { innerPadding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        Text(
          text = "Seleziona un file CSV o incolla i dati per importare più rifornimenti contemporaneamente nel database.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Option 1: File Picker
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.UploadFile,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier
                    .padding(6.dp)
                    .size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Seleziona file dal dispositivo",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                  text = "Supporta formati .csv e .txt con virgola o punto e virgola",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
              onClick = {
                filePickerLauncher.launch(arrayOf("text/*", "*/*"))
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("choose_csv_file_button")
            ) {
              Icon(imageVector = Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Scegli File CSV")
            }

            selectedFileName?.let { name ->
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "File aperto: $name",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = CyanAccent
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Option 2: Paste CSV text
        Text(
          text = "Oppure incolla il testo CSV",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
          value = rawCsvText,
          onValueChange = {
            rawCsvText = it
            if (it.isNotBlank()) {
              parseResult = onParseContent(it)
            }
          },
          label = { Text("Contenuto CSV (Data, Km, Litri, Prezzo, Costo...)") },
          placeholder = {
            Text("Data,Km_Percorsi,Km_Rimanenti,Consumo_Cruscotto,Prezzo_Litro,Litri,Costo_Totale\n01/10/2026,550.0,80.0,5.2,1.789,32.5,58.14")
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .testTag("paste_csv_field")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(
            onClick = {
              val sampleCsv = """
Data,Km_Percorsi,Km_Rimanenti,Consumo_Cruscotto,Unita_Consumo,Prezzo_Litro,Litri,Costo_Totale,Carburante,Note
15/09/2026,580.0,90.0,5.3,L_PER_100KM,1.799,33.0,59.37,Benzina,Pieno Lavoro
24/09/2026,620.0,75.0,5.1,L_PER_100KM,1.759,34.5,60.69,Benzina,Self Service
01/10/2026,590.0,105.0,5.4,L_PER_100KM,1.779,33.8,60.13,Benzina,Eni Station
              """.trimIndent()
              rawCsvText = sampleCsv
              parseResult = onParseContent(sampleCsv)
            }
          ) {
            Text("Carica esempio CSV")
          }

          if (rawCsvText.isNotBlank()) {
            TextButton(onClick = {
              rawCsvText = ""
              parseResult = null
              selectedFileName = null
            }) {
              Text("Pulisci")
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Parse Results Preview
        parseResult?.let { result ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (result.entries.isNotEmpty())
              EmeraldGreen.copy(alpha = 0.12f)
            else
              MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("parse_result_card")
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (result.entries.isNotEmpty()) Icons.Default.Check else Icons.Default.Info,
                  contentDescription = null,
                  tint = if (result.entries.isNotEmpty()) EmeraldGreen else MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (result.entries.isNotEmpty())
                    "Pronti per l'importazione: ${result.entries.size} rifornimenti"
                  else
                    "Nessun rifornimento valido rilevato",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = if (result.entries.isNotEmpty()) EmeraldGreen else MaterialTheme.colorScheme.error
                )
              }

              if (result.skippedRowsCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${result.skippedRowsCount} righe ignorate o non conformi.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              if (result.entries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                  text = "Anteprima primi record trovati:",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(6.dp))
                result.entries.take(4).forEach { entry ->
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 3.dp)
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(
                        text = "${entry.formattedDateShort()} • ${entry.distanceTraveledKm} km • ${entry.litersRefueled} L",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                      )
                      Text(
                        text = String.format(Locale.ITALIAN, "€ %.2f", entry.totalCost),
                        style = MaterialTheme.typography.bodySmall.copy(
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.primary
                        )
                      )
                    }
                  }
                }
              }

              if (result.errors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                result.errors.forEach { err ->
                  Text(
                    text = "• $err",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                  )
                }
              }
            }
          }

          if (result.entries.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
              onClick = {
                onConfirmImport(result.entries)
                onDismiss()
              },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("confirm_import_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
              )
            ) {
              Icon(imageVector = Icons.Default.Check, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Conferma e Salva ${result.entries.size} Rifornimenti",
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(30.dp))
      }
    }
  }
}
