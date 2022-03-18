package com.ut3.capturethefly.game.drawable

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.ut3.capturethefly.game.utils.Vector2f

/**
 * Base class used to indicate that a class is drawable on the screen.
 */
interface Drawable {

    /**
     * Bounds of this drawable.
     * Used to optimize rendering by calculating if this drawable intersects the camera field.
     * If required, you can still bypass this protection by calling [drawOnCanvas] directly.
     */
    val rect: ImmutableRect

    /**
     * Function that must be overridden by every implementation of [Drawable] in order
     * to draw it on a given canvas.
     *
     * @param bounds bounds of the camera we are currently drawing with
     * @param surfaceHolder the target on which the drawable should be drawn
     * @param paint parameters previously used to draw on the [Canvas]
     */
    fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint)

    /**
     * Default function to ensure this [Drawable] will only be drawn if the current [rect]
     * intersects the given [bounds].
     *
     * @param bounds bounds that will be used to compute the intersection between this drawable
     * and the target (camera)
     * @param target target on which this [Drawable] should be drawn
     * @param paint parameters previously used to draw on the [Canvas]
     * @return whether this [Drawable] has been drawn
     */
    fun draw(bounds: RectF, target: Canvas, paint: Paint): Boolean {
        val intersects = rect.intersects(bounds)
        if (intersects) {
            drawOnCanvas(bounds, target, paint)
            return true
        }

        return false
    }

    fun intersects(other: Drawable) = rect.intersects(other.rect)

    val center get() = Vector2f(rect.centerX, rect.centerY)
}

/**
 * Used to draw a [Drawable] on a [Canvas].
 * Defined as a top level function so it is accessible from everywhere in the project.
 *
 * @param bounds bounds that will be used to compute whether the [drawable] intersects the
 * camera fov
 * @param drawable drawable to draw on the current [Canvas]
 * @param paint parameters previously used to draw on the [Canvas]
 */
fun Canvas.draw(bounds: RectF, drawable: Drawable, paint: Paint) {
    drawable.draw(bounds, this, paint)
}