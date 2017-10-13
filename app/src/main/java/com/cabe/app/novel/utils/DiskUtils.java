package com.cabe.app.novel.utils;

import android.util.Log;

import com.cabe.lib.cache.disk.DiskCacheManager;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import rx.android.BuildConfig;

/**
 * 作者：沈建芳 on 2017/10/12 18:41
 */
public class DiskUtils {
    private static DiskLruCache cache;

    private static void initCache() {
        if(cache == null) {
            File cacheDir = new File(DiskCacheManager.DISK_CACHE_PATH);
            if(!cacheDir.exists()) {
                boolean result = cacheDir.mkdirs();
                Log.d("DiskCacheManager", "mkdirs " + result);
            }
            try {
                cache = DiskLruCache.open(cacheDir, BuildConfig.VERSION_CODE, 1, 1024 * 1024 * 10);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveData(String key, String val) {
        initCache();
        if(cache != null) {
            try {
                DiskLruCache.Editor editor = cache.edit(getFormatKey(key));
                editor.set(0, val);
                editor.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getData(String key) {
        initCache();
        if(cache != null) {
            try {
                DiskLruCache.Snapshot snapshot = cache.get(getFormatKey(key));
                if(snapshot != null) {
                    String dataStr = snapshot.getString(0);
                    snapshot.close();
                    return dataStr;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String getFormatKey(String key) {
        String cacheKey;
        try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            e.update(key.getBytes());
            cacheKey = bytesToHexString(e.digest());
        } catch (NoSuchAlgorithmException var4) {
            cacheKey = String.valueOf(key.hashCode());
        }

        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        byte[] var3 = bytes;
        int var4 = bytes.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            byte b = var3[var5];
            String hex = Integer.toHexString(255 & b);
            if(hex.length() == 1) {
                sb.append('0');
            }

            sb.append(hex);
        }

        return sb.toString();
    }
}
