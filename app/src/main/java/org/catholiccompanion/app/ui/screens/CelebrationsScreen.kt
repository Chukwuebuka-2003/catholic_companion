package org.catholiccompanion.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.catholiccompanion.app.data.liturgy.CelebrationDayRecord
import org.catholiccompanion.app.data.liturgy.CelebrationRank
import org.catholiccompanion.app.ui.BROWSABLE_RANKS
import org.catholiccompanion.app.ui.CelebrationsUiState

private val MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
private val DAY_FORMAT = DateTimeFormatter.ofPattern("EEE d", Locale.getDefault())

@Composable
fun CelebrationsScreen(
    contentPadding: PaddingValues,
    state: CelebrationsUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onReturnToCurrentMonth: () -> Unit,
    onToggleRank: (CelebrationRank) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            MonthHeader(
                state = state,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onReturnToCurrentMonth = onReturnToCurrentMonth,
            )
        }

        item {
            RankFilters(activeRanks = state.activeRanks, onToggleRank = onToggleRank)
        }

        when {
            state.isLoading -> item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.scope == null -> item {
                EmptyNotice(
                    title = "No calendar installed",
                    body = "The liturgical calendar has not been installed yet. Celebrations " +
                        "appear once a verified content release is available.",
                )
            }

            state.records.isEmpty() -> item {
                EmptyNotice(
                    title = "Nothing to show for this month",
                    body = "This month has no celebrations matching the selected ranks, or it " +
                        "falls outside the coverage of the installed calendar.",
                )
            }

            else -> items(state.records, key = { it.primary.id }) { record ->
                CelebrationRow(record = record, today = state.today)
            }
        }

        state.scope?.let { scope ->
            item {
                Text(
                    "Source: ${scope.sourceName} · ${scope.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MonthHeader(
    state: CelebrationsUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onReturnToCurrentMonth: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            state.month.atDay(1).format(MONTH_FORMAT),
            style = MaterialTheme.typography.headlineMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = onPreviousMonth) { Text("Previous") }
            OutlinedButton(onClick = onNextMonth) { Text("Next") }
            if (state.month != java.time.YearMonth.from(state.today)) {
                TextButton(onClick = onReturnToCurrentMonth) { Text("This month") }
            }
        }
    }
}

@Composable
private fun RankFilters(
    activeRanks: Set<CelebrationRank>,
    onToggleRank: (CelebrationRank) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BROWSABLE_RANKS.forEach { rank ->
            val selected = rank in activeRanks
            FilterChip(
                selected = selected,
                onClick = { onToggleRank(rank) },
                label = { Text(shortLabel(rank)) },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
    }
}

@Composable
private fun CelebrationRow(record: CelebrationDayRecord, today: LocalDate) {
    val isToday = record.date == today
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                record.date.format(DAY_FORMAT) + if (isToday) " · Today" else "",
                style = MaterialTheme.typography.labelLarge,
                color = if (isToday) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            )
            Text(record.primary.title, style = MaterialTheme.typography.titleMedium)
            Text(
                listOfNotNull(
                    record.rank.label,
                    record.day?.season?.takeIf { it.isNotBlank() },
                    record.day?.liturgicalColor?.takeIf { it.isNotBlank() },
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            if (record.alternatives.isNotEmpty()) {
                Text(
                    "Also permitted: " + record.alternatives.joinToString("; ") { it.title },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyNotice(title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun shortLabel(rank: CelebrationRank): String = when (rank) {
    CelebrationRank.OPTIONAL_MEMORIAL -> "Optional"
    else -> rank.label
}
