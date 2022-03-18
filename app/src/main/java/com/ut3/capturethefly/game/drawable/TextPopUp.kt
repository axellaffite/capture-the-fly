package com.ut3.capturethefly.game.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.ut3.capturethefly.game.utils.Vector2f

class TextPopUp(private val textToPrint: String, private val vector2f: Vector2f) : Drawable {
    override val rect: ImmutableRect = ImmutableRect(vector2f.x,vector2f.y,vector2f.x+1000f,vector2f.y+1000f)

    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) {
        paint.color = Color.WHITE
        surfaceHolder.drawText(textToPrint, vector2f.x, vector2f.y, paint)
    }
}