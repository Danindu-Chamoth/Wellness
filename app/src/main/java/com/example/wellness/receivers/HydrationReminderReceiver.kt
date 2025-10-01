package com.example.wellness.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.example.wellness.services.HydrationReminderService

class HydrationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "DRINK_WATER" -> {
                // Record water intake and dismiss notification
                recordWaterIntake(context)
                dismissNotification(context)
                scheduleNextReminder(context)
                Toast.makeText(context, "Great job! Stay hydrated! 💧", Toast.LENGTH_SHORT).show()
            }
            "SNOOZE_REMINDER" -> {
                // Snooze for 10 minutes
                dismissNotification(context)
                HydrationReminderService.scheduleHydrationReminder(context, 10)
                Toast.makeText(context, "Reminder snoozed for 10 minutes ⏰", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Default reminder trigger - show notification
                showHydrationReminder(context)
            }
        }
    }

    private fun recordWaterIntake(context: Context) {
        val prefs = context.getSharedPreferences("wellness_data", Context.MODE_PRIVATE)
        val currentIntake = prefs.getInt("daily_water_intake", 0)
        prefs.edit().putInt("daily_water_intake", currentIntake + 250).apply() // Add 250ml

        // Update last water intake timestamp
        prefs.edit().putLong("last_water_intake", System.currentTimeMillis()).apply()
    }

    private fun dismissNotification(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            cancel(HydrationReminderService.NOTIFICATION_ID)
        }
    }

    private fun scheduleNextReminder(context: Context) {
        val prefs = context.getSharedPreferences("wellness_settings", Context.MODE_PRIVATE)
        val reminderEnabled = prefs.getBoolean("water_reminders_enabled", false)

        if (reminderEnabled) {
            val intervalIndex = prefs.getInt("reminder_interval", 1)
            val intervalMinutes = HydrationReminderService.getIntervalMinutes(intervalIndex)
            HydrationReminderService.scheduleHydrationReminder(context, intervalMinutes)
        }
    }

    private fun showHydrationReminder(context: Context) {
        val serviceIntent = Intent(context, HydrationReminderService::class.java)
        context.startService(serviceIntent)
    }
}
