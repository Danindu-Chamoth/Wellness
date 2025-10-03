package com.example.wellness.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellness.R
import com.example.wellness.adapters.HabitAdapter
import com.example.wellness.models.Habit
import com.example.wellness.viewmodels.HabitsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HabitsFragment : Fragment() {

    private lateinit var viewModel: HabitsViewModel
    private lateinit var adapter: HabitAdapter
    private lateinit var rvHabits: RecyclerView
    private lateinit var tvCompletion: TextView
    private lateinit var tvEmptyState: TextView
    private lateinit var fabAddHabit: FloatingActionButton

    private val PREFS_NAME = "WellnessPrefs" // Changed to match DashboardFragment
    private val HABITS_KEY = "habits"
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[HabitsViewModel::class.java]

        // Initialize UI components
        initViews(view)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        loadHabitsFromPrefs()
    }

    private fun initViews(view: View) {
        rvHabits = view.findViewById(R.id.rvHabits)
        tvCompletion = view.findViewById(R.id.tvCompletion)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        fabAddHabit = view.findViewById(R.id.fabAddHabit)
    }

    private fun setupRecyclerView() {
        adapter = HabitAdapter(
            habits = emptyList(),
            onEditClick = { habit -> showEditHabitDialog(habit) },
            onDeleteClick = { habit -> showDeleteConfirmationDialog(habit) },
            onToggleComplete = { habit -> viewModel.toggleHabitCompletion(habit.id) }
        )

        rvHabits.layoutManager = LinearLayoutManager(requireContext())
        rvHabits.adapter = adapter
    }

    private fun saveHabitsToPrefs(habits: List<Habit>) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(habits)
        prefs.edit {
            putString(HABITS_KEY, json)
        }
    }

    private fun loadHabitsFromPrefs() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(HABITS_KEY, null)
        if (json != null) {
            val type = object : TypeToken<List<Habit>>() {}.type
            val habits: List<Habit> = gson.fromJson(json, type)
            viewModel.setHabits(habits)
        }
    }

    private fun setupObservers() {
        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            adapter.updateData(habits)
            updateEmptyState(habits.isEmpty())
            saveHabitsToPrefs(habits)
        }

        viewModel.completionPercentage.observe(viewLifecycleOwner) { percentage ->
            tvCompletion.text = "$percentage%"
        }
    }

    private fun setupClickListeners() {
        fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        val etHabitName = dialogView.findViewById<EditText>(R.id.etHabitName)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val habitName = etHabitName.text.toString().trim()
                if (habitName.isNotEmpty()) {
                    viewModel.addHabit(habitName)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        val etHabitName = dialogView.findViewById<EditText>(R.id.etHabitName)
        etHabitName.setText(habit.name)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Habit")
            .setPositiveButton("Save") { dialog, _ ->
                val habitName = etHabitName.text.toString().trim()
                if (habitName.isNotEmpty()) {
                    viewModel.updateHabit(habit.id, habitName)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteConfirmationDialog(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteHabit(habit.id)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            rvHabits.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
        } else {
            rvHabits.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
        }
    }
}
