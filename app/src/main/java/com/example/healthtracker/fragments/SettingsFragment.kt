package com.example.healthtracker.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.healthtracker.R
import com.example.healthtracker.services.HydrationReminderWorker
import com.example.healthtracker.utils.HealthPreferenceManager
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {

    private lateinit var sharedPrefsManager: HealthPreferenceManager
    private lateinit var switchHydrationReminders: SwitchCompat
    private lateinit var spinnerHydrationInterval: Spinner
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var etStepGoal: EditText
    private lateinit var btnSave: Button
    private lateinit var btnReset: Button
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        sharedPrefsManager = HealthPreferenceManager.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI(view)
        loadSettings()
        setupListeners()
    }

    private fun setupUI(view: View) {
        switchHydrationReminders = view.findViewById(R.id.switch_hydration_reminders)
        spinnerHydrationInterval = view.findViewById(R.id.spinner_hydration_interval)
        switchNotifications = view.findViewById(R.id.switch_notifications)
        etStepGoal = view.findViewById(R.id.et_step_goal)
        btnSave = view.findViewById(R.id.btn_save)
        btnReset = view.findViewById(R.id.btn_reset)
        btnBack = view.findViewById(R.id.btn_back)

        // Setup spinner for hydration intervals
        val intervals = arrayOf("1 hour", "2 hours", "3 hours", "4 hours", "6 hours", "8 hours")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, intervals)
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

        // Enable/disable interval spinner based on reminders switch
        spinnerHydrationInterval.isEnabled = switchHydrationReminders.isChecked
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

        btnBack.setOnClickListener {
            val userProfileFragment = UserProfileFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, userProfileFragment)
                .addToBackStack(null)
                .commit()
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

            Toast.makeText(requireContext(), "Settings saved successfully!", Toast.LENGTH_SHORT).show()

            // Optionally navigate back or show success indicator
            showSaveSuccessIndicator()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error saving settings: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showSaveSuccessIndicator() {
        // Change button text temporarily to show success
        val originalText = btnSave.text
        btnSave.text = "Saved ✓"
        btnSave.isEnabled = false

        // Restore button after 2 seconds
        btnSave.postDelayed({
            btnSave.text = originalText
            btnSave.isEnabled = true
        }, 2000)
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
        val workManager = WorkManager.getInstance(requireContext())

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
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Settings")
            .setMessage("Are you sure you want to reset all settings to default values?")
            .setPositiveButton("Reset") { _, _ ->
                // Reset to defaults
                switchHydrationReminders.isChecked = true
                spinnerHydrationInterval.setSelection(1) // 2 hours
                spinnerHydrationInterval.isEnabled = true
                switchNotifications.isChecked = true
                etStepGoal.setText("10000")

                Toast.makeText(requireContext(), "Settings reset to defaults", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_settings -> {
                saveSettings()
                true
            }
            R.id.action_reset_settings -> {
                resetSettings()
                true
            }
            R.id.action_backup_data -> {
                showBackupDialog()
                true
            }
            R.id.action_restore_data -> {
                showRestoreDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showBackupDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Backup Data")
            .setMessage("This feature will backup your habits and mood data to local storage.")
            .setPositiveButton("Backup") { _, _ ->
                // Implement backup functionality
                Toast.makeText(requireContext(), "Backup feature coming soon!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRestoreDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Restore Data")
            .setMessage("This feature will restore your data from a previous backup. Current data will be overwritten.")
            .setPositiveButton("Restore") { _, _ ->
                // Implement restore functionality
                Toast.makeText(requireContext(), "Restore feature coming soon!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}