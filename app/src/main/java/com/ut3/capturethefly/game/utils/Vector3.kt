package com.ut3.capturethefly.game.utils

import kotlin.math.sqrt

interface Vector3<T: Number> {
    val x: T
    val y: T
    val z: T
}

data class Vector3f(
    override val x: Float,
    override val y: Float,
    override val z: Float
): Vector3<Float>

data class Vector3i(
    override val x: Int,
    override val y: Int,
    override val z: Int
): Vector3<Int>

operator fun Vector3f.times(amount: Float) = Vector3f(x * amount, y * amount, z * amount)

operator fun Vector3i.times(amount: Int) = Vector3i(x * amount, y * amount, z * amount)
operator fun Vector3i.times(amount: Float) = Vector3f(x * amount, y * amount, z * amount)

val Vector3f.length get() = sqrt(x*x + y*y + z*z)