package com.example.healthtracker.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.example.healthtracker.models.*
import com.google.gson.reflect.TypeToken

class HealthPreferenceManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "health_tracker_prefs"

        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_USER_LOGGED_IN = "user_logged_in"
        private const val KEY_USER_PROFILE = "user_profile"

        private const val KEY_HABITS = "user_habits"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
        private const val KEY_HYDRATION_INTERVAL = "hydration_interval"
        private const val  KEY_HYDRATION_ENABLED = "hydration_enabled"
        private const val KEY_STEP_GOAL = "step_goal"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"


        @Volatile
        private var INSTANCE: HealthPreferenceManager? = null

        fun getInstance(context: Context): HealthPreferenceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HealthPreferenceManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    fun setFirstLaunch(isFirstLaunch: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_FIRST_LAUNCH, isFirstLaunch) }
    }

    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setUserLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_USER_LOGGED_IN, isLoggedIn)}
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_USER_LOGGED_IN, false)
    }

    data class UserProfile(
        val name: String,
        val age: Int,
        val weight: Float,
        val height: Float
    )

    fun saveUserProfile(profile: UserProfile) {
        val profileJson = gson.toJson(profile)
        sharedPreferences.edit {putString(KEY_USER_PROFILE, profileJson)}
    }

    fun getUserProfile(): UserProfile? {
        val profileJson = sharedPreferences.getString(KEY_USER_PROFILE, null)
        return try {
            gson.fromJson(profileJson, UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Habit management
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        sharedPreferences.edit { putString(KEY_HABITS, json) }
    }

    fun createDefaultHabits(): List<Habit> {
        val defaultHabits = listOf(
            Habit(name = "Drink Water", description = "Stay hydrated", targetCount = 8, unit = "glasses"),
            Habit(name = "Meditate", description = "Daily Meditation", targetCount = 1, unit = "session"),
            Habit(name = "Exercise", description = "Physical activity", targetCount = 30, unit = "minutes"),
            Habit(name = "Read", description = "Read books", targetCount = 20, unit = "pages")
        )
        saveHabits(defaultHabits)
        return defaultHabits
    }

    fun getHabits(): List<Habit> {
        val json = sharedPreferences.getString(KEY_HABITS, null)
        return if (json !== null) {
            val type = object : TypeToken<List<Habit>>() {}.type
            gson.fromJson(json, type)
        } else {
            createDefaultHabits()
        }
    }

    // Mood entry management
    fun saveMoodEntries(moodEntries: List<MoodEntry>) {
        val json = gson.toJson(moodEntries)
        sharedPreferences.edit { putString(KEY_MOOD_ENTRIES, json) }
    }

    fun getMoodEntries(): List<MoodEntry> {
        val json = sharedPreferences.getString(KEY_MOOD_ENTRIES, null)
        return if (json !== null) {
            val type = object : TypeToken<List<MoodEntry>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    // Hydration settings

    fun setHydrationInterval(intervalHours: Int) {
        sharedPreferences.edit { putInt(KEY_HYDRATION_INTERVAL, intervalHours )}
    }

    fun getHydrationInterval(): Int {
        return sharedPreferences.getInt(KEY_HYDRATION_INTERVAL, 2)
    }

    fun setHydrationEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_HYDRATION_ENABLED, enabled) }
    }

    fun isHydrationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_HYDRATION_ENABLED, true)
    }

    // step count
    fun setStepGoal(goal: Int) {
        sharedPreferences.edit { putInt(KEY_STEP_GOAL, goal) }
    }

    fun getStepGoal(): Int {
        return sharedPreferences.getInt(KEY_STEP_GOAL, 10000)
    }

    // Notifications settings
    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit {putBoolean(KEY_NOTIFICATION_ENABLED, enabled)}
    }

    fun isNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    }
}