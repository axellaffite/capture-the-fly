package com.ut3.capturethefly.game.drawable.tiledmap

import android.content.Context
import com.ut3.capturethefly.game.drawable.loadBitmapKeepSize
import com.ut3.capturethefly.game.utils.Vector2i

class Tileset(filename: String, private val chunkSize: Int, val tileSize: Int, context: Context) {

    val bitmap = context.loadBitmapKeepSize(filename)

    val width = bitmap.width / tileSize
    val height = bitmap.height / tileSize

    /**
     * Computes the position of the given [index] in the current tileset.
     * The position isn't multiplied by the [chunk size][chunkSize].
     *
     * Example:
     * current tileset properties :
     *  - width = 4
     *  - chunkSize = 16
     *
     * index = 50
     *
     * Result -> Vector2i(x=2, y=12)
     *
     *
     * @param index index of the tile
     * @return the x and y position of the tile in the current tileset (not * by [tileSize])
     */
    fun indicesIn2DForIndex(index: Int) = Vector2i(x = index % width, y = index / tileSize)
}