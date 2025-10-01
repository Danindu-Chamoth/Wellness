package com.example.wellness.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wellness.models.Habit

class HabitsViewModel : ViewModel() {
    private val _habits = MutableLiveData<MutableList<Habit>>(mutableListOf())
    val habits: LiveData<MutableList<Habit>> get() = _habits

    private val _completionPercentage = MutableLiveData(0)
    val completionPercentage: LiveData<Int> get() = _completionPercentage

    fun addHabit(name: String) {
        val currentList = _habits.value ?: mutableListOf()
        val newHabit = Habit(Habit.getNextId(), name)
        currentList.add(newHabit)
        _habits.value = currentList
        updateCompletionPercentage()
    }

    fun updateHabit(id: Int, name: String) {
        val currentList = _habits.value ?: return
        val habit = currentList.find { it.id == id }
        habit?.name = name
        _habits.value = currentList
    }

    fun deleteHabit(id: Int) {
        val currentList = _habits.value ?: return
        currentList.removeAll { it.id == id }
        _habits.value = currentList
        updateCompletionPercentage()
    }

    fun toggleHabitCompletion(id: Int) {
        val currentList = _habits.value ?: return
        val habit = currentList.find { it.id == id }
        habit?.isCompleted = !(habit?.isCompleted ?: false)
        _habits.value = currentList
        updateCompletionPercentage()
    }

    fun setHabits(habits: List<Habit>) {
        _habits.value = habits.toMutableList()
        updateCompletionPercentage()
    }

    private fun updateCompletionPercentage() {
        val currentList = _habits.value ?: return
        if (currentList.isEmpty()) {
            _completionPercentage.value = 0
            return
        }

        val completedCount = currentList.count { it.isCompleted }
        val percentage = (completedCount.toFloat() / currentList.size * 100).toInt()
        _completionPercentage.value = percentage
    }
}
