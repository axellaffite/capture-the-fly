package com.ut3.capturethefly.game.logic

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaPlayer
import androidx.core.graphics.withScale
import com.ut3.capturethefly.R
import com.ut3.capturethefly.game.GameView
import com.ut3.capturethefly.game.drawable.Drawable
import com.ut3.capturethefly.game.drawable.ImmutableRect
import com.ut3.capturethefly.game.drawable.hud.HUD
import com.ut3.capturethefly.game.drawable.hud.Joystick
import com.ut3.capturethefly.game.drawable.sprites.AnimatedSprite
import com.ut3.capturethefly.game.drawable.tiledmap.TiledMap
import com.ut3.capturethefly.game.utils.Vector2f
import com.ut3.capturethefly.game.utils.Vector2i

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

    private val runSound = MediaPlayer.create(gameView.context, R.raw.feet_49)
    private var isRunning = false
    private var verticalMovement = Joystick.Movement.None
    private var horizontalMovement = Joystick.Movement.None
    private var lastDirection = Joystick.Movement.Down
    private var isDead = false
    private var reactToEnvironment = true
    private var isUpsideDown = false
    var dx = 0f
    var dy = 0f

    private val collisionRect get() = RectF(rect.left + 5f, rect.top, rect.right - 5f, rect.bottom)

    init {
        reset()
        configuration()
    }

    private fun reset() {
        configuration()

        isDead = false
        reactToEnvironment = true
        verticalMovement = Joystick.Movement.None
        horizontalMovement = Joystick.Movement.None
        dx = 0f
        dy = 0f
    }

    override fun handleInput(inputState: InputState) {
        if (reactToEnvironment) {
            val event = inputState.touchEvent
            if (event == null) {
                verticalMovement = Joystick.Movement.None
                horizontalMovement = Joystick.Movement.None
                return
            }

            verticalMovement = hud.joystick.verticalDirection
            horizontalMovement = hud.joystick.horizontalDirection
            if(verticalMovement != Joystick.Movement.None){
                lastDirection = verticalMovement
            }
            if(horizontalMovement != Joystick.Movement.None){
                lastDirection = horizontalMovement
            }
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
            moveIfRequired(delta)

            dx = dx.coerceIn(-8f, 8f)
            dy = dy.coerceIn(-16f, 16f)

            updatePosition(delta)

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

    private fun moveIfRequired(delta: Float) {
        dx += 64f * delta * horizontalMovement.delta
        dy += 64f * delta * verticalMovement.delta

        if (horizontalMovement == Joystick.Movement.None) {
            dy /= 2
        }

        if (horizontalMovement == Joystick.Movement.None) {
            dx /= 2f
            if (dx < 0.5f) {
                dx = 0f
            }
        }

        if (verticalMovement == Joystick.Movement.None) {
            dy /= 2f
            if (dy < 0.5f) {
                dy = 0f
            }
        }

        // TODO play correct animation here
        setAction(verticalMovement.getAction("idle",lastDirection))
        /*when (verticalMovement) {
            Joystick.Movement.Right -> setAction("walk_right")
            Joystick.Movement.Left -> setAction("walk_left")
            Joystick.Movement.None -> {
                when (lastDirection) {
                    Joystick.Movement.Right -> setAction("idle_right")
                    Joystick.Movement.Left -> setAction("idle_left")
                    Joystick.Movement.Up -> setAction("idle_right")
                    Joystick.Movement.Bottom -> setAction("idle_left")
                }
                setAction("idle_up")
            }
        }
        when (horizontalMovement) {
            Joystick.Movement.Up -> setAction("walk_up")
            Joystick.Movement.Down -> setAction("walk_bottom")
            Joystick.Movement.None -> {
                setAction("idle_up")
            }/
        }*/
//        when (movement) {
//            Joystick.Movement.Right -> run()
//            Joystick.Movement.Left -> run(reverse = true)
//            Joystick.Movement.None -> {
//                setAction("idle", isBitmapReversed)
//                isRunning = false
//            }
//        }
    }

    private fun run(reverse: Boolean = false) {
        isRunning = true
        if (isTouchingGround()) {
            setAction("run", reverse)
        }else {
            setAction("jump",reverse)
        }
    }

    fun updatePosition(delta: Float) {
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
}