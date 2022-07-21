package com.cabe.app.novel.utils

import android.util.Log
import com.cabe.lib.cache.disk.DiskCacheManager
import com.jakewharton.disklrucache.DiskLruCache
import org.jsoup.internal.StringUtil
import rx.android.BuildConfig
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * 作者：沈建芳 on 2017/10/12 18:41
 */
object DiskUtils {
    private var cache: DiskLruCache? = null
    private fun initCache() {
        if (cache == null) {
            val cacheDir = File(DiskCacheManager.DISK_CACHE_PATH)
            if (!cacheDir.exists()) {
                val result = cacheDir.mkdirs()
                Log.d("DiskCacheManager", "mkdirs $result")
            }
            try {
                cache = DiskLruCache.open(cacheDir, BuildConfig.VERSION_CODE, 1, (1024 * 1024 * 10).toLong())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun saveData(key: String?, `val`: String?) {
        initCache()
        if(key.isNullOrBlank()) return

        cache?.let { cache ->
            try {
                val editor = cache.edit(getFormatKey(key))
                editor[0] = `val`
                editor.commit()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getData(key: String?): String? {
        initCache()
        if(key.isNullOrBlank()) return null

        if (cache != null) {
            try {
                val snapshot = cache!![getFormatKey(key)]
                if (snapshot != null) {
                    val dataStr = snapshot.getString(0)
                    snapshot.close()
                    return dataStr
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun getFormatKey(key: String): String {
        val cacheKey: String = try {
            val e = MessageDigest.getInstance("MD5")
            e.update(key.toByteArray())
            bytesToHexString(e.digest())
        } catch (var4: NoSuchAlgorithmException) {
            key.hashCode().toString()
        }
        return cacheKey
    }

    private fun bytesToHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        val var4 = bytes.size
        for (var5 in 0 until var4) {
            val b = bytes[var5]
            val hex = Integer.toHexString(255 and b.toInt())
            if (hex.length == 1) {
                sb.append('0')
            }
            sb.append(hex)
        }
        return sb.toString()
    }
}