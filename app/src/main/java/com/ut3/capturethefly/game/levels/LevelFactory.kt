package com.ut3.capturethefly.game.levels

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.ut3.capturethefly.MainActivity
import com.ut3.capturethefly.ScoreActivity
import com.ut3.capturethefly.game.GameView
import com.ut3.capturethefly.game.levels.introduction.IntroductionLevel
import com.ut3.capturethefly.game.logic.EntityManager
import com.ut3.capturethefly.game.logic.GameLogic
import com.ut3.capturethefly.game.utils.Preferences

object LevelFactory {
    fun getLevel(levelName: String, gameView: GameView, gameLogic: GameLogic, activity: Activity): EntityManager? {
        val nextLevel = goToNextLevel(activity, gameLogic)
        return when(levelName) {
            HomeLevel.NAME -> HomeLevel(gameView) { levelToLoad ->
                Handler(Looper.getMainLooper()).post {
                    gameLogic.stop()

                    Preferences(activity).currentLevel = getLevelName(levelToLoad)

                    val intent = Intent(activity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                    }
                    activity.startActivity(intent)
                    activity.finish()
                }
            }

            MainLevel.NAME -> MainLevel(gameView)


            IntroductionLevel.NAME -> IntroductionLevel(gameView)

            else -> null
        }
    }

    private fun getLevelName(levelToLoad: Int): String {
        return when (levelToLoad) {
            1 -> IntroductionLevel.NAME
            else -> HomeLevel.NAME
        }
    }

    private fun goToNextLevel(activity: Activity, gameLogic: GameLogic): (level: String) -> Unit {
        return { level ->
            val nextLevel = when (level) {
                IntroductionLevel.NAME -> HomeLevel.NAME
                else -> HomeLevel.NAME
            }

            Handler(Looper.getMainLooper()).post {
                gameLogic.stop()
                Preferences(activity).currentLevel = nextLevel

                val intent = Intent(activity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                }
                activity.startActivity(intent)
                activity.finish()
            }
        }
    }

    fun goToScore(activity: Activity): () -> Unit {
        return {
            Handler(Looper.getMainLooper()).post {

                val intent = Intent(activity, ScoreActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                }
                activity.startActivity(intent)
                activity.finish()
            }
        }
    }
}