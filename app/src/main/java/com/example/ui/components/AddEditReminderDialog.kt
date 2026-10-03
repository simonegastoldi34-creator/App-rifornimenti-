package com.example.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ReminderType
import com.example.data.VehicleReminder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderDialog(
  initialReminder: VehicleReminder?,
  onSave: (VehicleReminder) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedType by remember { mutableStateOf(initialReminder?.type ?: ReminderType.BOLLO) }
  var title by remember {
    mutableStateOf(initialReminder?.title ?: selectedType.displayName)
  }
  var dueDateTimestamp by remember {
    mutableStateOf(initialReminder?.dueDateTimestamp ?: System.currentTimeMillis())
  }
  var kmStr by remember {
    mutableStateOf(initialReminder?.kmThreshold?.let { String.format(Locale.US, "%.0f", it) } ?: "")
  }
  var costStr by remember {
    mutableStateOf(initialReminder?.cost?.let { String.format(Locale.US, "%.2f", it) } ?: "")
  }
  var notes by remember { mutableStateOf(initialReminder?.notes ?: "") }
  var isCompleted by remember { mutableStateOf(initialReminder?.isCompleted ?: false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  var showDatePicker by remember { mutableStateOf(false) }
  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateTimestamp)

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Scaffold(
      modifier = Modifier
        .fillMaxSize()
        .testTag("add_edit_reminder_dialog"),
      topBar = {
        TopAppBar(
          title = {
            Text(
              text = if (initialReminder != null) "Modifica Scadenza" else "Nuova Scadenza / Tagliando",
              fontWeight = FontWeight.Bold
            )
          },
          navigationIcon = {
            IconButton(onClick = onDismiss) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Chiudi")
            }
          },
          actions = {
            Button(
              onClick = {
                if (title.isBlank()) {
                  errorMessage = "Inserisci un titolo o seleziona una tipologia"
                  return@Button
                }
                val km = kmStr.replace(',', '.').toDoubleOrNull()
                val cost = costStr.replace(',', '.').toDoubleOrNull()

                val reminder = VehicleReminder(
                  id = initialReminder?.id ?: 0,
                  title = title.trim(),
                  type = selectedType,
                  dueDateTimestamp = dueDateTimestamp,
                  kmThreshold = km,
                  cost = cost,
                  notes = notes.trim(),
                  isCompleted = isCompleted
                )
                onSave(reminder)
              },
              modifier = Modifier
                .padding(end = 8.dp)
                .testTag("save_reminder_button")
            ) {
              Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Salva")
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
        errorMessage?.let { err ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 14.dp)
          ) {
            Text(
              text = err,
              color = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.padding(12.dp)
            )
          }
        }

        // Tipo di promemoria
        Text(
          text = "Tipologia",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          ReminderType.values().take(3).forEach { type ->
            FilterChip(
              selected = selectedType == type,
              onClick = {
                selectedType = type
                if (title.isBlank() || ReminderType.values().any { it.displayName == title }) {
                  title = type.displayName
                }
                if (type == ReminderType.TAGLIANDO && !isCompleted && initialReminder == null) {
                  // Tagliando fatto defaults to completed
                  isCompleted = true
                }
              },
              label = { Text(type.displayName.replace("Scadenza ", "")) },
              modifier = Modifier.testTag("chip_type_${type.name}")
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          ReminderType.values().drop(3).forEach { type ->
            FilterChip(
              selected = selectedType == type,
              onClick = {
                selectedType = type
                if (title.isBlank() || ReminderType.values().any { it.displayName == title }) {
                  title = type.displayName
                }
              },
              label = { Text(type.displayName.replace("Scadenza ", "")) },
              modifier = Modifier.testTag("chip_type_${type.name}")
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Titolo
        OutlinedTextField(
          value = title,
          onValueChange = {
            title = it
            errorMessage = null
          },
          label = { Text("Nome Promemoria / Descrizione *") },
          placeholder = { Text("es. Scadenza Bollo Auto, Tagliando 60.000 km") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_reminder_title")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Data Scadenza / Effettuazione
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true }
            .testTag("reminder_date_trigger")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = if (isCompleted) "Data Effettuazione" else "Data Scadenza",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.ITALIAN).format(Date(dueDateTimestamp))
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ITALIAN) else it.toString() },
                  style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
              }
            }
            Text(
              text = "Modifica",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chilometri (km)
        OutlinedTextField(
          value = kmStr,
          onValueChange = { kmStr = it },
          label = { Text("Chilometraggio associato (Opzionale)") },
          placeholder = { Text("es. 65000 (km del tagliando)") },
          suffix = { Text("km") },
          leadingIcon = {
            Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null)
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_reminder_km")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Costo (€)
        OutlinedTextField(
          value = costStr,
          onValueChange = { costStr = it },
          label = { Text("Importo / Costo pagato o previsto (Opzionale)") },
          placeholder = { Text("es. 195.00") },
          suffix = { Text("€") },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Euro, contentDescription = null)
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_reminder_cost")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Note
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Note aggiuntive (Officina, Compagnia, Dettagli)") },
          placeholder = { Text("es. Tagliando ufficiale con cambio olio e pastiglie freni...") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_reminder_notes")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Switch "Completato / Già effettuato"
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Intervento già effettuato / completato",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
              )
              Text(
                text = "Se attivo, il promemoria verrà archiviato nello storico anziché tra le scadenze aperte.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = isCompleted,
              onCheckedChange = { isCompleted = it },
              modifier = Modifier.testTag("switch_reminder_completed")
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = {
            if (title.isBlank()) {
              errorMessage = "Inserisci un titolo per la scadenza"
              return@Button
            }
            val km = kmStr.replace(',', '.').toDoubleOrNull()
            val cost = costStr.replace(',', '.').toDoubleOrNull()

            val reminder = VehicleReminder(
              id = initialReminder?.id ?: 0,
              title = title.trim(),
              type = selectedType,
              dueDateTimestamp = dueDateTimestamp,
              kmThreshold = km,
              cost = cost,
              notes = notes.trim(),
              isCompleted = isCompleted
            )
            onSave(reminder)
          },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("submit_reminder_button")
        ) {
          Icon(imageVector = Icons.Default.Check, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(if (initialReminder != null) "Aggiorna Scadenza" else "Salva Scadenza")
        }

        Spacer(modifier = Modifier.height(30.dp))
      }
    }

    if (showDatePicker) {
      DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = {
          TextButton(
            onClick = {
              datePickerState.selectedDateMillis?.let { selected ->
                dueDateTimestamp = selected
              }
              showDatePicker = false
            }
          ) {
            Text("Conferma")
          }
        },
        dismissButton = {
          TextButton(onClick = { showDatePicker = false }) {
            Text("Annulla")
          }
        }
      ) {
        DatePicker(state = datePickerState)
      }
    }
  }
}
