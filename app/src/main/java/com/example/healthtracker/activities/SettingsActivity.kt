package com.example.healthtracker.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.healthtracker.R
import com.example.healthtracker.services.HydrationReminderWorker
import com.example.healthtracker.utils.HealthPreferenceManager
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPrefsManager: HealthPreferenceManager
    private lateinit var switchHydrationReminders: SwitchCompat
    private lateinit var spinnerHydrationInterval: Spinner
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var etStepGoal: EditText
    private lateinit var btnSave: Button
    private lateinit var btnReset: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPrefsManager = HealthPreferenceManager.getInstance(this)

        setupUI()
        loadSettings()
        setupListeners()

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Settings"
        }
    }

    private fun setupUI() {
        switchHydrationReminders = findViewById(R.id.switch_hydration_reminders)
        spinnerHydrationInterval = findViewById(R.id.spinner_hydration_interval)
        switchNotifications = findViewById(R.id.switch_notifications)
        etStepGoal = findViewById(R.id.et_step_goal)
        btnSave = findViewById(R.id.btn_save)
        btnReset = findViewById(R.id.btn_reset)

        // Setup spinner for hydration intervals
        val intervals = arrayOf("1 hour", "2 hours", "3 hours", "4 hours", "6 hours", "8 hours")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, intervals)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerHydrationInterval.adapter = adapter
    }

    private fun loadSettings() {
        switchHydrationReminders.isChecked = sharedPrefsManager.isHydrationEnabled()
        switchNotifications.isChecked = sharedPrefsManager.isNotificationsEnabled()
        etStepGoal.setText(sharedPrefsManager.getStepGoal().toString())

        // Set hydration interval
        val currentInterval = sharedPrefsManager.getHydrationInterval()
        val intervalPosition = when (currentInterval) {
            1 -> 0
            2 -> 1
            3 -> 2
            4 -> 3
            6 -> 4
            8 -> 5
            else -> 1 // Default to 2 hours
        }
        spinnerHydrationInterval.setSelection(intervalPosition)
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            saveSettings()
        }

        btnReset.setOnClickListener {
            resetSettings()
        }

        switchHydrationReminders.setOnCheckedChangeListener { _, isChecked ->
            spinnerHydrationInterval.isEnabled = isChecked
        }
    }

    private fun saveSettings() {
        try {
            // Save hydration settings
            val hydrationEnabled = switchHydrationReminders.isChecked
            sharedPrefsManager.setHydrationEnabled(hydrationEnabled)

            val intervalHours = getSelectedInterval()
            sharedPrefsManager.setHydrationInterval(intervalHours)

            // Save notification settings
            sharedPrefsManager.setNotificationsEnabled(switchNotifications.isChecked)

            // Save step goal
            val stepGoal = etStepGoal.text.toString().toIntOrNull() ?: 10000
            sharedPrefsManager.setStepGoal(stepGoal.coerceIn(1000, 50000))

            // Update hydration reminders
            updateHydrationReminders(hydrationEnabled, intervalHours)

            Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "Error saving settings: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getSelectedInterval(): Int {
        return when (spinnerHydrationInterval.selectedItemPosition) {
            0 -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 6
            5 -> 8
            else -> 2
        }
    }

    private fun updateHydrationReminders(enabled: Boolean, intervalHours: Int) {
        val workManager = WorkManager.getInstance(this)

        if (enabled) {
            val hydrationWork = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
                intervalHours.toLong(), TimeUnit.HOURS
            )
                .addTag("hydration_reminder")
                .build()

            workManager.enqueueUniquePeriodicWork(
                "hydration_reminder",
                ExistingPeriodicWorkPolicy.REPLACE,
                hydrationWork
            )
        } else {
            workManager.cancelUniqueWork("hydration_reminder")
        }
    }

    private fun resetSettings() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reset Settings")
            .setMessage("Are you sure you want to reset all settings to default values?")
            .setPositiveButton("Reset") { _, _ ->
                // Reset to defaults
                switchHydrationReminders.isChecked = true
                spinnerHydrationInterval.setSelection(1) // 2 hours
                switchNotifications.isChecked = true
                etStepGoal.setText("10000")

                Toast.makeText(this, "Settings reset to defaults", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
