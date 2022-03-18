package com.ut3.capturethefly.game.drawable.cameras

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.translationMatrix
import com.ut3.capturethefly.game.drawable.Drawable
import com.ut3.capturethefly.game.drawable.contains
import com.ut3.capturethefly.game.utils.Vector2f

/**
 * Class that is used to efficiently display drawables.
 * This can also be used to move the camera into the virtual world
 * and also on the screen.
 *
 * @property screenPosition position of the camera on the screen
 * @property gamePosition position of the camera in the game
 */
open class Camera(private val screenPosition: RectF,val gamePosition: RectF) {

    /**
     * Copy constructor to initialize a copy of the current [Camera] without having to
     * manually copy its behavior.
     */
    constructor(other: Camera): this(
        screenPosition = RectF(other.screenPosition),
        gamePosition = RectF(other.gamePosition)
    )

    /**
     * Draw on the screen by saving and applying the current state of this camera
     * to the given [canvas].
     *
     * The parameters that will be applied are the position of the [Camera] but this is
     * likely to evolve if needed.
     *
     * @param canvas canvas on which we are going to apply the state of the current camera
     * @param paint parameters previously used to draw on the [Canvas]
     * @param block lambda in which you can draw everything with the parameters of the [Camera]
     * applied to the given [canvas]
     */
    fun draw(canvas: Canvas, paint: Paint, block: Camera.(Canvas, Paint) -> Unit) {
        canvas.save()
        canvas.clipRect(screenPosition)
        canvas.concat(
            translationMatrix(
                -gamePosition.left + screenPosition.left,
                -gamePosition.top + screenPosition.top
            )
        )

        block(this, canvas, paint)
        canvas.restore()
    }

    /**
     * Utility function to draw on the screen with the current parameters of this [Camera].
     * It automatically applies the bounds of this camera to the [drawable's draw][Drawable.draw]
     * function.
     *
     * @param drawable what to draw on the screen
     * @param paint parameters previously used to draw on the [Canvas]
     */
    fun Canvas.draw(drawable: Drawable, paint: Paint) {
        drawable.draw(gamePosition, this, paint)
    }

    /**
     * This will move the camera by the given offset on the screen.
     * If you want to move the camera in game, please use the [moveInGame] function.
     *
     * This function allows to place the current camera at different places on the screen.
     *
     * @param offsetX offset to apply to the current x-axis position (defaulted to 0)
     * @param offsetY offset to apply to the current y-axis position (defaulted to 0)
     */
    fun moveOnScreen(offsetX: Float = 0f, offsetY: Float = 0f) {
        screenPosition.offset(offsetX, offsetY)
    }

    /**
     * This will move the camera by the given offset in game.
     * If you want to move the camera on the screen, please use the [moveOnScreen] function.
     *
     * @param offsetX offset to apply to the current x-axis position (defaulted to 0)
     * @param offsetY offset to apply to the current y-axis position (defaulted to 0)
     */
    fun moveInGame(offsetX: Float = 0f, offsetY: Float = 0f) {
        gamePosition.offset(offsetX, offsetY)
    }

    /**
     * This will center the current camera on a given position.
     * This can be use to center the camera on the player by calling it in the
     * postUpdate hooks.
     *
     * @param position The position on which the current camera should be centered on.
     */
    fun centerOn(position: Vector2f) {
        gamePosition.offsetTo(
            position.x - gamePosition.width() / 2f,
            position.y - gamePosition.height() / 2f
        )
    }

    /**
     * Utility function to compute whether the current [Camera] contains a given [Drawable].
     * This function uses the [Drawable.rect] field of the Drawable class
     * coupled to the current [gamePosition].
     *
     * @param drawable drawable for which the intersection should be checked
     * @return whether this camera contains the given drawable
     */
    fun contains(drawable: Drawable): Boolean {
        return gamePosition.contains(drawable.rect)
    }

}