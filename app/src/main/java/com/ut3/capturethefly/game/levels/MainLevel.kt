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
    private val camera = createTrackingCamera(
        screenPosition = RectF(0f, 0f, gameView.width.toFloat(), gameView.height.toFloat()),
        gamePosition = RectF(0f, 0f, gameView.width.toFloat(), gameView.height.toFloat()),
        track = player::center
    )

    private var lastFly = 0f
    private val flies = mutableListOf<Fly>()

    override fun onSaveState() {
        TODO("save state of the level")
    }

    override fun handleInput(inputState: InputState) {
        super.handleInput(inputState)
        if(hud.controlButtons.isAPressed){
            player.attack()
        }
        luminosityLevel = inputState.luminosity
    }

    override fun update(delta: Float) {
        power += delta
        super.update(delta)

        println(lastFly)
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
                        flies
                    )
                }
            )

            lastFly = 0f
            println("added")
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