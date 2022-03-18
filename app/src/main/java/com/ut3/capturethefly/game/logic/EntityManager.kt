package com.ut3.capturethefly.game.logic

import androidx.annotation.CallSuper

/**
 * Utility class to easily handle entities.
 * Base class for each levels.
 */
abstract class EntityManager: Logic {

    private val entities = mutableListOf<Entity>()
    private val pendingEntities = mutableListOf<Entity>()

    @CallSuper
    open fun clean() {
        entities.forEach {
            if (it is EntityManager) {
                it.clean()
            }
        }

        entities.clear()
    }

    @CallSuper
    override fun handleInput(inputState: InputState) {
        entities.forEach { it.handleInput(inputState) }
    }

    /**
     * Hook that must be overridden to perform update within your level.
     *
     * This is what is commonly used to update your entities or your game logic.
     * Note though that entities' onUpdate are automatically called if you created
     * them with the [createEntity] function.
     *
     * This is guarantied to be called before the [postUpdate] hook.
     *
     * @param delta the delta time between now and the last update
     */
    @CallSuper
    override fun update(delta: Float) {
        entities.forEach { it.update(delta) }
        postUpdate(delta)
    }

    /**
     * Hook that must be overridden to perform operations after the logic has
     * been updated within your application.
     *
     * For example, to center a camera on a drawable after this drawable has been
     * updated, so this ensure the execution order.
     *
     * @param delta the delta time between now and the last update
     */
    @CallSuper
    override fun postUpdate(delta: Float) {
        entities.forEach { it.postUpdate(delta) }
        entities.addAll(pendingEntities)
        pendingEntities.clear()
    }

    /**
     * Hook that should be overridden to setup everything that is needed for
     * this level to work.
     */
    abstract fun onLoad()

    /**
     * Hook that will be called during auto-save or when the game is being closed.
     */
    abstract fun onSaveState()

    /**
     * Used to create an Entity, this function also registers the entity
     * to be called with the classic lifecycle hooks that are defined in
     * the [Logic] interface.
     *
     * @param block block that is responsible for the entity creation
     * @return the entity created in the given [block]
     */
    fun <T: Entity> createEntity(block: () -> T): T {
        return block().also { entities.add(it) }
    }

    fun <T: Entity> createEntityOnNextLoop(block: () -> T): T {
        return block().also { pendingEntities.add(it) }
    }

}