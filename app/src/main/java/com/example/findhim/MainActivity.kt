package com.example.findhim

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.findhim.utils.BaseActivity
import com.example.findhim.utils.MusicPlayer

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startBtn = findViewById<Button>(R.id.startButton)
        val helpBtn = findViewById<Button>(R.id.helpButton)
        val exitBtn = findViewById<Button>(R.id.exitButton)

        MusicPlayer.setupMusicButton(this)

        startBtn.setOnClickListener { launchActivity(StartActivity::class.java) }
        helpBtn.setOnClickListener { launchActivity(HelpActivity::class.java) }
        exitBtn.setOnClickListener { finishAffinity() }

    }

    private fun launchActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        MusicPlayer.updateMusicButton(this)
    }

    override fun onResume() {
        super.onResume()
        MusicPlayer.updateMusicButton(this)
    }


}