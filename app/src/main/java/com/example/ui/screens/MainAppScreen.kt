package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.VehicleReminder
import com.example.ui.RefuelViewModel
import com.example.ui.components.AddEditRefuelDialog
import com.example.ui.components.AddEditReminderDialog
import com.example.ui.components.CsvImportDialog
import com.example.ui.components.ExportCsvDialog

enum class AppDestination(val title: String) {
  DASHBOARD("Dashboard"),
  HISTORY("Storico"),
  REMINDERS("Scadenze"),
  CALCULATOR("Calcoli")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
  viewModel: RefuelViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val entries by viewModel.filteredEntries.collectAsStateWithLifecycle()
  val allEntries by viewModel.allEntries.collectAsStateWithLifecycle()
  val reminders by viewModel.allReminders.collectAsStateWithLifecycle()
  val stats by viewModel.stats.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val formState by viewModel.formState.collectAsStateWithLifecycle()
  val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

  var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }
  var showAddEditDialog by remember { mutableStateOf(false) }
  var showCsvImportDialog by remember { mutableStateOf(false) }
  var showExportCsvDialog by remember { mutableStateOf(false) }
  var showReminderDialog by remember { mutableStateOf(false) }
  var editingReminder by remember { mutableStateOf<VehicleReminder?>(null) }

  // Handle hardware back button to navigate to Dashboard before exiting
  BackHandler(enabled = currentDestination != AppDestination.DASHBOARD) {
    currentDestination = AppDestination.DASHBOARD
  }

  // Toast handler
  LaunchedEffect(toastMessage) {
    toastMessage?.let { msg ->
      Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
      viewModel.clearToast()
    }
  }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isWideScreen = maxWidth >= 600.dp

    Row(modifier = Modifier.fillMaxSize()) {
      if (isWideScreen) {
        NavigationRail(
          modifier = Modifier.testTag("app_navigation_rail"),
          containerColor = MaterialTheme.colorScheme.surface
        ) {
          FloatingActionButton(
            onClick = {
              if (currentDestination == AppDestination.REMINDERS) {
                editingReminder = null
                showReminderDialog = true
              } else {
                viewModel.initNewEntry()
                showAddEditDialog = true
              }
            },
            modifier = Modifier
              .padding(vertical = 16.dp)
              .testTag("rail_fab_add")
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Aggiungi")
          }

          NavigationRailItem(
            selected = currentDestination == AppDestination.DASHBOARD,
            onClick = { currentDestination = AppDestination.DASHBOARD },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            modifier = Modifier.testTag("rail_item_dashboard")
          )

          NavigationRailItem(
            selected = currentDestination == AppDestination.HISTORY,
            onClick = { currentDestination = AppDestination.HISTORY },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Storico") },
            label = { Text("Storico") },
            modifier = Modifier.testTag("rail_item_history")
          )

          NavigationRailItem(
            selected = currentDestination == AppDestination.REMINDERS,
            onClick = { currentDestination = AppDestination.REMINDERS },
            icon = { Icon(Icons.Default.EventNote, contentDescription = "Scadenze") },
            label = { Text("Scadenze") },
            modifier = Modifier.testTag("rail_item_reminders")
          )

          NavigationRailItem(
            selected = currentDestination == AppDestination.CALCULATOR,
            onClick = { currentDestination = AppDestination.CALCULATOR },
            icon = { Icon(Icons.Default.Calculate, contentDescription = "Calcolatore") },
            label = { Text("Calcoli") },
            modifier = Modifier.testTag("rail_item_calculator")
          )
        }
      }

      Scaffold(
        modifier = Modifier
          .weight(1f)
          .testTag("main_scaffold"),
        topBar = {
          TopAppBar(
            title = {
              Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                  imageVector = when (currentDestination) {
                    AppDestination.DASHBOARD -> Icons.Default.LocalGasStation
                    AppDestination.HISTORY -> Icons.AutoMirrored.Filled.List
                    AppDestination.REMINDERS -> Icons.Default.EventNote
                    AppDestination.CALCULATOR -> Icons.Default.Calculate
                  },
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(24.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                  text = when (currentDestination) {
                    AppDestination.DASHBOARD -> "Rifornimenti Auto"
                    AppDestination.HISTORY -> "Cronologia Rifornimenti"
                    AppDestination.REMINDERS -> "Date & Scadenze Auto"
                    AppDestination.CALCULATOR -> "Calcolatore & Dati"
                  },
                  fontWeight = FontWeight.Bold
                )
              }
            },
            actions = {
              IconButton(
                onClick = { showCsvImportDialog = true },
                modifier = Modifier.testTag("top_bar_import_csv_button")
              ) {
                Icon(
                  imageVector = Icons.Default.UploadFile,
                  contentDescription = "Importa file CSV",
                  tint = MaterialTheme.colorScheme.primary
                )
              }

              IconButton(
                onClick = { showExportCsvDialog = true },
                modifier = Modifier.testTag("top_bar_export_csv_button")
              ) {
                Icon(
                  imageVector = Icons.Default.FileDownload,
                  contentDescription = "Esporta file CSV",
                  tint = MaterialTheme.colorScheme.primary
                )
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.surface
            )
          )
        },
        bottomBar = {
          if (!isWideScreen) {
            NavigationBar(
              modifier = Modifier.testTag("app_bottom_bar")
            ) {
              NavigationBarItem(
                selected = currentDestination == AppDestination.DASHBOARD,
                onClick = { currentDestination = AppDestination.DASHBOARD },
                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                label = { Text("Dashboard") },
                modifier = Modifier.testTag("nav_item_dashboard")
              )

              NavigationBarItem(
                selected = currentDestination == AppDestination.HISTORY,
                onClick = { currentDestination = AppDestination.HISTORY },
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Storico") },
                label = { Text("Storico") },
                modifier = Modifier.testTag("nav_item_history")
              )

              NavigationBarItem(
                selected = currentDestination == AppDestination.REMINDERS,
                onClick = { currentDestination = AppDestination.REMINDERS },
                icon = { Icon(Icons.Default.EventNote, contentDescription = "Scadenze") },
                label = { Text("Scadenze") },
                modifier = Modifier.testTag("nav_item_reminders")
              )

              NavigationBarItem(
                selected = currentDestination == AppDestination.CALCULATOR,
                onClick = { currentDestination = AppDestination.CALCULATOR },
                icon = { Icon(Icons.Default.Calculate, contentDescription = "Calcolatore") },
                label = { Text("Calcoli") },
                modifier = Modifier.testTag("nav_item_calculator")
              )
            }
          }
        },
        floatingActionButton = {
          if (!isWideScreen) {
            ExtendedFloatingActionButton(
              onClick = {
                if (currentDestination == AppDestination.REMINDERS) {
                  editingReminder = null
                  showReminderDialog = true
                } else {
                  viewModel.initNewEntry()
                  showAddEditDialog = true
                }
              },
              icon = { Icon(Icons.Default.Add, contentDescription = null) },
              text = {
                Text(
                  text = if (currentDestination == AppDestination.REMINDERS) "Nuova Scadenza" else "Rifornimento",
                  fontWeight = FontWeight.Bold
                )
              },
              modifier = Modifier.testTag("fab_add_main"),
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            )
          }
        }
      ) { innerPadding ->
        Crossfade(
          targetState = currentDestination,
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) { destination ->
          when (destination) {
            AppDestination.DASHBOARD -> DashboardScreen(
              stats = stats,
              entries = allEntries,
              onAddNewRefuel = {
                viewModel.initNewEntry()
                showAddEditDialog = true
              },
              onAddSampleData = { viewModel.addSampleData() },
              onEditEntry = { entry ->
                viewModel.initEditEntry(entry)
                showAddEditDialog = true
              },
              onDeleteEntry = { entry -> viewModel.deleteEntry(entry) },
              onNavigateToHistory = { currentDestination = AppDestination.HISTORY }
            )

            AppDestination.HISTORY -> HistoryScreen(
              entries = entries,
              searchQuery = searchQuery,
              onSearchQueryChange = { viewModel.setSearchQuery(it) },
              onEditEntry = { entry ->
                viewModel.initEditEntry(entry)
                showAddEditDialog = true
              },
              onDeleteEntry = { entry -> viewModel.deleteEntry(entry) }
            )

            AppDestination.REMINDERS -> RemindersScreen(
              reminders = reminders,
              onAddNewReminder = {
                editingReminder = null
                showReminderDialog = true
              },
              onAddDefaultReminders = { viewModel.addDefaultReminders() },
              onEditReminder = { reminder ->
                editingReminder = reminder
                showReminderDialog = true
              },
              onDeleteReminder = { reminder -> viewModel.deleteReminder(reminder) },
              onToggleCompleted = { reminder -> viewModel.toggleReminderCompleted(reminder) },
              onRenewForOneYear = { reminder -> viewModel.renewReminderForOneYear(reminder) },
              onSendTestNotification = { viewModel.sendTestNotification() },
              onRescheduleAll = { viewModel.rescheduleAllActiveReminders() }
            )

            AppDestination.CALCULATOR -> TripCalculatorScreen(
              stats = stats,
              onExportCsv = { viewModel.exportCsv() },
              onExportRemindersCsv = { viewModel.exportRemindersCsv() },
              onOpenImportCsv = { showCsvImportDialog = true },
              onClearAll = { viewModel.clearAllEntries() }
            )
          }
        }
      }
    }
  }

  // Add / Edit Refuel Dialog
  if (showAddEditDialog) {
    AddEditRefuelDialog(
      formState = formState,
      onUpdateForm = { viewModel.updateForm(it) },
      onAutoCalculateCost = { viewModel.autoCalculateTotalCost() },
      onAutoCalculateLiters = { viewModel.autoCalculateLiters() },
      onSave = {
        viewModel.saveForm(
          onSuccess = { showAddEditDialog = false }
        )
      },
      onDismiss = { showAddEditDialog = false }
    )
  }

  // CSV Import Dialog
  if (showCsvImportDialog) {
    CsvImportDialog(
      onParseContent = { viewModel.parseCsvFromContent(it) },
      onParseUri = { viewModel.parseCsvFromUri(it, context) },
      onConfirmImport = { list ->
        viewModel.saveImportedEntries(list) {
          // Stay on current or go to history
        }
      },
      onDismiss = { showCsvImportDialog = false }
    )
  }

  // CSV Export Dialog
  if (showExportCsvDialog) {
    ExportCsvDialog(
      refuelsCsv = viewModel.exportCsv(),
      remindersCsv = viewModel.exportRemindersCsv(),
      onDismiss = { showExportCsvDialog = false }
    )
  }

  // Add / Edit Reminder Dialog
  if (showReminderDialog) {
    AddEditReminderDialog(
      initialReminder = editingReminder,
      onSave = { reminder ->
        viewModel.saveReminder(reminder) {
          showReminderDialog = false
          editingReminder = null
        }
      },
      onDismiss = {
        showReminderDialog = false
        editingReminder = null
      }
    )
  }
}
