package com.ut3.capturethefly.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.ut3.capturethefly.game.drawable.cameras.Camera
import com.ut3.capturethefly.game.logic.GameLogic

class GameView(context: Context): SurfaceView(context), SurfaceHolder.Callback {
    private val drawingContext = DrawingContext()
    val rect get() = RectF(0f, 0f, width.toFloat(), height.toFloat())

    private lateinit var logic: GameLogic

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        keepScreenOn = true
        holder.addCallback(this)
    }

    fun draw(paint: Paint = Paint(), block: DrawingContext.(Canvas, Paint) -> Unit) {
        holder?.withLock { canvas: Canvas? ->
            canvas?.let {
                drawingContext.use(paint, canvas, block)
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        post {
            invalidate()
            logic = GameLogic(this)
            logic.start()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        logic.stop()
    }

    inner class DrawingContext {
        private var paint: Paint = Paint()

        private var canvas: Canvas = Canvas()

        fun use(paint: Paint, canvas: Canvas, block: DrawingContext.(Canvas, Paint) -> Unit) {
            this.paint = paint
            this.canvas = canvas
            block(canvas, paint)
        }

        fun withCamera(camera: Camera, block: Camera.(Canvas, Paint) -> Unit) {
            val tmpPaint = Paint(paint)
            camera.draw(canvas, tmpPaint, block)
        }
    }
}

fun <T> SurfaceHolder.withLock(body: (Canvas) -> T) {
    var canvas: Canvas? = null
    try {
        canvas = lockHardwareCanvas()
        body(canvas)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        runCatching {
            canvas?.let { unlockCanvasAndPost(it) }
        }
    }
}