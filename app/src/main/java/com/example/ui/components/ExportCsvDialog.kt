package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExportCsvDialog(
  refuelsCsv: String,
  remindersCsv: String,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var selectedExportType by remember { mutableStateOf("Rifornimenti") }

  val currentCsvContent = if (selectedExportType == "Rifornimenti") refuelsCsv else remindersCsv
  val defaultFileName = if (selectedExportType == "Rifornimenti") {
    "rifornimenti_auto_${SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())}.csv"
  } else {
    "scadenze_manutenzioni_${SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())}.csv"
  }

  // System file creator launcher (SAF - Storage Access Framework)
  val createDocumentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("text/csv")
  ) { uri: Uri? ->
    uri?.let { fileUri ->
      try {
        context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
          outputStream.write(currentCsvContent.toByteArray(Charsets.UTF_8))
        }
        Toast.makeText(context, "File CSV salvato correttamente!", Toast.LENGTH_LONG).show()
        onDismiss()
      } catch (e: Exception) {
        Toast.makeText(context, "Errore salvataggio: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("export_csv_dialog"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.FileDownload,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Esporta in File CSV", fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onDismiss) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Chiudi")
        }
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Puoi salvare il file CSV direttamente nella memoria del telefono (compatibile con Microsoft Excel, Google Fogli e LibreOffice), oppure condividerlo con altre app.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Selector: Rifornimenti vs Scadenze
        Text(
          text = "Cosa vuoi esportare:",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = selectedExportType == "Rifornimenti",
            onClick = { selectedExportType = "Rifornimenti" },
            label = { Text("Rifornimenti") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.testTag("chip_export_refuels")
          )

          FilterChip(
            selected = selectedExportType == "Scadenze",
            onClick = { selectedExportType = "Scadenze" },
            label = { Text("Scadenze & Tagliandi") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.testTag("chip_export_reminders")
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preview box
        Text(
          text = "Anteprima contenuto CSV:",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
        ) {
          Text(
            text = currentCsvContent.ifBlank { "Nessun dato disponibile da esportare." },
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            modifier = Modifier.padding(8.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action 1: Salva come file CSV
        Button(
          onClick = {
            createDocumentLauncher.launch(defaultFileName)
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("save_csv_file_button")
        ) {
          Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Salva come File CSV (.csv)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action 2: Condividi File tramite app
        OutlinedButton(
          onClick = {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
              type = "text/csv"
              putExtra(Intent.EXTRA_SUBJECT, "Esportazione ${if (selectedExportType == "Rifornimenti") "Rifornimenti Auto" else "Scadenze Auto"}")
              putExtra(Intent.EXTRA_TEXT, currentCsvContent)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Condividi file CSV tramite"))
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("share_csv_button")
        ) {
          Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Condividi CSV (WhatsApp, Email...)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action 3: Copia negli appunti
        TextButton(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Rifornimenti CSV", currentCsvContent)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Dati CSV copiati negli appunti!", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("copy_csv_clipboard_button")
        ) {
          Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Copia testo negli appunti")
        }
      }
    },
    confirmButton = {},
    dismissButton = {}
  )
}
