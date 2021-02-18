package com.cabe.app.novel.model

import java.util.*

/**
 * 本地保存的小说列表
 * 作者：沈建芳 on 2017/10/11 11:19
 */
class LocalNovelList : BaseObject() {
    var list: LinkedList<NovelInfo>? = null
    val isEmpty: Boolean
        get() = list == null || list!!.isEmpty()

    fun addNovel(novel: NovelInfo?) {
        if (novel == null) return
        if (list == null) {
            list = LinkedList()
        }
        if (!list!!.contains(novel)) {
            list!!.addFirst(novel)
        }
    }

    fun removeNovel(novel: NovelInfo?) {
        if (novel == null) return
        if (list == null || !list!!.contains(novel)) return
        list!!.remove(novel)
    }

    fun setTop(novel: NovelInfo?) {
        if (novel == null) return
        if (list == null || !list!!.contains(novel)) return
        if (list!!.remove(novel)) {
            list!!.addFirst(novel)
        }
    }

    override fun toString(): String {
        return "" + list
    }
}