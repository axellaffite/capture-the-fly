package com.ut3.capturethefly.game.levels.introduction

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withClip
import androidx.core.graphics.withScale
import com.ut3.capturethefly.R
import com.ut3.capturethefly.game.GameView
import com.ut3.capturethefly.game.drawable.cameras.createTrackingCamera
import com.ut3.capturethefly.game.levels.Level
import com.ut3.capturethefly.game.logic.InputState


class IntroductionLevel(
    gameView: GameView,
    goToNextLevel: (String) -> Unit
) : Level(
    gameView = gameView,
    soundRes = R.raw.ambiance_sound,
    tilemapResource = TILE_MAP_RESOURCE,
    goToNextLevel = goToNextLevel
) {

    companion object {
        const val TILE_MAP_RESOURCE = R.raw.testmap
        const val NAME = "introduction"
    }

    private val luminosityReference = preferences.luminosityReference
    private val threshold = luminosityReference / 2
    private val score = 25

    private var luminosityLevel = 0f
    private var nightAlpha = 0
    private var nextLevelLoaded = false

    private val bridge = createEntity {
        Bridge(x = 27, y = 25, blockCount = 10, tilemap = tilemap, player = player)
    }

    private val lever = createEntity {
        Lever(gameView, hud, player, bridge, ::nightAlpha) {
            moveTo(352f, 384f)
        }
    }

    private val camera = createTrackingCamera(
        screenPosition = RectF(0f, 0f, gameView.width.toFloat(), gameView.height.toFloat()),
        gamePosition = RectF(0f, 0f, gameView.width.toFloat(), gameView.height.toFloat()),
        track = player::center
    )

    override fun onSaveState() {
        TODO("save state of the level")
    }

    override fun handleInput(inputState: InputState) {
        super.handleInput(inputState)
        luminosityLevel = inputState.luminosity
    }

    override fun update(delta: Float) {
        super.update(delta)

        val rawAlpha = (threshold - (luminosityLevel * 4f / 3f)).coerceAtLeast(0f)
        nightAlpha = (rawAlpha * (255f / threshold)).toInt()

        if (player.isTouchingLevel2) {
            val s = score - 5 * player.deathNumber
            preferences.scoreLevelOne = if (s >= 0) s else 0
            nextLevelLoaded = true
            goToNextLevel(NAME)
        }
    }

    override fun render() {
        gameView.draw { canvas, paint ->
            val scaleFactor = ((gameView.width / tilemap.tileSize) / 18f)
            val (pivotX, pivotY) = gameView.width / 2f to gameView.height / 2f


            canvas.drawColor(Color.parseColor("#34202b"))

            canvas.withScale(x = scaleFactor, y = scaleFactor, pivotX = pivotX, pivotY = pivotY) {

                withCamera(camera) { canvas, paint ->
                    canvas.withClip(tilemap.rect.copyOfUnderlyingRect) {
                        canvas.drawColor(Color.BLUE)
                    }

                    canvas.draw(tilemap, paint)
                    canvas.draw(player, paint)
                    canvas.draw(bridge, paint)

                    canvas.drawRect(
                        0f,
                        0f,
                        canvas.width.toFloat(),
                        canvas.height.toFloat(),
                        Paint().apply {
                            color = 0
                            alpha = nightAlpha
                        }
                    )

                    canvas.draw(lever, paint)
                }
            }

            hud.draw(gameView.rect, canvas, paint)
        }
    }

}