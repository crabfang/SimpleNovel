package com.cabe.app.novel.activity;

import android.app.Application;

import com.cabe.lib.cache.disk.DiskCacheManager;
import com.pgyersdk.crash.PgyCrashManager;

import java.io.File;

/**
 * 作者：沈建芳 on 2017/10/11 11:17
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        File fileDir = getExternalFilesDir("configs");
        if(fileDir != null) {
            DiskCacheManager.DISK_CACHE_PATH = fileDir.getAbsolutePath();
        } else {
            DiskCacheManager.DISK_CACHE_PATH = getFilesDir().getAbsolutePath();
        }
        PgyCrashManager.register();
    }
}
