package com.example.healthtracker.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.healthtracker.R
import com.example.healthtracker.activities.MainActivity
import com.example.healthtracker.utils.HealthPreferenceManager
import java.text.SimpleDateFormat
import java.util.*

class HabitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val sharedPrefsManager = HealthPreferenceManager.getInstance(context)
            val habits = sharedPrefsManager.getHabits().filter { it.isActive }
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            // Calculate overall completion percentage
            val totalProgress = if (habits.isNotEmpty()) {
                habits.sumOf { it.getProgressPercentage(today).toInt() } / habits.size
            } else {
                0
            }

            val completedHabits = habits.count { it.isCompletedForDate(today) }

            // Create intent to open main activity
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget_habit).apply {
                // Set texts
                setTextViewText(R.id.tv_widget_title, "Daily Habits")
                setTextViewText(R.id.tv_widget_progress, "$totalProgress%")
                setTextViewText(R.id.tv_widget_completed, "$completedHabits/${habits.size} completed")
                setTextViewText(R.id.tv_widget_updated, "Updated: $currentTime")

                // Set progress bar
                setProgressBar(R.id.progress_bar_widget, 100, totalProgress, false)

                // Set colors based on progress
                val progressColor = when {
                    totalProgress >= 80 -> android.graphics.Color.GREEN
                    totalProgress >= 50 -> android.graphics.Color.YELLOW
                    else -> android.graphics.Color.RED
                }

                // Set background color based on completion status
                val backgroundColor = when {
                    completedHabits == habits.size && habits.isNotEmpty() -> android.graphics.Color.parseColor("#E8F5E8")
                    totalProgress > 0 -> android.graphics.Color.parseColor("#FFF3E0")
                    else -> android.graphics.Color.parseColor("#FFEBEE")
                }
                setInt(R.id.widget_container, "setBackgroundColor", backgroundColor)

                // Set click intent
                setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            }

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}