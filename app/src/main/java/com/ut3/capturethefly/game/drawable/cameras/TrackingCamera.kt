package com.ut3.capturethefly.game.drawable.cameras

import android.graphics.RectF
import com.ut3.capturethefly.game.logic.Entity
import com.ut3.capturethefly.game.logic.EntityManager
import com.ut3.capturethefly.game.utils.Vector2f

/**
 * Automated tracking camera that will be centered on a given
 * point on each [postUpdate] call.
 * As this camera is an [Entity], you can easily add it to the automatically updated
 * level entities through the [EntityManager.createTrackingCamera] function.
 *
 * @property toFollow callback that will be used to get the position that this camera should be
 * centered on at every call of the [postUpdate] function.
 * @param screenPosition position of the camera on screen
 * @param gamePosition position of the camera in game
 */
class TrackingCamera(
    screenPosition: RectF,
    gamePosition: RectF,
    private val toFollow: () -> Vector2f
): Camera(screenPosition, gamePosition), Entity {

    override fun postUpdate(delta: Float) {
        centerOn(toFollow())
    }

}

fun EntityManager.createTrackingCamera(screenPosition: RectF, gamePosition: RectF, track: () -> Vector2f) =
    createEntity { TrackingCamera(screenPosition, gamePosition, track) }