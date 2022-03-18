package com.ut3.hiddendoor

import androidx.appcompat.app.AppCompatActivity
import com.ut3.capturethefly.game.GameView
import com.ut3.capturethefly.game.logic.GameLogic

abstract class BaseActivity: AppCompatActivity() {
    lateinit var logic: GameLogic
    lateinit var gameView: GameView

//    override fun onPause() {
//        super.onPause()
//        if (this::logic.isInitialized) {
//            logic.stop()
//            gameView.isEnabled = false
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (this::logic.isInitialized) {
//            gameView.isEnabled = true
//            logic.start()
//        }
//    }
}