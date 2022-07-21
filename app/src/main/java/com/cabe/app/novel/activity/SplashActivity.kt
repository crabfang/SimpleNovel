package com.cabe.app.novel.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.cabe.app.novel.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}