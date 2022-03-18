package com.ut3.capturethefly.game.levels.introduction

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.ut3.capturethefly.game.drawable.Drawable
import com.ut3.capturethefly.game.drawable.ImmutableRect
import com.ut3.capturethefly.game.drawable.draw
import com.ut3.capturethefly.game.drawable.tiledmap.TiledMap
import com.ut3.capturethefly.game.logic.Entity
import com.ut3.capturethefly.game.logic.Player
import com.ut3.capturethefly.game.utils.Vector2i

class Bridge(
    private val x: Int,
    private val y: Int,
    private val blockCount: Int,
    private val tilemap: TiledMap,
    private val player: Player
) : Drawable, Entity {

    override val rect = let {
        val left = x * tilemap.tileSize
        val top = y * tilemap.tileSize
        val right = left + tilemap.tileSize
        val bottom = top + tilemap.tileSize

        ImmutableRect(left, top, right, bottom)
    }


    var inConstruction: Boolean = false; private set
    var isShown: Boolean = false; private set
    private val blocks = mutableListOf<BridgeBlock>()
    private val sideBlocks = mutableListOf<BridgeBlock>()
    private var timeSinceLastUpdate = 0f

    fun constructBridge() {
        inConstruction = true
        isShown = true
    }

    override fun update(delta: Float) {
        if (!inConstruction) {
            return
        }

        timeSinceLastUpdate += delta
        if (timeSinceLastUpdate > 0.5f) {
            timeSinceLastUpdate -= 0.5f
            addNextBlock()

            if (blocks.size == blockCount) {
                inConstruction = false
            }
        }
    }

    private fun addNextBlock() {
        val currentX = x + blocks.size

        val newBlock = BridgeBlock(currentX, y, tilemap, Vector2i(12, 19))

        if (player.intersects(newBlock)) {
            player.die()
        }

        blocks.add(newBlock)
        tilemap.setCollision(currentX, y, 1)

        if (blocks.size == 1) {
            sideBlocks.add(BridgeBlock(currentX - 1, y, tilemap, Vector2i(3, 10)))
        }

        if (blocks.size == blockCount) {
            sideBlocks.add(BridgeBlock(currentX + 1, y, tilemap, Vector2i(3, 10)))
        }
    }

    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) {
        if (isShown) {
            blocks.forEach { surfaceHolder.draw(bounds, it, paint) }
            sideBlocks.forEach { surfaceHolder.draw(bounds, it, paint) }
        }
    }

}