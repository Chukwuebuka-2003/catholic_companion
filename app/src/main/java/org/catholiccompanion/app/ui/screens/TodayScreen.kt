package org.catholiccompanion.app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.catholiccompanion.app.data.bible.ResolvedBiblePassage
import org.catholiccompanion.app.data.bible.WEB_C_COPYRIGHT_URL
import org.catholiccompanion.app.data.bible.WEB_C_EDITION
import org.catholiccompanion.app.data.bible.WEB_C_TITLE
import org.catholiccompanion.app.data.liturgy.CalendarScopeEntity
import org.catholiccompanion.app.data.liturgy.CelebrationOptionEntity
import org.catholiccompanion.app.data.liturgy.LiturgicalDayEntity
import org.catholiccompanion.app.data.liturgy.LiturgicalDayRecord
import org.catholiccompanion.app.data.liturgy.ReadingReferenceEntity
import org.catholiccompanion.app.ui.TodayUiState
import org.catholiccompanion.app.ui.theme.CatholicCompanionTheme

private val displayDateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")

@Composable
fun TodayScreen(
    contentPadding: PaddingValues,
    state: TodayUiState,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onReturnToToday: () -> Unit,
    onPrayRosary: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        DateNavigator(state.date, onPreviousDay, onNextDay, onReturnToToday)

        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            state.scope == null -> NoCalendarCard()
            state.record == null -> MissingDateCard(state.scope, state.date)
            else -> LiturgicalContent(
                scope = state.scope,
                record = state.record,
                biblePassages = state.biblePassages,
            )
        }

        Spacer(Modifier.height(4.dp))
        Text("Prayer is available offline.", style = MaterialTheme.typography.titleLarge)
        Text(
            "Begin a complete five-decade Rosary. Your place is saved automatically on this device.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onPrayRosary, modifier = Modifier.fillMaxWidth()) {
            Text("Pray the Rosary")
        }
    }
}

@Composable
private fun DateNavigator(
    date: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onReturnToToday: () -> Unit,
) {
    Text(
        text = date.format(displayDateFormatter),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(onClick = onPreviousDay, modifier = Modifier.weight(1f)) {
            Text("Previous")
        }
        OutlinedButton(
            onClick = onReturnToToday,
            enabled = date != LocalDate.now(),
            modifier = Modifier.weight(1f),
        ) {
            Text("Today")
        }
        OutlinedButton(onClick = onNextDay, modifier = Modifier.weight(1f)) {
            Text("Next")
        }
    }
}

@Composable
private fun NoCalendarCard() {
    Text("Make space for the day’s liturgy.", style = MaterialTheme.typography.headlineLarge)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Liturgical calendar not installed", style = MaterialTheme.typography.titleLarge)
            Text(
                "Today will remain clearly unavailable until a launch calendar and its content sources " +
                    "are verified. The app will not guess the celebration or readings.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                "Coverage: not configured",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun MissingDateCard(scope: CalendarScopeEntity, date: LocalDate) {
    val coverageStart = runCatching { LocalDate.parse(scope.coverageStart) }.getOrNull()
    val coverageEnd = runCatching { LocalDate.parse(scope.coverageEnd) }.getOrNull()
    val outsideCoverage = coverageStart != null && coverageEnd != null && date !in coverageStart..coverageEnd

    Text(scope.displayName, style = MaterialTheme.typography.headlineLarge)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                if (outsideCoverage) "Date outside downloaded coverage" else "Verified record unavailable",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                if (outsideCoverage) {
                    "This calendar covers ${scope.coverageStart} through ${scope.coverageEnd}."
                } else {
                    "The installed release does not contain this date. Treat this as missing content, not " +
                        "as an ordinary ferial day."
                },
                style = MaterialTheme.typography.bodyLarge,
            )
            Provenance(scope)
        }
    }
}

