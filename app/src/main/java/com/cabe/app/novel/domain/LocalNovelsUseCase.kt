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
                    localData.list?.find { it.url == url }?.let { result ->
                        data.picUrl?.let { result.picUrl = it }
                        data.author?.let { result.author = it }
                        data.type?.let { result.type = it }
                        data.state?.let { result.state = it }
                        data.update?.let { result.update = it }
                        data.lastChapter?.let { result.lastChapter = it }
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