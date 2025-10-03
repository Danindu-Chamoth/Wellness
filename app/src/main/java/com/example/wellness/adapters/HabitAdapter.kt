package com.example.wellness.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellness.R
import com.example.wellness.models.Habit

class HabitAdapter(
    private var habits: List<Habit>,
    private val onEditClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit,
    private val onToggleComplete: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.cbHabit)
        val tvHabitName: TextView = itemView.findViewById(R.id.tvHabitName)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.tvHabitName.text = habit.name

        // Set checkbox state without triggering the listener
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = habit.isCompleted

        // Set up the checkbox listener
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            // Only call the toggle if the state actually changed
            if (isChecked != habit.isCompleted) {
                onToggleComplete(habit)
            }
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(habit)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(habit)
        }
    }

    override fun getItemCount(): Int = habits.size

    fun updateData(newHabits: List<Habit>) {
        habits = newHabits
        notifyDataSetChanged()
    }
}
