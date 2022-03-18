package com.ut3.capturethefly

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import com.ut3.capturethefly.databinding.SensorActivityBinding
import com.ut3.capturethefly.game.logic.MutableInputState
import com.ut3.capturethefly.game.utils.Preferences
import com.ut3.capturethefly.game.utils.SensorsListener
import com.ut3.capturethefly.game.utils.Vector3f
import java.lang.Float.max
import java.lang.System.currentTimeMillis


class SensorActivity: AppCompatActivity() {

    private lateinit var preferences: Preferences
    private lateinit var binding: SensorActivityBinding
    private lateinit var sensorsListener: SensorsListener
    private var count = 0
    private var hasShakeBeenRecorded = false

    private val referenceState = MutableInputState(
        touchEvent = null,
        acceleration = Vector3f(0f,0f,0f),
        luminosity = 0f,
        orientation = Vector3f(0f,0f, 0f)
    )

    private val mutableState = referenceState.copy()

    override fun onResume() {
        super.onResume()

        if (this::sensorsListener.isInitialized) {
            sensorsListener.startListeners()
        }
    }

    override fun onPause() {
        if (this::sensorsListener.isInitialized) {
            sensorsListener.stopListeners()
        }

        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = Preferences(this)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding = SensorActivityBinding.inflate(layoutInflater)

        binding.run {
            luminosityButton.apply {
                setOnClickListener {
                    text = "✔"
                    isEnabled = false
                    referenceState.luminosity = mutableState.luminosity
                    count ++
                    enablePlayButtonIfNeeded()

                    scrollView.smoothScrollTo(0, orientationButton.y.toInt())
                }
            }

            orientationButton.apply {
                setOnClickListener {
                    text = "✔"
                    isEnabled = false
                    referenceState.orientation = mutableState.orientation
                    referenceState.angle = mutableState.angle
                    count ++
                    enablePlayButtonIfNeeded()

                    scrollView.smoothScrollTo(0, shakeButton.y.toInt())
                }
            }

            binding.shakeButton.apply {
                setOnClickListener {
                    recordShaking(currentTimeMillis())
                }
            }

            playButton.setOnClickListener {
                preferences.luminosityReference = referenceState.luminosity
                preferences.accelerationReference = referenceState.acceleration
                preferences.orientationReference = referenceState.orientation
                preferences.angleReference = referenceState.angle

                val intent = Intent(this@SensorActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                }
                startActivity(intent)
                finish()
            }
        }

        setContentView(binding.root)
        sensorsListener = SensorsListener(view = binding.root, state = mutableState).apply {
            startListeners()
        }
    }

    private fun recordShaking(startTime: Long) {
        var maxX = 0f
        var maxY = 0f
        var maxZ = 0f

        fun checkEnd() {
            val remaining = 3000 - (currentTimeMillis() - startTime)
            binding.shakeButton.isEnabled = false

            if (remaining < 0) {
                sensorsListener.onAccelerometerValueChanged = { }
                binding.shakeButton.setText(R.string.activity_sensor_record_again_text)
                binding.shakeButton.isEnabled = true
                referenceState.acceleration = Vector3f(x = maxX, y = maxY, z = maxZ)

                if (!hasShakeBeenRecorded) {
                    count++
                    enablePlayButtonIfNeeded()
                    hasShakeBeenRecorded = true
                }

                return
            }

            binding.shakeButton.text = (remaining / 1000L).toString()
            val handler = Handler(mainLooper)
            handler.postDelayed(200L) {
                checkEnd()
            }
        }

        sensorsListener.onAccelerometerValueChanged = { (x, y, z) ->
            maxX = max(maxX, x)
            maxY = max(maxY, y)
            maxZ = max(maxZ, z)
        }

        checkEnd()
    }

    private fun enablePlayButtonIfNeeded() {
        if (count == 3) {
            binding.playButton.isEnabled = true
            binding.run {
                scrollView.smoothScrollTo(0, playButton.y.toInt())
            }
        }
    }

}