@Composable
private fun LiturgicalContent(
    scope: CalendarScopeEntity,
    record: LiturgicalDayRecord,
    biblePassages: Map<String, ResolvedBiblePassage>,
) {
    val uriHandler = LocalUriHandler.current
    val celebration = record.selectedCelebration

    Text(celebration.title, style = MaterialTheme.typography.headlineLarge)
    Text(
        listOf(celebration.rank, record.day.season, record.day.liturgicalColor)
            .filter { it.isNotBlank() }
            .joinToString(" · "),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )

    val alternatives = record.celebrations.filterNot { it.id == celebration.id }
    if (alternatives.isNotEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Other permitted celebrations", fontWeight = FontWeight.SemiBold)
                alternatives.forEach { Text("${it.title} · ${it.rank}") }
            }
        }
    }

    Text("Readings", style = MaterialTheme.typography.titleLarge)
    if (record.readings.isEmpty()) {
        Text("No reading references were included in this content release.")
    } else {
        record.readings.forEach { reading ->
            val biblePassage = biblePassages[reading.id]
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(reading.label, style = MaterialTheme.typography.labelLarge)
                    Text(reading.citation, style = MaterialTheme.typography.titleLarge)
                    reading.permittedText?.let { text ->
                        HorizontalDivider()
                        Text("Source text", style = MaterialTheme.typography.labelMedium)
                        Text(text, style = MaterialTheme.typography.bodyLarge)
                    }
                    if (reading.permittedText == null && biblePassage != null) {
                        BiblePassageText(biblePassage)
                    } else if (reading.permittedText == null) {
                        Text(
                            "The bundled Bible could not resolve this reference. The citation is " +
                                "preserved for content review.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    TextButton(onClick = { uriHandler.openUri(reading.sourceUrl) }) {
                        Text("Reference source: ${reading.sourceDocumentTitle}")
                    }
                }
            }
        }
    }
    ScriptureEditionCard()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Calendar provenance", fontWeight = FontWeight.SemiBold)
            Provenance(scope)
            Text("Release: ${record.day.contentReleaseId}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun BiblePassageText(passage: ResolvedBiblePassage) {
    HorizontalDivider()
    if (passage.hasAlternatives) {
        Text(
            "Alternative reading selections",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
    passage.sections.forEachIndexed { index, section ->
        if (passage.hasAlternatives) {
            Text(
                "Option ${index + 1}: ${section.citation}",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Text(
            section.verses.joinToString(separator = "\n") { verse ->
                "${verse.chapter}:${verse.verse}  ${verse.text}"
            },
            style = MaterialTheme.typography.bodyLarge,
        )
    }
    if (passage.usesPartialVerses) {
        Text(
            "A lettered reference selects part of a verse; this edition displays the complete verse.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ScriptureEditionCard() {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Scripture edition", fontWeight = FontWeight.SemiBold)
            Text("$WEB_C_TITLE · $WEB_C_EDITION", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Public-domain Bible text with the traditional Catholic book set. Its wording may " +
                    "differ from the official English Lectionary used at Mass.",
                style = MaterialTheme.typography.bodySmall,
            )
            TextButton(onClick = { uriHandler.openUri(WEB_C_COPYRIGHT_URL) }) {
                Text("Open Bible source and terms")
            }
        }
    }
}

@Composable
private fun Provenance(scope: CalendarScopeEntity) {
    val uriHandler = LocalUriHandler.current
    Text("${scope.rite} · ${scope.region}", style = MaterialTheme.typography.bodyMedium)
    Text(
        "Coverage: ${scope.coverageStart} – ${scope.coverageEnd}",
        style = MaterialTheme.typography.bodyMedium,
    )
    Text(
        "Version ${scope.contentVersion} · reviewed ${scope.reviewedAt}",
        style = MaterialTheme.typography.bodySmall,
    )
    Text(scope.permissionStatus, style = MaterialTheme.typography.bodySmall)
    TextButton(onClick = { uriHandler.openUri(scope.sourceUrl) }) {
        Text("Source: ${scope.sourceName}")
    }
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenPreview() {
    CatholicCompanionTheme {
        Surface {
            TodayScreen(
                contentPadding = PaddingValues(0.dp),
                state = TodayUiState(
                    isLoading = false,
                    date = LocalDate.of(2026, 3, 29),
                    scope = CalendarScopeEntity(
                        id = "roman-2026",
                        displayName = "General Roman Calendar 2026",
                        rite = "Roman Rite",
                        region = "General Roman",
                        coverageStart = "2026-01-01",
                        coverageEnd = "2026-12-31",
                        sourceName = "Vatican Liturgical Calendar Data",
                        sourceUrl = "https://www.vatican.va",
                        permissionStatus = "Public Domain",
                        contentVersion = "1.0.0",
                        reviewedAt = "2026-01-15",
                    ),
                    record = LiturgicalDayRecord(
                        day = LiturgicalDayEntity(
                            calendarId = "roman-2026",
                            date = "2026-03-29",
                            season = "Lent",
                            liturgicalColor = "Violet",
                            contentReleaseId = "rel-1",
                        ),
                        celebrations = listOf(
                            CelebrationOptionEntity(
                                id = "cel-1",
                                calendarId = "roman-2026",
                                date = "2026-03-29",
                                title = "Palm Sunday of the Passion of the Lord",
                                rank = "Solemnity",
                                isPrimary = true,
                            )
                        ),
                        selectedCelebration = CelebrationOptionEntity(
                            id = "cel-1",
                            calendarId = "roman-2026",
                            date = "2026-03-29",
                            title = "Palm Sunday of the Passion of the Lord",
                            rank = "Solemnity",
                            isPrimary = true,
                        ),
                        readings = listOf(
                            ReadingReferenceEntity(
                                id = "r1",
                                celebrationId = "cel-1",
                                orderIndex = 1,
                                label = "First Reading",
                                citation = "Isaiah 50:4-7",
                                permittedText = null,
                                sourceDocumentTitle = "Mass Lectionary",
                                sourceUrl = "https://www.usccb.org",
                            )
                        ),
                    ),
                ),
                onPreviousDay = {},
                onNextDay = {},
                onReturnToToday = {},
                onPrayRosary = {},
            )
        }
    }
}

