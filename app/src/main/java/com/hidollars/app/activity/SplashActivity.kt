package com.hidollars.app.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hidollars.app.R
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        loadGif()
        moveToNext()
    }


    private lateinit var postDelayed: Handler
    private lateinit var runnable: Runnable
    private fun moveToNext() {
        postDelayed = Handler(Looper.getMainLooper())
        runnable = Runnable {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
        postDelayed.postDelayed(runnable, 2300)
    }

    private fun loadGif() {
        Glide.with(this)
            .asGif()
            .load(R.drawable.splash_anim)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(imgSplash)
    }
}