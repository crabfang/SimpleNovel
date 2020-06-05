package com.cabe.app.novel.domain

import android.text.TextUtils
import com.cabe.app.novel.model.LocalNovelList
import com.cabe.lib.cache.impl.DiskCacheUseCase
import com.google.gson.reflect.TypeToken

/**
 * 本地小说列表查询
 * 作者：沈建芳 on 2017/10/9 16:30
 */
class LocalNovelsUseCase : DiskCacheUseCase<LocalNovelList>(object: TypeToken<LocalNovelList>(){}) {
    companion object {
        fun updateLocalNovelPic(url: String?, picUrl: String?) {
            if (!TextUtils.isEmpty(picUrl)) {
                val localUseCase = LocalNovelsUseCase()
                val localData: LocalNovelList = localUseCase.diskRepository.get(localUseCase.typeToken)
                if (localData.list != null) {
                    var change = false
                    for (item in localData.list) {
                        if (item.url == url) {
                            item.picUrl = picUrl
                            change = true
                            break
                        }
                    }
                    if (change) {
                        localUseCase.saveCacheDisk(localData)
                    }
                }
            }
        }
    }
}