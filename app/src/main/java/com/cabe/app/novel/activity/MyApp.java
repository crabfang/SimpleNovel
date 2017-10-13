package com.cabe.app.novel.activity;

import android.app.Application;

import com.cabe.lib.cache.disk.DiskCacheManager;

/**
 * 作者：沈建芳 on 2017/10/11 11:17
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DiskCacheManager.DISK_CACHE_PATH = getExternalCacheDir().toString();
    }
}
