package com.example.healthtracker.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.healthtracker.R
import com.example.healthtracker.fragments.ChartsFragment
import com.example.healthtracker.fragments.HabitTrackerFragment
import com.example.healthtracker.fragments.MoodJournalFragment
import com.example.healthtracker.fragments.SettingsFragment
import com.example.healthtracker.fragments.UserProfileFragment
import com.example.healthtracker.services.HydrationReminderWorker
import com.example.healthtracker.services.StepTrackingService
import com.example.healthtracker.utils.HealthPreferenceManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPrefsManager: HealthPreferenceManager
    private lateinit var bottomNavigation: BottomNavigationView

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setupHydrationReminders()
        } else {
            Toast.makeText(this, "Notification permission denied. Reminders won't work.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPrefsManager = HealthPreferenceManager.getInstance(this)

        checkLoginStatus()
        sharedPrefsManager.setLastLoginDate()

        setupUI()
        requestNotificationPermission()
        requestActivityRecognitionPermission()

        // Load initial fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HabitTrackerFragment())
                .commit()
        }

        if (sharedPrefsManager.isFirstLaunch()) {
            showWelcomeMessage()
            sharedPrefsManager.setFirstLaunch(false)
        }
    }

    private fun checkLoginStatus() {
        val loggedIn: Boolean = sharedPrefsManager.isUserLoggedIn()
        if (!loggedIn) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupUI() {

        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )

        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.nav_item_select),
            ContextCompat.getColor(this, R.color.white)
        )

        val colorStateList = ColorStateList(states, colors)

        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setBackgroundColor(ContextCompat.getColor(this, R.color.navbar_background))
        bottomNavigation.itemIconTintList = colorStateList
        bottomNavigation.itemTextColor = colorStateList

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_habits -> {
                    loadFragment(HabitTrackerFragment())
                    true
                }
                R.id.nav_mood -> {
                    loadFragment(MoodJournalFragment())
                    true
                }
                R.id.nav_charts -> {
                    loadFragment(ChartsFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(UserProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                setupHydrationReminders()
            }
        } else {
            setupHydrationReminders()
        }
    }

    private fun requestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                this,
                    Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            } else {
                startStepService()
            }
        } else {
            startStepService()
        }
    }

    private fun startStepService() {
        val intent = Intent(this, StepTrackingService::class.java)
        startService(intent)
    }

//    private fun setupHydrationReminders() {
//        if (sharedPrefsManager.isHydrationEnabled()) {
//            val intervalHours = sharedPrefsManager.getHydrationInterval().toLong()
//
//            val constraints = Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
//                .build()
//
//            val hydrationWork = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
//                intervalHours, TimeUnit.HOURS
//            )
//                .setConstraints(constraints)
//                .addTag("hydration_reminder")
//                .build()
//
//            WorkManager.getInstance(this)
//                .enqueueUniquePeriodicWork(
//                    "hydration_reminder",
//                    ExistingPeriodicWorkPolicy.REPLACE,
//                    hydrationWork
//                )
//        }
//    }

    private fun setupHydrationReminders() {
        if (sharedPrefsManager.isHydrationEnabled()) {
            val intervalMinutes = sharedPrefsManager.getHydrationInterval().toLong()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val hydrationWork = OneTimeWorkRequestBuilder<HydrationReminderWorker>()
                .setInitialDelay(intervalMinutes, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag("hydration_reminder")
                .build()

            WorkManager.getInstance(this).enqueueUniqueWork(
                "hydration_reminder",
                ExistingWorkPolicy.REPLACE,
                hydrationWork
            )
        }
    }



    private fun showWelcomeMessage() {
        Toast.makeText(
            this,
            "Welcome to Wellness App! Start tracking your habits and mood.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_share -> {
                shareAppStats()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareAppStats() {
        val habits = sharedPrefsManager.getHabits()
        val moods = sharedPrefsManager.getMoodEntries()
        val today = android.text.format.DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()).toString()

        val completedHabits = habits.count { it.isCompletedForDate(today) }
        val totalHabits = habits.size
        val recentMood = moods.lastOrNull()?.emoji ?: "😐"

        val shareText = "My Wellness Progress Today:\n" +
                "✅ Completed $completedHabits out of $totalHabits habits\n" +
                "🎭 Current mood: $recentMood\n" +
                "\nTracking my wellness with Wellness App! 🌟"

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(shareIntent, "Share your wellness progress"))
    }
}