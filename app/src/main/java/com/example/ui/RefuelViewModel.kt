package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CsvImportHelper
import com.example.data.RefuelEntry
import com.example.data.RefuelRepository
import com.example.data.ReminderRepository
import com.example.data.ReminderType
import com.example.data.VehicleReminder
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Calendar
import java.util.Locale

class RefuelViewModel(application: Application) : AndroidViewModel(application) {
  private val refuelRepository: RefuelRepository
  private val reminderRepository: ReminderRepository

  init {
    val db = AppDatabase.getDatabase(application)
    refuelRepository = RefuelRepository(db.refuelDao())
    reminderRepository = ReminderRepository(db.reminderDao())
  }

  // Refuel entries Flow
  val allEntries: StateFlow<List<RefuelEntry>> = refuelRepository.allEntries
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  // Reminders Flow
  val allReminders: StateFlow<List<VehicleReminder>> = reminderRepository.allReminders
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  val filteredEntries: StateFlow<List<RefuelEntry>> = combine(allEntries, _searchQuery) { list, query ->
    if (query.isBlank()) {
      list
    } else {
      val q = query.trim().lowercase(Locale.ITALIAN)
      list.filter { entry ->
        entry.notes.lowercase(Locale.ITALIAN).contains(q) ||
            entry.fuelType.lowercase(Locale.ITALIAN).contains(q) ||
            entry.formattedDate().lowercase(Locale.ITALIAN).contains(q)
      }
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val stats: StateFlow<RefuelStats> = allEntries.combine(MutableStateFlow(Unit)) { list, _ ->
    RefuelStats.calculate(list)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = RefuelStats()
  )

  private val _formState = MutableStateFlow(RefuelFormState())
  val formState: StateFlow<RefuelFormState> = _formState.asStateFlow()

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  fun clearToast() {
    _toastMessage.value = null
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun initNewEntry() {
    _formState.value = RefuelFormState(
      dateTimestamp = System.currentTimeMillis()
    )
  }

  fun initEditEntry(entry: RefuelEntry) {
    _formState.value = RefuelFormState.fromEntity(entry)
  }

  fun updateForm(transform: RefuelFormState.() -> RefuelFormState) {
    _formState.value = _formState.value.transform()
  }

  fun autoCalculateTotalCost() {
    val current = _formState.value
    val liters = current.litersRefueledStr.replace(',', '.').toDoubleOrNull()
    val price = current.pricePerLiterStr.replace(',', '.').toDoubleOrNull()
    if (liters != null && price != null) {
      val calculated = liters * price
      _formState.value = current.copy(
        totalCostStr = String.format(Locale.US, "%.2f", calculated)
      )
    }
  }

  fun autoCalculateLiters() {
    val current = _formState.value
    val cost = current.totalCostStr.replace(',', '.').toDoubleOrNull()
    val price = current.pricePerLiterStr.replace(',', '.').toDoubleOrNull()
    if (cost != null && price != null && price > 0.0) {
      val calculated = cost / price
      _formState.value = current.copy(
        litersRefueledStr = String.format(Locale.US, "%.2f", calculated)
      )
    }
  }

  fun saveForm(onSuccess: () -> Unit) {
    val current = _formState.value

    val distance = current.distanceTraveledKmStr.replace(',', '.').toDoubleOrNull()
    if (distance == null || distance <= 0.0) {
      _formState.value = current.copy(validationError = "Inserisci una distanza valida percorsa (km)")
      return
    }

    val price = current.pricePerLiterStr.replace(',', '.').toDoubleOrNull()
    if (price == null || price <= 0.0) {
      _formState.value = current.copy(validationError = "Inserisci un prezzo al litro valido (€/L)")
      return
    }

    val liters = current.litersRefueledStr.replace(',', '.').toDoubleOrNull()
    if (liters == null || liters <= 0.0) {
      _formState.value = current.copy(validationError = "Inserisci i litri riforniti validi (L)")
      return
    }

    val entity = current.toEntity()
    if (entity == null) {
      _formState.value = current.copy(validationError = "Dati non validi, controlla i campi numerici")
      return
    }

    viewModelScope.launch {
      if (entity.id > 0) {
        refuelRepository.update(entity)
        _toastMessage.value = "Rifornimento aggiornato con successo"
      } else {
        refuelRepository.insert(entity)
        _toastMessage.value = "Rifornimento salvato con successo"
      }
      onSuccess()
    }
  }

  fun deleteEntry(entry: RefuelEntry) {
    viewModelScope.launch {
      refuelRepository.delete(entry)
      _toastMessage.value = "Rifornimento eliminato"
    }
  }

  fun clearAllEntries() {
    viewModelScope.launch {
      refuelRepository.clearAll()
      _toastMessage.value = "Tutti i rifornimenti sono stati cancellati"
    }
  }

  fun addSampleData() {
    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val dayMillis = 86400000L

      val sample1 = RefuelEntry(
        dateTimestamp = now - dayMillis * 18,
        distanceTraveledKm = 640.0,
        remainingDistanceKm = 90.0,
        tripConsumption = 5.4,
        tripConsumptionUnit = "L_PER_100KM",
        pricePerLiter = 1.789,
        litersRefueled = 36.5,
        totalCost = 65.30,
        fuelType = "Benzina",
        notes = "Eni Station - Pieno autostrada"
      )

      val sample2 = RefuelEntry(
        dateTimestamp = now - dayMillis * 9,
        distanceTraveledKm = 580.0,
        remainingDistanceKm = 110.0,
        tripConsumption = 5.2,
        tripConsumptionUnit = "L_PER_100KM",
        pricePerLiter = 1.749,
        litersRefueled = 31.8,
        totalCost = 55.62,
        fuelType = "Benzina",
        notes = "Q8 Self service"
      )

      val sample3 = RefuelEntry(
        dateTimestamp = now - dayMillis * 2,
        distanceTraveledKm = 612.0,
        remainingDistanceKm = 75.0,
        tripConsumption = 5.5,
        tripConsumptionUnit = "L_PER_100KM",
        pricePerLiter = 1.765,
        litersRefueled = 34.2,
        totalCost = 60.36,
        fuelType = "Benzina",
        notes = "IP - Pieno città"
      )

      refuelRepository.insert(sample1)
      refuelRepository.insert(sample2)
      refuelRepository.insert(sample3)
      _toastMessage.value = "Aggiunti dati di esempio"
    }
  }

  fun exportCsv(): String {
    val list = allEntries.value
    val sb = StringBuilder()
    sb.append("ID,Data,Km_Percorsi,Km_Rimanenti,Consumo_Cruscotto,Unita_Consumo,Prezzo_Litro,Litri,Costo_Totale,Carburante,Note\n")
    for (e in list) {
      sb.append("${e.id},${e.formattedDateShort()},${e.distanceTraveledKm},${e.remainingDistanceKm},${e.tripConsumption},${e.tripConsumptionUnit},${e.pricePerLiter},${e.litersRefueled},${e.totalCost},\"${e.fuelType}\",\"${e.notes.replace("\"", "\"\"")}\"\n")
    }
    return sb.toString()
  }

