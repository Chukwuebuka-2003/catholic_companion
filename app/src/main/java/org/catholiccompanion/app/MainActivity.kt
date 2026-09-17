package org.catholiccompanion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.catholiccompanion.app.ui.CatholicCompanionApp
import org.catholiccompanion.app.ui.theme.CatholicCompanionTheme
import org.catholiccompanion.app.notifications.AngelusReminderScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatholicCompanionTheme {
                CatholicCompanionApp(
                    openAngelus = intent.getBooleanExtra(
                        AngelusReminderScheduler.EXTRA_OPEN_ANGELUS,
                        false,
                    ),
                )
            }
        }
    }
}
