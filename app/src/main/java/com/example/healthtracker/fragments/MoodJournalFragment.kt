package com.example.healthtracker.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.healthtracker.R
import com.example.healthtracker.adapters.EmojiAdapter
import com.example.healthtracker.adapters.MoodAdapter
import com.example.healthtracker.models.MoodEntry
import com.example.healthtracker.utils.GreetingHelper
import com.example.healthtracker.utils.HealthPreferenceManager
import com.example.healthtracker.utils.SensorHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MoodJournalFragment : Fragment() {

    private lateinit var tvTotalEntries: TextView
    private lateinit var tvAvgMood: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var moodAdapter: MoodAdapter
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var sharedPrefsManager: HealthPreferenceManager
    private lateinit var sensorHelper: SensorHelper

    private var moodEntries = mutableListOf<MoodEntry>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood_journal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefsManager = HealthPreferenceManager.getInstance(requireContext())
        sensorHelper = SensorHelper(requireContext())

        val tvGreeting: TextView = view.findViewById(R.id.tv_greeting)
        GreetingHelper.setGreeting(requireContext(), tvGreeting, "Mood")

        setupUI(view)
        loadMoodEntries()
        setupSensors()
    }

    private fun setupUI(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_moods)
        fabAdd = view.findViewById(R.id.fab_add_mood)
        tvTotalEntries = view.findViewById(R.id.tv_total_entries)
        tvAvgMood = view.findViewById(R.id.tv_avg_mood)

        moodAdapter = MoodAdapter(
            moodEntries = moodEntries,
            onMoodClick = { moodEntry -> showMoodDetailDialog(moodEntry) },
            onDeleteClick = { moodEntry -> deleteMoodEntry(moodEntry) }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moodAdapter
        }

        fabAdd.setOnClickListener {
            showAddMoodDialog()
        }
    }

    private fun setupSensors() {
        sensorHelper.startShakeDetection {
            showQuickMoodDialog()
        }
    }

    private fun setAverageMood() {
        val last7Days = moodEntries.filter {
            val entryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
            val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.time
            entryDate != null && entryDate.after(weekAgo)
        }

        if (last7Days.isNotEmpty()) {
            val averageMood = last7Days.map { it.mood }.average()
            val mostCommonEmoji = last7Days.groupBy { it.emoji }
                .maxByOrNull { it.value.size }?.key ?: "😐"

            tvAvgMood.text = mostCommonEmoji
        } else {
            tvAvgMood.text = "-"
        }
    }

    private fun loadMoodEntries() {
        moodEntries.clear()
        moodEntries.addAll(sharedPrefsManager.getMoodEntries().sortedByDescending { it.timestamp })
        tvTotalEntries.text = "${moodEntries.size}"

        setAverageMood()

        moodAdapter.notifyDataSetChanged()
    }

    private fun saveMoodEntries() {
        sharedPrefsManager.saveMoodEntries(moodEntries)
        setAverageMood()
    }

    private fun showAddMoodDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_mood, null)
        val recyclerViewEmojis = dialogView.findViewById<RecyclerView>(R.id.recycler_view_emojis)
        val etNote = dialogView.findViewById<EditText>(R.id.et_mood_note)

        var selectedMood = MoodEntry.MOOD_NEUTRAL
        var selectedEmoji = "😐"

        val emojiAdapter = EmojiAdapter(
            emojis = MoodEntry.getMoodEmojis(),
            labels = MoodEntry.getMoodLabels(),
            onEmojiSelected = { position, emoji ->
                selectedMood = position + 1
                selectedEmoji = emoji
            }
        )

        recyclerViewEmojis.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            clipToPadding = false
            setPadding(16, 0, 16, 0)
            adapter = emojiAdapter
        }

        AlertDialog.Builder(requireContext())
            .setTitle("How are you feeling?")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val note = etNote.text.toString().trim()
                val currentDate = DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()).toString()

                val moodEntry = MoodEntry(
                    mood = selectedMood,
                    emoji = selectedEmoji,
                    note = note,
                    date = currentDate
                )

                moodEntries.add(0, moodEntry)
                saveMoodEntries()
                moodAdapter.notifyItemInserted(0)
                tvTotalEntries.text = "${moodEntries.size}"

                Toast.makeText(requireContext(), "Mood logged successfully!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showQuickMoodDialog() {
        val emojis = MoodEntry.getMoodEmojis()
        val labels = MoodEntry.getMoodLabels()

        AlertDialog.Builder(requireContext())
            .setTitle("Quick Mood Check (Shake detected!)")
            .setItems(labels.toTypedArray()) { _, position ->
                val currentDate = DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()).toString()

                val moodEntry = MoodEntry(
                    mood = position + 1,
                    emoji = emojis[position],
                    note = "Quick mood entry",
                    date = currentDate
                )

                moodEntries.add(0, moodEntry)
                saveMoodEntries()
                moodAdapter.notifyItemInserted(0)

                Toast.makeText(requireContext(),
                    "Quick mood ${emojis[position]} logged!",
                    Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMoodDetailDialog(moodEntry: MoodEntry) {
        val timestamp = DateFormat.format("MMM dd, yyyy 'at' hh:mm a", moodEntry.timestamp).toString()
        val moodLabel = MoodEntry.getMoodLabels()[moodEntry.mood - 1]

        val message = """
            Date: $timestamp
            Mood: ${moodEntry.emoji} $moodLabel
            Note: ${moodEntry.note.ifEmpty { "No notes" }}
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Mood Entry Details")
            .setMessage(message)
            .setPositiveButton("Edit") { _, _ ->
                showEditMoodDialog(moodEntry)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showEditMoodDialog(moodEntry: MoodEntry) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_mood, null)
        val recyclerViewEmojis = dialogView.findViewById<RecyclerView>(R.id.recycler_view_emojis)
        val etNote = dialogView.findViewById<EditText>(R.id.et_mood_note)

        etNote.setText(moodEntry.note)

        var selectedMood = moodEntry.mood
        var selectedEmoji = moodEntry.emoji

        val emojiAdapter = EmojiAdapter(
            emojis = MoodEntry.getMoodEmojis(),
            labels = MoodEntry.getMoodLabels(),
            selectedPosition = moodEntry.mood - 1,
            onEmojiSelected = { position, emoji ->
                selectedMood = position + 1
                selectedEmoji = emoji
            }
        )

        recyclerViewEmojis.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            clipToPadding = false
            setPadding(16, 0, 16, 0)
            adapter = emojiAdapter
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Mood Entry")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                moodEntry.mood = selectedMood
                moodEntry.emoji = selectedEmoji
                moodEntry.note = etNote.text.toString().trim()

                saveMoodEntries()
                moodAdapter.notifyDataSetChanged()

                Toast.makeText(requireContext(), "Mood entry updated!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMoodEntry(moodEntry: MoodEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { _, _ ->
                val position = moodEntries.indexOf(moodEntry)
                moodEntries.remove(moodEntry)
                saveMoodEntries()
                moodAdapter.notifyItemRemoved(position)
                tvTotalEntries.text = "${moodEntries.size}"
                setAverageMood()
                Toast.makeText(requireContext(), "Mood entry deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sensorHelper.stopSensors()
    }
}