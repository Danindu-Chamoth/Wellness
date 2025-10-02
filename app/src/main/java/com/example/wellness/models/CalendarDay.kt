package com.example.wellness.models

import java.util.*

data class CalendarDay(
    val date: Date,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean = false,
    val hasMoodEntry: Boolean = false,
    val moodEntries: List<Mood> = emptyList()
)
