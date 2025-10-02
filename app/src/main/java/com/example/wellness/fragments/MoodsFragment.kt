package com.example.wellness.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellness.R
import com.example.wellness.adapters.CalendarAdapter
import com.example.wellness.adapters.MoodAdapter
import com.example.wellness.models.CalendarDay
import com.example.wellness.models.Mood
import com.example.wellness.models.validations.MoodValidation
import com.example.wellness.models.validations.ValidationResult
import com.example.wellness.utils.CalendarHelper
import com.example.wellness.utils.PreferencesHelper
import com.example.wellness.viewmodels.MoodViewModel
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar
import java.util.Date

class MoodsFragment : Fragment() {

    private lateinit var viewModel: MoodViewModel
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var adapter: MoodAdapter
    private lateinit var calendarAdapter: CalendarAdapter

    private lateinit var etMoodNote: EditText
    private lateinit var tvCharCount: TextView
    private lateinit var rvMoodHistory: RecyclerView
    private lateinit var btnSaveMood: Button
    private lateinit var btnShareMood: Button
    private lateinit var btnDetailedEntry: Button
    private lateinit var tvTotalEntries: TextView
    private lateinit var tvTodayEntries: TextView

    // View toggle buttons
    private lateinit var btnListView: Button
    private lateinit var btnCalendarView: Button

    // Calendar related views
    private lateinit var calendarView: View
    private lateinit var rvCalendar: RecyclerView
    private lateinit var tvMonthYear: TextView
    private lateinit var btnPrevMonth: Button
    private lateinit var btnNextMonth: Button

    // Mood selector layouts
    private lateinit var layoutEcstatic: LinearLayout
    private lateinit var layoutVeryHappy: LinearLayout
    private lateinit var layoutExcited: LinearLayout
    private lateinit var layoutHappy: LinearLayout
    private lateinit var layoutContent: LinearLayout
    private lateinit var layoutUpset: LinearLayout
    private var layoutSad: LinearLayout? = null
    private var layoutAngry: LinearLayout? = null

    private var selectedEmoji: String = ""
    private var selectedMoodType: String = ""
    private var detailedEntryVisible: Boolean = false
    private var isCalendarViewActive: Boolean = false

