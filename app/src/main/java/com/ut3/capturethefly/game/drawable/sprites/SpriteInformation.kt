package com.ut3.capturethefly.game.drawable.sprites

import kotlinx.serialization.Serializable

@Serializable
data class SpriteInformation(
    val metadata: SpriteMetadata,
    val actions: Map<String, SpriteAction>
)

@Serializable
data class SpriteMetadata(
    val resource: String,
    val tileWidth: Float,
    val tileHeight: Float,
    val tileCount: Int
)

@Serializable
data class SpriteAction(
    val index: Int,
    val count: Int,
    val time: Float = -1f,
    val loop: Boolean = true
)