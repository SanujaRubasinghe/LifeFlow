package com.example.healthtracker.models

import java.util.*
import com.google.gson.annotations.SerializedName

data class Habit(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerializedName("name")
    var name: String,

    @SerializedName("description")
    var description: String,

    @SerializedName("targetCount")
    var targetCount: Int,

    @SerializedName("unit")
    var unit: String = "times",

    @SerializedName("isActive")
    var isActive: Boolean = true,

    @SerializedName("createdDate")
    val createdDate: Long = System.currentTimeMillis(),

    @SerializedName("completions")
    var completions: MutableMap<String, Int> = mutableMapOf()
) {
    var color: Int? = null
    fun getCompletionForDate(date: String): Int {
        return completions[date] ?: 0
    }

    fun isCompletedForDate(date: String): Boolean {
        return getCompletionForDate(date) >= targetCount
    }

    fun addCompletion(date: String, count: Int = 1) {
        completions[date] = getCompletionForDate(date) + count
    }

    fun setCompletion(date: String, count: Int) {
        if (count <= 0) {
            completions.remove(date)
        } else {
            completions[date] = count
        }
    }

    fun getProgressPercentage(date: String): Float {
        val completed = getCompletionForDate(date)
        return if (targetCount > 0) {
            (completed.toFloat() / targetCount.toFloat() * 100f).coerceAtMost(100f)
        } else 0f
    }
}

data class MoodEntry(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerializedName("mood")
    var mood: Int, // 1-5 scale

    @SerializedName("emoji")
    var emoji: String,

    @SerializedName("note")
    var note: String = "",

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @SerializedName("date")
    val date: String // YYYY-MM-DD format
) {
    companion object {
        const val MOOD_VERY_SAD = 1
        const val MOOD_SAD = 2
        const val MOOD_NEUTRAL = 3
        const val MOOD_HAPPY = 4
        const val MOOD_VERY_HAPPY = 5

        fun getMoodEmojis(): List<String> {
            return listOf("😢", "😕", "😐", "😊", "😄")
        }

        fun getMoodLabels(): List<String> {
            return listOf("Very Sad", "Sad", "Neutral", "Happy", "Very Happy")
        }
    }
}