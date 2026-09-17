package org.catholiccompanion.app.ui.previews

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import org.catholiccompanion.app.ai.AiCitation
import org.catholiccompanion.app.ai.AiDayContext
import org.catholiccompanion.app.data.bible.BibleVerse
import org.catholiccompanion.app.data.bible.ResolvedBiblePassage
import org.catholiccompanion.app.data.bible.ResolvedBibleSection
import org.catholiccompanion.app.data.liturgy.CalendarScopeEntity
import org.catholiccompanion.app.data.liturgy.CelebrationOptionEntity
import org.catholiccompanion.app.data.liturgy.LiturgicalDayEntity
import org.catholiccompanion.app.data.liturgy.LiturgicalDayRecord
import org.catholiccompanion.app.data.liturgy.ReadingReferenceEntity
import org.catholiccompanion.app.model.MysterySet
import org.catholiccompanion.app.model.RosaryProgress
import org.catholiccompanion.app.ui.LearnMessage
import org.catholiccompanion.app.ui.LearnMessageRole
import org.catholiccompanion.app.ui.LearnUiState
import org.catholiccompanion.app.ui.RosaryUiState
import org.catholiccompanion.app.ui.TodayUiState
import org.catholiccompanion.app.ui.screens.LearnScreen
import org.catholiccompanion.app.ui.screens.RosaryScreen
import org.catholiccompanion.app.ui.screens.SettingsScreen
import org.catholiccompanion.app.ui.screens.TodayScreen
import org.catholiccompanion.app.ui.theme.CatholicCompanionTheme

private val sampleScope = CalendarScopeEntity(
    id = "roman-2026",
    displayName = "General Roman Calendar 2026",
    rite = "Roman Rite",
    region = "General Roman",
    coverageStart = "2026-01-01",
    coverageEnd = "2026-12-31",
    sourceName = "Vatican Liturgical Calendar Data",
    sourceUrl = "https://www.vatican.va",
    permissionStatus = "Public Domain / Bundled Release",
    contentVersion = "1.0.0",
    reviewedAt = "2026-01-15",
)

private val sampleDay = LiturgicalDayEntity(
    calendarId = "roman-2026",
    date = "2026-03-29",
    season = "Lent",
    liturgicalColor = "Violet / Red",
    contentReleaseId = "rel-2026-01",
)

private val sampleCelebration = CelebrationOptionEntity(
    id = "cel-2026-03-29",
    calendarId = "roman-2026",
    date = "2026-03-29",
    title = "Palm Sunday of the Passion of the Lord",
    rank = "Solemnity",
    isPrimary = true,
)

private val sampleReading = ReadingReferenceEntity(
    id = "rdg-1",
    celebrationId = "cel-2026-03-29",
    orderIndex = 1,
    label = "First Reading",
    citation = "Isaiah 50:4-7",
    permittedText = null,
    sourceDocumentTitle = "Mass Lectionary Readings for Palm Sunday",
    sourceUrl = "https://www.usccb.org/bible/readings",
)

private val samplePassage = ResolvedBiblePassage(
    sections = listOf(
        ResolvedBibleSection(
            citation = "Isaiah 50:4-7",
            verses = listOf(
                BibleVerse(50, 4, "The Lord GOD has given me the tongue of those who are taught, that I may know how to sustain with a word him who is weary."),
                BibleVerse(50, 5, "The Lord GOD has opened my ear, and I was not rebellious, neither turned away backward."),
                BibleVerse(50, 6, "I gave my back to those who struck me, and my cheeks to those who plucked out the hair; I didn't hide my face from shame and spitting."),
                BibleVerse(50, 7, "For the Lord GOD will help me; therefore I have not been confounded. Therefore I have set my face like a flint, and I know that I will not be disappointed."),
            ),
        ),
    ),
    usesPartialVerses = false,
    hasAlternatives = false,
)

private val sampleRecord = LiturgicalDayRecord(
    day = sampleDay,
    celebrations = listOf(sampleCelebration),
    selectedCelebration = sampleCelebration,
    readings = listOf(sampleReading),
)

