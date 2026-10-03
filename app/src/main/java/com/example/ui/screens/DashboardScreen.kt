package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.RefuelEntry
import com.example.ui.RefuelStats
import com.example.ui.components.KpiCard
import com.example.ui.components.RefuelCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import java.util.Locale

@Composable
fun DashboardScreen(
  stats: RefuelStats,
  entries: List<RefuelEntry>,
  onAddNewRefuel: () -> Unit,
  onAddSampleData: () -> Unit,
  onEditEntry: (RefuelEntry) -> Unit,
  onDeleteEntry: (RefuelEntry) -> Unit,
  onNavigateToHistory: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .testTag("dashboard_screen")
  ) {
    // Hero Banner Card with visual asset
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .testTag("hero_banner_card"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(170.dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.refuel_hero_art_1791015756695),
          contentDescription = "Carburante banner",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Dark gradient scrim
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                  androidx.compose.ui.graphics.Color.Transparent,
                  androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.85f)
                )
              )
            )
        )

        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(16.dp)
        ) {
          Text(
            text = "Gestione Rifornimenti",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = androidx.compose.ui.graphics.Color.White
            )
          )
          Text(
            text = if (stats.refuelsCount > 0)
              "${stats.refuelsCount} rifornimenti registrati • ${String.format(Locale.ITALIAN, "%.0f", stats.totalDistanceKm)} km tracciati"
            else "Tieni sotto controllo consumi, km e costi del carburante",
            style = MaterialTheme.typography.bodySmall.copy(
              color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (entries.isEmpty()) {
      // Empty State
      EmptyStateSection(
        onAddNewRefuel = onAddNewRefuel,
        onAddSampleData = onAddSampleData
      )
    } else {
      // KPI Overview Grid
      Text(
        text = "Riepilogo Totale",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 10.dp)
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        KpiCard(
          title = "Spesa Totale",
          value = String.format(Locale.ITALIAN, "€ %.2f", stats.totalSpent),
          unit = "",
          icon = Icons.Default.Euro,
          accentColor = AmberAccent,
          subtitle = "${String.format(Locale.ITALIAN, "%.1f", stats.totalLiters)} Litri",
          modifier = Modifier.weight(1f),
          testTag = "kpi_total_spent"
        )

        KpiCard(
          title = "Distanza Totale",
          value = String.format(Locale.ITALIAN, "%.0f", stats.totalDistanceKm),
          unit = "km",
          icon = Icons.Default.Route,
          accentColor = CyanAccent,
          subtitle = "${stats.refuelsCount} rifornimenti",
          modifier = Modifier.weight(1f),
          testTag = "kpi_total_distance"
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        KpiCard(
          title = "Consumo Effettivo",
          value = String.format(Locale.ITALIAN, "%.2f", stats.avgRealKmPerL),
          unit = "km/L",
          icon = Icons.Default.Speed,
          accentColor = EmeraldGreen,
          subtitle = "${String.format(Locale.ITALIAN, "%.2f", stats.avgRealLPer100Km)} L/100km",
          modifier = Modifier.weight(1f),
          testTag = "kpi_avg_consumption"
        )

        KpiCard(
          title = "Prezzo Medio",
          value = String.format(Locale.ITALIAN, "€ %.3f", stats.weightedAvgPricePerLiter),
          unit = "/L",
          icon = Icons.Default.LocalGasStation,
          accentColor = MaterialTheme.colorScheme.primary,
          subtitle = String.format(Locale.ITALIAN, "€ %.3f /km", stats.avgCostPerKm),
          modifier = Modifier.weight(1f),
          testTag = "kpi_avg_price"
        )
      }

      // Confronto Cruscotto vs Consumo Effettivo
      if (stats.avgTripConsumption > 0 && stats.avgRealLPer100Km > 0) {
        Spacer(modifier = Modifier.height(14.dp))
        ComparisonCard(
          tripAvgL100 = stats.avgTripConsumption,
          realAvgL100 = stats.avgRealLPer100Km
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Ultimo Rifornimento Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Ultimo Rifornimento",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onBackground
        )

        OutlinedButton(
          onClick = onNavigateToHistory,
          modifier = Modifier.testTag("view_all_refuels_button")
        ) {
          Text("Vedi tutti (${entries.size})", style = MaterialTheme.typography.labelMedium)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Mostra la scheda dell'ultimo rifornimento
      entries.firstOrNull()?.let { latest ->
        RefuelCard(
          entry = latest,
          onEdit = { onEditEntry(latest) },
          onDelete = { onDeleteEntry(latest) }
        )
      }

      Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
    }
  }
}

@Composable
private fun ComparisonCard(
  tripAvgL100: Double,
  realAvgL100: Double
) {
  val diff = realAvgL100 - tripAvgL100
  val isRealHigher = diff > 0.05
  val isRealLower = diff < -0.05

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("consumption_comparison_card")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.CompareArrows,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Confronto Consumo: Reale vs Cruscotto",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Dichiarato Cruscotto",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = String.format(Locale.ITALIAN, "%.2f L/100km", tripAvgL100),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Effettivo Reale",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = String.format(Locale.ITALIAN, "%.2f L/100km", realAvgL100),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = CyanAccent
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Differenza",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = if (isRealHigher) "+${String.format(Locale.ITALIAN, "%.2f", diff)} L"
            else String.format(Locale.ITALIAN, "%.2f L", diff),
            style = MaterialTheme.typography.bodyLarge.copy(
              fontWeight = FontWeight.Bold,
              color = if (isRealHigher) AmberAccent else EmeraldGreen
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      val explanation = when {
        isRealHigher -> "Il consumo effettivo calcolato dai litri è leggermente superiore a quello del computer di bordo."
        isRealLower -> "Ottimo! L'auto consuma leggermente meno di quanto indicato a display."
        else -> "Il computer di bordo coincide perfettamente con il consumo effettivo calcolato."
      }
      Text(
        text = explanation,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun EmptyStateSection(
  onAddNewRefuel: () -> Unit,
  onAddSampleData: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp)
      .testTag("empty_state_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(64.dp)
      ) {
        Icon(
          imageVector = Icons.Default.LocalGasStation,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier
            .padding(14.dp)
            .size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Nessun rifornimento registrato",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Inizia inserendo il tuo primo rifornimento con data, km percorsi, litri, prezzo e scontrino per calcolare consumi e costi.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )

      Spacer(modifier = Modifier.height(20.dp))

      Button(
        onClick = onAddNewRefuel,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("empty_add_refuel_button")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Inserisci Primo Rifornimento")
      }

      Spacer(modifier = Modifier.height(10.dp))

      OutlinedButton(
        onClick = onAddSampleData,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("load_sample_data_button")
      ) {
        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = AmberAccent)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Carica Dati di Esempio")
      }
    }
  }
}
