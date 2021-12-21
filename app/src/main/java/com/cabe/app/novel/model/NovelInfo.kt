package com.cabe.app.novel.model

import android.text.TextUtils

/**
 * 作者：沈建芳 on 2017/10/9 16:21
 */
class NovelInfo : BaseObject() {
    var title: String? = null
    var url: String? = null
    var picUrl: String? = null
        get() {
            if (!TextUtils.isEmpty(field)) return field
            return if (source == null) "" else source!!.picUrl
        }
    var author: String? = null
    var type: String? = null
    var update: String? = null
    var state: String? = null
    var lastChapter: String? = null
    var readChapter: String? = null
    var source: SourceType? = SourceType.FPZW

    override fun equals(other: Any?): Boolean {
        return (other as? NovelInfo)?.let { novelInfo ->
            var isSameSource = true
            if (source != null && novelInfo.source != null) {
                isSameSource = source.toString() == novelInfo.source.toString()
            }
            title == novelInfo.title && url == novelInfo.url && author == novelInfo.author && isSameSource
        } ?: false
    }

    override fun toString(): String {
        return "$title($author)$type#$source"
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (picUrl?.hashCode() ?: 0)
        result = 31 * result + (author?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (update?.hashCode() ?: 0)
        result = 31 * result + (state?.hashCode() ?: 0)
        result = 31 * result + (source?.hashCode() ?: 0)
        return result
    }
}