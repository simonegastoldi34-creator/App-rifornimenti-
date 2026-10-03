package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.RefuelEntry
import com.example.ui.components.RefuelCard
import java.util.Locale

@Composable
fun HistoryScreen(
  entries: List<RefuelEntry>,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onEditEntry: (RefuelEntry) -> Unit,
  onDeleteEntry: (RefuelEntry) -> Unit,
  modifier: Modifier = Modifier
) {
  var entryToDelete by remember { mutableStateOf<RefuelEntry?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("history_screen")
  ) {
    // Search & Filter Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchQueryChange,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .testTag("history_search_field"),
      placeholder = { Text("Cerca per data, stazione o carburante...") },
      leadingIcon = {
        Icon(imageVector = Icons.Default.Search, contentDescription = "Cerca")
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { onSearchQueryChange("") }) {
            Icon(imageVector = Icons.Default.Clear, contentDescription = "Cancella")
          }
        }
      },
      shape = RoundedCornerShape(14.dp),
      singleLine = true
    )

    // Count and Quick summary row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "${entries.size} rifornimenti trovati",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (entries.isNotEmpty()) {
        val totalSpent = entries.sumOf { it.totalCost }
        Text(
          text = "Totale: ${String.format(Locale.ITALIAN, "€ %.2f", totalSpent)}",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )
      }
    }

    if (entries.isEmpty()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.size(56.dp)
        ) {
          Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
              .padding(12.dp)
              .size(32.dp)
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = if (searchQuery.isNotBlank()) "Nessun risultato per \"$searchQuery\""
          else "Nessun rifornimento registrato",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("refuel_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(
          items = entries,
          key = { it.id }
        ) { entry ->
          RefuelCard(
            entry = entry,
            onEdit = { onEditEntry(entry) },
            onDelete = { entryToDelete = entry }
          )
        }

        item {
          Spacer(modifier = Modifier.height(80.dp)) // clearance for FAB
        }
      }
    }
  }

  // Delete Confirmation Dialog
  entryToDelete?.let { entry ->
    AlertDialog(
      onDismissRequest = { entryToDelete = null },
      title = { Text("Elimina Rifornimento") },
      text = {
        Text("Sei sicuro di voler eliminare il rifornimento del ${entry.formattedDateShort()} di € ${String.format(Locale.ITALIAN, "%.2f", entry.totalCost)}?")
      },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteEntry(entry)
            entryToDelete = null
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_delete_button")
        ) {
          Text("Elimina")
        }
      },
      dismissButton = {
        TextButton(onClick = { entryToDelete = null }) {
          Text("Annulla")
        }
      }
    )
  }
}
