package com.example.healthtracker.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import kotlin.math.sqrt

class SensorHelper(private val context: Context) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var stepCounter: Sensor? = null
    private var stepDetector: Sensor? = null
    private var accelerometer: Sensor? = null

    private var stepCountListener: ((Int) -> Unit)? = null
    private var shakeListener: (() -> Unit)? = null

    // Step counter
    private var initialStepCount: Int = -1
    private var lastSteps: Int = 0

    // Shake detection
    private var initialized = false
    private var eventCount = 0
    private val ignoreEvents = 5
    private var lastAcceleration = 0f
    private var currentAcceleration = 0f
    private var acceleration = 10f
    private val shakeThreshold = 12f
    private var lastShakeTime: Long = 0
    private val shakeCooldown = 1000L

    init {
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    /** Start counting steps using either STEP_COUNTER or STEP_DETECTOR */
    fun startStepCounting(onStepDetected: (Int) -> Unit) {
        if (stepCounter == null && stepDetector == null) {
            Toast.makeText(context, "Step Counter not available on this device", Toast.LENGTH_SHORT).show()
            return
        }

        stepCountListener = onStepDetected
        lastSteps = 0
        initialStepCount = -1

        stepCounter?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        stepDetector?.let {
            sensorManager.registerListener(object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
                        lastSteps++
                        onStepDetected(lastSteps)
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /** Start shake detection */
    fun startShakeDetection(listener: () -> Unit) {
        if (accelerometer == null) {
            Toast.makeText(context, "Accelerometer not available on this device", Toast.LENGTH_SHORT).show()
            return
        }
        shakeListener = listener
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    /** Stop all sensors */
    fun stopSensors() {
        sensorManager.unregisterListener(this)
        stepCountListener = null
        shakeListener = null
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> handleStepCounter(event)
            Sensor.TYPE_ACCELEROMETER -> handleShake(event)
        }
    }

    /** Handles STEP_COUNTER events */
    private fun handleStepCounter(event: SensorEvent) {
        val totalSteps = event.values[0].toInt()

        if (initialStepCount == -1) {
            initialStepCount = totalSteps
        }

        val steps = totalSteps - initialStepCount
        if (steps != lastSteps) {
            lastSteps = steps
            stepCountListener?.invoke(steps)
        }
    }

    /** Handles shake detection */
    private fun handleShake(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        if (!initialized) {
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            lastAcceleration = currentAcceleration
            initialized = true
            return
        }

        if (eventCount < ignoreEvents) {
            eventCount++
            return
        }

        lastAcceleration = currentAcceleration
        currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta = currentAcceleration - lastAcceleration
        acceleration = acceleration * 0.9f + delta

        if (acceleration > shakeThreshold) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > shakeCooldown) {
                shakeListener?.invoke()
                lastShakeTime = now
            }
        }
    }

    /** Checks if sensors are available */
    fun isStepCounterAvailable(): Boolean = stepCounter != null || stepDetector != null
    fun isAccelerometerAvailable(): Boolean = accelerometer != null
}
