package com.ut3.capturethefly.game.logic

import android.content.Context
import android.graphics.Canvas
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
    private val playerPosition: () -> Vector2f,
    private val otherFlies: List<Fly>,
    private val onDie: () -> Unit
): Entity, Drawable, AnimatedSprite(context, R.raw.fly, "fly") {

    val maxSpeed = tiledMap.tileSize
    var currentSpeed = maxSpeed
    override var rect = ImmutableRect(x, y, x+24f, y+24f)
    private var isDead = false
    private var isAttacking = false
    private var isStunned = false
    private var lastDirection = Joystick.Movement.Down

    private var verticalMovement = Joystick.Movement.None
    private var horizontalMovement = Joystick.Movement.None

    val attackRect: ImmutableRect get() {
        return when (lastDirection) {
            Joystick.Movement.Left -> {
                ImmutableRect(rect.left , rect.top, rect.centerX, rect.bottom)
            }
            Joystick.Movement.Right -> {
                ImmutableRect(rect.centerX, rect.top, rect.right, rect.bottom)
            }
            Joystick.Movement.Up -> {
                ImmutableRect(rect.left, rect.top, rect.right, rect.centerY)
            }
            Joystick.Movement.Down -> {
                ImmutableRect(rect.left , rect.centerY, rect.right , rect.bottom)
            }
            else -> ImmutableRect()
        }
    }

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

        if(verticalMovement != Joystick.Movement.None){
            lastDirection = verticalMovement
        }
        if(horizontalMovement != Joystick.Movement.None){
            lastDirection = horizontalMovement
        }
    }

    override fun update(delta: Float) {
        super<AnimatedSprite>.update(delta)

        rect.copyOfUnderlyingRect.offset(
            horizontalMovement.delta * currentSpeed * delta,
            verticalMovement.delta * currentSpeed * delta
        )

        moveX(delta)
        moveY(delta)

        if(!isDead) {
            isAttacking = isAttacking && !isAnimationFinished
            if (!isStunned) {
                when (horizontalMovement) {
                    Joystick.Movement.Left -> if (isAttacking) {
                        setAction("attack", reverse = true)
                    } else {
                        setAction("fly", reverse = true)
                    }
                    else -> if (isAttacking) {
                        setAction("attack", reverse = true)
                    } else {
                        setAction("fly", reverse = true)
                    }
                }
            }
        }

    }

    private fun moveX(delta: Float) {
        val tmp = rect.copyOfUnderlyingRect.apply {
            offset(
                horizontalMovement.delta * currentSpeed * delta,
                0f
            )
        }

        if (!otherFlies.any { it !== this && it.rect.intersects(tmp) }) {
            rect = ImmutableRect(tmp)
        }
    }

    private fun moveY(delta: Float) {
        val tmp = rect.copyOfUnderlyingRect.apply {
            offset(
                0f,
                verticalMovement.delta * currentSpeed * delta,
            )
        }

        val offsetX = rect.centerX - playerPosition().x
        if (abs(offsetX) < 1f) {
            tmp.offset(-offsetX, 0f)
        }

        if (!otherFlies.any { it !== this && it.rect.intersects(tmp) }) {
            rect = ImmutableRect(tmp)
        }
    }

    fun die() {
        if (!isDead) {
            setAction("die")
            println("Im dying")
            isDead = true
            onDie()

        }
    }

    fun stun(isStunned : Boolean) {
        if (!isDead) {
            if (isStunned) {
                currentSpeed = 0f
                this.isStunned = true
                setAction("stun")
            } else {
                currentSpeed = maxSpeed
                this.isStunned = false
            }
        }
    }

    fun attack(){
        setAction("attack")
        isAttacking = true
    }

    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) {
        if(!isDead || !isAnimationFinished){
            surfaceHolder.withScale(x = if (horizontalMovement == Joystick.Movement.Left) -1f else 1f, pivotX = center.x, pivotY = center.y) {
                super.drawOnCanvas(bounds, surfaceHolder, paint)
            }
        }
    }

}