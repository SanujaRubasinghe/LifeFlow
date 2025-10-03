package com.example.healthtracker.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
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

            val views = RemoteViews(context.packageName, R.layout.widget_habit).apply {
                setTextViewText(R.id.tv_widget_title, "Daily Habits")
                setTextViewText(R.id.tv_widget_progress, "$totalProgress%")
                setTextViewText(R.id.tv_widget_completed, "$completedHabits/${habits.size} completed")
                setTextViewText(R.id.tv_widget_updated, "Updated: $currentTime")

                val progressColor = when {
                    totalProgress >= 80 -> "#4CAF50"  // Green
                    totalProgress >= 50 -> "#FF9800"  // Orange
                    else -> "#F44336"                 // Red
                }

                setInt(R.id.progress_bar_widget, "setColorFilter", Color.parseColor(progressColor))

                val backgroundColor = when {
                    completedHabits == habits.size && habits.isNotEmpty() -> "#E8F5E9"  // Light Green
                    totalProgress > 0 -> "#FFF3E0"  // Light Orange
                    else -> "#FFEBEE"               // Light Red
                }
                setInt(R.id.widget_container, "setBackgroundResource", R.drawable.widget_background)

                setInt(R.id.widget_container, "setBackgroundColor", Color.parseColor(backgroundColor))

                setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateHabitWidget(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, HabitWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

    }
}