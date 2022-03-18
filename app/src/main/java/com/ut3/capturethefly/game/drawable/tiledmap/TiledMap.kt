package com.ut3.capturethefly.game.drawable.tiledmap

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import androidx.annotation.RawRes
import androidx.core.graphics.times
import com.charleskorn.kaml.Yaml
import com.ut3.capturethefly.game.drawable.Drawable
import com.ut3.capturethefly.game.drawable.ImmutableRect
import com.ut3.capturethefly.game.utils.Vector2i
import com.ut3.capturethefly.game.utils.times
import com.ut3.capturethefly.game.utils.toVector2f
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.lang.Integer.min
import kotlin.math.ceil

class TiledMap(
    private val data: TiledMapData,
    private val context: Context
) : Drawable {

    companion object {
        const val DEATH_BLOCK = 0
        const val COLLISION_BLOCK = 1
        const val SPAWN_BLOCK = 2
        const val LEVEL_1_BLOCK = 3
        const val LEVEL_2_BLOCK = 4
        const val LEVEL_3_BLOCK = 5
        const val LEVEL_4_BLOCK = 6
    }

    private data class Tile(
        val x: Int,
        val y: Int,
        val tx: Int,
        val ty: Int
    )

    val tileSize = data.tileSize
    val width = data.width
    val height = data.height

    private val availableTilesets = mutableMapOf<String, Tileset>()
    private val tileset = getOrLoadTileset(data.tileset)

    val bitmap get() = tileset.bitmap

    private val layers = let {

        val res = data.layers.mapValues { (_, values) ->
            val currentTileset = getOrLoadTileset(values.tileset ?: data.tileset)

            currentTileset to values.data.mapIndexed { i, v ->
                val interpretedValue = v.split(".").map(String::toInt)
                val tx = interpretedValue[0]
                val ty = interpretedValue.getOrElse(1) { if (tx == -1) -1 else 0 }
                Tile(x = i % data.width, y = i / data.width, tx = tx, ty = ty)
            }.groupBy { it.y / data.chunkSize }
                .mapValues { (_, v) -> v.groupBy { it.x / data.chunkSize } }
        }

        res.mapValues { (_, tilesetAndLayer) ->
            val (currentTileset, layer) = tilesetAndLayer
            layer.flatMap { (y, lines) ->
                lines.map { (x, chunk) ->
                    // Chunk bounds in pixels
                    val top = y * data.chunkSize * data.tileSize
                    val left = x * data.chunkSize * data.tileSize
                    val bottom = top + data.chunkSize * data.tileSize
                    val right = left + data.chunkSize * data.tileSize

                    // Maximum x index in the current chunk
                    // (to avoid drawing issues on non-properly divided tilemap)
                    // For example, in a tilemap with a width of 50 and a chunk size of 16,
                    // the last chunk will have a width of 2.
                    val width = min(data.chunkSize, (data.width - (x * data.chunkSize)))

                    Chunk(
                        vertices = chunk.flatMapIndexed { index, _ ->
                            // Current tile bounds in pixels
                            val topTile = top + (index / width * data.tileSize)
                            val leftTile = left + (index % width * data.tileSize)
                            val bottomTile = topTile + data.tileSize
                            val rightTile = leftTile + data.tileSize

                            // Constructed as 2 triangles
                            listOf(
                                // Upper left triangle
                                leftTile, topTile,
                                rightTile, topTile,
                                leftTile, bottomTile,
                                // Bottom right triangle
                                leftTile, bottomTile,
                                rightTile, topTile,
                                rightTile, bottomTile
                            )
                        }.toFloatArray(),
                        textCoordinates = chunk.flatMap { tile ->
                            // Texture coordinates
                            val position = Vector2i(x = tile.tx, y = tile.ty).toVector2f()
                            val leftTCoords = position.x * data.tileSize
                            val topTCoords = position.y * data.tileSize
                            val rightTCoords = leftTCoords + data.tileSize
                            val bottomTCoords = topTCoords + data.tileSize

                            listOf(
                                // Upper left triangle
                                leftTCoords, topTCoords,
                                rightTCoords, topTCoords,
                                leftTCoords, bottomTCoords,
                                // Bottom right triangle
                                leftTCoords, bottomTCoords,
                                rightTCoords, topTCoords,
                                rightTCoords, bottomTCoords
                            )
                        }.toFloatArray(),
                        tileset = currentTileset,
                        rect = ImmutableRect(left, top, right, bottom),
                        chunkSize = Vector2i(data.chunkSize, data.chunkSize)
                    )
                }
            }
        }
    }.toSortedMap { o1, o2 -> data.layersOrder.indexOf(o1).compareTo(data.layersOrder.indexOf(o2)) }

    private val collisions = data.collisions.chunked(data.width).map { it.toMutableList() }.toMutableList()

    val initialPlayerPosition by lazy {
        for ((y, line) in collisions.withIndex()) {
            for ((x, block) in line.withIndex()) {
                if (block == SPAWN_BLOCK) {
                    return@lazy Vector2i(x, y)
                }
            }
        }

        Vector2i(0, 0)
    }

    override val rect = ImmutableRect(
        0f,
        0f,
        data.width * data.tileSize,
        data.height * data.tileSize
    )

    private fun getOrLoadTileset(res: String): Tileset {
        return availableTilesets[res]
            ?: Tileset(res, data.chunkSize, data.tileSize.toInt(), context).also { availableTilesets[res] = it }
    }

    fun textVerticesGivenIndex(index: Int): FloatArray {
        return textVerticesGivenPosition(tileset.indicesIn2DForIndex(index))
    }

    fun textVerticesGivenPosition(tilePosition: Vector2i): FloatArray {
        val (left, top) = tilePosition * tileSize
        val (right, bottom) = (left + tileSize to top + tileSize)
        return floatArrayOf(
            left, top,
            left, bottom,
            right, top,
            right, top,
            left, bottom,
            right, bottom
        )
    }

    fun collisionTilesIntersecting(rect: RectF): List<Int> {
        val coordinates = rect.times(1f / data.tileSize)
        val left = coordinates.left.toInt()
        val right = ceil(coordinates.right).toInt()
        val top = coordinates.top.toInt()
        val bottom = ceil(coordinates.bottom).toInt()

        return (top until bottom).flatMap { y ->
            (left until right).map { x ->
                collisions.getOrNull(y)?.getOrNull(x)
            }
        }.filterNotNull()
    }

    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) {
        layers.forEach { (_, chunks) ->
            chunks.forEach { chunk ->
                chunk.draw(bounds, surfaceHolder, paint)
            }
        }
    }

    fun setCollision(x: Int, y: Int, value: Int) {
        collisions[y][x] = value
    }

}

@Serializable
data class LayerData(
    val data: List<String>,
    val tileset: String? = null
)

@Serializable
data class TiledMapData(
    val tileset: String,
    val chunkSize: Int = 16,
    val tileSize: Float,
    val width: Int,
    val height: Int,
    val layersOrder: List<String>,
    val layers: Map<String, LayerData>,
    val collisions: List<Int> = emptyList()
)

@Throws(Resources.NotFoundException::class)
fun Context.loadTiledMap(@RawRes res: Int): TiledMap {
    val content = runCatching { resources.openRawResource(res).reader().readText() }
        .getOrElse {
            Log.e("TILED MAP", "Unable to tiled map file, reason: $it")
            throw it
        }

    return TiledMap(
        Yaml.default.decodeFromString(content),
        this
    )
}