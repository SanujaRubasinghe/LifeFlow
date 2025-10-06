package com.example.healthtracker.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.healthtracker.R
import com.example.healthtracker.utils.HealthPreferenceManager
import kotlin.math.sqrt

class StepTrackingService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sharedPreferenceManager: HealthPreferenceManager
    private var stepCounter: Sensor? = null
    private var initialSteps = -1
    private var lastSteps = 0

    companion object {
        private const val CHANNEL_ID = "StepTrackerChannel"
        private const val NOTIFICATION_ID = 1003
        const val ACTION_STEPS_UPDATED = "com.example.healthtracker.STEPS_UPDATED"
        const val EXTRA_STEPS = "steps"
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferenceManager = HealthPreferenceManager.getInstance(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(0))

        stepCounter?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: android.hardware.SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()
            if (initialSteps == -1) {
                initialSteps = totalSteps
            }
            val steps = totalSteps - initialSteps
            if (steps != lastSteps) {
                lastSteps = steps
                updateNotification(steps)
                sharedPreferenceManager.addStepsForToday(steps)

                // Broadcast step updates so UI can listen
                val intent = Intent(ACTION_STEPS_UPDATED).apply {
                    putExtra(EXTRA_STEPS, steps)
                }
                sendBroadcast(intent)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Step Tracker Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun buildNotification(steps: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Step Tracking Active")
            .setContentText("Steps: $steps")
            .setSmallIcon(R.drawable.ic_step)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(steps))
    }
}
