package com.ut3.capturethefly.game.levels

import android.graphics.Color
import android.graphics.RectF
import android.media.MediaPlayer
import androidx.core.graphics.withSave
import com.ut3.capturethefly.R
import com.ut3.capturethefly.game.GameView
import com.ut3.capturethefly.game.logic.EntityManager
import com.ut3.capturethefly.game.logic.InputState
import com.ut3.capturethefly.game.logic.Player
import com.ut3.capturethefly.game.utils.Vector2f
import com.ut3.capturethefly.game.drawable.TextPopUp
import com.ut3.capturethefly.game.drawable.cameras.createTrackingCamera
import com.ut3.capturethefly.game.drawable.hud.createHud
import com.ut3.capturethefly.game.drawable.tiledmap.loadTiledMap

class HomeLevel(
    private val gameView: GameView,
    private val launchNewActivity: (Int) -> Unit
) : EntityManager() {

    companion object {
        const val TILE_MAP_RESOURCE = R.raw.home
        const val NAME = "home"
    }

    private lateinit var sound: MediaPlayer
    private var popup : TextPopUp? = null
    private var levelTouched = -1
    private var quitHome = false
    private val tilemap = gameView.context.loadTiledMap(TILE_MAP_RESOURCE)
    private val hud = createHud(gameView, {1f},{1f}) { controlButtons.isBVisible = false }
    private val player = createEntity { Player(gameView, tilemap, hud) { setPosition(tilemap.initialPlayerPosition, tilemap.tileSize) } }
    private var levelLaunched = false

    private val camera = createTrackingCamera(
        screenPosition = RectF(0f, 0f, gameView.width.toFloat(), gameView.height.toFloat()),
        gamePosition = RectF(0f, 0f, gameView.width.toFloat(), gameView.height.toFloat()),
        track = player::center
    )

    override fun onLoad() {
        sound = MediaPlayer.create(gameView.context, R.raw.ambiance_sound).apply {
            isLooping = true
            start()
        }
    }

    override fun clean() {
        super.clean()
        runCatching{
            sound.stop()
            sound.release()
        }

    }

    override fun onSaveState() {
        TODO("save state of the level")
    }

    override fun handleInput(inputState: InputState) {
        super.handleInput(inputState)

        if (!levelLaunched && levelTouched != -1 && hud.controlButtons.isBPressed) {
            levelLaunched = true
            launchNewActivity(levelTouched)
        }
    }

    override fun update(delta: Float) {
        super.update(delta)
        levelTouched = when {
            else -> -1
        }
        popup = levelTouched.takeIf { it != -1 }?.let { TextPopUp("Play level $it", Vector2f(player.rect.left, player.rect.top)) }
        hud.controlButtons.isBVisible = popup != null
    }

    override fun render() {
        gameView.draw { canvas, paint ->
            canvas.withSave {
                val scaleFactor = ((gameView.width / tilemap.tileSize) / 17f)
                canvas.scale(scaleFactor, scaleFactor, gameView.width / 2f, gameView.height / 2f)
                canvas.drawColor(Color.BLUE)

                withCamera(camera) { canvas, paint ->
                    canvas.draw(tilemap, paint)
                    paint.color = Color.RED
                    canvas.draw(player, paint)
                    popup?.let {
                        canvas.draw(it, paint)
                    }
                }
            }


            hud.draw(gameView.rect, canvas, paint)
        }
    }
}