package com.ut3.capturethefly.game.drawable.tiledmap

import android.content.Context
import com.ut3.capturethefly.game.drawable.loadBitmapKeepSize
import com.ut3.capturethefly.game.utils.Vector2i

class Tileset(filename: String, private val chunkSize: Int, val tileSize: Int, context: Context) {

    val bitmap = context.loadBitmapKeepSize(filename)

    val width = bitmap.width / tileSize
    val height = bitmap.height / tileSize

}