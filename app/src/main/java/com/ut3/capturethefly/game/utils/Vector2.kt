package com.ut3.capturethefly.game.utils

interface Vector2<T: Number> {
    val x: T
    val y: T
}

data class Vector2f(
    override val x: Float,
    override val y: Float
): Vector2<Float>

data class Vector2i(
    override val x: Int,
    override val y: Int
): Vector2<Int>

fun Vector2i.toVector2f() = Vector2f(x = x.toFloat(), y = y.toFloat())
operator fun Vector2f.times(amount: Float) = Vector2f(x * amount, y * amount)

operator fun Vector2i.times(amount: Int) = Vector2i(x * amount, y * amount)
operator fun Vector2i.times(amount: Float) = Vector2f(x * amount, y * amount)