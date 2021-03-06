package com.ut3.capturethefly.game.drawable.hud

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.ut3.capturethefly.game.GameView
import com.ut3.capturethefly.game.drawable.Drawable
import com.ut3.capturethefly.game.drawable.ImmutableRect
import com.ut3.capturethefly.game.drawable.draw
import com.ut3.capturethefly.game.logic.Entity
import com.ut3.capturethefly.game.logic.EntityManager

/**
 * Interface that is shown above all other drawable on screen.
 * This interface allows the user to perform input actions on the game.
 *
 * @param gameView target on which this should be drawn
 */
class HUD(gameView: GameView, currentPower : () -> Float, currentHealth : () -> Float) : Entity, Drawable, EntityManager() {

    override val rect = ImmutableRect(gameView.rect)

    /** Used to display FPS at each frame */
    private var fps = 0f
    val joystick = createEntity { Joystick(gameView.rect,gameView.context ) }
    val controlButtons = createEntity { ControlButtons(gameView) }
    private val chargeBar = createEntity { ChargeBar(gameView.rect,currentPower) }
    private val healthBar = createEntity { HealthBar(gameView.rect,currentHealth) }

    override fun onLoad() = Unit
    override fun onSaveState() = Unit

    override fun update(delta: Float) {
        super<EntityManager>.update(delta)
        fps = (1f / delta)
    }

    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) {
        val buttonPaint = Paint(paint).apply { alpha = 200 }
        surfaceHolder.draw(bounds, joystick, buttonPaint)
        surfaceHolder.draw(bounds, controlButtons, buttonPaint)
        surfaceHolder.draw(bounds, chargeBar, buttonPaint )
        surfaceHolder.draw(bounds, healthBar, buttonPaint)
    }

}

/**
 * Utility function to register a HUD as an automatically-updated entity.
 *
 * @param gameView target on which the [HUD] should be drawn
 * @param config
 * @return
 */
fun EntityManager.createHud(gameView: GameView, currentPower: () -> Float, currentHealth: () -> Float, config: HUD.() -> Unit = {}) =
    createEntity { HUD(gameView,currentPower,currentHealth).apply(config) }