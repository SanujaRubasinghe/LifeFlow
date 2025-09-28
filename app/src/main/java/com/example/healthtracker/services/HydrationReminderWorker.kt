package com.example.healthtracker.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.healthtracker.utils.NotificationHelper
import com.example.healthtracker.utils.HealthPreferenceManager

class HydrationReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val sharedPrefsManager = HealthPreferenceManager.getInstance(applicationContext)

        return try {
            if (sharedPrefsManager.isHydrationEnabled() && sharedPrefsManager.isNotificationsEnabled()) {
                val notificationHelper = NotificationHelper(applicationContext)
                notificationHelper.showHydrationReminder()
            }
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }
}