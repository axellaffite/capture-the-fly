package com.ut3.capturethefly.game.logic

import android.view.MotionEvent
import com.ut3.capturethefly.game.utils.Vector3f
import com.ut3.capturethefly.game.utils.length

interface InputState {
    val touchEvent: MotionEvent?
    val luminosity: Float
    val acceleration: Vector3f
    val orientation: Vector3f
    val rotation: Vector3f
    val angle: Int
}

data class MutableInputState(
    override var touchEvent: MotionEvent? = null,
    override var acceleration: Vector3f = Vector3f(0f, 0f, 0f),
    override var luminosity: Float = 0f,
    override var orientation: Vector3f = Vector3f(0f, 0f, 0f),
    override var rotation: Vector3f = Vector3f(0f, 0f, 0f),
    override var angle: Int = 0,
): InputState

fun InputState.isShaking(accelerationReference: Vector3f) =
    acceleration.length >= (accelerationReference.length * 3f / 4f)

fun InputState.isUpsideDown(angleReference: Int) = when (angleReference) {
    0 -> angle == 180
    90 -> angle == 270
    180 -> angle == 0
    270 -> angle == 90
    else -> false
}