package com.ut3.capturethefly.game.drawable.hud

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.ut3.capturethefly.game.drawable.Drawable
import com.ut3.capturethefly.game.drawable.ImmutableRect
import com.ut3.capturethefly.game.logic.Entity

class ChargeBar(screenSize : RectF,private val currentPower : () -> Float) :Drawable, Entity {

    val width = screenSize.width()/5
    val height = screenSize.height()/10

    private val borderRadius = 10f
    private val alpha = 130
    private val padding = 15f

    var percentageFilled = 0f

    var border = Paint()
    var fill = Paint()

    override val rect = ImmutableRect(
        left = screenSize.right - width + padding,
        top = screenSize.top + padding,
        right = screenSize.right - padding,
        bottom = screenSize.top+height - padding
    )

    var rectFill = ImmutableRect(
        left = rect.left + borderRadius,
        top = rect.top + borderRadius,
        right = rect.left + borderRadius,
        bottom = rect.bottom - borderRadius
    )


    init {
        border.color = Color.BLACK
        border.alpha = alpha
        fill.color = Color.CYAN
        fill.alpha = alpha
    }

    override fun update(delta: Float) {
        super.update(delta)

        if (currentPower() != percentageFilled) {
            changeCharge(currentPower())
        }
    }

    fun changeCharge(value:Float) {

        percentageFilled = value.coerceIn(0f,1f)

        rectFill = ImmutableRect(
            left = rect.left + borderRadius,
            top = rect.top + borderRadius,
            right = rect.left +borderRadius + ((rect.width - 2*borderRadius) * percentageFilled),
            bottom = rect.bottom - borderRadius
        )
    }


    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) {
        surfaceHolder.drawRoundRect(rect.copyOfUnderlyingRect,8f,8f,border)
        surfaceHolder.drawRoundRect(rectFill.copyOfUnderlyingRect,8f,8f,fill)
    }

}