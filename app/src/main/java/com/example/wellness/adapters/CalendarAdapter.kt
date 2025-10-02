package com.example.wellness.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellness.R
import com.example.wellness.models.CalendarDay
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(
    private var calendarDays: List<CalendarDay>,
    private val onDayClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        val moodIndicator: View = itemView.findViewById(R.id.moodIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val calendarDay = calendarDays[position]

        holder.tvDay.text = if (calendarDay.dayOfMonth > 0) calendarDay.dayOfMonth.toString() else ""

        // Set text color based on day type
        when {
            calendarDay.isToday -> {
                holder.tvDay.setTextColor(holder.itemView.context.getColor(android.R.color.white))
                holder.itemView.setBackgroundResource(R.drawable.mood_highlight_today)
            }
            calendarDay.hasMoodEntry -> {
                holder.tvDay.setTextColor(holder.itemView.context.getColor(android.R.color.white))
                holder.itemView.setBackgroundResource(R.drawable.mood_highlight_entry)
            }
            !calendarDay.isCurrentMonth -> {
                holder.tvDay.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
                holder.itemView.setBackgroundResource(R.drawable.calendar_day_background)
            }
            else -> {
                holder.tvDay.setTextColor(holder.itemView.context.getColor(android.R.color.black))
                holder.itemView.setBackgroundResource(R.drawable.calendar_day_background)
            }
        }

        // Show mood indicator dot if there are mood entries
        if (calendarDay.hasMoodEntry && !calendarDay.isToday) {
            holder.moodIndicator.visibility = View.VISIBLE
        } else {
            holder.moodIndicator.visibility = View.GONE
        }

        // Set click listener
        holder.itemView.setOnClickListener {
            if (calendarDay.dayOfMonth > 0) {
                onDayClick(calendarDay)
            }
        }

        // Handle selection state
        holder.itemView.isSelected = calendarDay.isSelected
    }

    override fun getItemCount(): Int = calendarDays.size

    fun updateCalendarDays(newCalendarDays: List<CalendarDay>) {
        calendarDays = newCalendarDays
        notifyDataSetChanged()
    }

    fun setSelectedDay(selectedDay: CalendarDay) {
        val updatedDays = calendarDays.map { day ->
            day.copy(isSelected = day.dayOfMonth == selectedDay.dayOfMonth && day.isCurrentMonth)
        }
        calendarDays = updatedDays
        notifyDataSetChanged()
    }
}
