package com.ut3.capturethefly.game.utils

import android.content.Context
import androidx.core.content.edit
import com.ut3.capturethefly.game.logic.InputState
import com.ut3.capturethefly.game.logic.MutableInputState
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.map

class Preferences(val context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    var luminosityReference: Float
        get() = sharedPreferences.getFloat("ref_luminosity", 0f)
        set(value) = sharedPreferences.edit { putFloat("ref_luminosity", value) }

    var accelerationReference: Vector3f
        get() {
            val (x,y,z) = sharedPreferences.getString("ref_acceleration", "0;0;0")!!.split(";").map(String::toFloat)
            return Vector3f(x = x, y = y, z = z)
        }
        set(value) = sharedPreferences.edit { putString("ref_acceleration", "${value.x};${value.y};${value.z}") }

    var orientationReference: Vector3f
        get() {
            val (x,y,z) = sharedPreferences.getString("ref_orientation", "0;0;0")!!.split(";").map(String::toFloat)
            return Vector3f(x = x, y = y, z = z)
        }
        set(value) = sharedPreferences.edit { putString("ref_orientation", "${value.x};${value.y};${value.z}") }

    var angleReference: Int
        get() = sharedPreferences.getInt("ref_angle", 0)
        set(value) = sharedPreferences.edit { putInt("ref_angle", value) }

}

