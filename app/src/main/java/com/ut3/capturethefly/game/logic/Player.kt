package com.ut3.capturethefly.game.logic

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaPlayer
import androidx.core.graphics.withScale
import com.ut3.capturethefly.R
import com.ut3.capturethefly.game.GameView
import com.ut3.capturethefly.game.utils.Vector2f
import com.ut3.capturethefly.game.utils.Vector2i
import com.ut3.capturethefly.game.drawable.Drawable
import com.ut3.capturethefly.game.drawable.ImmutableRect
import com.ut3.capturethefly.game.drawable.hud.HUD
import com.ut3.capturethefly.game.drawable.hud.Joystick
import com.ut3.capturethefly.game.drawable.sprites.AnimatedSprite
import com.ut3.capturethefly.game.drawable.tiledmap.TiledMap
import kotlin.math.abs

class Player(
    gameView: GameView,
    private val tilemap: TiledMap,
    private val hud: HUD,
    private val configuration: Player.() -> Unit = {}
): Entity,
    Drawable,
    AnimatedSprite(gameView.context, R.raw.character, "idle_up")
{

    var deathNumber = 0

    companion object {
        const val SPEED = 12f
    }

    enum class ROTATION {
        STRAIGHT,REVERSED
    }

    private val runSound = MediaPlayer.create(gameView.context, R.raw.feet_49)
    private var isRunning = false
    private var movement = Joystick.Movement.None
    private var isJumping = false
    private var isDead = false
    private var reactToEnvironment = true
    private var isUpsideDown = false
    var dx = 0f
    var dy = 0f
    var gravity = ROTATION.STRAIGHT

    private val collisionRect get() = RectF(rect.left + 5f, rect.top, rect.right - 5f, rect.bottom)

    val isTouchingLevel1 get() = tilemap.collisionTilesIntersecting(collisionRect).any { it == TiledMap.LEVEL_1_BLOCK }
    val isTouchingLevel2 get() = tilemap.collisionTilesIntersecting(collisionRect).any { it == TiledMap.LEVEL_2_BLOCK }
    val isTouchingLevel3 get() = tilemap.collisionTilesIntersecting(collisionRect).any { it == TiledMap.LEVEL_3_BLOCK }
    val isTouchingLevel4 get() = tilemap.collisionTilesIntersecting(collisionRect).any { it == TiledMap.LEVEL_4_BLOCK }

    init {
        reset()
        configuration()
    }

    private fun reset() {
        configuration()

        isDead = false
        reactToEnvironment = true
        movement = Joystick.Movement.None
        isJumping = false
        dx = 0f
        dy = 0f
    }

    fun flipUpsideDown(flipped: Boolean) {
        isUpsideDown = flipped
    }

    override fun handleInput(inputState: InputState) {
        if (reactToEnvironment) {
            val event = inputState.touchEvent
            if (event == null) {
                movement = Joystick.Movement.None
                isJumping = false
                return
            }

            movement = hud.joystick.direction
        }
    }

    override fun update(delta: Float) {
        val lastSprite = actionIndexOffset
        super<AnimatedSprite>.update(delta)
        if (shouldBeDead()) {
            die()
            if (isAnimationFinished) {
                reset()
            }
            return
        }

        if (reactToEnvironment) {
            val isTouchingGround = isTouchingGround()
            applyGravity(isTouchingGround, delta)
            moveIfRequired(isTouchingGround, delta)
            jump { hud.controlButtons.isAPressed && isTouchingGround && abs(dy) < 1f }


            dx = dx.coerceIn(-8f, 8f)
            dy = dy.coerceIn(-16f, 16f)

            updatePosition(isTouchingGround, delta)

            if (lastSprite != actionIndexOffset && isRunning) {
                runSound.start()
            }
        }
    }

    private fun isTouchingGround(): Boolean {
        val intersectionRect = if (isUpsideDown) {
            RectF(collisionRect.left, collisionRect.top-1, collisionRect.right, collisionRect.top)
        } else {
            RectF(collisionRect.left, collisionRect.bottom, collisionRect.right, collisionRect.bottom + 1f)
        }

        return tilemap.collisionTilesIntersecting(intersectionRect).any { tileValue ->
            tileValue == TiledMap.COLLISION_BLOCK
        }
    }

    private fun shouldBeDead(): Boolean {
        return isDead || tilemap.collisionTilesIntersecting(collisionRect)
            .any { it == TiledMap.DEATH_BLOCK }
    }

    fun die() {
        if (!isDead) {
            deathNumber++
            reactToEnvironment = false
            isDead = true
            setAction("hit", isBitmapReversed)
        }
    }

    private fun moveIfRequired(touchingGround: Boolean, delta: Float) {
        if (movement == Joystick.Movement.None) {
            dx /= 2f
            if (dx < 0.5f) {
                dx = 0f
            }
        }

        var dm = when (movement) {
            Joystick.Movement.Right -> 1f
            Joystick.Movement.Left -> -1f
            else -> 0f
        }

        if (!touchingGround) {
            dm /= 2f
        }

        dx += dm * 64f * delta

        when (movement) {
            Joystick.Movement.Right -> run()
            Joystick.Movement.Left -> run(reverse = true)
            Joystick.Movement.None -> {
                setAction("idle", isBitmapReversed)
                isRunning = false
            }
        }
    }

    private fun run(reverse: Boolean = false) {
        isRunning = true
        if (isTouchingGround()) {
            setAction("run", reverse)
        }else {
            setAction("jump",reverse)
        }
    }

    private fun jump(predicate: () -> Boolean = { true }) {
        val operand = if (isUpsideDown) 1 else -1
        if (predicate()) {
            dy += 6f * operand
        } else {
            isJumping = false
        }
    }

    private fun applyGravity(isTouchingGround: Boolean, delta: Float) {
        if (gravity == ROTATION.STRAIGHT) {
            if (!isTouchingGround) {
                dy += (12.2f * delta)
            }
        } else {
            dy -= (12.2f * delta)
        }
    }

    fun changeRotation(rotation : ROTATION) {
        gravity = rotation
    }

    fun updatePosition(isTouchingGround: Boolean, delta: Float) {
        let {
            val tmp = collisionRect.apply { offset(0f, dy * delta * SPEED) }
            if (tilemap.collisionTilesIntersecting(tmp).any { it == 1 }) {
                dy = 0f
            } else {
                rect = ImmutableRect(rect.copyOfUnderlyingRect.apply { offset(0f, dy * delta * SPEED) })
            }
        }

        let {
            val tmp = collisionRect.apply { offset(dx * delta * SPEED, 0f) }
            if (!tilemap.collisionTilesIntersecting(tmp).any { it == 1 }) {
                rect = ImmutableRect(rect.copyOfUnderlyingRect.apply { offset(dx * delta * SPEED, 0f) })
            }
        }
    }

    fun setPosition(position: Vector2i, tileSize: Float) {
        val bottom = (position.y + 1) * tileSize
        val top = bottom - rect.height
        val center = position.x * tileSize + tileSize / 2f
        val left = center - tileSize / 2f
        val right = left + rect.width

        rect = ImmutableRect(left, top, right, bottom)
    }

    fun center() = Vector2f(rect.centerX, rect.centerY)

    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) {
        if (isUpsideDown) {
            val (pivotX, pivotY) = center()
            surfaceHolder.withScale(y = -1f, pivotX = pivotX, pivotY = pivotY) {
                super.drawOnCanvas(bounds, surfaceHolder, paint)
            }
        } else {
            super.drawOnCanvas(bounds, surfaceHolder, paint)
        }
    }
}