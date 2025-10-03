package com.example.wellness.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.wellness.models.Habit
import com.example.wellness.models.Mood
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

object DashboardDataHelper {

    private const val WELLNESS_PREFS = "WellnessPrefs"
    private const val SETTINGS_PREFS = "wellness_settings"
    private val gson = Gson()

    fun getHabitsData(context: Context): DashboardHabitsData {
        val sharedPreferences = context.getSharedPreferences(WELLNESS_PREFS, Context.MODE_PRIVATE)
        val habitsJson = sharedPreferences.getString("habits", "[]")
        val habitsType = object : TypeToken<List<Habit>>() {}.type
        val habits: List<Habit> = gson.fromJson(habitsJson, habitsType) ?: emptyList()

        val totalHabits = habits.size
        val completedHabits = habits.count { it.isCompleted }
        val percentage = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 0

        return DashboardHabitsData(
            totalHabits = totalHabits,
            completedHabits = completedHabits,
            percentage = percentage,
            statusMessage = when {
                totalHabits == 0 -> "No habits yet - Add some!"
                completedHabits == totalHabits && totalHabits > 0 -> "🎉 All habits completed!"
                completedHabits == 0 -> "Start your habits for today!"
                else -> "Keep going! You're doing great!"
            }
        )
    }

    fun getTodayMoodData(context: Context): DashboardMoodData {
        val sharedPreferences = context.getSharedPreferences(WELLNESS_PREFS, Context.MODE_PRIVATE)
        val moodsJson = sharedPreferences.getString("moods", "[]")
        val moodsType = object : TypeToken<List<Mood>>() {}.type
        val moods: List<Mood> = gson.fromJson(moodsJson, moodsType) ?: emptyList()

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayMood = moods.lastOrNull { mood ->
            val moodDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(mood.dateTime)
            moodDate == today
        }

        return if (todayMood != null) {
            DashboardMoodData(
                emoji = todayMood.emoji,
                status = "Mood: ${todayMood.moodType}",
                note = todayMood.note,
                hasNote = todayMood.note.isNotEmpty(),
                hasMood = true
            )
        } else {
            DashboardMoodData(
                emoji = "❓",
                status = "No mood logged yet",
                note = "Tap to add your mood",
                hasNote = true,
                hasMood = false
            )
        }
    }

    fun getHydrationData(context: Context): DashboardHydrationData {
        val wellnessPreferences = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        val remindersEnabled = wellnessPreferences.getBoolean("water_reminders_enabled", false)
        val reminderInterval = wellnessPreferences.getInt("reminder_interval", 2)
        val waterIntake = wellnessPreferences.getInt("water_intake", 0)

        val intervalText = when (reminderInterval) {
            0 -> "30min"
            1 -> "1h"
            2 -> "2h"
            3 -> "3h"
            else -> "${reminderInterval}h"
        }

        val statusText = if (remindersEnabled) {
            "Reminders: ON (every $intervalText)"
        } else {
            "Reminders: OFF"
        }

        return DashboardHydrationData(
            remindersEnabled = remindersEnabled,
            statusText = statusText,
            waterIntake = waterIntake,
            waterGoal = 8,
            intakeText = "Water: $waterIntake/8 glasses"
        )
    }

    fun updateWaterIntake(context: Context, glasses: Int) {
        val wellnessPreferences = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        wellnessPreferences.edit()
            .putInt("water_intake", glasses)
            .apply()
    }

    fun getMoodTrendData(context: Context, days: Int): MoodTrendData {
        val sharedPreferences = context.getSharedPreferences(WELLNESS_PREFS, Context.MODE_PRIVATE)
        val moodsJson = sharedPreferences.getString("moods", "[]")
        val moodsType = object : TypeToken<List<Mood>>() {}.type
        val moods: List<Mood> = gson.fromJson(moodsJson, moodsType) ?: emptyList()

        // Generate date labels based on the period
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dates = mutableListOf<String>()
        val chartEntries = mutableListOf<com.github.mikephil.charting.data.Entry>()

        // Get the last 'days' dates and create chart entries
        for (i in days-1 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, -i)
            dates.add(dateFormat.format(calendar.time))

            // Find mood for this date
            val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dayString = dayFormat.format(calendar.time)
            val dayMood = moods.lastOrNull { mood ->
                val moodDateString = dayFormat.format(mood.dateTime)
                moodDateString == dayString
            }

            val moodValue = if (dayMood != null) {
                getMoodValue(dayMood.emoji).toFloat()
            } else {
                3.0f // Default neutral value
            }

            chartEntries.add(com.github.mikephil.charting.data.Entry((days - i - 1).toFloat(), moodValue))
        }

