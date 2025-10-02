package com.example.wellness.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellness.R
import com.example.wellness.models.Mood
import java.text.SimpleDateFormat
import java.util.Locale

class MoodAdapter(
    private var moods: List<Mood>,
    private val onMoodClick: (Mood) -> Unit,
    private val onMoodEdit: (Mood) -> Unit,
    private val onMoodDelete: (Mood) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji: TextView = itemView.findViewById(R.id.tvEmoji)
        val tvMoodType: TextView = itemView.findViewById(R.id.tvMoodType)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvNote: TextView = itemView.findViewById(R.id.tvNote)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val btnEdit: LinearLayout = itemView.findViewById(R.id.btnEdit)
        val btnDelete: LinearLayout = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position]

        holder.tvEmoji.text = mood.emoji
        holder.tvMoodType.text = mood.moodType
        holder.tvNote.text = if (mood.note.isNotEmpty()) mood.note else "No notes added"

        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        holder.tvDate.text = dateFormat.format(mood.dateTime)
        holder.tvTime.text = timeFormat.format(mood.dateTime)

        // Click listeners
        holder.itemView.setOnClickListener {
            onMoodClick(mood)
        }

        holder.btnEdit.setOnClickListener {
            onMoodEdit(mood)
        }

        holder.btnDelete.setOnClickListener {
            onMoodDelete(mood)
        }
    }

    override fun getItemCount(): Int = moods.size

    fun updateMoods(newMoods: List<Mood>) {
        moods = newMoods
        notifyDataSetChanged()
    }
}
