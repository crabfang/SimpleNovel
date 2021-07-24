package com.cabe.app.novel.model

import android.text.TextUtils

/**
 * 作者：沈建芳 on 2017/10/11 10:42
 */
class NovelList : BaseObject() {
    var title: String? = null
    var author: String? = null
    var update: String? = null
    var lastChapter: String? = null
    var picUrl: String? = null
    var type: String? = null
    var state: String? = null
    var source = SourceType.X23US
    var list: List<NovelContent>? = null
    val tips: String
        get() {
            var tips = "作者：$author"
            if (!TextUtils.isEmpty(update)) {
                tips += "  更新时间：$update"
            }
            return tips
        }

    override fun toString(): String {
        return "$title($author)#$update"
    }
}