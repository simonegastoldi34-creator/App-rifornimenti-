package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReminderType
import com.example.data.VehicleReminder
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import java.util.Locale

@Composable
fun RemindersScreen(
  reminders: List<VehicleReminder>,
  onAddNewReminder: () -> Unit,
  onAddDefaultReminders: () -> Unit,
  onEditReminder: (VehicleReminder) -> Unit,
  onDeleteReminder: (VehicleReminder) -> Unit,
  onToggleCompleted: (VehicleReminder) -> Unit,
  onRenewForOneYear: (VehicleReminder) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedFilter by remember { mutableStateOf("Tutte") }
  var reminderToDelete by remember { mutableStateOf<VehicleReminder?>(null) }

  val activeReminders = reminders.filter { !it.isCompleted }
  val completedReminders = reminders.filter { it.isCompleted }
  val expiringSoonCount = reminders.count { it.isExpiringSoon }
  val expiredCount = reminders.count { it.isExpired }

  val filteredList = when (selectedFilter) {
    "Scadenze Attive" -> activeReminders
    "Manutenzioni Effettuate" -> completedReminders
    else -> reminders
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("reminders_screen")
  ) {
    // Header summary card
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .testTag("reminders_summary_card")
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
              color = MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Default.EventNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                  .padding(6.dp)
                  .size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Scadenze & Manutenzione",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Bollo, assicurazione, tagliandi e revisione",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Button(
            onClick = onAddNewReminder,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("add_reminder_header_button")
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Aggiungi", fontSize = 12.sp)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick status pills
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          StatusSummaryBadge(
            label = "Attive",
            count = activeReminders.size,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
          )

          StatusSummaryBadge(
            label = "In Scadenza",
            count = expiringSoonCount,
            color = AmberAccent,
            modifier = Modifier.weight(1f)
          )

          StatusSummaryBadge(
            label = "Scadute",
            count = expiredCount,
            color = if (expiredCount > 0) CoralRed else MaterialTheme.colorScheme.outline,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf("Tutte", "Scadenze Attive", "Manutenzioni Effettuate").forEach { filter ->
        FilterChip(
          selected = selectedFilter == filter,
          onClick = { selectedFilter = filter },
          label = { Text(filter) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          ),
          modifier = Modifier.testTag("filter_chip_$filter")
        )
      }
    }

    if (reminders.isEmpty()) {
      // Empty State
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.size(72.dp)
        ) {
          Icon(
            imageVector = Icons.Default.EventNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
              .padding(16.dp)
              .size(40.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Nessuna data o scadenza memorizzata",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Memorizza la scadenza del bollo, della polizza assicurativa, la data dell'ultimo tagliando effettuato o la revisione per non dimenticare nulla.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = onAddNewReminder,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("empty_add_reminder_btn")
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Inserisci Nuova Scadenza")
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
          onClick = onAddDefaultReminders,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("add_default_reminders_btn")
        ) {
          Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Imposta Scadenze Standard (Bollo, Assicurazione, Tagliando)")
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("reminders_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(
          items = filteredList,
          key = { it.id }
        ) { reminder ->
          ReminderCard(
            reminder = reminder,
            onToggleCompleted = { onToggleCompleted(reminder) },
            onRenewOneYear = { onRenewForOneYear(reminder) },
            onEdit = { onEditReminder(reminder) },
            onDelete = { reminderToDelete = reminder }
          )
        }

        item {
          Spacer(modifier = Modifier.height(80.dp))
        }
      }
    }
  }

  // Delete Confirmation
  reminderToDelete?.let { rem ->
    AlertDialog(
      onDismissRequest = { reminderToDelete = null },
      title = { Text("Eliminare promemoria?") },
      text = { Text("Vuoi davvero rimuovere \"${rem.title}\"?") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteReminder(rem)
            reminderToDelete = null
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Elimina")
        }
      },
      dismissButton = {
        TextButton(onClick = { reminderToDelete = null }) {
          Text("Annulla")
        }
      }
    )
  }
}

@Composable
private fun StatusSummaryBadge(
  label: String,
  count: Int,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = color.copy(alpha = 0.15f),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = count.toString(),
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = color
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun ReminderCard(
  reminder: VehicleReminder,
  onToggleCompleted: () -> Unit,
  onRenewOneYear: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val (icon, typeColor) = when (reminder.type) {
    ReminderType.BOLLO -> Icons.Default.Receipt to AmberAccent
    ReminderType.ASSICURAZIONE -> Icons.Default.Security to CyanAccent
    ReminderType.TAGLIANDO -> Icons.Default.Build to MaterialTheme.colorScheme.primary
    ReminderType.REVISIONE -> Icons.Default.CheckCircle to EmeraldGreen
    ReminderType.GOMME -> Icons.Default.DirectionsCar to CyanAccent
    ReminderType.ALTRO -> Icons.Default.DateRange to MaterialTheme.colorScheme.secondary
  }

  val badgeColor = when {
    reminder.isCompleted -> MaterialTheme.colorScheme.outline
    reminder.isExpired -> CoralRed
    reminder.isExpiringSoon -> AmberAccent
    else -> EmeraldGreen
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (reminder.isCompleted)
        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
      else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("reminder_card_${reminder.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = CircleShape,
            color = typeColor.copy(alpha = 0.15f),
            modifier = Modifier.size(42.dp)
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = typeColor,
              modifier = Modifier
                .padding(10.dp)
                .size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = reminder.title,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                textDecoration = if (reminder.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
              ),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = reminder.formattedDueDate(),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Status countdown chip
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = badgeColor.copy(alpha = 0.15f),
          modifier = Modifier.padding(start = 8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (reminder.isExpired) {
              Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CoralRed, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
              text = reminder.statusLabel(),
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = badgeColor
            )
          }
        }
      }

      // Metadata: Km or Cost
      if (reminder.kmThreshold != null || reminder.cost != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          reminder.kmThreshold?.let { km ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${String.format(Locale.ITALIAN, "%.0f", km)} km",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )
              }
            }
          }

          reminder.cost?.let { cost ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(imageVector = Icons.Default.Euro, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmberAccent)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = String.format(Locale.ITALIAN, "€ %.2f", cost),
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )
              }
            }
          }
        }
      }

      if (reminder.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = reminder.notes,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        modifier = Modifier.padding(vertical = 10.dp)
      )

      // Actions Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Toggle Completed Button
        OutlinedButton(
          onClick = onToggleCompleted,
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          modifier = Modifier.height(34.dp).testTag("toggle_complete_${reminder.id}")
        ) {
          Icon(
            imageVector = if (reminder.isCompleted) Icons.Default.Autorenew else Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (reminder.isCompleted) "Riapri" else "Completato",
            fontSize = 12.sp
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Quick renew for annual events (Bollo, Assicurazione, Revisione)
          if (!reminder.isCompleted && (reminder.type == ReminderType.BOLLO || reminder.type == ReminderType.ASSICURAZIONE || reminder.type == ReminderType.REVISIONE)) {
            TextButton(
              onClick = onRenewOneYear,
              modifier = Modifier.testTag("renew_year_${reminder.id}")
            ) {
              Icon(imageVector = Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(2.dp))
              Text("+1 Anno", fontSize = 12.sp)
            }
          }

          IconButton(
            onClick = onEdit,
            modifier = Modifier.size(36.dp).testTag("edit_reminder_${reminder.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Modifica",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp).testTag("delete_reminder_${reminder.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Elimina",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}
