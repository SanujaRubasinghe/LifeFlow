package com.example.healthtracker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.healthtracker.R
import com.example.healthtracker.activities.LoginActivity
import com.example.healthtracker.utils.HealthPreferenceManager
import com.example.healthtracker.utils.NotificationHelper
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UserProfileFragment : Fragment() {

    private lateinit var sharedPrefsManager: HealthPreferenceManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var settingsButton: Button
    private lateinit var tvUserFullName: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvMemberSince: TextView
    private lateinit var tvAvgMood: TextView
    private lateinit var tvCurrentStreak: TextView
    private lateinit var tvGoalCount: TextView

    private lateinit var logoutBtn: Button

    private lateinit var demoNotificationBtn: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        sharedPrefsManager = HealthPreferenceManager.getInstance(requireContext())
        notificationHelper = NotificationHelper(requireContext())
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
        setupUserInfo()
        setupStats()
        setupListeners()
    }

    private fun setupUi(view: View) {
        settingsButton = view.findViewById(R.id.settings_button)
        tvCurrentStreak = view.findViewById(R.id.current_streak_value)
        tvGoalCount = view.findViewById(R.id.goals_achieved_value)
        logoutBtn = view.findViewById(R.id.logout_button)
        demoNotificationBtn = view.findViewById(R.id.demo_notification_button)

        tvUserFullName = view.findViewById(R.id.user_full_name)
        tvUserName = view.findViewById(R.id.user_name)
        tvMemberSince = view.findViewById(R.id.member_since_value)
        tvAvgMood = view.findViewById(R.id.tv_avg_mood)
    }

    private fun setupUserInfo() {
        val user = sharedPrefsManager.getUserProfile()
        val moodEntries = sharedPrefsManager.getMoodEntries()

        val last7Days = moodEntries.filter {
            val entryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
            val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.time
            entryDate != null && entryDate.after(weekAgo)
        }

        if (last7Days.isNotEmpty()) {
            val mostCommonEmoji = last7Days.groupBy { it.emoji }
                .maxByOrNull { it.value.size }?.key ?: "😐"

            tvAvgMood.text = mostCommonEmoji
        } else {
            tvAvgMood.text = "-"
        }



        tvUserFullName.text = "${user?.name}"
        tvUserName.text = "@${user?.userName}"
        tvMemberSince.text = sharedPrefsManager.getFirstLoginDate()


    }

    private fun setupStats() {
        tvCurrentStreak.text = "${sharedPrefsManager.getStreakCount()} days"
        tvGoalCount.text = "${sharedPrefsManager.getHabits().filter { it.isActive }.size}"
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

        demoNotificationBtn.setOnClickListener {
            notificationHelper.showHydrationReminder()
        }
    }


    companion object {
        fun newInstance(): UserProfileFragment {
            return UserProfileFragment()
        }
    }
}