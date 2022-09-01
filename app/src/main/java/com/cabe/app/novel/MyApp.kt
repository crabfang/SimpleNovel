package com.cabe.app.novel

import android.app.Application
import android.content.Context
import com.cabe.lib.cache.disk.DiskCacheManager
import com.pgyer.pgyersdk.PgyerSDKManager
import com.pgyer.pgyersdk.pgyerenum.Features

class MyApp: Application() {
    companion object {
        lateinit var instance: MyApp
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        val fileDir = getExternalFilesDir("configs")
        if (fileDir != null) {
            DiskCacheManager.DISK_CACHE_PATH = fileDir.absolutePath
        } else {
            DiskCacheManager.DISK_CACHE_PATH = filesDir.absolutePath
        }
        PgyerSDKManager.Init()
            .setContext(this) //设置上下问对象
            .enable(Features.CHECK_UPDATE) //添加检查新版本
            .start()
    }
}