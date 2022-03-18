package com.ut3.capturethefly

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.ut3.capturethefly.game.GameView

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        gameView = GameView(this)
        setContentView(gameView)
    }

}