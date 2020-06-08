package com.cabe.app.novel.domain

import com.cabe.app.novel.model.LocalNovelList
import com.cabe.app.novel.model.NovelList
import com.cabe.lib.cache.impl.DiskCacheUseCase
import com.google.gson.reflect.TypeToken

/**
 * 本地小说列表查询
 * 作者：沈建芳 on 2017/10/9 16:30
 */
class LocalNovelsUseCase : DiskCacheUseCase<LocalNovelList>(object: TypeToken<LocalNovelList>(){}) {
    companion object {
        fun updateLocalNovelPic(url: String?, data: NovelList?) {
            if (data != null) {
                val localUseCase = LocalNovelsUseCase()
                val localData: LocalNovelList = localUseCase.diskRepository.get(localUseCase.typeToken)
                if (localData.list != null) {
                    var change = false
                    localData.list.find { it.url == url }?.let { result ->
                        result.picUrl = data.picUrl
                        result.author = data.author
                        result.type = data.type
                        result.state = data.state
                        result.update = data.update
                        result.lastChapter = data.lastChapter
                        change = true
                    }
                    if (change) {
                        localUseCase.saveCacheDisk(localData)
                    }
                }
            }
        }
    }
}