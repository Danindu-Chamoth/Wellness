package com.example.wellness.fragments

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.wellness.R
import com.example.wellness.activities.MainActivity
import com.example.wellness.utils.DashboardDataHelper
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class DashboardFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var wellnessPreferences: SharedPreferences

    // Views
    private lateinit var tvCompletionPercentage: TextView
    private lateinit var tvRemainingPercentage: TextView
    private lateinit var tvHabitsCompleted: TextView
    private lateinit var tvTotalHabits: TextView
    private lateinit var tvHabitStatus: TextView
    private lateinit var tvMoodEmoji: TextView
    private lateinit var tvMoodStatus: TextView
    private lateinit var tvMoodNote: TextView
    private lateinit var tvHydrationStatus: TextView
    private lateinit var tvWaterIntake: TextView
    private lateinit var tvMotivationalMessage: TextView

    // Chart views
    private lateinit var cardMoodTrend: androidx.cardview.widget.CardView
    private lateinit var tvChartPeriod: TextView
    private lateinit var btnCloseChart: android.widget.ImageButton
    private lateinit var tvDate1: TextView
    private lateinit var tvDate2: TextView
    private lateinit var tvDate3: TextView
    private lateinit var tvDate4: TextView
    private lateinit var tvDate5: TextView
    private lateinit var tvDate6: TextView
    private lateinit var tvDate7: TextView
    private lateinit var tvAverageValue: TextView
    private lateinit var tvTrendValue: TextView
    private lateinit var tvPeriodValue: TextView
    private lateinit var tvMoodTrendStatus: TextView
    private lateinit var btn7d: Button
    private lateinit var btn14d: Button
    private lateinit var btn30d: Button

    // Buttons
    private lateinit var btnAddHabit: Button
    private lateinit var btnLogMood: Button
    private lateinit var btnHydrationSettings: Button
    private lateinit var btnViewTrends: Button
    private lateinit var btnAddWater: androidx.cardview.widget.CardView

    // LineChart for mood trend
    private lateinit var lineChartMoodTrend: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SharedPreferences - using both your existing preference files
        sharedPreferences = requireContext().getSharedPreferences("WellnessPrefs", Context.MODE_PRIVATE)
        wellnessPreferences = requireContext().getSharedPreferences("wellness_settings", Context.MODE_PRIVATE)

        initializeViews(view)
        setupClickListeners()
        loadDashboardData()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData() // Refresh data when coming back from other fragments
    }

    private fun initializeViews(view: View) {
        // Habit views
        tvCompletionPercentage = view.findViewById(R.id.tvCompletionPercentage)
        tvRemainingPercentage = view.findViewById(R.id.tvRemainingPercentage)
        tvHabitsCompleted = view.findViewById(R.id.tvHabitsCompleted)
        tvTotalHabits = view.findViewById(R.id.tvTotalHabits)
        tvHabitStatus = view.findViewById(R.id.tvHabitStatus)

        // Mood views
        tvMoodEmoji = view.findViewById(R.id.tvMoodEmoji)
        tvMoodStatus = view.findViewById(R.id.tvMoodStatus)
        tvMoodNote = view.findViewById(R.id.tvMoodNote)

        // Hydration views
        tvHydrationStatus = view.findViewById(R.id.tvHydrationStatus)
        tvWaterIntake = view.findViewById(R.id.tvWaterIntake)

        // Other views
        tvMotivationalMessage = view.findViewById(R.id.tvMotivationalMessage)

        // Chart views
        cardMoodTrend = view.findViewById(R.id.cardMoodTrend)
        tvChartPeriod = view.findViewById(R.id.tvChartPeriod)
        btnCloseChart = view.findViewById(R.id.btnCloseChart)
        tvDate1 = view.findViewById(R.id.tvDate1)
        tvDate2 = view.findViewById(R.id.tvDate2)
        tvDate3 = view.findViewById(R.id.tvDate3)
        tvDate4 = view.findViewById(R.id.tvDate4)
        tvDate5 = view.findViewById(R.id.tvDate5)
        tvDate6 = view.findViewById(R.id.tvDate6)
        tvDate7 = view.findViewById(R.id.tvDate7)
        tvAverageValue = view.findViewById(R.id.tvAverageValue)
        tvTrendValue = view.findViewById(R.id.tvTrendValue)
        tvPeriodValue = view.findViewById(R.id.tvPeriodValue)
        tvMoodTrendStatus = view.findViewById(R.id.tvMoodTrendStatus)
        btn7d = view.findViewById(R.id.btn7d)
        btn14d = view.findViewById(R.id.btn14d)
        btn30d = view.findViewById(R.id.btn30d)

        // Buttons
        btnAddHabit = view.findViewById(R.id.btnAddHabit)
        btnLogMood = view.findViewById(R.id.btnLogMood)
        btnHydrationSettings = view.findViewById(R.id.btnHydrationSettings)
        btnViewTrends = view.findViewById(R.id.btnViewTrends)
        btnAddWater = view.findViewById(R.id.btnAddWater)

        // LineChart for mood trend
        lineChartMoodTrend = view.findViewById(R.id.moodLineChart)
    }

    private fun setupClickListeners() {
        btnAddHabit.setOnClickListener {
            // Navigate to Habits Fragment
            (requireActivity() as? MainActivity)?.let { activity ->
                activity.bottomNavigationView.selectedItemId = R.id.navigation_habits
            }
        }

        btnLogMood.setOnClickListener {
            // Navigate to Moods Fragment
            (requireActivity() as? MainActivity)?.let { activity ->
                activity.bottomNavigationView.selectedItemId = R.id.navigation_moods
            }
        }

        btnHydrationSettings.setOnClickListener {
            // Navigate to Settings Fragment
            (requireActivity() as? MainActivity)?.let { activity ->
                activity.bottomNavigationView.selectedItemId = R.id.navigation_settings
            }
        }

        btnViewTrends.setOnClickListener {
            // Show mood trend chart in dashboard
            showMoodTrendChart()
        }

        // Make mood section clickable too
        tvMoodStatus.setOnClickListener {
            btnLogMood.performClick()
        }

        tvMoodEmoji.setOnClickListener {
            btnLogMood.performClick()
        }

        // Make hydration section clickable for quick water logging
        tvWaterIntake.setOnClickListener {
            quickAddWater()
        }

        // Close chart button
        btnCloseChart.setOnClickListener {
            closeMoodTrendChart()
        }

        // Chart period buttons
        btn7d.setOnClickListener {
            loadMoodTrendData(7)
        }

        btn14d.setOnClickListener {
            loadMoodTrendData(14)
        }

        btn30d.setOnClickListener {
            loadMoodTrendData(30)
        }

        btnAddWater.setOnClickListener {
            quickAddWater()
        }
    }

    private fun loadDashboardData() {
        updateHabitCompletion()
        updateTodayMood()
        updateHydrationStatus()
        updateMotivationalMessage()
        // Show mood trend chart by default and load data
        showMoodTrendChart()
        loadMoodTrendData(7)
    }

    private fun updateHabitCompletion() {
        val habitsData = DashboardDataHelper.getHabitsData(requireContext())

        val completedHabits = habitsData.completedHabits
        val totalHabits = habitsData.totalHabits
        val percentage = habitsData.percentage
        val remainingPercentage = 100 - percentage

        tvHabitsCompleted.text = completedHabits.toString()
        tvTotalHabits.text = "of $totalHabits"
        tvCompletionPercentage.text = "$percentage%"
        tvRemainingPercentage.text = "$remainingPercentage%"
        tvHabitStatus.text = habitsData.statusMessage

        updateHabitStatusColor(percentage)
    }

    private fun updateHabitStatusColor(percentage: Int) {
        // Update the color of the habit status text based on completion percentage
        val color = when {
            percentage == 0 -> android.R.color.darker_gray
            percentage < 50 -> android.R.color.holo_red_light
            percentage < 80 -> android.R.color.holo_orange_light
            else -> android.R.color.holo_green_light
        }

        tvHabitStatus.setTextColor(requireContext().getColor(color))
    }

    private fun updateTodayMood() {
        val moodData = DashboardDataHelper.getTodayMoodData(requireContext())

        tvMoodEmoji.text = moodData.emoji
        tvMoodStatus.text = moodData.status
        tvMoodNote.text = moodData.note
        tvMoodNote.visibility = if (moodData.hasNote) View.VISIBLE else View.GONE
    }

    private fun updateHydrationStatus() {
        val hydrationData = DashboardDataHelper.getHydrationData(requireContext())

        tvHydrationStatus.text = hydrationData.statusText
        tvWaterIntake.text = hydrationData.intakeText
    }

    private fun quickAddWater() {
        val hydrationData = DashboardDataHelper.getHydrationData(requireContext())
        if (hydrationData.waterIntake < hydrationData.waterGoal) {
            val newIntake = hydrationData.waterIntake + 1
            DashboardDataHelper.updateWaterIntake(requireContext(), newIntake)
            updateHydrationStatus()
            Toast.makeText(requireContext(), "Water logged! 💧", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Daily water goal reached! 🎉", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMotivationalMessage() {
        val messages = listOf(
            "Stay positive, stay healthy! 🌱",
            "Small steps lead to big changes! 💫",
            "You're doing amazing! Keep going! ✨",
            "Your wellness journey matters! 🌟",
            "Every day is a fresh start! 🌈",
            "Progress, not perfection! 🎯",
            "Believe in yourself! 💪",
            "One habit at a time! 🔄"
        )

        val randomMessage = messages.random()
        tvMotivationalMessage.text = randomMessage
    }

    private fun loadMoodTrendData(days: Int) {
        // Load and display mood trend data for the selected period (7, 14, or 30 days)
        val trendData = DashboardDataHelper.getMoodTrendData(requireContext(), days)

        // Update chart views with the trend data
        tvDate1.text = trendData.date1
        tvDate2.text = trendData.date2
        tvDate3.text = trendData.date3
        tvDate4.text = trendData.date4
        tvDate5.text = trendData.date5
        tvDate6.text = trendData.date6
        tvDate7.text = trendData.date7
        tvAverageValue.text = trendData.averageValue
        tvTrendValue.text = trendData.trendValue
        tvPeriodValue.text = trendData.periodValue
        tvMoodTrendStatus.text = trendData.statusMessage

        // Update LineChart data
        updateMoodTrendChart(trendData.entries)

        // Show the mood trend card
        cardMoodTrend.visibility = View.VISIBLE
    }

    private fun updateMoodTrendChart(entries: List<Entry>) {
        // Prepare data for the LineChart
        val lineDataSet = LineDataSet(entries, "Mood Trend")
        lineDataSet.color = Color.BLUE
        lineDataSet.valueTextColor = Color.BLACK
        lineDataSet.lineWidth = 2f
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawValues(false)

        // Configure XAxis and YAxis
        val xAxis: XAxis = lineChartMoodTrend.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(entries.map { it.x.toInt().toString() })

        val yAxis: YAxis = lineChartMoodTrend.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 10f
        yAxis.granularity = 1f

        // Disable right YAxis
        lineChartMoodTrend.axisRight.isEnabled = false

        // Set data and refresh chart
        lineChartMoodTrend.data = LineData(lineDataSet)
        lineChartMoodTrend.invalidate()
    }

    private fun closeMoodTrendChart() {
        // Hide the mood trend chart
        cardMoodTrend.visibility = View.GONE
    }

    private fun showMoodTrendChart() {
        // Show the mood trend chart
        cardMoodTrend.visibility = View.VISIBLE
        // Optionally, load the default trend data (e.g., last 7 days)
        loadMoodTrendData(7)
    }
}
