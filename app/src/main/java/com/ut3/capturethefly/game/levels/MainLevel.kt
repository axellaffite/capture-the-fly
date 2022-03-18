package com.ut3.capturethefly.game.levels

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withClip
import androidx.core.graphics.withScale
import com.ut3.capturethefly.R
import com.ut3.capturethefly.game.GameView
import com.ut3.capturethefly.game.drawable.cameras.createTrackingCamera
import com.ut3.capturethefly.game.logic.Fly
import com.ut3.capturethefly.game.logic.InputState
import com.ut3.capturethefly.game.logic.isShaking
import java.util.*


class MainLevel(
    gameView : GameView
) : Level(
    gameView = gameView,
    soundRes = R.raw.ambiance_sound,
    tilemapResource = TILE_MAP_RESOURCE
) {

    companion object {
        const val TILE_MAP_RESOURCE = R.raw.level
        const val NAME = "level"
    }

    private val timeNeededToStun = 1f
    private var luminosityLevel  = 0f
    private var fliesAlive = 10
    private var fliesAreStunned = false
    private val camera = createTrackingCamera(
        screenPosition = RectF(0f, 0f, gameView.width.toFloat(), gameView.height.toFloat()),
        gamePosition = RectF(0f, 0f, gameView.width.toFloat(), gameView.height.toFloat()),
        track = player::center
    )

    private var timeElapsedWithLowLuminosity = 0f
    private var remainingFlies = 0
    private var targetFlies = 10
    private var spawnInterval = 3f
    private var lastFly = 0f
    private val flies = mutableListOf<Fly>()
    private var isShaking = false

    private var currentWave = 0
    private var launchNextWave = false
    private var text = "Wave 1"
    private var textOpacity = 255f
    private var playerIsDead = false

    private var shouldReset = false

    private val random = Random(System.currentTimeMillis())

    init {
        reset()
    }

    override fun onSaveState() {
        TODO("save state of the level")
    }

    private fun reset() {
        timeElapsedWithLowLuminosity = 0f
        remainingFlies = 0
        targetFlies = 10
        spawnInterval = 3f
        lastFly = 0f
        flies.clear()
        isShaking = false

        currentWave = 0
        launchNextWave = false
        text = "Wave 1"
        textOpacity = 255f
        playerIsDead = false

        player.moveTo(tilemap.rect.width/2.toFloat(),tilemap.rect.height/2.toFloat())
        player.resetHealth()

        launchNextWave()
    }

    override fun handleInput(inputState: InputState) {
        super.handleInput(inputState)
        if(hud.controlButtons.isAPressed){
            player.attack()
            flies.forEach {
                if (player.attackRect.intersects(it.rect)){
                    it.die()
                }
            }
        }
        luminosityLevel = inputState.luminosity
        isShaking = inputState.isShaking(preferences.accelerationReference)
        Log.d("LUMINOSITY",luminosityLevel.toString())
    }

    fun countLowLuminosityTime(delta:Float) {
        if (luminosityLevel <10) {
            timeElapsedWithLowLuminosity += delta
        } else {
            timeElapsedWithLowLuminosity = 0f
        }
    }

    fun stunFlies() {
        if (timeElapsedWithLowLuminosity >= timeNeededToStun && !fliesAreStunned) {
            for (fly in flies) {
                fly.stun(true)
            }
            fliesAreStunned = true
        }
        if (timeElapsedWithLowLuminosity <= timeNeededToStun && fliesAreStunned) {
            for (fly in flies) {
                fly.stun(false)
            }
            fliesAreStunned = false
        }
    }

    override fun update(delta: Float) {
        super.update(delta)

        if (shouldReset && player.isAnimationFinished) {
            shouldReset = false
            reset()
        }

        player.gatherPower(delta)

        lastFly += delta
        if (lastFly >= spawnInterval && flies.size < targetFlies) {

            flies.add(
                createEntity {
                    Fly(
                        context = gameView.context,
                        x = random.nextFloat() * tilemap.rect.width,
                        y = random.nextFloat() * tilemap.rect.height,
                        tiledMap = tilemap,
                        player::center,
                        flies,
                        onDie = {
                            --remainingFlies
                            if (remainingFlies == 0) {
                                launchNextWave = true
                            }
                        }
                    )
                }
            )

            lastFly = 0f
        }

        countLowLuminosityTime(delta)
        stunFlies()

        val playerRect = player.collisionRect
        for (fly in flies) {
            if(fly.attackRect.intersects(playerRect)){
                if (fly.attack()) {
                    playerIsDead = player.takeDamage()
                    if(playerIsDead){
                        shouldReset = true
                    }
                }
            }
        }
        if (isShaking && player.power >= 1f ){
            player.power = 0f
            flies.forEach {
                if (it.rect.intersects(camera.gamePosition)) {
                    it.die()
                }
            }
        }

        textOpacity = (textOpacity - (255 / 3) * delta).coerceAtLeast(0f)
        player.gatherHealth(delta)
    }

    override fun postUpdate(delta: Float) {
        super.postUpdate(delta)

        if (launchNextWave) {
            launchNextWave = false
            launchNextWave()
        }
    }

    private fun launchNextWave() {
        flies.clear()
        currentWave ++
        targetFlies = 2 * currentWave + 5
        remainingFlies = targetFlies
        spawnInterval = (2f - 0.05f * currentWave).coerceAtLeast(0.2f)
        lastFly = 0f
        textOpacity = 255f
        text = "Wave $currentWave"
    }

    override fun render() {
        gameView.draw { canvas, paint ->
            val scaleFactor = ((gameView.width / tilemap.tileSize) / 12f)
            val (pivotX, pivotY) = gameView.width / 2f to gameView.height / 2f


            canvas.drawColor(Color.parseColor("#34202b"))

            canvas.withScale(x = scaleFactor, y = scaleFactor, pivotX = pivotX, pivotY = pivotY) {

                withCamera(camera) { canvas, paint ->
                    canvas.withClip(tilemap.rect.copyOfUnderlyingRect) {
                        canvas.drawColor(Color.BLUE)
                    }

                    canvas.draw(tilemap, paint)
                    canvas.draw(player, paint)
                    flies.forEach {
                        canvas.draw(it, paint)
                    }


                    canvas.drawRect(
                        0f,
                        0f,
                        canvas.width.toFloat(),
                        canvas.height.toFloat(),
                        Paint().apply {
                            color = 0
                        }
                    )
                }
            }

            hud.draw(gameView.rect, canvas, paint)

            val bold = ResourcesCompat.getFont(gameView.context, R.font.monogram)
            val textPaint = Paint().apply {
                textAlign = Paint.Align.CENTER
                textSize = 250f
                color = Color.WHITE
                alpha = textOpacity.toInt()
                typeface = bold
            }


            val xPos = canvas.width / 2
            val yPos = (canvas.height / 4 - (textPaint.descent() + textPaint.ascent()) / 2).toInt()
            //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.

            //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.
            canvas.drawText(text, xPos.toFloat(), yPos.toFloat(), textPaint)
        }
    }
}