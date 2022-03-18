package com.ut3.capturethefly.game.drawable.hud

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import androidx.core.graphics.withSave
import com.ut3.capturethefly.game.drawable.Drawable
import com.ut3.capturethefly.game.drawable.ImmutableRect
import com.ut3.capturethefly.game.drawable.loadBitmapKeepSize
import com.ut3.capturethefly.game.logic.Entity
import com.ut3.capturethefly.game.logic.InputState


class Joystick(screenSize: RectF, context: Context) : Drawable, Entity {

    enum class Movement { Left, Right,Up,Down,Up_Left,Up_Right,Down_Left,Down_Right, None }

    private val bitmap: Bitmap = context.loadBitmapKeepSize("direction_button")

    private val height = screenSize.height() / 3f

    override val rect = ImmutableRect(
        20f,
        screenSize.bottom - (height) - 20f,
        20f + height,
        screenSize.bottom - 20f
    )

    private val leftZone = RectF(
        rect.left,
        rect.top,
        rect.left + rect.width * 1f / 3f,
        rect.bottom
    )

    private val rightZone = RectF(
        rect.right - rect.width * 1f / 3f,
        rect.top,
        rect.right,
        rect.bottom
    )

    private val upZone = RectF(
        rect.left,
        rect.top,
        rect.right,
        rect.top + rect.height * 1f / 3f
    )

    private val downZone = RectF(
        rect.left,
        rect.bottom - rect.height * 1f / 3f,
        rect.right,
        rect.bottom
    )

    var horizontalDirection: Movement = Movement.None; private set
    var verticalDirection: Movement = Movement.None; private set
    private var targetPointer = -1

    override fun handleInput(inputState: InputState) {
        val event = inputState.touchEvent
            ?.takeIf { targetPointer == -1 || targetPointer == it.actionIndex }
            ?: return


        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_MOVE -> {
                horizontalDirection = when {
                    // Not the right height
                    event.y !in rect.top..rect.bottom -> Movement.None
                    // Left side of the button
                    event.x in leftZone.left..leftZone.right -> Movement.Left
                    // Right side of the button
                    event.x in rightZone.left..rightZone.right -> Movement.Right
                    else -> Movement.None
                }

                verticalDirection = when {
                    // Not the right x position
                    event.x !in rect.left..rect.right -> Movement.None
                    event.y in upZone.top..upZone.bottom -> Movement.Up
                    event.y in downZone.top..downZone.bottom -> Movement.Down
                    else -> Movement.None
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                targetPointer = -1
                horizontalDirection = Movement.None
                verticalDirection = Movement.None
            }

            else -> {
                horizontalDirection = Movement.None
                verticalDirection = Movement.None
            }
        }

//        println("vertical=$verticalDirection | horizontal=$horizontalDirection")

        if (horizontalDirection != Movement.None) {
            targetPointer = event.actionIndex
        }
    }

    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) =
        surfaceHolder.withSave {
            surfaceHolder.drawBitmap(bitmap,null,rect.copyOfUnderlyingRect, null)
            //surfaceHolder.drawRect(rect.copyOfUnderlyingRect,paint)

//            paint.color = Color.RED
//            surfaceHolder.drawRect(rightZone,paint)
//            paint.color = Color.BLUE
//            surfaceHolder.drawRect(leftZone,paint)
//            paint.color = Color.GREEN
//            surfaceHolder.drawRect(upZone,paint)
//            paint.color = Color.YELLOW
//            surfaceHolder.drawRect(downZone,paint)
        }

}