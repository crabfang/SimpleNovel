package com.cabe.app.novel.activity

import android.app.Application
import com.cabe.lib.cache.disk.DiskCacheManager
import com.pgyer.pgyersdk.PgyerSDKManager
import com.pgyer.pgyersdk.pgyerenum.Features

/**
 * 作者：沈建芳 on 2017/10/11 11:17
 */
class MyApp : Application() {
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