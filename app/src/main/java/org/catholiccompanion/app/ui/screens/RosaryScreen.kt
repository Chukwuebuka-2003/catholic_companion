package org.catholiccompanion.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import org.catholiccompanion.app.R
import org.catholiccompanion.app.audio.RosaryHaptics
import org.catholiccompanion.app.audio.RosarySoundEffects
import org.catholiccompanion.app.model.MysterySet
import org.catholiccompanion.app.model.PrayerStep
import org.catholiccompanion.app.model.RosaryProgress
import org.catholiccompanion.app.ui.RosaryUiState
import org.catholiccompanion.app.ui.components.RosaryBeads
import org.catholiccompanion.app.ui.theme.CatholicCompanionTheme

@Composable
fun RosaryScreen(
    contentPadding: PaddingValues,
    state: RosaryUiState,
    onSelectSet: (MysterySet) -> Unit,
    onStart: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onStartAgain: () -> Unit,
    onEndSession: () -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onHapticsEnabledChange: (Boolean) -> Unit,
) {
    val appContext = LocalContext.current.applicationContext
    val sounds = remember(appContext) { RosarySoundEffects(appContext) }
    val haptics = remember(appContext) { RosaryHaptics(appContext) }
    DisposableEffect(sounds) {
        onDispose { sounds.close() }
    }
    val updateSoundEnabled: (Boolean) -> Unit = { enabled ->
        onSoundEnabledChange(enabled)
        if (enabled) sounds.playBead()
    }
    val updateHapticsEnabled: (Boolean) -> Unit = { enabled ->
        onHapticsEnabledChange(enabled)
        if (enabled) haptics.playBead()
    }
    val testFeedback: () -> Unit = {
        if (state.soundEnabled) sounds.playBead()
        if (state.hapticsEnabled) haptics.playBead()
    }

    when {
        state.isLoading -> LoadingRosary(contentPadding)
        state.progress == null -> RosarySetup(
            contentPadding = contentPadding,
            selectedSet = state.selectedSet,
            soundEnabled = state.soundEnabled,
            hapticsEnabled = state.hapticsEnabled,
            onSelectSet = onSelectSet,
            onStart = onStart,
            onSoundEnabledChange = updateSoundEnabled,
            onHapticsEnabledChange = updateHapticsEnabled,
            onTestFeedback = testFeedback,
        )
        state.isComplete -> RosaryComplete(contentPadding, state.progress.mysterySet, onStartAgain, onEndSession)
        else -> ActiveRosary(
            contentPadding = contentPadding,
            state = state,
            onPrevious = onPrevious,
            onNext = onNext,
            onRestart = onStartAgain,
            onEndSession = onEndSession,
            sounds = sounds,
            haptics = haptics,
            onSoundEnabledChange = updateSoundEnabled,
            onHapticsEnabledChange = updateHapticsEnabled,
            onTestFeedback = testFeedback,
        )
    }
}

@Composable
private fun LoadingRosary(contentPadding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun RosarySetup(
    contentPadding: PaddingValues,
    selectedSet: MysterySet,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onSelectSet: (MysterySet) -> Unit,
    onStart: () -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onHapticsEnabledChange: (Boolean) -> Unit,
    onTestFeedback: () -> Unit,
) {
    val today = remember { LocalDate.now() }
    val suggested = remember(today) { MysterySet.recommendedFor(today.dayOfWeek) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Pray at your own pace.", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Choose a set of mysteries. The suggestion follows the customary weekly pattern, and you can " +
                "always choose another set.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            "Suggested today: ${suggested.displayName}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MysterySet.entries.forEach { set ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedSet == set,
                            onClick = { onSelectSet(set) },
                            role = Role.RadioButton,
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedSet == set) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selectedSet == set, onClick = null)
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(set.displayName, style = MaterialTheme.typography.titleMedium)
                            Text(
                                set.mysteries.joinToString { it.title.removePrefix("The ") },
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }

        FeedbackSettings(
            soundEnabled = soundEnabled,
            hapticsEnabled = hapticsEnabled,
            onSoundEnabledChange = onSoundEnabledChange,
            onHapticsEnabledChange = onHapticsEnabledChange,
            onTestFeedback = onTestFeedback,
        )
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text("Begin ${selectedSet.displayName}")
        }
    }
}

