package com.ut3.capturethefly.game.logic

import com.ut3.capturethefly.game.GameView
import com.ut3.capturethefly.game.levels.MainLevel
import com.ut3.capturethefly.game.utils.SensorsListener
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.schedule
import kotlin.concurrent.thread


class GameLogic(gameView: GameView) : Logic {
    private var state = MutableInputState()
    private val sensorsListener = SensorsListener(gameView, state)

    private var previousUpdate = 0L


    private var isAlive = AtomicBoolean(false)
    private var gameThread = generateThread()

    private val timer = Timer()

    private val level = MainLevel(gameView)

    private fun generateThread() = thread(start = false) {
        gameLoop()
    }

    fun start() {
        previousUpdate = System.currentTimeMillis()
        level.onLoad()

        sensorsListener.startListeners()
        isAlive.set(true)

        assert(!gameThread.isAlive)
        gameThread = generateThread()
        gameThread.start()
    }

    fun stop() {
        sensorsListener.stopListeners()
        isAlive.set(false)

        gameThread.join()
        level.clean()
        assert(!gameThread.isAlive)
    }

    private fun gameLoop() {
        if (isAlive.get()) {
            val currentTime = System.currentTimeMillis()
            val deltaMs = (currentTime - previousUpdate)
            val deltaS = deltaMs / 1000f
            previousUpdate = currentTime

            level.handleInput(state)
            level.update(deltaS)
            level.postUpdate(deltaS)
            level.render()

            timer.schedule(0) {
                gameLoop()
            }
        }
    }
}