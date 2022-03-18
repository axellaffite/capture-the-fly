package com.ut3.capturethefly.game.levels

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withClip
import androidx.core.graphics.withScale
import com.ut3.capturethefly.R
import com.ut3.capturethefly.game.GameView
import com.ut3.capturethefly.game.drawable.cameras.createTrackingCamera
import com.ut3.capturethefly.game.logic.Fly
import com.ut3.capturethefly.game.logic.InputState
import com.ut3.capturethefly.game.logic.isShaking

class MainLevel(
    gameView : GameView
) : Level(
    gameView = gameView,
    soundRes = R.raw.ambiance_sound,
    tilemapResource = TILE_MAP_RESOURCE
) {

    companion object {
        const val TILE_MAP_RESOURCE = R.raw.level
        const val NAME = "level"
    }

    init {
        player.move(tilemap.rect.width/2.toFloat(),tilemap.rect.height/2.toFloat())
    }

    private var luminosityLevel  = 0f
    private var fliesAlive = 10
    private val camera = createTrackingCamera(
        screenPosition = RectF(0f, 0f, gameView.width.toFloat(), gameView.height.toFloat()),
        gamePosition = RectF(0f, 0f, gameView.width.toFloat(), gameView.height.toFloat()),
        track = player::center
    )

    private var lastFly = 0f
    private val flies = mutableListOf<Fly>()
    private var isShaking = false

    override fun onSaveState() {
        TODO("save state of the level")
    }

    override fun handleInput(inputState: InputState) {
        super.handleInput(inputState)
        if(hud.controlButtons.isAPressed){
            player.attack()
            flies.forEach {
                if (player.attackRect.intersects(it.rect)){
                    it.die()
                }
            }
        }
        luminosityLevel = inputState.luminosity
        isShaking = inputState.isShaking(preferences.accelerationReference)
    }

    override fun update(delta: Float) {
        player.gatherPower(delta)
        super.update(delta)

        lastFly += delta
        if (lastFly >= 0.5 && flies.size < 10) {
            flies.add(
                createEntity {
                    Fly(
                        context = gameView.context,
                        x = player.rect.left,
                        y = player.rect.top + 50f,
                        tiledMap = tilemap,
                        player::center,
                        flies,
                        onDie = {
                            fliesAlive--;
                        }
                    )
                }
            )

            lastFly = 0f
        }

        val playerRect = player.collisionRect
        for (fly in flies) {
            if(fly.attackRect.intersects(playerRect)){
                fly.attack()
                player.takeDamage()
            }
        }
        if (isShaking && player.power >= 1f ){
            player.power = 0f
            flies.forEach {
                if (it.rect.intersects(camera.gamePosition)) {
                    it.die()
                }
            }
        }
    }

    override fun render() {
        gameView.draw { canvas, paint ->
            val scaleFactor = ((gameView.width / tilemap.tileSize) / 12f)
            val (pivotX, pivotY) = gameView.width / 2f to gameView.height / 2f


            canvas.drawColor(Color.parseColor("#34202b"))

            canvas.withScale(x = scaleFactor, y = scaleFactor, pivotX = pivotX, pivotY = pivotY) {

                withCamera(camera) { canvas, paint ->
                    canvas.withClip(tilemap.rect.copyOfUnderlyingRect) {
                        canvas.drawColor(Color.BLUE)
                    }

                    canvas.draw(tilemap, paint)
                    canvas.draw(player, paint)
                    flies.forEach {
                        canvas.draw(it, paint)
                    }


                    canvas.drawRect(
                        0f,
                        0f,
                        canvas.width.toFloat(),
                        canvas.height.toFloat(),
                        Paint().apply {
                            color = 0
                        }
                    )
                }
            }

            hud.draw(gameView.rect, canvas, paint)
        }
    }
}