package com.example.wellness.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.wellness.R
import com.example.wellness.activities.MainActivity
import com.example.wellness.receivers.HydrationReminderReceiver
import java.util.*

class HydrationReminderService : Service() {

    companion object {
        const val CHANNEL_ID = "hydration_reminders"
        const val NOTIFICATION_ID = 1001

        fun scheduleHydrationReminder(context: Context, intervalMinutes: Int) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, HydrationReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Handle special case for 5-second test interval
                val intervalMillis = if (intervalMinutes == 0) {
                    5 * 1000L // 5 seconds for testing
                } else {
                    intervalMinutes * 60 * 1000L // Normal intervals in minutes
                }

                val triggerTime = System.currentTimeMillis() + intervalMillis

                // Check if we can schedule exact alarms (Android 12+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    } else {
                        // Fallback to inexact alarm
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                // Permission denied - this shouldn't crash the app
                throw Exception("Permission denied for scheduling alarms")
            } catch (e: Exception) {
                // Any other error
                throw Exception("Failed to schedule alarm: ${e.message}")
            }
        }

        fun cancelHydrationReminder(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, HydrationReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            } catch (e: Exception) {
                // Ignore cancellation errors
            }
        }

        fun getIntervalMinutes(intervalIndex: Int): Int {
            return when (intervalIndex) {
                0 -> 1     // Every 1 minute
                1 -> 30    // Every 30 minutes
                2 -> 60    // Every 1 hour
                3 -> 120   // Every 2 hours
                4 -> 180   // Every 3 hours
                else -> 60 // Default: 1 hour
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showHydrationNotification()
        scheduleNextReminder()
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hydration Reminders"
            val descriptionText = "Notifications to remind you to stay hydrated"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = 0xFF2196F3.toInt() // Blue color
                enableVibration(true)
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleNextReminder() {
        val prefs = getSharedPreferences("wellness_settings", Context.MODE_PRIVATE)
        val reminderEnabled = prefs.getBoolean("water_reminders_enabled", false)

        if (reminderEnabled) {
            val intervalIndex = prefs.getInt("reminder_interval", 1)
            val intervalMinutes = getIntervalMinutes(intervalIndex)
            scheduleHydrationReminder(this, intervalMinutes)
        }
    }

    private fun showHydrationNotification() {
        // Intent to open app when notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Intent for "Drink Water" action
        val drinkWaterIntent = Intent(this, HydrationReminderReceiver::class.java).apply {
            action = "DRINK_WATER"
        }
        val drinkWaterPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            drinkWaterIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Intent for "Snooze 10m" action
        val snoozeIntent = Intent(this, HydrationReminderReceiver::class.java).apply {
            action = "SNOOZE_REMINDER"
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            2,
            snoozeIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("💧 Hydration Time!")
            .setContentText("Time to drink some water! Stay hydrated for better health and energy.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Time to drink some water! Stay hydrated for better health and energy."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setColor(0xFF2196F3.toInt()) // Blue color matching the image
            .addAction(R.drawable.ic_water_drop, "💧 Drink Water", drinkWaterPendingIntent)
            .addAction(R.drawable.ic_snooze, "⏰ Snooze 10m", snoozePendingIntent)
            .setOngoing(false)
            .setTimeoutAfter(600000) // Auto-dismiss after 10 minutes
            .build()

        with(NotificationManagerCompat.from(this)) {
            try {
                notify(NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                // Permission denied - notification won't be shown but app won't crash
            }
        }

        stopSelf()
    }
}
