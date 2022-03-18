package com.ut3.capturethefly.game.drawable.tiledmap

import android.graphics.*
import com.ut3.capturethefly.game.drawable.Drawable
import com.ut3.capturethefly.game.drawable.ImmutableRect
import com.ut3.capturethefly.game.utils.Vector2i

class Chunk(
    private val vertices: FloatArray,
    private val textCoordinates: FloatArray,
    private val tileset: Tileset,
    override val rect: ImmutableRect
) : Drawable {

    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) {
        val texture = Paint(paint).apply {
            shader = BitmapShader(tileset.bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            isAntiAlias = false
            isDither = true
            isFilterBitmap = false
        }

        surfaceHolder.drawVertices(
            Canvas.VertexMode.TRIANGLES,
            vertices.size,
            vertices,
            0,
            textCoordinates,
            0,
            null,
            0,
            null,
            0,
            0,
            texture
        )
    }
}