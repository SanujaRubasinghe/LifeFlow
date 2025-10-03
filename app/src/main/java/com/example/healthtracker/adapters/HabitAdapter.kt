package com.example.healthtracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.healthtracker.R
import com.example.healthtracker.models.Habit
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val onHabitClick: (Habit) -> Unit,
    private val onProgressChange: (Habit, Int) -> Unit,
    private val onDeleteClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    private val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHabitName: TextView = itemView.findViewById(R.id.tv_habit_name)
        val tvHabitDescription: TextView = itemView.findViewById(R.id.tv_habit_description)
        val tvProgress: TextView = itemView.findViewById(R.id.tv_progress)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        val tvPercentage: TextView = itemView.findViewById(R.id.tv_percentage)
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
        val habit = habits[position]
        val currentProgress = habit.getCompletionForDate(currentDate)
        val progressPercentage = habit.getProgressPercentage(currentDate)
        val isCompleted = habit.isCompletedForDate(currentDate)

        holder.tvHabitName.text = habit.name
        holder.tvHabitDescription.text = habit.description
        holder.tvProgress.text = "$currentProgress / ${habit.targetCount} ${habit.unit}"
        holder.progressBar.progress = progressPercentage.toInt()
        holder.tvPercentage.text = "${floor( progressPercentage)}%"

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