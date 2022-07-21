package com.cabe.app.novel.activity

import android.os.Bundle
import com.cabe.app.novel.R

class AboutActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        title = "关于我们"
    }
}