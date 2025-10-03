package com.example.healthtracker.services

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.healthtracker.utils.NotificationHelper
import com.example.healthtracker.utils.HealthPreferenceManager
import java.util.concurrent.TimeUnit

class HydrationReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val sharedPrefsManager = HealthPreferenceManager.getInstance(applicationContext)

        return try {
            if (sharedPrefsManager.isHydrationEnabled() && sharedPrefsManager.isNotificationsEnabled()) {
                val notificationHelper = NotificationHelper(applicationContext)
                notificationHelper.showHydrationReminder()

                val intervalMinutes = sharedPrefsManager.getHydrationInterval().toLong()
                val newWork = OneTimeWorkRequestBuilder<HydrationReminderWorker>()
                    .setInitialDelay(intervalMinutes, TimeUnit.MINUTES)
                    .build()

                WorkManager.getInstance(applicationContext).enqueue(newWork)
            }
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }
}