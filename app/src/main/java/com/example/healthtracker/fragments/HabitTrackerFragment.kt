package com.example.healthtracker.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.healthtracker.R
import com.example.healthtracker.adapters.HabitAdapter
import com.example.healthtracker.models.Habit
import com.example.healthtracker.utils.HealthPreferenceManager
import com.example.healthtracker.utils.SensorHelper

import kotlin.collections.*

class HabitTrackerFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var sharedPrefsManager: HealthPreferenceManager
    private lateinit var sensorHelper: SensorHelper

    private var habits = mutableListOf<Habit>()
    private val currentDate = DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()).toString()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habit_tracker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefsManager = HealthPreferenceManager.Companion.getInstance(requireContext())
        sensorHelper = SensorHelper(requireContext())

        setupUI(view)
        loadHabits()
//        setupSensors()
    }

    private fun setupUI(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_habits)
        fabAdd = view.findViewById(R.id.fab_add_habit)

        habitAdapter = HabitAdapter(
            habits = habits,
            onHabitClick = { habit -> showHabitDetailDialog(habit) },
            onProgressChange = { habit, progress -> updateHabitProgress(habit, progress) },
            onDeleteClick = { habit -> deleteHabit(habit) }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = habitAdapter
        }

        fabAdd.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun setupSensors() {
        // Setup shake detection for quick water habit completion
        sensorHelper.startShakeDetection {
            // Quick add water habit completion
            val waterHabit = habits.find { it.name.contains("Water", ignoreCase = true) }
            waterHabit?.let { habit ->
                habit.addCompletion(currentDate, 1)
                saveHabits()
                habitAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "💧 Water intake added!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadHabits() {
        habits.clear()
        habits.addAll(sharedPrefsManager.getHabits().filter { it.isActive })
        habitAdapter.notifyDataSetChanged()
    }

    private fun saveHabits() {
        // Get all habits (including inactive ones) and update them
        val allHabits = sharedPrefsManager.getHabits().toMutableList()
        habits.forEach { updatedHabit ->
            val index = allHabits.indexOfFirst { it.id == updatedHabit.id }
            if (index != -1) {
                allHabits[index] = updatedHabit
            }
        }
        sharedPrefsManager.saveHabits(allHabits)
    }

    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_habit_name)
        val etDescription = dialogView.findViewById<EditText>(R.id.et_habit_description)
        val etUnit = dialogView.findViewById<EditText>(R.id.et_habit_unit)
        val npTarget = dialogView.findViewById<NumberPicker>(R.id.np_target_count)

        npTarget.minValue = 1
        npTarget.maxValue = 100
        npTarget.value = 1

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Habit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val unit = etUnit.text.toString().trim().ifEmpty { "times" }
                val target = npTarget.value

                if (name.isNotEmpty()) {
                    val newHabit = Habit(
                        name = name,
                        description = description,
                        targetCount = target,
                        unit = unit
                    )
                    habits.add(newHabit)
                    saveHabits()
                    habitAdapter.notifyItemInserted(habits.size - 1)
                    Toast.makeText(requireContext(), "Habit added successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Please enter a habit name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showHabitDetailDialog(habit: Habit) {
        val currentProgress = habit.getCompletionForDate(currentDate)
        val progressPercent = habit.getProgressPercentage(currentDate)

        val message = """
            Description: ${habit.description}
            Today's Progress: $currentProgress / ${habit.targetCount} ${habit.unit}
            Completion: ${String.format("%.1f", progressPercent)}%
            
            Tap the progress bar to update your progress.
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle(habit.name)
            .setMessage(message)
            .setPositiveButton("Edit Habit") { _, _ ->
                showEditHabitDialog(habit)
            }
            .setNeutralButton("View History") { _, _ ->
                showHabitHistoryDialog(habit)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_habit_name)
        val etDescription = dialogView.findViewById<EditText>(R.id.et_habit_description)
        val etUnit = dialogView.findViewById<EditText>(R.id.et_habit_unit)
        val npTarget = dialogView.findViewById<NumberPicker>(R.id.np_target_count)

        // Pre-fill current values
        etName.setText(habit.name)
        etDescription.setText(habit.description)
        etUnit.setText(habit.unit)

        npTarget.minValue = 1
        npTarget.maxValue = 100
        npTarget.value = habit.targetCount

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                habit.name = etName.text.toString().trim()
                habit.description = etDescription.text.toString().trim()
                habit.unit = etUnit.text.toString().trim().ifEmpty { "times" }
                habit.targetCount = npTarget.value

                saveHabits()
                habitAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Habit updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showHabitHistoryDialog(habit: Habit) {
        val history = StringBuilder()
        val sortedCompletions = habit.completions.toSortedMap(compareByDescending { it })

        if (sortedCompletions.isEmpty()) {
            history.append("No history available yet.")
        } else {
            sortedCompletions.toList().take(7).forEach { (date, count) ->
                val percentage = (count.toFloat() / habit.targetCount * 100).coerceAtMost(100f)
                history.append("$date: $count/${habit.targetCount} ${habit.unit} (${String.format("%.1f", percentage)}%)\n")
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("${habit.name} - Last 7 Days")
            .setMessage(history.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun updateHabitProgress(habit: Habit, progress: Int) {
        habit.setCompletion(currentDate, progress)
        saveHabits()
        habitAdapter.notifyDataSetChanged()
    }

    private fun deleteHabit(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                habit.isActive = false
                saveHabits()
                habits.remove(habit)
                habitAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Habit deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sensorHelper.stopSensors()
    }
}
