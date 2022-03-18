package com.ut3.capturethefly.game.levels.introduction

import android.graphics.*
import com.ut3.capturethefly.game.drawable.Drawable
import com.ut3.capturethefly.game.drawable.ImmutableRect
import com.ut3.capturethefly.game.drawable.tiledmap.TiledMap
import com.ut3.capturethefly.game.logic.Entity
import com.ut3.capturethefly.game.utils.Vector2i

class BridgeBlock(
    x: Int,
    y: Int,
    tilemap: TiledMap,
    private val textVertices: FloatArray
) : Drawable, Entity
{

    constructor(x: Int, y: Int, tilemap: TiledMap, tilePosition: Vector2i) : this(
        x, y, tilemap, textVertices = tilemap.textVerticesGivenPosition(tilePosition)
    )

    constructor(x: Int, y: Int, tilemap: TiledMap, tileIndex: Int) : this(
        x, y, tilemap, textVertices = tilemap.textVerticesGivenIndex(tileIndex)
    )

    private val bitmap = tilemap.bitmap
    private val left = x * tilemap.tileSize
    private val top = y * tilemap.tileSize
    private val right = left + tilemap.tileSize
    private val bottom = top + tilemap.tileSize

    override val rect = ImmutableRect(left, top, right, bottom)

    private val posVertices = floatArrayOf(
        rect.left, rect.top,
        rect.left, rect.bottom,
        rect.right, rect.top,
        rect.right, rect.top,
        rect.left, rect.bottom,
        rect.right, rect.bottom
    )

    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) {
        val texture = Paint(paint).apply {
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            isAntiAlias = false
            isDither = true
            isFilterBitmap = false
        }

        // draw vertices
        surfaceHolder.drawVertices(
            Canvas.VertexMode.TRIANGLES,
            12,
            posVertices,
            0,
            textVertices,
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