  fun exportRemindersCsv(): String {
    val list = allReminders.value
    val sb = StringBuilder()
    sb.append("ID,Titolo,Tipologia,Data,Km_Associati,Costo,Note,Stato\n")
    for (r in list) {
      val status = if (r.isCompleted) "Completato" else "Attivo"
      val kmVal = r.kmThreshold?.let { String.format(Locale.US, "%.0f", it) } ?: ""
      val costVal = r.cost?.let { String.format(Locale.US, "%.2f", it) } ?: ""
      sb.append("${r.id},\"${r.title}\",\"${r.type.displayName}\",${r.formattedDueDate()},${kmVal},${costVal},\"${r.notes.replace("\"", "\"\"")}\",$status\n")
    }
    return sb.toString()
  }

  // --- CSV Import Operations ---
  fun parseCsvFromContent(content: String): CsvImportHelper.ParseResult {
    return CsvImportHelper.parseCsv(content)
  }

  fun parseCsvFromUri(uri: Uri, context: Context): CsvImportHelper.ParseResult {
    return try {
      val inputStream = context.contentResolver.openInputStream(uri)
        ?: return CsvImportHelper.ParseResult(emptyList(), 0, listOf("Impossibile aprire il file selezionato"))
      val reader = BufferedReader(InputStreamReader(inputStream))
      val content = reader.readText()
      reader.close()
      CsvImportHelper.parseCsv(content)
    } catch (e: Exception) {
      CsvImportHelper.ParseResult(emptyList(), 0, listOf("Errore nella lettura del file: ${e.localizedMessage}"))
    }
  }

  fun saveImportedEntries(entries: List<RefuelEntry>, onComplete: () -> Unit) {
    if (entries.isEmpty()) return
    viewModelScope.launch {
      for (entry in entries) {
        refuelRepository.insert(entry)
      }
      _toastMessage.value = "Importati con successo ${entries.size} rifornimenti!"
      onComplete()
    }
  }

  // --- Reminder Operations ---
  fun saveReminder(reminder: VehicleReminder, onComplete: () -> Unit) {
    viewModelScope.launch {
      val savedId = if (reminder.id > 0) {
        reminderRepository.update(reminder)
        reminder.id
      } else {
        reminderRepository.insert(reminder)
      }
      val toSchedule = reminder.copy(id = savedId)
      NotificationHelper.scheduleReminderNotifications(getApplication(), toSchedule)
      _toastMessage.value = "Scadenza salvata: avvisi programmati (1 e 2 mesi prima)"
      onComplete()
    }
  }

  fun toggleReminderCompleted(reminder: VehicleReminder) {
    viewModelScope.launch {
      val updated = reminder.copy(isCompleted = !reminder.isCompleted)
      reminderRepository.update(updated)
      if (updated.isCompleted) {
        NotificationHelper.cancelReminderNotifications(getApplication(), reminder.id)
        _toastMessage.value = "Scadenza completata: notifiche disattivate"
      } else {
        NotificationHelper.scheduleReminderNotifications(getApplication(), updated)
        _toastMessage.value = "Scadenza riattivata: avvisi programmati"
      }
    }
  }

  fun renewReminderForOneYear(reminder: VehicleReminder) {
    viewModelScope.launch {
      val cal = Calendar.getInstance()
      cal.timeInMillis = reminder.dueDateTimestamp
      cal.add(Calendar.YEAR, 1)
      val renewed = reminder.copy(
        dueDateTimestamp = cal.timeInMillis,
        isCompleted = false
      )
      reminderRepository.update(renewed)
      NotificationHelper.scheduleReminderNotifications(getApplication(), renewed)
      _toastMessage.value = "Scadenza rinnovata (+1 anno) e notifiche aggiornate"
    }
  }

  fun deleteReminder(reminder: VehicleReminder) {
    viewModelScope.launch {
      reminderRepository.delete(reminder)
      NotificationHelper.cancelReminderNotifications(getApplication(), reminder.id)
      _toastMessage.value = "Promemoria eliminato"
    }
  }

  fun sendTestNotification() {
    NotificationHelper.showInstantTestNotification(getApplication())
    _toastMessage.value = "Notifica di test inviata! Controlla l'area notifiche."
  }

  fun rescheduleAllActiveReminders() {
    viewModelScope.launch {
      val list = reminderRepository.allReminders.first()
      for (r in list.filter { !it.isCompleted }) {
        NotificationHelper.scheduleReminderNotifications(getApplication(), r)
      }
    }
  }

  fun addDefaultReminders() {
    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val day = 86400000L

      val bollo = VehicleReminder(
        title = "Scadenza Bollo Auto",
        type = ReminderType.BOLLO,
        dueDateTimestamp = now + day * 45,
        cost = 180.0,
        notes = "Pagabile tramite PagoPA o tabaccheria"
      )

      val assicurazione = VehicleReminder(
        title = "Scadenza Assicurazione RCA",
        type = ReminderType.ASSICURAZIONE,
        dueDateTimestamp = now + day * 85,
        cost = 380.0,
        notes = "Polizza annuale con furto e incendio"
      )

      val tagliando = VehicleReminder(
        title = "Ultimo Tagliando Fatto",
        type = ReminderType.TAGLIANDO,
        dueDateTimestamp = now - day * 120,
        kmThreshold = 65000.0,
        cost = 240.0,
        notes = "Olio, filtri olio/aria/abitacolo e candele",
        isCompleted = true
      )

      val revisione = VehicleReminder(
        title = "Scadenza Revisione Ministeriale",
        type = ReminderType.REVISIONE,
        dueDateTimestamp = now + day * 210,
        cost = 79.0,
        notes = "Revisione periodica biennale"
      )

      val id1 = reminderRepository.insert(bollo)
      val id2 = reminderRepository.insert(assicurazione)
      reminderRepository.insert(tagliando)
      val id4 = reminderRepository.insert(revisione)

      NotificationHelper.scheduleReminderNotifications(getApplication(), bollo.copy(id = id1))
      NotificationHelper.scheduleReminderNotifications(getApplication(), assicurazione.copy(id = id2))
      NotificationHelper.scheduleReminderNotifications(getApplication(), revisione.copy(id = id4))

      _toastMessage.value = "Aggiunte le scadenze standard con notifiche attive"
    }
  }
}
