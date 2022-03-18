package com.ut3.capturethefly.game.logic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withScale
import com.ut3.capturethefly.R
import com.ut3.capturethefly.game.drawable.Drawable
import com.ut3.capturethefly.game.drawable.ImmutableRect
import com.ut3.capturethefly.game.drawable.hud.Joystick
import com.ut3.capturethefly.game.drawable.sprites.AnimatedSprite
import com.ut3.capturethefly.game.drawable.tiledmap.TiledMap
import com.ut3.capturethefly.game.utils.Vector2f
import kotlin.math.abs

class Fly(
    context: Context,
    x: Float,
    y: Float,
    private val tiledMap: TiledMap,
    private val playerPosition: () -> Vector2f
): Entity, Drawable, AnimatedSprite(context, R.raw.fly, "fly") {
    private val speed = tiledMap.tileSize
    override var rect = ImmutableRect(x, y, x+24f, y+24f)

    private var verticalMovement = Joystick.Movement.None
    private var horizontalMovement = Joystick.Movement.None

    override fun handleInput(inputState: InputState) {
        super<AnimatedSprite>.handleInput(inputState)

        val positionToReach = playerPosition()
        val currentPosition = center
        verticalMovement = when {
            positionToReach.y < currentPosition.y -> Joystick.Movement.Up
            positionToReach.y > currentPosition.y -> Joystick.Movement.Down
            else -> Joystick.Movement.None
        }

        horizontalMovement = when {
            positionToReach.x < currentPosition.x -> Joystick.Movement.Left
            positionToReach.x > currentPosition.x -> Joystick.Movement.Right
            else -> Joystick.Movement.None
        }
    }

    override fun update(delta: Float) {
        super<AnimatedSprite>.update(delta)

        rect.copyOfUnderlyingRect.offset(
            horizontalMovement.delta * speed * delta,
            verticalMovement.delta * speed * delta
        )

        moveX(delta)
        moveY(delta)

        when (horizontalMovement) {
            Joystick.Movement.Left -> setAction("fly", reverse = true)
            else -> setAction("fly", reverse = false)
        }
    }

    private fun moveX(delta: Float) {
        val tmp = rect.copyOfUnderlyingRect.apply {
            offset(
                horizontalMovement.delta * speed * delta,
                0f
            )
        }

        if (!tiledMap.collisionTilesIntersecting(tmp).any { it == TiledMap.COLLISION_BLOCK }) {
            rect = ImmutableRect(tmp)
        }
    }

    private fun moveY(delta: Float) {
        val tmp = rect.copyOfUnderlyingRect.apply {
            offset(
                0f,
                verticalMovement.delta * speed * delta,
            )
        }

        val offsetX = rect.centerX - playerPosition().x
        if (abs(offsetX) < 1f) {
            tmp.offset(-offsetX, 0f)
        }

        if (!tiledMap.collisionTilesIntersecting(tmp).any { it == TiledMap.COLLISION_BLOCK }) {
            rect = ImmutableRect(tmp)
        }
    }

    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) {
        surfaceHolder.withScale(x = if (horizontalMovement == Joystick.Movement.Left) -1f else 1f, pivotX = center.x, pivotY = center.y) {
            super.drawOnCanvas(bounds, surfaceHolder, paint)
        }
    }

}