package org.catholiccompanion.app.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import org.catholiccompanion.app.MainActivity
import org.catholiccompanion.app.R

class AngelusReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduler = AngelusReminderScheduler(context)
        if (!scheduler.isEnabled()) return

        if (intent.action == AngelusReminderScheduler.ACTION_ANGELUS_REMINDER) {
            val hour = intent.getIntExtra(AngelusReminderScheduler.EXTRA_REMINDER_HOUR, -1)
            if (hour !in AngelusReminderScheduler.REMINDER_HOURS) return
            scheduler.schedule(hour)
            showReminder(context, hour)
        } else {
            scheduler.scheduleAll()
        }
    }

    private fun showReminder(context: Context, hour: Int) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Angelus reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Prayer reminders at 6 AM, noon, and 6 PM"
            },
        )

        val openAngelus = PendingIntent.getActivity(
            context,
            OPEN_ANGELUS_REQUEST_CODE,
            Intent(context, MainActivity::class.java)
                .putExtra(AngelusReminderScheduler.EXTRA_OPEN_ANGELUS, true),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val message = when (hour) {
            6 -> "Good morning. It is time to pray the Angelus."
            12 -> "It is noon. Take a moment to pray the Angelus."
            else -> "Good evening. It is time to pray the Angelus."
        }
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for the Angelus")
            .setContentText(message)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setContentIntent(openAngelus)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(hour, notification)
    }

    private companion object {
        const val CHANNEL_ID = "angelus_reminders"
        const val OPEN_ANGELUS_REQUEST_CODE = 4100
    }
}
