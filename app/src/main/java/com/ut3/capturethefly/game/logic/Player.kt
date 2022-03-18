package com.ut3.capturethefly.game.logic

import android.graphics.RectF
import android.media.MediaPlayer
import com.ut3.capturethefly.R
import com.ut3.capturethefly.game.GameView
import com.ut3.capturethefly.game.drawable.Drawable
import com.ut3.capturethefly.game.drawable.ImmutableRect
import com.ut3.capturethefly.game.drawable.hud.HUD
import com.ut3.capturethefly.game.drawable.hud.Joystick
import com.ut3.capturethefly.game.drawable.sprites.AnimatedSprite
import com.ut3.capturethefly.game.drawable.tiledmap.TiledMap
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
        const val SPEED = 6f
    }

    private val runSound = MediaPlayer.create(gameView.context, R.raw.feet_49)
    private var isRunning = false
    private var verticalMovement = Joystick.Movement.None
    private var horizontalMovement = Joystick.Movement.None
    private var lastDirection = Joystick.Movement.Down
    private var isAttacking = false
    private var isDead = false
    private var reactToEnvironment = true
    private var isUpsideDown = false
    private var invincible = 0f

    var power = 0f
    var health = 1f; private set (value) { field = value.coerceAtMost(1f) }


    var dx = 0f
    var dy = 0f

    val collisionRect get() = RectF(rect.left + 9f, rect.top+9f, rect.right - 9f, rect.bottom-9f)
    val attackRect: ImmutableRect get() {
        return when {
            !isAttacking -> ImmutableRect()
            lastDirection == Joystick.Movement.Left -> {
                ImmutableRect(rect.left , rect.top, rect.centerX, rect.bottom)
            }
            lastDirection == Joystick.Movement.Right -> {
                ImmutableRect(rect.centerX, rect.top, rect.right, rect.bottom)
            }
            lastDirection == Joystick.Movement.Up -> {
                ImmutableRect(rect.left, rect.top, rect.right, rect.centerY)
            }
            lastDirection == Joystick.Movement.Down -> {
                ImmutableRect(rect.left , rect.centerY, rect.right , rect.bottom)
            }
            else -> ImmutableRect()
        }
    }

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
        invincible -= delta
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
            isAttacking = isAttacking && !isAnimationFinished
            moveIfRequired(delta)

            dx = dx.coerceIn(-16f, 16f)
            dy = dy.coerceIn(-16f, 16f)

            updatePosition(delta)

            if (lastSprite != actionIndexOffset && isRunning) {
                runSound.start()
            }
        }
    }

    private fun shouldBeDead(): Boolean {
        return isDead || tilemap.collisionTilesIntersecting(collisionRect)
            .any { it == TiledMap.DEATH_BLOCK }
    }

    fun die() {
        if (!isDead) {
            setAction("die")
            deathNumber++
            reactToEnvironment = false
            isDead = true
        }
    }

    private fun moveIfRequired(delta: Float) {
        dx += 64f * delta * horizontalMovement.delta
        dy += 64f * delta * verticalMovement.delta

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

        if(isAttacking){
            setAction(verticalMovement.getAction("attack",lastDirection))
        }else if( verticalMovement == Joystick.Movement.None && horizontalMovement == Joystick.Movement.None){
            setAction(verticalMovement.getAction("idle",lastDirection))
        }else{
            setAction(verticalMovement.getAction("walk",lastDirection))
        }

    }


    fun updatePosition(delta: Float) {
        let {
            val tmp = collisionRect.apply { offset(0f, dy * delta * SPEED) }
            if (!tilemap.collisionTilesIntersecting(tmp).any { it == 1 }) {
                rect = ImmutableRect(rect.copyOfUnderlyingRect.apply {
                    offset(
                        0f,
                        dy * delta * SPEED
                    )
                })
            }
        }

        let {
            val tmp = collisionRect.apply { offset(dx * delta * SPEED, 0f) }
            if (!tilemap.collisionTilesIntersecting(tmp).any { it == 1 }) {
                rect = ImmutableRect(rect.copyOfUnderlyingRect.apply {
                    offset(
                        dx * delta * SPEED,
                        0f
                    )
                })
            }
        }
    }

    fun attack() {
        isAttacking = true
    }

    fun setPosition(position: Vector2i, tileSize: Float) {
        val bottom = (position.y + 1) * tileSize
        val top = bottom - rect.height
        val center = position.x * tileSize + tileSize / 2f
        val left = center - tileSize / 2f
        val right = left + rect.width

        rect = ImmutableRect(left, top, right, bottom)
    }

    fun takeDamage() : Boolean {
        if (invincible <= 0f) {
            invincible = 1f
            health -= 0.2f
            if(health <= 0f){
                die()
                return true
            }
        }
        return false
    }

    fun gatherPower(delta: Float) {
        power += delta * 0.01f
    }

    fun gatherHealth(delta: Float) {
        health += delta * 0.03f
    }

    fun resetHealth() {
        health = 1f
    }
}