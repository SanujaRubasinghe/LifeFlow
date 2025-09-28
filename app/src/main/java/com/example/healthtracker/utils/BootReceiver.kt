package com.example.healthtracker.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.healthtracker.services.HydrationReminderWorker
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {

            val sharedPrefsManager = HealthPreferenceManager.getInstance(context)

            if (sharedPrefsManager.isHydrationEnabled()) {
                val intervalHours = sharedPrefsManager.getHydrationInterval().toLong()

                val hydrationWork = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
                    intervalHours, TimeUnit.HOURS
                )
                    .addTag("hydration_reminder")
                    .build()

                WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(
                        "hydration_reminder",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        hydrationWork
                    )
            }
        }
    }
}