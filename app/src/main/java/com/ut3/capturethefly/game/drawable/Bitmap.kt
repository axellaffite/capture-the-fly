package com.ut3.capturethefly.game.drawable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

fun Context.loadBitmapKeepSize(filename: String): Bitmap {
    val id = resources.getIdentifier(filename, "drawable", packageName)

    return BitmapFactory.decodeResource(resources, id, BitmapFactory.Options().apply { inScaled = false })
        ?: throw IllegalStateException("Unable to load tileset: ${filename}")
}