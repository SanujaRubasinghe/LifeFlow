package com.example.healthtracker.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class SensorHelper(private val context: Context): SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var stepCounter: Sensor? = null
    private var accelerometer: Sensor? = null

    private var stepCountListener: ((Int) -> Unit)? = null
    private var shakeListener: (() -> Unit)? = null

    // Shake detection
    private var lastAcceleration = 0f
    private var currentAcceleration = 0f
    private var acceleration = 10f
    private val shakeThreshold = 12f

    init {
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun startStepCounting(listener: (Int) -> Unit) {
        stepCountListener = listener
        stepCounter?.let {sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun startShakeDetection(listener: () -> Unit) {
        shakeListener = listener
        accelerometer?.let {sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopSensors() {
        sensorManager.unregisterListener(this)
        stepCountListener = null
        shakeListener = null
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {sensorEvent ->
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    val steps = sensorEvent.values[0].toInt()
                    stepCountListener?.invoke(steps)
                }

                Sensor.TYPE_ACCELEROMETER -> {
                    val x = sensorEvent.values[0]
                    val y = sensorEvent.values[1]
                    val z = sensorEvent.values[2]

                    lastAcceleration = currentAcceleration
                    currentAcceleration = sqrt((x*x + y*y + z*z).toDouble()).toFloat()
                    val delta = currentAcceleration - lastAcceleration
                    acceleration = acceleration * 0.9f + delta

                    if (acceleration > shakeThreshold) {
                        shakeListener?.invoke()
                    }
                }
            }
        }
    }

    fun isStepCounterAvailable(): Boolean = stepCounter !== null
    fun isAccelerometerAvailable(): Boolean = accelerometer !== null
}