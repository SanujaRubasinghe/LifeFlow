package com.example.healthtracker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.glance.layout.Row
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.healthtracker.R
import com.example.healthtracker.activities.LoginActivity
import com.example.healthtracker.services.HydrationReminderWorker
import com.example.healthtracker.utils.HealthPreferenceManager
import java.util.concurrent.TimeUnit

class UserProfileFragment : Fragment() {

    private lateinit var sharedPrefsManager: HealthPreferenceManager
    private lateinit var settingsButton: Button
    private lateinit var tvCurrentStreak: TextView

    private lateinit var logoutBtn: Button


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
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUi(view)
        setupStats()
        setupListeners()
    }

    private fun setupUi(view: View) {
        settingsButton = view.findViewById(R.id.settings_button)
        tvCurrentStreak = view.findViewById(R.id.current_streak_value)
        logoutBtn = view.findViewById(R.id.logout_button)
    }

    private fun setupStats() {
        tvCurrentStreak.text = "${sharedPrefsManager.getStreakCount()} days"
    }

    private fun setupListeners() {
        settingsButton.setOnClickListener {
            val settingsFragment = SettingsFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, settingsFragment)
                .addToBackStack(null)
                .commit()
        }

        logoutBtn.setOnClickListener {
            sharedPrefsManager.setUserLoggedIn(false)
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
    }


    companion object {
        fun newInstance(): UserProfileFragment {
            return UserProfileFragment()
        }
    }
}