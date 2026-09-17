package org.catholiccompanion.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import java.time.ZonedDateTime

class AngelusReminderScheduler(context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager = requireNotNull(
        appContext.getSystemService(AlarmManager::class.java),
    )
    private val preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = preferences.getBoolean(REMINDERS_ENABLED, false)

    fun setEnabled(enabled: Boolean) {
        preferences.edit { putBoolean(REMINDERS_ENABLED, enabled) }
        if (enabled) {
            scheduleAll()
        } else {
            cancelAll()
        }
    }

    fun scheduleAll() {
        if (!isEnabled()) return
        REMINDER_HOURS.forEach(::schedule)
    }

    fun schedule(hour: Int) {
        if (!isEnabled() || hour !in REMINDER_HOURS) return
        val triggerAt = nextOccurrence(ZonedDateTime.now(), hour)
        val pendingIntent = requireNotNull(
            reminderIntent(hour, PendingIntent.FLAG_UPDATE_CURRENT),
        )
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt.toInstant().toEpochMilli(),
            pendingIntent,
        )
    }

    private fun cancelAll() {
        REMINDER_HOURS.forEach { hour ->
            val intent = reminderIntent(hour, PendingIntent.FLAG_NO_CREATE)
            if (intent != null) {
                alarmManager.cancel(intent)
                intent.cancel()
            }
        }
    }

    private fun reminderIntent(hour: Int, additionalFlag: Int): PendingIntent? {
        val intent = Intent(appContext, AngelusReminderReceiver::class.java)
            .setAction(ACTION_ANGELUS_REMINDER)
            .putExtra(EXTRA_REMINDER_HOUR, hour)
        return PendingIntent.getBroadcast(
            appContext,
            hour,
            intent,
            additionalFlag or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_ANGELUS_REMINDER =
            "org.catholiccompanion.app.action.ANGELUS_REMINDER"
        const val EXTRA_REMINDER_HOUR = "angelus_reminder_hour"
        const val EXTRA_OPEN_ANGELUS = "open_angelus"
        val REMINDER_HOURS = listOf(6, 12, 18)

        private const val PREFERENCES_NAME = "angelus_reminder_preferences"
        private const val REMINDERS_ENABLED = "reminders_enabled"

        internal fun nextOccurrence(now: ZonedDateTime, hour: Int): ZonedDateTime {
            require(hour in 0..23)
            val today = now
                .withHour(hour)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
            return if (today.isAfter(now)) today else today.plusDays(1)
        }
    }
}
