package com.ut3.capturethefly.game.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class DrawableRect(override val rect: ImmutableRect) : Drawable {
    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) {
        paint.color = Color.RED
        surfaceHolder.drawRect(rect.copyOfUnderlyingRect, paint)
    }
}