package com.ut3.capturethefly.game.levels.introduction

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.ut3.capturethefly.R
import com.ut3.capturethefly.game.GameView
import com.ut3.capturethefly.game.drawable.hud.HUD
import com.ut3.capturethefly.game.drawable.sprites.AnimatedSprite
import com.ut3.capturethefly.game.logic.InputState
import com.ut3.capturethefly.game.logic.Player

class Lever(
    gameView: GameView,
    private val hud: HUD,
    private val player: Player,
    private val bridge: Bridge,
    private val alpha: () -> Int,
    conf: Lever.() -> Unit
) : AnimatedSprite(gameView.context, R.raw.lever, "off")
{

    init { conf() }

    private var luminosityLevel = 0f

    override fun handleInput(inputState: InputState) {
        luminosityLevel = inputState.luminosity
        if (hud.controlButtons.isBPressed && rect.intersects(player.rect)) {
            setAction("on")
            bridge.constructBridge()
        }
    }

    override fun update(delta: Float) {
        super.update(delta)

        hud.controlButtons.isBVisible = alpha() > 200
            && !bridge.isShown
            && rect.intersects(player.rect)
    }

    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) {
        val selfPaint = Paint(paint)
        selfPaint.alpha = alpha()
        super.drawOnCanvas(bounds, surfaceHolder, selfPaint)
    }

}