package com.cabe.app.novel.activity;

import android.os.Bundle;

import com.cabe.app.novel.R;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle("关于我们");
    }
}
