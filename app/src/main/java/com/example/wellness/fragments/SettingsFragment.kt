package com.example.wellness.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.wellness.R
import com.example.wellness.services.HydrationReminderService
import com.example.wellness.utils.ThemeManager

class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    // Hydration Reminders
    private lateinit var switchWaterReminders: Switch
    private lateinit var spinnerReminderInterval: Spinner
    private lateinit var switchNotificationSound: Switch
    private lateinit var switchVibration: Switch
    private lateinit var btnTestNotification: Button

    // App Preferences
    private lateinit var spinnerTheme: Spinner
    private lateinit var spinnerFontSize: Spinner

    // Data Management
    private lateinit var btnBackupData: Button
    private lateinit var btnReinstall: Button
    private lateinit var btnClearHabits: Button
    private lateinit var btnClearMoods: Button
    private lateinit var btnClearAllData: Button

    // Advanced Features
    private lateinit var spinnerChartDuration: Spinner
    private lateinit var switchHomeWidget: Switch
    private lateinit var switchStepCounter: Switch
    private lateinit var switchShakeDetection: Switch

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("wellness_settings", Context.MODE_PRIVATE)

        // Initialize views
        initializeViews(view)

        // Load saved settings
        loadSettings()

        // Set up listeners
        setupListeners()

        return view
    }

    private fun initializeViews(view: View) {
        // Hydration Reminders
        switchWaterReminders = view.findViewById(R.id.switch_water_reminders)
        spinnerReminderInterval = view.findViewById(R.id.spinner_reminder_interval)
        switchNotificationSound = view.findViewById(R.id.switch_notification_sound)
        switchVibration = view.findViewById(R.id.switch_vibration)
        btnTestNotification = view.findViewById(R.id.btn_test_notification)

        // App Preferences
        spinnerTheme = view.findViewById(R.id.spinner_theme)
        spinnerFontSize = view.findViewById(R.id.spinner_font_size)

        // Data Management
        btnBackupData = view.findViewById(R.id.btn_backup_data)
        btnReinstall = view.findViewById(R.id.btn_reinstall)
        btnClearHabits = view.findViewById(R.id.btn_clear_habits)
        btnClearMoods = view.findViewById(R.id.btn_clear_moods)
        btnClearAllData = view.findViewById(R.id.btn_clear_all_data)

        // Advanced Features
        spinnerChartDuration = view.findViewById(R.id.spinner_chart_duration)
        switchHomeWidget = view.findViewById(R.id.switch_home_widget)
        switchStepCounter = view.findViewById(R.id.switch_step_counter)
        switchShakeDetection = view.findViewById(R.id.switch_shake_detection)
    }

    private fun loadSettings() {
        // Load hydration settings
        switchWaterReminders.isChecked = sharedPreferences.getBoolean("water_reminders_enabled", false)
        spinnerReminderInterval.setSelection(sharedPreferences.getInt("reminder_interval", 2)) // Default: 1 hour (index 2)
        switchNotificationSound.isChecked = sharedPreferences.getBoolean("notification_sound", true)
        switchVibration.isChecked = sharedPreferences.getBoolean("vibration_enabled", true)

        // Load app preferences - use ThemeManager to get current theme
        spinnerTheme.setSelection(ThemeManager.getCurrentTheme(requireContext()))
        spinnerFontSize.setSelection(sharedPreferences.getInt("font_size", 1)) // Default: Medium

        // Load advanced features
        spinnerChartDuration.setSelection(sharedPreferences.getInt("chart_duration", 0)) // Default: 1 Week
        switchHomeWidget.isChecked = sharedPreferences.getBoolean("home_widget_enabled", false)
        switchStepCounter.isChecked = sharedPreferences.getBoolean("step_counter_enabled", false)
        switchShakeDetection.isChecked = sharedPreferences.getBoolean("shake_detection_enabled", false)
    }

    private fun setupListeners() {
        // Hydration Reminders
        switchWaterReminders.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("water_reminders_enabled", isChecked).apply()
            showToast("Water reminders ${if (isChecked) "enabled" else "disabled"}")
            if (isChecked) {
                // Start the hydration reminder with current interval
                startHydrationReminder()
            } else {
                // Stop the hydration reminder service
                stopHydrationReminder()
            }
        }

        spinnerReminderInterval.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                sharedPreferences.edit().putInt("reminder_interval", position).apply()
                val intervals = resources.getStringArray(R.array.reminder_intervals)
                showToast("Reminder interval set to ${intervals[position]}")

                // Restart reminders with new interval if enabled
                if (switchWaterReminders.isChecked) {
                    startHydrationReminder()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        switchNotificationSound.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("notification_sound", isChecked).apply()
            showToast("Notification sound ${if (isChecked) "enabled" else "disabled"}")
        }

        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("vibration_enabled", isChecked).apply()
            showToast("Vibration ${if (isChecked) "enabled" else "disabled"}")
        }

        // App Preferences - Updated theme listener
        spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Only apply theme if it's different from current selection
                val currentTheme = ThemeManager.getCurrentTheme(requireContext())
                if (position != currentTheme) {
                    // Set the new theme using ThemeManager
                    ThemeManager.setTheme(requireContext(), position)

                    val themeNames = arrayOf("Light Mode", "Dark Mode", "System Default")
                    showToast("Theme changed to ${themeNames[position]}")

                    // Recreate activity to apply theme immediately
                    requireActivity().recreate()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerFontSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                sharedPreferences.edit().putInt("font_size", position).apply()
                showToast("Font size setting saved")
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Advanced Features
        spinnerChartDuration.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                sharedPreferences.edit().putInt("chart_duration", position).apply()
                showToast("Chart duration updated")
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        switchHomeWidget.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("home_widget_enabled", isChecked).apply()
            showToast("Home widget ${if (isChecked) "enabled" else "disabled"}")
        }

        switchStepCounter.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("step_counter_enabled", isChecked).apply()
            showToast("Step counter ${if (isChecked) "enabled" else "disabled"}")
        }

        switchShakeDetection.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("shake_detection_enabled", isChecked).apply()
            showToast("Shake detection ${if (isChecked) "enabled" else "disabled"}")
        }

        // Data Management - Backup Data
        btnBackupData.setOnClickListener {
            // TODO: Implement backup data functionality
            showToast("Backup data feature is not yet implemented")
        }

        // Data Management - Reinstall App
        btnReinstall.setOnClickListener {
            // TODO: Implement reinstall app functionality
            showToast("Reinstall app feature is not yet implemented")
        }

        // Data Management - Clear Habits
        btnClearHabits.setOnClickListener {
            showConfirmationDialog(
                "Clear All Habits",
                "This will permanently delete all your habits and their progress. Are you sure?",
                "habits"
            )
        }

        // Data Management - Clear Moods
        btnClearMoods.setOnClickListener {
            showConfirmationDialog(
                "Clear Mood History",
                "This will permanently delete all your mood entries. Are you sure?",
                "moods"
            )
        }

        // Data Management - Clear All Data
        btnClearAllData.setOnClickListener {
            showConfirmationDialog(
                "Clear All Data",
                "This will permanently delete ALL your wellness data including habits, moods, and settings. This cannot be undone!",
                "all"
            )
        }

        // Test Notification button - Send a test notification
        btnTestNotification.setOnClickListener {
            sendTestNotification()
        }
    }

    private fun startHydrationReminder() {
        try {
            val intervalIndex = sharedPreferences.getInt("reminder_interval", 1)
            val intervalMinutes = HydrationReminderService.getIntervalMinutes(intervalIndex)
            HydrationReminderService.scheduleHydrationReminder(requireContext(), intervalMinutes)

            // Show appropriate message based on interval
            val message = if (intervalIndex == 0) {
                "Hydration reminders scheduled every 5 seconds (Test Mode)"
            } else {
                "Hydration reminders scheduled every ${if (intervalMinutes < 60) "${intervalMinutes} minutes" else "${intervalMinutes/60} hour(s)"}"
            }
            showToast(message)
        } catch (e: Exception) {
            // Handle any permission or scheduling errors
            showToast("Error setting up reminders. Please check app permissions.")
            // Disable the switch if scheduling fails
            switchWaterReminders.isChecked = false
            sharedPreferences.edit().putBoolean("water_reminders_enabled", false).apply()
        }
    }

    private fun stopHydrationReminder() {
        try {
            HydrationReminderService.cancelHydrationReminder(requireContext())
            showToast("Hydration reminders cancelled")
        } catch (e: Exception) {
            showToast("Error cancelling reminders")
        }
    }

    private fun showConfirmationDialog(title: String, message: String, dataType: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton("Confirm") { dialog, which ->
            when (dataType) {
                "habits" -> clearHabitsData()
                "moods" -> clearMoodsData()
                "all" -> clearAllData()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun clearHabitsData() {
        // Clear habits data from SharedPreferences
        val habitsPrefs = requireActivity().getSharedPreferences("habits_data", Context.MODE_PRIVATE)
        habitsPrefs.edit().clear().apply()
        showToast("All habits cleared successfully")
    }

    private fun clearMoodsData() {
        // Clear moods data from SharedPreferences
        val moodsPrefs = requireActivity().getSharedPreferences("moods_data", Context.MODE_PRIVATE)
        moodsPrefs.edit().clear().apply()
        showToast("Mood history cleared successfully")
    }

    private fun clearAllData() {
        // Clear all app data
        val habitsPrefs = requireActivity().getSharedPreferences("habits_data", Context.MODE_PRIVATE)
        val moodsPrefs = requireActivity().getSharedPreferences("moods_data", Context.MODE_PRIVATE)

        habitsPrefs.edit().clear().apply()
        moodsPrefs.edit().clear().apply()
        sharedPreferences.edit().clear().apply()

        // Reload default settings
        loadSettings()

        showToast("All data cleared successfully")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun sendTestNotification() {
        try {
            // Start the hydration reminder service immediately to show a test notification
            val serviceIntent = Intent(requireContext(), HydrationReminderService::class.java)
            requireContext().startService(serviceIntent)
            showToast("Test notification sent! Check your notification panel.")
        } catch (e: Exception) {
            showToast("Error sending test notification: ${e.message}")
        }
    }
}
