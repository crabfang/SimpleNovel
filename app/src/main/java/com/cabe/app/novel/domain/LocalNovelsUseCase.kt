package com.cabe.app.novel.domain

import android.net.Uri
import com.cabe.app.novel.model.LocalNovelList
import com.cabe.app.novel.model.NovelList
import com.cabe.app.novel.model.SourceType
import com.cabe.lib.cache.impl.DiskCacheUseCase
import com.google.gson.reflect.TypeToken

/**
 * 本地小说列表查询
 * 作者：沈建芳 on 2017/10/9 16:30
 */
class LocalNovelsUseCase : DiskCacheUseCase<LocalNovelList>(object: TypeToken<LocalNovelList>(){}) {
    companion object {
        fun updateLocalPic(url: String?, data: NovelList?) {
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
        fun updateLocalSource(url: String?, source: SourceType) {
            val localUseCase = LocalNovelsUseCase()
            val localData: LocalNovelList = localUseCase.diskRepository.get(localUseCase.typeToken)
            if (localData.list != null) {
                var change = false
                localData.list?.find { it.url == url }?.let { result ->
                    result.source = source
                    change = true
                }
                if (change) {
                    localUseCase.saveCacheDisk(localData)
                }
            }
        }
        fun updateLocalHost(url: String?, host: String) {
            val localUseCase = LocalNovelsUseCase()
            val localData: LocalNovelList = localUseCase.diskRepository.get(localUseCase.typeToken)
            if (localData.list != null) {
                var change = false
                localData.list?.find { it.url == url }?.let { result ->
                    val path = Uri.parse(result.url).path
                    result.url = Uri.parse(host).buildUpon().path(path).build().toString()
                    change = true
                }
                if (change) {
                    localUseCase.saveCacheDisk(localData)
                }
            }
        }
    }
}