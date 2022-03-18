package com.ut3.capturethefly.game.drawable.sprites

import android.content.Context
import android.graphics.*
import androidx.annotation.RawRes
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.ut3.capturethefly.game.drawable.Drawable
import com.ut3.capturethefly.game.drawable.ImmutableRect
import com.ut3.capturethefly.game.drawable.loadBitmapKeepSize
import com.ut3.capturethefly.game.logic.Entity
import com.ut3.capturethefly.game.utils.Vector2f

abstract class AnimatedSprite(
    context: Context,
    @RawRes resource: Int,
    private val defaultAction: String
) : Drawable, Entity {

    protected val information: SpriteInformation = Yaml.default.decodeFromStream(
        context.resources.openRawResource(resource)
    )

    private val bitmap: Bitmap = context.loadBitmapKeepSize(information.metadata.resource)
    private val actions get() = information.actions
    private val tileSize = Vector2f(information.metadata.tileWidth, information.metadata.tileHeight)
    private val tileCount get() = information.metadata.tileCount
    override var rect = ImmutableRect(0f, 0f, tileSize.x, tileSize.y); protected set

    protected var currentAction: SpriteAction = actions.getValue(defaultAction); private set
    private var timeSinceLastUpdate = 0f
    protected var actionIndexOffset = 0; private set
    var isBitmapReversed = false; private set
    private var drawVertices = verticesForIndex(currentAction, actionIndexOffset, isBitmapReversed)

    val isAnimationFinished: Boolean get() {
        return currentAction.count == actionIndexOffset + 1
    }

    fun setAction(action: String, reverse: Boolean = false): Boolean {
        val newAction = actions[action]
            ?: return false
        if (newAction == currentAction) {
            return false
        }

        currentAction = newAction
        actionIndexOffset = 0
        isBitmapReversed = reverse
        timeSinceLastUpdate = currentAction.time

        drawVertices = verticesForIndex(currentAction, offset = actionIndexOffset, reverse = isBitmapReversed)

        return true
    }

    fun move(dx: Float, dy: Float) {
        rect = ImmutableRect(rect.copyOfUnderlyingRect.apply { offset(dx, dy) })
    }

    fun moveTo(x: Float, y: Float) {
        rect = ImmutableRect(rect.copyOfUnderlyingRect.apply { offsetTo(x, y) })
    }

    private fun verticesForIndex(action: SpriteAction, offset: Int, reverse: Boolean): FloatArray {
        val index = action.index + offset

        var left = tileSize.x * index
        var right = left + tileSize.x
        var top = 0f
        var bottom = top + tileSize.y


        if (reverse) {
            left = right.also { right = left }
        }

        return floatArrayOf(
            left, top,
            right, top,
            left, bottom,
            left, bottom,
            right, top,
            right, bottom
        )
    }

    override fun update(delta: Float) {
        timeSinceLastUpdate += delta
        if (currentAction.time == -1f || currentAction.count == 1) {
            return
        }

        if (!currentAction.loop && isAnimationFinished) {
            return
        }

        if (timeSinceLastUpdate >= currentAction.time) {
            timeSinceLastUpdate = 0f
            actionIndexOffset = (actionIndexOffset + 1) % currentAction.count
            drawVertices = verticesForIndex(currentAction, actionIndexOffset, isBitmapReversed)
        }
    }

    override fun drawOnCanvas(bounds: RectF, surfaceHolder: Canvas, paint: Paint) {
        val texture = Paint(paint).apply {
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            isAntiAlias = false
            isDither = true
            isFilterBitmap = false
        }

        surfaceHolder.drawVertices(
            Canvas.VertexMode.TRIANGLES,
            drawVertices.size,
            floatArrayOf(
                rect.left, rect.top,
                rect.right, rect.top,
                rect.left, rect.bottom,
                rect.left, rect.bottom,
                rect.right, rect.top,
                rect.right, rect.bottom
            ),
            0,
            drawVertices,
            0,
            null,
            0,
            null,
            0,
            0,
            texture
        )
    }

}