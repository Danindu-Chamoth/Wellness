package com.example.wellness.utils

import com.example.wellness.models.CalendarDay
import com.example.wellness.models.Mood
import java.util.*

class CalendarHelper {

    companion object {
        fun generateCalendarDays(year: Int, month: Int, moods: List<Mood>): List<CalendarDay> {
            val calendar = Calendar.getInstance()
            val today = Calendar.getInstance()
            val calendarDays = mutableListOf<CalendarDay>()

            // Set calendar to first day of the month
            calendar.set(year, month, 1)
            val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            // Add empty cells for days before the first day of the month
            for (i in 0 until firstDayOfWeek) {
                calendarDays.add(CalendarDay(
                    date = Date(),
                    dayOfMonth = 0,
                    isCurrentMonth = false,
                    isToday = false,
                    hasMoodEntry = false
                ))
            }

            // Add actual days of the month
            for (day in 1..daysInMonth) {
                calendar.set(year, month, day)
                val date = calendar.time

                val isToday = (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                              calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                              calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH))

                val dayMoods = moods.filter { mood ->
                    isSameDay(mood.dateTime, date)
                }

                calendarDays.add(CalendarDay(
                    date = date,
                    dayOfMonth = day,
                    isCurrentMonth = true,
                    isToday = isToday,
                    hasMoodEntry = dayMoods.isNotEmpty(),
                    moodEntries = dayMoods
                ))
            }

            // Fill remaining cells to complete the grid (42 cells = 6 weeks)
            while (calendarDays.size < 42) {
                calendarDays.add(CalendarDay(
                    date = Date(),
                    dayOfMonth = 0,
                    isCurrentMonth = false,
                    isToday = false,
                    hasMoodEntry = false
                ))
            }

            return calendarDays
        }

        fun getMonthName(month: Int): String {
            val months = arrayOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            return if (month in 0..11) months[month] else "Unknown"
        }

        fun getDateFromDay(day: CalendarDay, year: Int, month: Int): Date {
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day.dayOfMonth)
            return calendar.time
        }

        fun isSameDay(date1: Date, date2: Date): Boolean {
            val cal1 = Calendar.getInstance().apply { time = date1 }
            val cal2 = Calendar.getInstance().apply { time = date2 }
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                   cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                   cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
        }

        fun getMonthYearString(year: Int, month: Int): String {
            val calendar = Calendar.getInstance()
            calendar.set(year, month, 1)
            val formatter = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
            return formatter.format(calendar.time)
        }
    }
}
