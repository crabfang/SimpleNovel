package com.cabe.app.novel.utils

import android.net.Uri
import android.text.TextUtils

/**
 * 作者：沈建芳 on 2017/10/11 10:45
 */
object UrlUtils {
    @JvmStatic
    fun splitUrl(url: String?): Array<String> {
        if (TextUtils.isEmpty(url)) return arrayOf("", "")
        val uri = Uri.parse(url)
        val host = uri.scheme + "://" + uri.host
        var last = ""
        if (url!!.length > host.length + 1) {
            last = url.substring(host.length + 1)
        }
        return arrayOf(host, last)
    }

    fun getHostName(url: String?): String? {
        var name = url
        if (TextUtils.isEmpty(url)) return null
        val uri = Uri.parse(url)
        if (uri.host!!.endsWith("23us.com")) {
            name = "顶点小说"
        }
        return name
    }
}