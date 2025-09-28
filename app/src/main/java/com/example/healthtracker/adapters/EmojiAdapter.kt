package com.example.healthtracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.healthtracker.R
import com.google.android.material.card.MaterialCardView

class EmojiAdapter(
    private val emojis: List<String>,
    private val labels: List<String>,
    private var selectedPosition: Int = 2, // Default to neutral
    private val onEmojiSelected: (Int, String) -> Unit
) : RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {

    class EmojiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardEmoji: MaterialCardView = itemView.findViewById(R.id.card_emoji)
        val tvEmoji: TextView = itemView.findViewById(R.id.tv_emoji)
        val tvLabel: TextView = itemView.findViewById(R.id.tv_label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_emoji_card, parent, false)
        return EmojiViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        val emoji = emojis[position]
        val label = labels[position]

        holder.tvEmoji.text = emoji
        holder.tvLabel.text = label

        // Highlight selected item
        if (position == selectedPosition) {
            holder.cardEmoji.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.selected_emoji_background)
            )
            holder.cardEmoji.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.colorPrimary)
            holder.cardEmoji.strokeWidth = 3
        } else {
            holder.cardEmoji.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.unselected_emoji_background)
            )
            holder.cardEmoji.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.colorOutline)
            holder.cardEmoji.strokeWidth = 1
        }

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onEmojiSelected(position, emoji)
        }
    }

    override fun getItemCount(): Int = emojis.size
}