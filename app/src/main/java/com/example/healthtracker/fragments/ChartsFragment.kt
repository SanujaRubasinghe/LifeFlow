package com.example.healthtracker.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.example.healthtracker.R
import com.example.healthtracker.models.MoodEntry
import com.example.healthtracker.utils.HealthPreferenceManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChartsFragment : Fragment() {

    private lateinit var moodChart: LineChart
    private lateinit var habitChart: PieChart
    private lateinit var tvMoodStats: TextView
    private lateinit var tvHabitStats: TextView
    private lateinit var sharedPrefsManager: HealthPreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_charts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefsManager = HealthPreferenceManager.getInstance(requireContext())

        setupUI(view)
        loadCharts()
    }

    private fun setupUI(view: View) {
        moodChart = view.findViewById(R.id.mood_chart)
        habitChart = view.findViewById(R.id.habit_chart)
        tvMoodStats = view.findViewById(R.id.tv_mood_stats)
        tvHabitStats = view.findViewById(R.id.tv_habit_stats)

        setupMoodChart()
        setupHabitChart()
    }

    private fun setupMoodChart() {
        moodChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(false)
            setBackgroundColor(Color.WHITE)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                isGranularityEnabled = true
            }

            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 6f
                setDrawGridLines(true)
            }

            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }

    private fun setupHabitChart() {
        habitChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)

            dragDecelerationFrictionCoef = 0.95f

            setDrawHoleEnabled(true)
            setHoleColor(Color.WHITE)
            setHoleRadius(58f)
            setTransparentCircleRadius(61f)

            setDrawCenterText(true)
            centerText = "Today's\nHabits"

            isRotationEnabled = true
            isHighlightPerTapEnabled = true

            legend.isEnabled = true
        }
    }

    private fun loadCharts() {
        loadMoodChart()
        loadHabitChart()
        updateStats()
    }

    private fun loadMoodChart() {
        val moodEntries = sharedPrefsManager.getMoodEntries()
        val last7Days = getLast7Days()
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        last7Days.forEachIndexed { index, date ->
            val dayMoods = moodEntries.filter { it.date == date }
            val averageMood = if (dayMoods.isNotEmpty()) {
                dayMoods.map { it.mood }.average().toFloat()
            } else {
                0f
            }

            entries.add(Entry(index.toFloat(), averageMood))
            labels.add(formatDateForChart(date))
        }

        val dataSet = LineDataSet(entries, "Average Mood").apply {
            color = Color.rgb(76, 175, 80)
            setCircleColor(Color.rgb(76, 175, 80))
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = Color.rgb(76, 175, 80)
            fillAlpha = 50
        }

        val lineData = LineData(dataSet)
        moodChart.data = lineData
        moodChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        moodChart.invalidate()
    }

    private fun loadHabitChart() {
        val habits = sharedPrefsManager.getHabits().filter { it.isActive }
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        habits.forEach { habit ->
            val progress = habit.getProgressPercentage(today)
            if (progress > 0) {
                entries.add(PieEntry(progress, habit.name))
            }
        }

        if (entries.isEmpty()) {
            entries.add(PieEntry(100f, "No habits tracked today"))
            colors.add(Color.GRAY)
        } else {
            colors.addAll(ColorTemplate.MATERIAL_COLORS.toList())
            colors.addAll(ColorTemplate.COLORFUL_COLORS.toList())
        }

        val dataSet = PieDataSet(entries, "Habit Progress").apply {
            setDrawIcons(false)
            sliceSpace = 3f
            iconsOffset = com.github.mikephil.charting.utils.MPPointF(0f, 40f)
            selectionShift = 5f
            setColors(colors)
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter())
            setValueTextSize(11f)
            setValueTextColor(Color.WHITE)
        }

        habitChart.data = data
        habitChart.highlightValues(null)
        habitChart.invalidate()
    }

    private fun updateStats() {
        updateMoodStats()
        updateHabitStats()
    }

    private fun updateMoodStats() {
        val moodEntries = sharedPrefsManager.getMoodEntries()
        val last7Days = moodEntries.filter {
            val entryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
            val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.time
            entryDate != null && entryDate.after(weekAgo)
        }

        if (last7Days.isNotEmpty()) {
            val averageMood = last7Days.map { it.mood }.average()
            val mostCommonEmoji = last7Days.groupBy { it.emoji }
                .maxByOrNull { it.value.size }?.key ?: "😐"

            val moodLabel = when {
                averageMood >= 4.5 -> "Very Happy"
                averageMood >= 3.5 -> "Happy"
                averageMood >= 2.5 -> "Neutral"
                averageMood >= 1.5 -> "Sad"
                else -> "Very Sad"
            }

            tvMoodStats.text = """
                📊 Mood Stats (Last 7 Days)
                Average Mood: $moodLabel
                Most Common: $mostCommonEmoji
                Entries: ${last7Days.size}
            """.trimIndent()
        } else {
            tvMoodStats.text = "No mood data available for the last 7 days"
        }
    }

    private fun updateHabitStats() {
        val habits = sharedPrefsManager.getHabits().filter { it.isActive }
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val completedHabits = habits.count { it.isCompletedForDate(today) }
        val totalProgress = habits.sumOf { it.getProgressPercentage(today).toInt() }
        val averageProgress = if (habits.isNotEmpty()) totalProgress / habits.size else 0

        tvHabitStats.text = """
            🎯 Habit Stats (Today)
            Completed: $completedHabits/${habits.size} habits
            Average Progress: $averageProgress%
            Total Habits: ${habits.size}
        """.trimIndent()
    }

    private fun getLast7Days(): List<String> {
        val dates = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        repeat(7) {
            dates.add(0, dateFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return dates
    }

    private fun formatDateForChart(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate!!)
        } catch (e: Exception) {
            date
        }
    }

    override fun onResume() {
        super.onResume()
        loadCharts() // Refresh charts when fragment becomes visible
    }
}