package com.ut3.capturethefly.game.drawable.hud

import android.graphics.*
import android.view.MotionEvent
import com.ut3.capturethefly.game.drawable.Drawable
import com.ut3.capturethefly.game.drawable.ImmutableRect
import com.ut3.capturethefly.game.logic.Entity
import com.ut3.capturethefly.game.logic.InputState
import com.ut3.capturethefly.game.utils.Vector2f

class HomeButton(screenSize: RectF, private val bitmap: Bitmap) : Drawable, Entity {

    val width = screenSize.width() / 20f
    val height = screenSize.height() / 10f

    private var targetPointer = -1
    var isPressed = false


    override val rect = ImmutableRect(
        left = screenSize.right - width,
        top = screenSize.top,
        right = screenSize.right,
        bottom = screenSize.top + height
    )

    override fun handleInput(inputState: InputState) {
        val event = inputState.touchEvent
            ?.takeIf { targetPointer == -1 || it.actionIndex == targetPointer }
            ?: return

        if (
            event.actionMasked == MotionEvent.ACTION_UP ||
            event.actionMasked == MotionEvent.ACTION_POINTER_UP ||
            event.actionMasked == MotionEvent.ACTION_MOVE
        ) {
            isPressed = false
        } else {
            val (x,y) = event.getX(event.actionIndex) to event.getY(event.actionIndex)
            isPressed = rect.contains(Vector2f(x,y))
            }
        }

    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) {
        surfaceHolder.drawBitmap(bitmap,null,rect.copyOfUnderlyingRect,paint)
    }

}