package com.cabe.app.novel.utils;

import android.net.Uri;
import android.text.TextUtils;

/**
 * 作者：沈建芳 on 2017/10/11 10:45
 */
public class UrlUtils {
    public static String[] splitUrl(String url) {
        if(TextUtils.isEmpty(url)) return new String[] { "", "" };

        Uri uri = Uri.parse(url);
        String host = uri.getScheme() + "://" + uri.getHost();
        String last = "";
        if(url.length() > host.length() + 1) {
            last = url.substring(host.length() + 1);
        }

        return new String[] {host, last};
    }

    public static String getHostName(String url) {
        String name = url;
        if(TextUtils.isEmpty(url)) return null;

        Uri uri = Uri.parse(url);
        if(uri.getHost().endsWith("23us.com")) {
            name = "顶点小说";
        }
        return name;
    }
}
