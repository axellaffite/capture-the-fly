package com.ut3.capturethefly.game.drawable

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.ut3.capturethefly.game.utils.Vector2f

class ImmutableRect(left: Float, top: Float, right: Float, bottom: Float) {
    constructor(rect: RectF): this(rect.left, rect.top, rect.right, rect.bottom)
    constructor(): this(0f, 0f, 0f, 0f)

    private val rect = RectF(left, top, right, bottom)

    fun intersects(other: RectF) = RectF.intersects(rect, other)
    fun intersects(other: ImmutableRect) = intersects(other.rect)

    fun contains(other: RectF) = rect.contains(other)
    fun contains(other: ImmutableRect) = rect.contains(other.rect)

    fun contains(point: Vector2f) = rect.contains(point.x, point.y)
    fun contains(x: Float, y: Float) = rect.contains(x, y)

    val copyOfUnderlyingRect get() = RectF(rect)

    val left get() = rect.left
    val right get() = rect.right
    val top get() = rect.top
    val bottom get() = rect.bottom
    val width = rect.width()
    val height = rect.height()
    val centerX = rect.centerX()
    val centerY = rect.centerY()

    override fun toString() = rect.toString()
}

fun RectF.intersects(other: ImmutableRect) = other.intersects(this)
fun RectF.contains(other: ImmutableRect) = contains(other.copyOfUnderlyingRect)
fun Canvas.drawRect(rect: ImmutableRect, paint: Paint) = drawRect(rect.copyOfUnderlyingRect, paint)