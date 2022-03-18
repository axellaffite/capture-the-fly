package com.ut3.capturethefly.game.logic

/**
 * Interface that is used by the game logic to
 * update the entities at every state of the game.
 *
 * Functions are defined as Unit so classes that implements
 * this class don't have to override functions if they don't need to.
 */
interface Logic {
    fun handleInput(inputState: InputState) = Unit
    fun update(delta: Float) = Unit
    fun postUpdate(delta: Float) = Unit
    fun render() = Unit
}