@Composable
private fun ActiveRosary(
    contentPadding: PaddingValues,
    state: RosaryUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit,
    onEndSession: () -> Unit,
    sounds: RosarySoundEffects,
    haptics: RosaryHaptics,
    onSoundEnabledChange: (Boolean) -> Unit,
    onHapticsEnabledChange: (Boolean) -> Unit,
    onTestFeedback: () -> Unit,
) {
    val progress = requireNotNull(state.progress)
    val step = requireNotNull(state.currentStep)
    var showEndConfirmation by remember { mutableStateOf(false) }
    var showRestartConfirmation by remember { mutableStateOf(false) }
    val completedSteps = progress.stepIndex
    val progressFraction = completedSteps.toFloat() / state.steps.size
    val prayerScrollState = rememberScrollState()
    val currentMystery = step.decade?.let { decade ->
        progress.mysterySet.mysteries.getOrNull(decade - 1)
    }

    LaunchedEffect(step.id) {
        prayerScrollState.scrollTo(0)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    progress.mysterySet.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "Today is ${LocalDate.now().dayOfWeek.name.lowercase().replaceFirstChar(Char::uppercase)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = { showRestartConfirmation = true }) { Text("Restart") }
            TextButton(onClick = { showEndConfirmation = true }) { Text("End") }
        }

        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .semantics {
                    stateDescription = "$completedSteps of ${state.steps.size} prayer steps completed"
                },
        )

        ImmersiveRosaryHero(
            step = step,
            stepNumber = progress.stepIndex + 1,
            totalSteps = state.steps.size,
            decade = step.decade,
            modifier = Modifier
                .weight(1.22f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
        )

        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 2.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        step.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        step.instruction,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(
                    onClick = onTestFeedback,
                    enabled = state.soundEnabled || state.hapticsEnabled,
                ) {
                    Text("Test feel")
                }
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(prayerScrollState)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (currentMystery != null) {
                        Text(
                            text = "MYSTERY ${step.decade} OF 5",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        Text(
                            "OPENING & CLOSING PRAYERS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        "${progress.stepIndex + 1}/${state.steps.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = step.instruction,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(text = step.text, style = MaterialTheme.typography.bodyLarge)

                if (currentMystery != null) {
                    HorizontalDivider()
                    Text(
                        text = currentMystery.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = currentMystery.scriptureReference,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = currentMystery.summary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Fruit: ${currentMystery.fruit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = currentMystery.meditation,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    "The gold halo marks your current bead. Your place is saved automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompactFeedbackToggle(
                label = "Sound",
                checked = state.soundEnabled,
                onCheckedChange = onSoundEnabledChange,
                modifier = Modifier.weight(1f),
            )
            CompactFeedbackToggle(
                label = "Haptics",
                checked = state.hapticsEnabled,
                onCheckedChange = onHapticsEnabledChange,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = {
                    if (state.soundEnabled) sounds.playBead()
                    if (state.hapticsEnabled) haptics.playBead()
                    onPrevious()
                },
                enabled = progress.stepIndex > 0,
                modifier = Modifier.weight(1f),
            ) {
                Text("Previous")
            }
            Button(
                onClick = {
                    if (progress.stepIndex == state.steps.lastIndex) {
                        if (state.soundEnabled) sounds.playCompletion()
                    } else if (state.soundEnabled) {
                        sounds.playBead()
                    }
                    if (state.hapticsEnabled) {
                        if (progress.stepIndex == state.steps.lastIndex) {
                            haptics.playCompletion()
                        } else {
                            haptics.playBead()
                        }
                    }
                    onNext()
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(if (progress.stepIndex == state.steps.lastIndex) "Finish" else "Continue")
            }
        }
    }

    if (showRestartConfirmation) {
        AlertDialog(
            onDismissRequest = { showRestartConfirmation = false },
            title = { Text("Restart this Rosary?") },
            text = { Text("This returns to the Sign of the Cross for the same mysteries.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartConfirmation = false
                        onRestart()
                    },
                ) { Text("Restart") }
            },
            dismissButton = {
                TextButton(onClick = { showRestartConfirmation = false }) { Text("Cancel") }
            },
        )
    }

    if (showEndConfirmation) {
        AlertDialog(
            onDismissRequest = { showEndConfirmation = false },
            title = { Text("End this Rosary?") },
            text = { Text("This will remove the saved place for the current prayer session.") },
            confirmButton = {
                TextButton(onClick = onEndSession) { Text("End session") }
            },
            dismissButton = {
                TextButton(onClick = { showEndConfirmation = false }) { Text("Keep praying") }
            },
        )
    }
}

@Composable
private fun ImmersiveRosaryHero(
    step: PrayerStep,
    stepNumber: Int,
    totalSteps: Int,
    decade: Int?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.marian_rosary_background),
                contentDescription = "The Blessed Virgin Mary holding the child Jesus",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.44f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.30f),
                            ),
                        ),
                    ),
            )
            RosaryBeads(
                step = step,
                stepNumber = stepNumber,
                totalSteps = totalSteps,
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp),
            )
            Surface(
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                color = Color.Black.copy(alpha = 0.58f),
                contentColor = Color.White,
                shape = RoundedCornerShape(50),
            ) {
                Text(
                    text = decade?.let { "Decade $it of 5" } ?: "Opening prayers",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun FeedbackSettings(
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onSoundEnabledChange: (Boolean) -> Unit,
    onHapticsEnabledChange: (Boolean) -> Unit,
    onTestFeedback: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Rosary feel", style = MaterialTheme.typography.titleMedium)
            FeedbackSwitchRow(
                label = "Wooden bead sound",
                supportingText = "A quiet click as you move between prayers",
                checked = soundEnabled,
                onCheckedChange = onSoundEnabledChange,
            )
            FeedbackSwitchRow(
                label = "Gentle haptics",
                supportingText = "A light tactile response for each step",
                checked = hapticsEnabled,
                onCheckedChange = onHapticsEnabledChange,
            )
            OutlinedButton(
                onClick = onTestFeedback,
                enabled = soundEnabled || hapticsEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Test enabled feedback")
            }
        }
    }
}

@Composable
private fun FeedbackSwitchRow(
    label: String,
    supportingText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun CompactFeedbackToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun RosaryComplete(
    contentPadding: PaddingValues,
    set: MysterySet,
    onStartAgain: () -> Unit,
    onEndSession: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(28.dp))
        Text("Amen.", style = MaterialTheme.typography.headlineLarge)
        Text(
            "You have reached the end of the ${set.displayName}.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onEndSession, modifier = Modifier.fillMaxWidth()) {
            Text("Done")
        }
        OutlinedButton(onClick = onStartAgain, modifier = Modifier.fillMaxWidth()) {
            Text("Pray again")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RosaryScreenPreview() {
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