        // Calculate mood trend statistics
        val recentMoods = moods.filter { mood ->
            val moodDate = Calendar.getInstance().apply { time = mood.dateTime }
            val daysDiff = ((Date().time - mood.dateTime.time) / (1000 * 60 * 60 * 24)).toInt()
            daysDiff < days
        }

        // Calculate average mood
        val averageMood = if (recentMoods.isNotEmpty()) {
            val moodValues = recentMoods.map { getMoodValue(it.emoji) }
            val avg = moodValues.average()
            getMoodLabel(avg)
        } else {
            "No data"
        }

        // Calculate trend
        val trend = if (recentMoods.size >= 2) {
            val firstHalf = recentMoods.take(recentMoods.size / 2).map { getMoodValue(it.emoji) }.average()
            val secondHalf = recentMoods.takeLast(recentMoods.size / 2).map { getMoodValue(it.emoji) }.average()
            when {
                secondHalf > firstHalf + 0.5 -> "Improving"
                secondHalf < firstHalf - 0.5 -> "Declining"
                else -> "Stable"
            }
        } else {
            "Stable"
        }

        // Create status message
        val statusMessage = when {
            recentMoods.isEmpty() -> "No mood data available 📊"
            trend == "Improving" -> "Your mood is improving! 📈"
            trend == "Declining" -> "Consider self-care activities 💙"
            else -> "Your mood is stable 😊"
        }

        return MoodTrendData(
            date1 = dates.getOrElse(0) { "" },
            date2 = dates.getOrElse(1) { "" },
            date3 = dates.getOrElse(2) { "" },
            date4 = dates.getOrElse(3) { "" },
            date5 = dates.getOrElse(4) { "" },
            date6 = dates.getOrElse(5) { "" },
            date7 = dates.getOrElse(6) { "" },
            averageValue = averageMood,
            trendValue = trend,
            periodValue = "$days days",
            statusMessage = statusMessage,
            entries = chartEntries
        )
    }

    private fun getMoodValue(emoji: String): Double {
        return when (emoji) {
            "😄", "🤩", "😍", "🥰" -> 5.0  // Very Happy
            "😊", "🙂", "😌", "😁" -> 4.0  // Happy
            "😐", "😑", "😶", "🤔" -> 3.0  // Neutral
            "😔", "😕", "🙁", "😞" -> 2.0  // Sad
            "😢", "😭", "😰", "😨" -> 1.0  // Very Sad
            else -> 3.0 // Default to neutral
        }
    }

    private fun getMoodLabel(value: Double): String {
        return when {
            value >= 4.5 -> "Great"
            value >= 3.5 -> "Good"
            value >= 2.5 -> "Okay"
            value >= 1.5 -> "Poor"
            else -> "Very Poor"
        }
    }

    data class DashboardHabitsData(
        val totalHabits: Int,
        val completedHabits: Int,
        val percentage: Int,
        val statusMessage: String
    )

    data class DashboardMoodData(
        val emoji: String,
        val status: String,
        val note: String,
        val hasNote: Boolean,
        val hasMood: Boolean
    )

    data class DashboardHydrationData(
        val remindersEnabled: Boolean,
        val statusText: String,
        val waterIntake: Int,
        val waterGoal: Int,
        val intakeText: String
    )

    data class MoodTrendData(
        val date1: String,
        val date2: String,
        val date3: String,
        val date4: String,
        val date5: String,
        val date6: String,
        val date7: String,
        val averageValue: String,
        val trendValue: String,
        val periodValue: String,
        val statusMessage: String,
        val entries: List<com.github.mikephil.charting.data.Entry>
    )
}
