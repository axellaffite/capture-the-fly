package com.ut3.capturethefly.game.utils

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.Sensor.*
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.core.content.getSystemService
import com.ut3.capturethefly.game.logic.MutableInputState
import kotlin.math.atan2
import kotlin.math.roundToInt

class SensorsListener(
    private val view: View,
    private val state: MutableInputState
) : OnTouchListener, SensorEventListener {

    companion object {
        private const val X_AXIS_INDEX = 0
        private const val Y_AXIS_INDEX = 1
        private const val Z_AXIS_AXIS = 2
        private const val ORIENTATION_UNKNOWN = -1
        private const val ONE_EIGHTY_OVER_PI = 57.29577957855f
    }

    private val sensorManager = view.context.getSystemService<SensorManager>()
    var onLuminosityValueChanged: (Float) -> Unit = { }
    var onAccelerometerValueChanged: (Vector3f) -> Unit = { }
    var onOrientationValueChanged: (Vector3f) -> Unit = { }
    var isListening = false; private set

    fun startListeners(): Boolean {
        if (isListening) {
            return false
        }

        view.setOnTouchListener(this)
        sensorManager?.run {
            getDefaultSensor(TYPE_LIGHT)?.let { lightSensor ->
                registerListener(
                    this@SensorsListener,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
            }

            getDefaultSensor(TYPE_ACCELEROMETER)?.let { accelerometer ->
                registerListener(
                    this@SensorsListener,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
            }

            getDefaultSensor(TYPE_GYROSCOPE)?.let { accelerometer ->
                registerListener(
                    this@SensorsListener,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
            }
        }

        return true
    }

    fun stopListeners() {
        view.setOnClickListener(null)
        sensorManager?.unregisterListener(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        state.touchEvent = motionEvent
        return true
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) = Unit

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            TYPE_LIGHT -> {
                state.luminosity = event.values[0]
                onLuminosityValueChanged(state.luminosity)
            }

            TYPE_ACCELEROMETER -> {
                state.acceleration = Vector3f(
                    x = event.values[0],
                    y = event.values[1],
                    z = event.values[2]
                )

                val newRotationDeg = calculateNewRotationDegree(event)
                val rotationRoundedClockwise = calculateRoundedRotation(newRotationDeg)
                state.angle = rotationRoundedClockwise

                onAccelerometerValueChanged(state.acceleration)
            }

            TYPE_GYROSCOPE -> {
                state.orientation = Vector3f(
                    x = event.values[0],
                    y = event.values[1],
                    z = event.values[2],
                )

                onOrientationValueChanged(state.orientation)
            }
        }
    }

    private fun calculateNewRotationDegree(event: SensorEvent): Int {
        val values = event.values
        var newRotationDeg = ORIENTATION_UNKNOWN
        val x = -values[X_AXIS_INDEX]
        val y = -values[Y_AXIS_INDEX]
        val z = -values[Z_AXIS_AXIS]
        val magnitude = x * x + y * y
        // Don't trust the angle if the magnitude is small compared to the y value
        if (magnitude * 4 >= z * z) {
            val angle = atan2((-y).toDouble(), x.toDouble()).toFloat() * ONE_EIGHTY_OVER_PI
            newRotationDeg = 90 - angle.roundToInt()
            // normalize to 0 - 359 range
            while (newRotationDeg >= 360) {
                newRotationDeg -= 360
            }
            while (newRotationDeg < 0) {
                newRotationDeg += 360
            }
        }
        return newRotationDeg
    }

    private fun calculateRoundedRotation(newRotationDeg: Int): Int {
        return when ((newRotationDeg + 45) % 360) {
            in 0 .. 90 -> 0
            in 91 .. 180 -> 90
            in 181 .. 270 -> 180
            in 270 .. 356 -> 270
            else -> 0
        }
    }

}