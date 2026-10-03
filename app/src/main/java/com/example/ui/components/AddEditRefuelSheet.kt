package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.RefuelFormState
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRefuelDialog(
  formState: RefuelFormState,
  onUpdateForm: (RefuelFormState.() -> RefuelFormState) -> Unit,
  onAutoCalculateCost: () -> Unit,
  onAutoCalculateLiters: () -> Unit,
  onSave: () -> Unit,
  onDismiss: () -> Unit
) {
  var showDatePicker by remember { mutableStateOf(false) }

  val datePickerState = rememberDatePickerState(
    initialSelectedDateMillis = formState.dateTimestamp
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Scaffold(
      modifier = Modifier
        .fillMaxSize()
        .testTag("add_edit_refuel_dialog"),
      topBar = {
        TopAppBar(
          title = {
            Text(
              text = if (formState.id != null) "Modifica Rifornimento" else "Nuovo Rifornimento",
              fontWeight = FontWeight.Bold
            )
          },
          navigationIcon = {
            IconButton(
              onClick = onDismiss,
              modifier = Modifier.testTag("close_dialog_button")
            ) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Chiudi")
            }
          },
          actions = {
            Button(
              onClick = onSave,
              modifier = Modifier
                .padding(end = 8.dp)
                .testTag("save_refuel_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
              )
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
          .padding(horizontal = 20.dp, vertical = 12.dp)
      ) {
        // Validation Error Banner
        formState.validationError?.let { err ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 16.dp)
              .testTag("validation_error_banner")
          ) {
            Text(
              text = err,
              color = MaterialTheme.colorScheme.onErrorContainer,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(12.dp)
            )
          }
        }

        // Section 1: Data e Tipo Carburante
        SectionHeader(title = "1. Data e Tipo Carburante")

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true }
            .testTag("date_picker_trigger")
            .padding(vertical = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Data",
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Data Rifornimento",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.ITALIAN).format(Date(formState.dateTimestamp))
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ITALIAN) else it.toString() },
                  style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                  color = MaterialTheme.colorScheme.onSurface
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

        Spacer(modifier = Modifier.height(10.dp))

        // Fuel Type Chips
        Text(
          text = "Carburante",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("Benzina", "Diesel", "GPL", "Metano", "Ibrido").forEach { type ->
            FilterChip(
              selected = formState.fuelType == type,
              onClick = { onUpdateForm { copy(fuelType = type) } },
              label = { Text(type) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
              ),
              modifier = Modifier.testTag("fuel_chip_$type")
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Chilometri e Cruscotto
        SectionHeader(title = "2. Chilometri & Consumo Cruscotto")

        // Distanza percorsa dall'ultimo rifornimento
        OutlinedTextField(
          value = formState.distanceTraveledKmStr,
          onValueChange = { newVal ->
            onUpdateForm { copy(distanceTraveledKmStr = newVal, validationError = null) }
          },
          label = { Text("Distanza percorsa dall'ultimo rifornimento *") },
          placeholder = { Text("es. 580.5") },
          suffix = { Text("km", fontWeight = FontWeight.Bold) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_distance_traveled")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Distanza rimanente (autonomia residua indicata)
        OutlinedTextField(
          value = formState.remainingDistanceKmStr,
          onValueChange = { newVal ->
            onUpdateForm { copy(remainingDistanceKmStr = newVal, validationError = null) }
          },
          label = { Text("Distanza rimanente (Autonomia residua)") },
          placeholder = { Text("es. 90") },
          suffix = { Text("km", fontWeight = FontWeight.Bold) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = CyanAccent)
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_remaining_distance")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Consumo medio dal computer di bordo
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = formState.tripConsumptionStr,
            onValueChange = { newVal ->
              onUpdateForm { copy(tripConsumptionStr = newVal, validationError = null) }
            },
            label = { Text("Consumo medio cruscotto") },
            placeholder = { Text("es. 5.4") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = AmberAccent)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("input_trip_consumption")
          )

          // Toggle Unit: L/100km vs km/L
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.height(56.dp)
          ) {
            Row(
              modifier = Modifier.padding(4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              UnitButton(
                text = "L/100km",
                isSelected = formState.tripConsumptionUnit == "L_PER_100KM",
                onClick = { onUpdateForm { copy(tripConsumptionUnit = "L_PER_100KM") } }
              )
              UnitButton(
                text = "km/L",
                isSelected = formState.tripConsumptionUnit == "KM_PER_L",
                onClick = { onUpdateForm { copy(tripConsumptionUnit = "KM_PER_L") } }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: Carburante e Costi allo Scontrino
        SectionHeader(title = "3. Prezzo, Litri & Costo Scontrino")

        // Prezzo al litro
        OutlinedTextField(
          value = formState.pricePerLiterStr,
          onValueChange = { newVal ->
            onUpdateForm { copy(pricePerLiterStr = newVal, validationError = null) }
          },
          label = { Text("Prezzo al litro *") },
          placeholder = { Text("es. 1.789") },
          suffix = { Text("€/L", fontWeight = FontWeight.Bold) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Euro, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_price_per_liter")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Litri riforniti
        OutlinedTextField(
          value = formState.litersRefueledStr,
          onValueChange = { newVal ->
            onUpdateForm { copy(litersRefueledStr = newVal, validationError = null) }
          },
          label = { Text("Litri riforniti *") },
          placeholder = { Text("es. 35.50") },
          suffix = { Text("L", fontWeight = FontWeight.Bold) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null, tint = CyanAccent)
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_liters_refueled")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Costo totale allo scontrino
        OutlinedTextField(
          value = formState.totalCostStr,
          onValueChange = { newVal ->
            onUpdateForm { copy(totalCostStr = newVal, validationError = null) }
          },
          label = { Text("Costo totale allo scontrino *") },
          placeholder = { Text("es. 63.50") },
          suffix = { Text("€", fontWeight = FontWeight.Bold) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Euro, contentDescription = null, tint = AmberAccent)
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_total_cost")
        )

        // Helper calculations row
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onAutoCalculateCost,
            modifier = Modifier
              .weight(1f)
              .testTag("calc_total_cost_button"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Calcola Totale (€)", fontSize = 12.sp)
          }

          OutlinedButton(
            onClick = onAutoCalculateLiters,
            modifier = Modifier
              .weight(1f)
              .testTag("calc_liters_button"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Calcola Litri (L)", fontSize = 12.sp)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 4: Note / Distributore
        SectionHeader(title = "4. Note & Distributore (Opzionale)")
        OutlinedTextField(
          value = formState.notes,
          onValueChange = { newVal ->
            onUpdateForm { copy(notes = newVal) }
          },
          label = { Text("Note o Stazione di servizio") },
          placeholder = { Text("es. Eni Station Autostrada, Self Service...") },
          singleLine = false,
          maxLines = 2,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_notes")
        )

        // Live calculation summary card
        Spacer(modifier = Modifier.height(20.dp))
        LivePreviewCard(formState = formState)

        Spacer(modifier = Modifier.height(30.dp))

        // Final Save Button
        Button(
          onClick = onSave,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("submit_refuel_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
          )
        ) {
          Icon(imageVector = Icons.Default.Check, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (formState.id != null) "Aggiorna Rifornimento" else "Salva Rifornimento",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }

    if (showDatePicker) {
      DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = {
          TextButton(
            onClick = {
              datePickerState.selectedDateMillis?.let { selected ->
                onUpdateForm { copy(dateTimestamp = selected) }
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

@Composable
private fun SectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(bottom = 8.dp)
  )
}

@Composable
private fun UnitButton(
  text: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
    modifier = Modifier
      .clickable(onClick = onClick)
      .padding(horizontal = 2.dp)
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
      color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
    )
  }
}

@Composable
private fun LivePreviewCard(formState: RefuelFormState) {
  val kmPerL = formState.computedRealConsumptionKmPerL
  val lPer100 = formState.computedRealConsumptionLPer100Km
  val costPerKm = formState.computedCostPerKm

  if (kmPerL != null || costPerKm != null) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = "Anteprima Statistiche Rifornimento",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          if (kmPerL != null && lPer100 != null) {
            Column {
              Text(
                text = "Consumo Reale",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = String.format(Locale.ITALIAN, "%.2f km/L", kmPerL),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = String.format(Locale.ITALIAN, "%.2f L/100km", lPer100),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          if (costPerKm != null) {
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "Costo per Km",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = String.format(Locale.ITALIAN, "€ %.3f /km", costPerKm),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }
    }
  }
}
