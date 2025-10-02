package com.example.wellness.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wellness.models.Mood
import com.example.wellness.utils.PreferencesHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class MoodViewModel : ViewModel() {

    private val _moods = MutableLiveData<List<Mood>>()
    val moods: LiveData<List<Mood>> = _moods

    private val _selectedMood = MutableLiveData<Mood?>()
    val selectedMood: LiveData<Mood?> = _selectedMood

    fun loadMoods(preferencesHelper: PreferencesHelper) {
        val savedMoods = preferencesHelper.getMoods()
        _moods.value = savedMoods
    }

    fun saveMood(mood: Mood, preferencesHelper: PreferencesHelper) {
        val currentMoods = _moods.value?.toMutableList() ?: mutableListOf()
        currentMoods.add(0, mood) // Add to beginning for latest first
        _moods.value = currentMoods
        preferencesHelper.saveMoods(currentMoods)
    }

    fun deleteMood(mood: Mood, preferencesHelper: PreferencesHelper) {
        val currentMoods = _moods.value?.toMutableList() ?: mutableListOf()
        currentMoods.remove(mood)
        _moods.value = currentMoods
        preferencesHelper.saveMoods(currentMoods)
    }

    fun setSelectedMood(mood: Mood?) {
        _selectedMood.value = mood
    }

    fun getMoodsCount(): Int {
        return _moods.value?.size ?: 0
    }

    fun getTodayMoodsCount(): Int {
        val today = Date()
        return _moods.value?.count {
            isSameDay(it.dateTime, today)
        } ?: 0
    }

    fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { time = date1 }
        val cal2 = java.util.Calendar.getInstance().apply { time = date2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH) &&
                cal1.get(java.util.Calendar.DAY_OF_MONTH) == cal2.get(java.util.Calendar.DAY_OF_MONTH)
    }

    fun loadMoodsForDate(preferencesHelper: PreferencesHelper, selectedDate: Date) {
        val allMoods = preferencesHelper.getMoods()
        val filteredMoods = allMoods.filter { mood ->
            isSameDay(mood.dateTime, selectedDate)
        }
        _moods.value = filteredMoods
    }

    fun getAllMoods(preferencesHelper: PreferencesHelper): List<Mood> {
        return preferencesHelper.getMoods()
    }
}
