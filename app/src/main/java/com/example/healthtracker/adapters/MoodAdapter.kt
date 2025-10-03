package com.example.healthtracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthtracker.R
import com.example.healthtracker.models.MoodEntry
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

class MoodAdapter(
    private val moodEntries: MutableList<MoodEntry>,
    private val onMoodClick: (MoodEntry) -> Unit,
    private val onDeleteClick: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTotalEntries: TextView = itemView.findViewById(R.id.tv_total_entries)
        val tvEmoji: TextView = itemView.findViewById(R.id.tv_emoji)
        val tvMoodLabel: TextView = itemView.findViewById(R.id.tv_mood_label)
        val tvNote: TextView = itemView.findViewById(R.id.tv_note)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val moodEntry = moodEntries[position]
        val moodLabels = MoodEntry.getMoodLabels()
        val totalEntries = moodEntries.filter {
            val entryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
            entryDate != null
        }

        holder.tvTotalEntries.text = "${totalEntries.size}"
        holder.tvEmoji.text = moodEntry.emoji
        holder.tvMoodLabel.text = moodLabels[moodEntry.mood - 1]
        holder.tvNote.text = if (moodEntry.note.isNotEmpty()) moodEntry.note else "No notes"

        // Format timestamp
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        holder.tvTimestamp.text = dateFormat.format(Date(moodEntry.timestamp))

        // Set note visibility
        if (moodEntry.note.isEmpty()) {
            holder.tvNote.visibility = View.GONE
        } else {
            holder.tvNote.visibility = View.VISIBLE
        }

        // Click listeners
        holder.itemView.setOnClickListener {
            onMoodClick(moodEntry)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(moodEntry)
        }
    }

    override fun getItemCount(): Int = moodEntries.size
}