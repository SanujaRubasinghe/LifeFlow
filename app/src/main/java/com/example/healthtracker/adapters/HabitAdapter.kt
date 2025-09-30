package com.example.healthtracker.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthtracker.R
import com.example.healthtracker.models.Habit
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor
import androidx.core.graphics.toColorInt

class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val onHabitClick: (Habit) -> Unit,
    private val onProgressChange: (Habit, Int) -> Unit,
    private val onDeleteClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    private val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardHabit: CardView = itemView.findViewById(R.id.card_habit)
        val tvHabitName: TextView = itemView.findViewById(R.id.tv_habit_name)
        val tvHabitDescription: TextView = itemView.findViewById(R.id.tv_habit_description)
        val tvProgress: TextView = itemView.findViewById(R.id.tv_progress)
        val tvProgressPercentage: TextView = itemView.findViewById(R.id.tv_percentage)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        val btnDecrease: Button = itemView.findViewById(R.id.btn_decrease)
        val btnIncrease: Button = itemView.findViewById(R.id.btn_increase)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
        val ivCompletionStatus: ImageView = itemView.findViewById(R.id.iv_completion_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {

        val habitCardColors = listOf(
            "#3F8FFE".toColorInt(),  // blue
            "#9465FF".toColorInt(),  // purple
            "#FF6B81".toColorInt(),  // pink
            "#4BD1A0".toColorInt(),  // teal
            "#FF9AD3".toColorInt(),  // light pink
            "#6CCFFF".toColorInt(),  // light blue
            "#B084FF".toColorInt(),  // violet
            "#FF7F7F".toColorInt(),  // coral-red
            "#63FFB3".toColorInt(),  // mint green
            "#66FFF0".toColorInt(),  // aqua
            "#9AE6FF".toColorInt(),  // sky blue
            "#FF8FC4".toColorInt(),  // bubblegum pink
            "#AB83FF".toColorInt(),  // lavender
            "#FFB3B3".toColorInt(),  // pastel red
            "#00D9A6".toColorInt(),  // emerald green
            "#7DFFB2".toColorInt(),  // fresh lime green
            "#5BE7FF".toColorInt(),  // cyan
            "#E89DFF".toColorInt(),  // bright lilac
            "#FF97E0".toColorInt(),  // candy pink
            "#7FA7FF".toColorInt()   // periwinkle blue
        )

        val habit = habits[position]
        val currentProgress = habit.getCompletionForDate(currentDate)
        val progressPercentage = habit.getProgressPercentage(currentDate)
        val isCompleted = habit.isCompletedForDate(currentDate)

        val colorIndex = kotlin.math.abs(habit.id.hashCode()) % habitCardColors.size
        holder.cardHabit.setCardBackgroundColor(habitCardColors[colorIndex])

        holder.tvProgressPercentage.text = "${floor(habit.getProgressPercentage(currentDate))}%"
        holder.tvHabitName.text = habit.name
        holder.tvHabitDescription.text = habit.description
        holder.tvProgress.text = "$currentProgress / ${habit.targetCount} ${habit.unit}"
        holder.progressBar.progress = progressPercentage.toInt()

        // Set completion status icon
        if (isCompleted) {
            holder.ivCompletionStatus.setImageResource(R.drawable.ic_check_circle)
            holder.ivCompletionStatus.setColorFilter(android.graphics.Color.GREEN)
        } else {
            holder.ivCompletionStatus.setImageResource(R.drawable.ic_circle_outline)
            holder.ivCompletionStatus.setColorFilter(android.graphics.Color.GRAY)
        }

        // Click listeners
        holder.itemView.setOnClickListener {
            onHabitClick(habit)
        }

        holder.btnIncrease.setOnClickListener {
            val newProgress = currentProgress + 1
            onProgressChange(habit, newProgress)
        }

        holder.btnDecrease.setOnClickListener {
            val newProgress = maxOf(0, currentProgress - 1)
            onProgressChange(habit, newProgress)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(habit)
        }

        holder.progressBar.setOnClickListener {
            // Allow direct progress editing by clicking progress bar
            showProgressEditDialog(holder.itemView.context, habit, currentProgress)
        }
    }

    private fun showProgressEditDialog(context: android.content.Context, habit: Habit, currentProgress: Int) {
        val editText = EditText(context).apply {
            setText(currentProgress.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Enter progress (0-${habit.targetCount})"
        }

        android.app.AlertDialog.Builder(context)
            .setTitle("Edit Progress for ${habit.name}")
            .setView(editText)
            .setPositiveButton("Update") { _, _ ->
                try {
                    val newProgress = editText.text.toString().toInt()
                    val clampedProgress = newProgress.coerceIn(0, habit.targetCount * 2) // Allow slight overflow
                    onProgressChange(habit, clampedProgress)
                } catch (e: NumberFormatException) {
                    Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount(): Int = habits.size
}