package com.cabe.app.novel.activity;

import android.content.Intent;
import android.os.Bundle;

import com.cabe.app.novel.R;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
