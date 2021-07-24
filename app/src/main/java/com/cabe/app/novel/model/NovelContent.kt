package com.cabe.app.novel.model

import android.text.TextUtils

/**
 * 作者：沈建芳 on 2017/10/9 16:23
 */
class NovelContent : BaseObject() {
    @JvmField
    var title: String? = null
    @JvmField
    var content: String? = null
    @JvmField
    var url: String? = null
    @JvmField
    var preUrl: String? = null
    @JvmField
    var nextUrl: String? = null
    @JvmField
    var flagLast = false
    @JvmField
    var source = SourceType.X23US
    override fun equals(other: Any?): Boolean {
        if (other is NovelContent) {
            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(other.title)
                    && !TextUtils.isEmpty(url) && !TextUtils.isEmpty(other.url)
                    && title == other.title && url == other.url) {
                return true
            }
        }
        return false
    }

    override fun toString(): String {
        return "$title#$url"
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (preUrl?.hashCode() ?: 0)
        result = 31 * result + (nextUrl?.hashCode() ?: 0)
        result = 31 * result + flagLast.hashCode()
        result = 31 * result + source.hashCode()
        return result
    }
}