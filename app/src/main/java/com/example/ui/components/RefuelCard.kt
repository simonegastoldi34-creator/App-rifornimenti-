package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RefuelEntry
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import java.util.Locale

@Composable
fun RefuelCard(
  entry: RefuelEntry,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = modifier
      .testTag("refuel_card_${entry.id}")
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .clickable { isExpanded = !isExpanded },
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      // Header: Date + Fuel Badge + Total Cost
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
          ) {
            Icon(
              imageVector = Icons.Default.LocalGasStation,
              contentDescription = "Carburante",
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier
                .padding(8.dp)
                .size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = entry.formattedDate(),
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
              ) {
                Text(
                  text = entry.fuelType,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
              if (entry.notes.isNotBlank()) {
                Text(
                  text = entry.notes,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1
                )
              }
            }
          }
        }

        // Costo totale allo scontrino
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = String.format(Locale.ITALIAN, "€ %.2f", entry.totalCost),
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("refuel_total_cost_${entry.id}")
          )
          Text(
            text = "scontrino",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Core Key Data Row: Distanza Percorsa, Litri, Prezzo/L
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
          )
          .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Distanza percorsa
        MetricCell(
          label = "Percorsi",
          value = String.format(Locale.ITALIAN, "%.1f", entry.distanceTraveledKm),
          unit = "km",
          accentColor = CyanAccent
        )

        // Litri riforniti
        MetricCell(
          label = "Litri",
          value = String.format(Locale.ITALIAN, "%.2f", entry.litersRefueled),
          unit = "L",
          accentColor = AmberAccent
        )

        // Prezzo al litro
        MetricCell(
          label = "Prezzo/L",
          value = String.format(Locale.ITALIAN, "€ %.3f", entry.pricePerLiter),
          unit = "",
          accentColor = EmeraldGreen
        )

        // Distanza rimanente
        MetricCell(
          label = "Rimanenti",
          value = String.format(Locale.ITALIAN, "%.0f", entry.remainingDistanceKm),
          unit = "km",
          accentColor = MaterialTheme.colorScheme.secondary
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Consumo info row (Cruscotto & Reale)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        val unitLabel = if (entry.tripConsumptionUnit == "KM_PER_L") "km/L" else "L/100km"
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Speed,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Consumo cruscotto: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${String.format(Locale.ITALIAN, "%.1f", entry.tripConsumption)} $unitLabel",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        entry.costPerKm?.let { costKm ->
          Text(
            text = String.format(Locale.ITALIAN, "€ %.3f/km", costKm),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.secondary
          )
        }
      }

      // Expandable section with deeper metrics, comparison and edit/delete actions
      AnimatedVisibility(visible = isExpanded) {
        Column(
          modifier = Modifier.padding(top = 12.dp)
        ) {
          HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 8.dp)
          )

          // Extra details
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "Consumo effettivo calcolato:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              val realKmL = entry.realConsumptionKmPerL
              val realL100 = entry.realConsumptionLPer100Km
              if (realKmL != null && realL100 != null) {
                Text(
                  text = "${String.format(Locale.ITALIAN, "%.2f", realKmL)} km/L  (${String.format(Locale.ITALIAN, "%.2f", realL100)} L/100km)",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                  color = CyanAccent
                )
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "Autonomia pieno tot.:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = String.format(Locale.ITALIAN, "%.0f km", entry.totalEstimatedRangeKm),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Actions: Edit and Delete
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = onEdit,
              modifier = Modifier.testTag("edit_button_${entry.id}")
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Modifica rifornimento",
                tint = MaterialTheme.colorScheme.primary
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
              onClick = onDelete,
              modifier = Modifier.testTag("delete_button_${entry.id}")
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Elimina rifornimento",
                tint = MaterialTheme.colorScheme.error
              )
            }
          }
        }
      }

      // Expand arrow indicator
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
          contentDescription = if (isExpanded) "Comprimi" else "Espandi",
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
private fun MetricCell(
  label: String,
  value: String,
  unit: String,
  accentColor: Color
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Row(verticalAlignment = Alignment.Bottom) {
      Text(
        text = value,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      if (unit.isNotBlank()) {
        Text(
          text = " $unit",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 1.dp)
        )
      }
    }
  }
}
