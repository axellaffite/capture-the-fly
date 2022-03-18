package com.ut3.capturethefly.game.drawable

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.ut3.capturethefly.game.drawable.Drawable
import com.ut3.capturethefly.game.drawable.ImmutableRect
import com.ut3.capturethefly.game.utils.Vector2f
import kotlin.math.sqrt

data class Circle(
    val centerX: Float,
    val centerY: Float,
    val radius: Float,
    val color: Int,
    val alpha: Int
): Drawable {

    override val rect = ImmutableRect(
        centerX - radius,
        centerY - radius,
        centerX + radius,
        centerY + radius
    )

    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) {
        paint.color = color
        paint.alpha = alpha
        surfaceHolder.drawCircle(centerX, centerY, radius, paint)
    }

    fun contains(point: Vector2f): Boolean {
        val x = point.x - centerX
        val y = point.y - centerY

        return sqrt((x * x) + (y * y)) < radius
    }

}