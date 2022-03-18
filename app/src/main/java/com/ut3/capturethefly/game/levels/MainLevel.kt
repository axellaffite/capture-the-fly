package com.ut3.capturethefly.game.levels

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
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

    private val timeNeededToStun = 1f
    private var luminosityLevel  = 0f
    private var fliesAlive = 10
    private var fliesAreStunned = false
    private val camera = createTrackingCamera(
        screenPosition = RectF(0f, 0f, gameView.width.toFloat(), gameView.height.toFloat()),
        gamePosition = RectF(0f, 0f, gameView.width.toFloat(), gameView.height.toFloat()),
        track = player::center
    )

    private var timeElapsedWithLowLuminosity = 0f
    private var lastFly = 0f
    private val flies = mutableListOf<Fly>()

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
        Log.d("LUMINOSITY",luminosityLevel.toString())
    }

    fun countLowLuminosityTime(delta:Float) {
        if (luminosityLevel <10) {
            timeElapsedWithLowLuminosity += delta
        } else {
            timeElapsedWithLowLuminosity = 0f
        }
    }

    fun stunFlies() {
        if (timeElapsedWithLowLuminosity >= timeNeededToStun && !fliesAreStunned) {
            for (fly in flies) {
                fly.stun(true)
            }
            fliesAreStunned = true
        }
        if (timeElapsedWithLowLuminosity <= timeNeededToStun && fliesAreStunned) {
            for (fly in flies) {
                fly.stun(false)
            }
            fliesAreStunned = false
        }
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

        countLowLuminosityTime(delta)
        stunFlies()

        val playerRect = player.collisionRect
        for (fly in flies) {
            if(fly.attackRect.intersects(playerRect)){
                fly.attack()
                player.takeDamage()
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