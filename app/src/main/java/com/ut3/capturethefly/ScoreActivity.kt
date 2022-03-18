package com.ut3.capturethefly

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.ut3.capturethefly.databinding.ScoreActivityBinding
import com.ut3.capturethefly.game.utils.Preferences

class ScoreActivity : AppCompatActivity() {

    private lateinit var binding: ScoreActivityBinding
    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


//        preferences = Preferences(this)
//        val total = preferences.scoreLevelOne + preferences.scoreLevelTwo + preferences.scoreLevelThree
//        binding = ScoreActivityBinding.inflate(layoutInflater)
//        val view = binding.root
//        setContentView(view)
//
//        binding.scoreLevel1.text = "${preferences.scoreLevelOne}/25"
//        binding.scoreLevel2.text = "${preferences.scoreLevelTwo}/50"
//        binding.scoreLevel3.text = "${preferences.scoreLevelThree}/25"
//        binding.scoreTotal.text = "${total}/100"
//
//        binding.button.setOnClickListener {
//            val intent = Intent(this@ScoreActivity, HomeActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
//            }
//            startActivity(intent)
//            finish()
//        }
//
//        if (total == 100) {
//            binding.bravoLayout.visibility = View.VISIBLE
//        }else{
//            binding.bravoLayout.visibility = View.GONE
//        }
    }


}