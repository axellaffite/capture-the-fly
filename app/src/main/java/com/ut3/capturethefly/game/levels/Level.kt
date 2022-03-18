package com.ut3.capturethefly.game.levels

import android.media.MediaPlayer
import androidx.annotation.CallSuper
import androidx.annotation.RawRes
import com.ut3.capturethefly.game.GameView
import com.ut3.capturethefly.game.drawable.hud.createHud
import com.ut3.capturethefly.game.drawable.tiledmap.loadTiledMap
import com.ut3.capturethefly.game.logic.EntityManager
import com.ut3.capturethefly.game.logic.Player
import com.ut3.capturethefly.game.utils.Preferences

abstract class Level(
    protected val gameView: GameView,
    @RawRes private val soundRes: Int,
    @RawRes tilemapResource: Int
): EntityManager() {

    private val health: () -> Float = { player.health }
    private val power: () -> Float = { player.power }
    protected val tilemap = gameView.context.loadTiledMap(tilemapResource)
    protected val hud = createHud(gameView, power, health) { controlButtons.isBVisible = false }
    protected val player = createEntity { Player(gameView, tilemap, hud) { setPosition(tilemap.initialPlayerPosition, tilemap.tileSize) } }
    protected val preferences = Preferences(gameView.context)

    private lateinit var sound: MediaPlayer

    @CallSuper
    override fun onLoad() {
        sound = MediaPlayer.create(gameView.context, soundRes).apply {
            isLooping = true
            start()
        }
    }
    override fun clean() {
        super.clean()
        runCatching {
            sound.stop()
            sound.release()
        }
    }

}