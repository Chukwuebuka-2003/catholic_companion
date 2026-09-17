package org.catholiccompanion.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.catholiccompanion.app.ui.theme.CatholicCompanionTheme

@Composable
fun SettingsScreen(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Preferences", style = MaterialTheme.typography.headlineLarge)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SettingSummary("Calendar", "General Roman Calendar · 2026 offline")
                HorizontalDivider()
                SettingSummary("Appearance", "Follow system")
                HorizontalDivider()
                SettingSummary("Downloads", "Rosary and 2026 calendar bundled")
                HorizontalDivider()
                SettingSummary(
                    "Angelus reminders",
                    "Manage 6 AM, noon, and 6 PM reminders on the Angelus page",
                )
                HorizontalDivider()
                SettingSummary("Privacy", "Prayer progress stays on this device")
            }
        }
        Text(
            "Catholic Companion 0.5.0",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingSummary(title: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    CatholicCompanionTheme {
        Surface {
            SettingsScreen(contentPadding = PaddingValues(0.dp))
        }
    }
}