    // Calendar state
    private var currentYear: Int = 0
    private var currentMonth: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_moods, container, false)

        preferencesHelper = PreferencesHelper(requireContext())
        viewModel = ViewModelProvider(this)[MoodViewModel::class.java]

        // Initialize calendar state
        val calendar = Calendar.getInstance()
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH)

        initializeViews(view)
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        setupTextWatcher()

        viewModel.loadMoods(preferencesHelper)

        return view
    }

    private fun initializeViews(view: View) {
        etMoodNote = view.findViewById(R.id.etMoodNote)
        tvCharCount = view.findViewById(R.id.tvCharCount)
        rvMoodHistory = view.findViewById(R.id.rvMoodHistory)
        btnSaveMood = view.findViewById(R.id.btnSaveMood)
        btnShareMood = view.findViewById(R.id.btnShareMood)
        btnDetailedEntry = view.findViewById(R.id.btnDetailedEntry)
        tvTotalEntries = view.findViewById(R.id.tvTotalEntries)
        tvTodayEntries = view.findViewById(R.id.tvTodayEntries)

        // Mood selector layouts
        layoutEcstatic = view.findViewById(R.id.layoutEcstatic)
        layoutVeryHappy = view.findViewById(R.id.layoutVeryHappy)
        layoutExcited = view.findViewById(R.id.layoutExcited)
        layoutHappy = view.findViewById(R.id.layoutHappy)
        layoutContent = view.findViewById(R.id.layoutContent)
        layoutUpset = view.findViewById(R.id.layoutUpset)
        layoutSad = view.findViewById(R.id.layoutSad)
        layoutAngry = view.findViewById(R.id.layoutAngry)

        // View toggle buttons
        btnListView = view.findViewById(R.id.btnListView)
        btnCalendarView = view.findViewById(R.id.btnCalendarView)

        // Calendar related views
        calendarView = view.findViewById(R.id.calendarView)
        rvCalendar = view.findViewById(R.id.rvCalendar)
        tvMonthYear = view.findViewById(R.id.tvMonthYear)
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)
    }

    private fun setupRecyclerView() {
        adapter = MoodAdapter(
            moods = emptyList(),
            onMoodClick = { mood ->
                showMoodDetails(mood)
            },
            onMoodEdit = { mood ->
                editMoodEntry(mood)
            },
            onMoodDelete = { mood ->
                showDeleteConfirmation(mood)
            }
        )

        rvMoodHistory.layoutManager = LinearLayoutManager(requireContext())
        rvMoodHistory.adapter = adapter

        // Calendar view setup
        calendarAdapter = CalendarAdapter(emptyList()) { day ->
            onCalendarDaySelected(day)
        }
        rvCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        rvCalendar.adapter = calendarAdapter
    }

    private fun setupClickListeners() {
        // Mood selector click listeners
        layoutEcstatic.setOnClickListener {
            selectMood("😍", "Ecstatic")
            highlightSelectedMood(layoutEcstatic)
        }

        layoutVeryHappy.setOnClickListener {
            selectMood("😊", "Very Happy")
            highlightSelectedMood(layoutVeryHappy)
        }

        layoutExcited.setOnClickListener {
            selectMood("🤩", "Excited")
            highlightSelectedMood(layoutExcited)
        }

        layoutHappy.setOnClickListener {
            selectMood("😃", "Happy")
            highlightSelectedMood(layoutHappy)
        }

        layoutContent.setOnClickListener {
            selectMood("😊", "Content")
            highlightSelectedMood(layoutContent)
        }

        layoutUpset.setOnClickListener {
            selectMood("😔", "Upset")
            highlightSelectedMood(layoutUpset)
        }

        layoutSad?.setOnClickListener {
            selectMood("😢", "Sad")
            layoutSad?.let { highlightSelectedMood(it) }
        }

        layoutAngry?.setOnClickListener {
            selectMood("😠", "Angry")
            layoutAngry?.let { highlightSelectedMood(it) }
        }

        // Detailed entry button
        btnDetailedEntry.setOnClickListener {
            toggleDetailedEntry()
        }

        // Save mood button
        btnSaveMood.setOnClickListener {
            saveMoodEntry()
        }

        // Share mood button
        btnShareMood.setOnClickListener {
            shareMoodSummary()
        }

        // View toggle buttons
        btnListView.setOnClickListener {
            setListView()
        }

        btnCalendarView.setOnClickListener {
            setCalendarView()
        }

        // Calendar navigation buttons
        btnPrevMonth.setOnClickListener {
            navigateToPreviousMonth()
        }

        btnNextMonth.setOnClickListener {
            navigateToNextMonth()
        }
    }

    private fun setupTextWatcher() {
        etMoodNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateCharacterCount()
            }
        })
    }

    private fun setupObservers() {
        viewModel.moods.observe(viewLifecycleOwner) { moods ->
            adapter.updateMoods(moods)
            updateStats()
            updateCalendar(moods)
        }
    }

    private fun highlightSelectedMood(selectedLayout: LinearLayout) {
        // Reset all layouts - filter out null values
        val layouts = listOfNotNull(layoutEcstatic, layoutVeryHappy, layoutExcited, layoutHappy, layoutContent, layoutUpset, layoutSad, layoutAngry)
        layouts.forEach { layout ->
            layout.isSelected = false
        }

        // Highlight selected layout
        selectedLayout.isSelected = true
    }

    private fun selectMood(emoji: String, moodType: String) {
        selectedEmoji = emoji
        selectedMoodType = moodType

        // If detailed entry is not visible, save immediately
        if (!detailedEntryVisible) {
            val newMood = Mood(
                emoji = selectedEmoji,
                note = "",
                dateTime = Date(),
                moodType = selectedMoodType
            )
            viewModel.saveMood(newMood, preferencesHelper)
            Snackbar.make(requireView(), "Mood saved: $moodType", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(requireView(), "Selected: $moodType", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun toggleDetailedEntry() {
        detailedEntryVisible = !detailedEntryVisible

        if (detailedEntryVisible) {
            etMoodNote.visibility = View.VISIBLE
            tvCharCount.visibility = View.VISIBLE
            btnSaveMood.visibility = View.VISIBLE
            btnDetailedEntry.text = "✏️ Quick Entry"
        } else {
            etMoodNote.visibility = View.GONE
            tvCharCount.visibility = View.GONE
            btnSaveMood.visibility = View.GONE
            btnDetailedEntry.text = "✏️ Detailed Entry"
        }
    }

    private fun editMoodEntry(mood: Mood) {
        // Pre-fill the form with existing mood data
        selectedEmoji = mood.emoji
        selectedMoodType = mood.moodType

        // Show detailed entry mode
        if (!detailedEntryVisible) {
            toggleDetailedEntry()
        }

        // Fill the note field
        etMoodNote.setText(mood.note)

        // Highlight the corresponding mood selector - filter out null values
        val layouts = listOfNotNull(layoutEcstatic, layoutVeryHappy, layoutExcited, layoutHappy, layoutContent, layoutUpset, layoutSad, layoutAngry)
        layouts.forEach { layout -> layout.isSelected = false }

        when (mood.moodType) {
            "Ecstatic" -> highlightSelectedMood(layoutEcstatic)
            "Very Happy" -> highlightSelectedMood(layoutVeryHappy)
            "Excited" -> highlightSelectedMood(layoutExcited)
            "Happy" -> highlightSelectedMood(layoutHappy)
            "Content" -> highlightSelectedMood(layoutContent)
            "Upset" -> highlightSelectedMood(layoutUpset)
            "Sad" -> layoutSad?.let { highlightSelectedMood(it) }
            "Angry" -> layoutAngry?.let { highlightSelectedMood(it) }
        }

        // Update save button text and functionality for editing
        btnSaveMood.text = "Update Mood Entry"
        btnSaveMood.setOnClickListener {
            updateMoodEntry(mood)
        }

        Snackbar.make(requireView(), "Editing mood entry", Snackbar.LENGTH_SHORT).show()
    }

    private fun updateMoodEntry(originalMood: Mood) {
        val note = etMoodNote.text.toString().trim()

        val validation = MoodValidation(selectedEmoji, note)
        val emojiValidation = validation.validateEmoji()
        val noteValidation = validation.validateNote()

        if (emojiValidation is ValidationResult.Empty) {
            Snackbar.make(requireView(), emojiValidation.errorMessage, Snackbar.LENGTH_LONG).show()
            return
        }

        if (noteValidation is ValidationResult.Invalid) {
            Snackbar.make(requireView(), noteValidation.errorMessage, Snackbar.LENGTH_LONG).show()
            return
        }

        // Create updated mood
        val updatedMood = Mood(
            emoji = selectedEmoji,
            note = note,
            dateTime = originalMood.dateTime, // Keep original timestamp
            moodType = selectedMoodType
        )

        // Remove old mood and add updated one
        viewModel.deleteMood(originalMood, preferencesHelper)
        viewModel.saveMood(updatedMood, preferencesHelper)

        // Reset form
        resetMoodForm()

        Snackbar.make(requireView(), "Mood entry updated successfully!", Snackbar.LENGTH_SHORT).show()
    }

    private fun resetMoodForm() {
        etMoodNote.text.clear()
        selectedEmoji = ""
        selectedMoodType = ""

        // Reset mood selection highlight - filter out null values
        val layouts = listOfNotNull(layoutEcstatic, layoutVeryHappy, layoutExcited, layoutHappy, layoutContent, layoutUpset, layoutSad, layoutAngry)
        layouts.forEach { layout ->
            layout.isSelected = false
        }

        // Reset save button
        btnSaveMood.text = "Save Mood Entry"
        btnSaveMood.setOnClickListener {
            saveMoodEntry()
        }
    }

    private fun saveMoodEntry() {
        val note = etMoodNote.text.toString().trim()

        val validation = MoodValidation(selectedEmoji, note)
        val emojiValidation = validation.validateEmoji()
        val noteValidation = validation.validateNote()

        if (emojiValidation is ValidationResult.Empty) {
            Snackbar.make(requireView(), emojiValidation.errorMessage, Snackbar.LENGTH_LONG).show()
            return
        }

        if (noteValidation is ValidationResult.Invalid) {
            Snackbar.make(requireView(), noteValidation.errorMessage, Snackbar.LENGTH_LONG).show()
            return
        }

        val newMood = Mood(
            emoji = selectedEmoji,
            note = note,
            dateTime = Date(),
            moodType = selectedMoodType
        )

        viewModel.saveMood(newMood, preferencesHelper)

        // Reset form
        resetMoodForm()

        Snackbar.make(requireView(), "Mood saved successfully!", Snackbar.LENGTH_SHORT).show()
    }

    private fun showMoodDetails(mood: Mood) {
        val details = "${mood.moodType}: ${mood.note}\nRecorded on ${java.text.SimpleDateFormat("MMM d, yyyy 'at' h:mm a", java.util.Locale.getDefault()).format(mood.dateTime)}"
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Mood Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDeleteConfirmation(mood: Mood) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteMood(mood, preferencesHelper)
                Snackbar.make(requireView(), "Mood entry deleted", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareMoodSummary() {
        val todayMoods = viewModel.moods.value?.filter { mood ->
            viewModel.isSameDay(mood.dateTime, Date())
        } ?: emptyList()

        if (todayMoods.isEmpty()) {
            Snackbar.make(requireView(), "No moods to share today", Snackbar.LENGTH_SHORT).show()
            return
        }

        val latestMood = todayMoods.first()
        val shareText = "My mood today: ${latestMood.emoji} ${latestMood.moodType}\n" +
                       "Note: ${latestMood.note}\n\n" +
                       "Shared from Wellness App"

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(shareIntent, "Share your mood"))
    }

    private fun updateCharacterCount() {
        val currentLength = etMoodNote.text.length
        tvCharCount.text = "$currentLength/500 characters"

        if (currentLength > 450) {
            tvCharCount.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
        } else {
            tvCharCount.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }
    }

    private fun updateStats() {
        val totalCount = viewModel.getMoodsCount()
        val todayCount = viewModel.getTodayMoodsCount()

        tvTotalEntries.text = totalCount.toString()
        tvTodayEntries.text = todayCount.toString()
    }

    private fun setListView() {
        isCalendarViewActive = false
        rvMoodHistory.visibility = View.VISIBLE
        calendarView.visibility = View.GONE
        btnListView.isSelected = true
        btnCalendarView.isSelected = false
        viewModel.loadMoods(preferencesHelper)
    }

    private fun setCalendarView() {
        isCalendarViewActive = true
        rvMoodHistory.visibility = View.GONE
        calendarView.visibility = View.VISIBLE
        btnListView.isSelected = false
        btnCalendarView.isSelected = true

        // Force update calendar with current data
        val allMoods = viewModel.getAllMoods(preferencesHelper)
        updateCalendar(allMoods)
    }

    private fun updateCalendar(moods: List<Mood>) {
        // Debug: Print calendar state
        android.util.Log.d("Calendar", "Updating calendar for $currentYear-$currentMonth with ${moods.size} moods")

        val calendarDays = CalendarHelper.generateCalendarDays(currentYear, currentMonth, moods)
        android.util.Log.d("Calendar", "Generated ${calendarDays.size} calendar days")

        calendarAdapter.updateCalendarDays(calendarDays)
        tvMonthYear.text = "${CalendarHelper.getMonthName(currentMonth)} $currentYear"

        // Debug: Print month name
        android.util.Log.d("Calendar", "Month display: ${CalendarHelper.getMonthName(currentMonth)} $currentYear")
    }

    private fun onCalendarDaySelected(day: CalendarDay) {
        // Highlight selected day and load corresponding moods
        calendarAdapter.setSelectedDay(day)

        // Load moods for the selected day
        val selectedDate = CalendarHelper.getDateFromDay(day, currentYear, currentMonth)
        viewModel.loadMoodsForDate(preferencesHelper, selectedDate)

        // Switch to list view
        setListView()
    }

    private fun navigateToPreviousMonth() {
        if (currentMonth == 0) {
            currentMonth = 11
            currentYear--
        } else {
            currentMonth--
        }
        updateCalendar(viewModel.moods.value ?: emptyList())
    }

    private fun navigateToNextMonth() {
        if (currentMonth == 11) {
            currentMonth = 0
            currentYear++
        } else {
            currentMonth++
        }
        updateCalendar(viewModel.moods.value ?: emptyList())
    }
}