@Preview(name = "Today Screen", showBackground = true, widthDp = 390, heightDp = 800)
@Composable
fun TodayScreenPreview() {
    CatholicCompanionTheme {
        Surface {
            TodayScreen(
                contentPadding = PaddingValues(0.dp),
                state = TodayUiState(
                    isLoading = false,
                    date = LocalDate.of(2026, 3, 29),
                    scope = sampleScope,
                    record = sampleRecord,
                    biblePassages = mapOf("rdg-1" to samplePassage),
                ),
                onPreviousDay = {},
                onNextDay = {},
                onReturnToToday = {},
                onPrayRosary = {},
            )
        }
    }
}

@Preview(name = "Rosary Setup", showBackground = true, widthDp = 390, heightDp = 800)
@Composable
fun RosarySetupPreview() {
    CatholicCompanionTheme {
        Surface {
            RosaryScreen(
                contentPadding = PaddingValues(0.dp),
                state = RosaryUiState(
                    isLoading = false,
                    selectedSet = MysterySet.GLORIOUS,
                    progress = null,
                    soundEnabled = true,
                    hapticsEnabled = true,
                ),
                onSelectSet = {},
                onStart = {},
                onPrevious = {},
                onNext = {},
                onStartAgain = {},
                onEndSession = {},
                onSoundEnabledChange = {},
                onHapticsEnabledChange = {},
            )
        }
    }
}

@Preview(name = "Rosary Active Prayer", showBackground = true, widthDp = 390, heightDp = 800)
@Composable
fun RosaryActivePreview() {
    CatholicCompanionTheme {
        Surface {
            RosaryScreen(
                contentPadding = PaddingValues(0.dp),
                state = RosaryUiState(
                    isLoading = false,
                    selectedSet = MysterySet.GLORIOUS,
                    progress = RosaryProgress(MysterySet.GLORIOUS, 16),
                    soundEnabled = true,
                    hapticsEnabled = true,
                ),
                onSelectSet = {},
                onStart = {},
                onPrevious = {},
                onNext = {},
                onStartAgain = {},
                onEndSession = {},
                onSoundEnabledChange = {},
                onHapticsEnabledChange = {},
            )
        }
    }
}

@Preview(name = "Learn Screen", showBackground = true, widthDp = 390, heightDp = 800)
@Composable
fun LearnScreenPreview() {
    CatholicCompanionTheme {
        Surface {
            LearnScreen(
                contentPadding = PaddingValues(0.dp),
                state = LearnUiState(
                    isConfigured = true,
                    draft = "",
                    messages = listOf(
                        LearnMessage(
                            role = LearnMessageRole.USER,
                            text = "What is the spiritual meaning of the Resurrection?",
                        ),
                        LearnMessage(
                            role = LearnMessageRole.ASSISTANT,
                            text = "The Resurrection of Jesus Christ is the central pillar of Catholic faith and hope.\n\n### Key Theological Reflections\n* **Victory over Death**: Through His Resurrection, Christ conquered sin and eternal death, opening the gates of Heaven to humanity.\n* **Fulfillment of Scripture**: The Resurrection fulfills ancient prophecies and confirms the divine authority of Jesus Christ.\n* **Pledge of Our Resurrection**: St. Paul reminds us that as Christ was raised, those who believe will also share in His eternal life.",
                            citations = listOf(
                                AiCitation(
                                    title = "Catechism of the Catholic Church §638",
                                    url = "https://www.vatican.va/archive/ENG0015/_P1V.HTM",
                                    startIndex = 0,
                                    endIndex = 0,
                                ),
                            ),
                        ),
                    ),
                ),
                dayContext = AiDayContext(
                    date = "2026-03-29",
                    calendar = sampleScope.displayName,
                    celebration = sampleCelebration.title,
                    rank = sampleCelebration.rank,
                    season = sampleDay.season,
                    readingReferences = listOf("First Reading: Isaiah 50:4-7"),
                ),
                mysterySet = MysterySet.GLORIOUS,
                onDraftChange = {},
                onAsk = { _, _, _ -> },
                onClear = {},
            )
        }
    }
}

@Preview(name = "Settings Screen", showBackground = true, widthDp = 390, heightDp = 800)
@Composable
fun SettingsScreenPreview() {
    CatholicCompanionTheme {
        Surface {
            SettingsScreen(contentPadding = PaddingValues(0.dp))
        }
    }
}
