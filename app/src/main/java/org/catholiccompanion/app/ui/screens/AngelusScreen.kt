package org.catholiccompanion.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.ZoneId
import org.catholiccompanion.app.audio.AngelusRecordedPlayer
import org.catholiccompanion.app.model.AngelusLanguage
import org.catholiccompanion.app.model.AngelusPrayer
import org.catholiccompanion.app.notifications.AngelusReminderScheduler

@Composable
fun AngelusScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current.applicationContext
    val narrator = remember(context) { AngelusRecordedPlayer(context) }
    val reminderScheduler = remember(context) { AngelusReminderScheduler(context) }
    val narration by narrator.state.collectAsStateWithLifecycle()
    var language by rememberSaveable { mutableStateOf(AngelusLanguage.ENGLISH) }
    var remindersEnabled by rememberSaveable { mutableStateOf(reminderScheduler.isEnabled()) }
    var permissionDenied by rememberSaveable { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionDenied = !granted
        remindersEnabled = granted
        reminderScheduler.setEnabled(granted)
    }
    val spokenParts = remember(language) {
        AngelusPrayer.parts.map { part -> part.text(language) }
    }

    DisposableEffect(narrator) {
        onDispose { narrator.close() }
    }
    LaunchedEffect(Unit) {
        val permissionGranted =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (remindersEnabled && !permissionGranted) {
            reminderScheduler.setEnabled(false)
            remindersEnabled = false
            permissionDenied = true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "The Angelus",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Pray in English or Latin, and listen as each part is read aloud.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AngelusLanguage.entries.forEach { option ->
                    FilterChip(
                        selected = language == option,
                        onClick = {
                            if (language != option) {
                                narrator.stop()
                                language = option
                            }
                        },
                        label = { Text(option.displayName) },
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(
                            text = "Angelus reminders",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "6:00 AM · 12:00 PM · 6:00 PM",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "Uses your device time zone (${ZoneId.systemDefault().id}) and updates when it changes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (permissionDenied) {
                            Text(
                                text = "Allow notifications to turn on Angelus reminders.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    Switch(
                        checked = remindersEnabled,
                        onCheckedChange = { enabled ->
                            permissionDenied = false
                            if (!enabled) {
                                reminderScheduler.setEnabled(false)
                                remindersEnabled = false
                            } else if (
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                                PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS,
                                )
                            } else {
                                reminderScheduler.setEnabled(true)
                                remindersEnabled = true
                            }
                        },
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Audio prayer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = when {
                            !narration.isReady -> "Preparing your device's speech voice…"
                            narration.isPlaying -> "Reading part ${(narration.currentPartIndex ?: 0) + 1} of ${spokenParts.size}"
                            narration.isPaused -> "Paused at part ${(narration.currentPartIndex ?: 0) + 1} of ${spokenParts.size}"
                            else -> "Ready to read the complete ${language.displayName} prayer"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    if (narration.isPlaying || narration.isPaused) {
                        val progress = ((narration.currentPartIndex ?: 0) + 1).toFloat() / spokenParts.size
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                when {
                                    narration.isPlaying -> narrator.pause()
                                    narration.isPaused && narration.activeLanguage == language -> {
                                        narrator.resume()
                                    }
                                    else -> narrator.play(language)
                                }
                            },
                            enabled = narration.isReady,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                when {
                                    narration.isPlaying -> "Pause"
                                    narration.isPaused -> "Resume"
                                    else -> "Play"
                                },
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        OutlinedButton(
                            onClick = narrator::stop,
                            enabled = narration.isPlaying || narration.isPaused,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Stop")
                        }
                    }

                    narration.voiceNotice?.let { notice ->
                        Text(
                            text = notice,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    narration.error?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Text(
                        text = "AI-generated prayer audio is bundled with the app and works offline. Use your phone's media volume to adjust it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        itemsIndexed(AngelusPrayer.parts) { index, part ->
            val isCurrent = narration.activeLanguage == language &&
                narration.currentPartIndex == index &&
                (narration.isPlaying || narration.isPaused)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = if (isCurrent) {
                            "Currently reading. ${part.role.orEmpty()} ${part.text(language)}"
                        } else {
                            "${part.role.orEmpty()} ${part.text(language)}"
                        }
                    }
                    .clickable { narrator.play(language, index) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    part.role?.let { role ->
                        Text(
                            text = role,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = part.text(language),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            val uriHandler = LocalUriHandler.current
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)) {
                Text(
                    text = "Prayer text sources",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "English: United States Conference of Catholic Bishops. Latin: The Holy See.",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row {
                    TextButton(onClick = { uriHandler.openUri(AngelusPrayer.ENGLISH_SOURCE_URL) }) {
                        Text("English source")
                    }
                    TextButton(onClick = { uriHandler.openUri(AngelusPrayer.LATIN_SOURCE_URL) }) {
                        Text("Latin source")
                    }
                }
            }
        }
    }
}
