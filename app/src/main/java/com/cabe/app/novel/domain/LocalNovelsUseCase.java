package com.cabe.app.novel.domain;

import android.text.TextUtils;

import com.cabe.app.novel.model.LocalNovelList;
import com.cabe.app.novel.model.NovelInfo;
import com.cabe.lib.cache.impl.DiskCacheUseCase;
import com.google.gson.reflect.TypeToken;

/**
 *  本地小说列表查询
 * 作者：沈建芳 on 2017/10/9 16:30
 */
public class LocalNovelsUseCase extends DiskCacheUseCase<LocalNovelList> {
    public LocalNovelsUseCase() {
        super(new TypeToken<LocalNovelList>(){});
    }

    public static void updateLocalNovelPic(String url, String picUrl) {
        if(!TextUtils.isEmpty(picUrl)) {
            LocalNovelsUseCase localUseCase = new LocalNovelsUseCase();
            LocalNovelList localData = localUseCase.getDiskRepository().get(localUseCase.getTypeToken());
            if(localData != null && localData.list != null) {
                boolean change = false;
                for(NovelInfo item : localData.list) {
                    if(item.url.equals(url)) {
                        item.picUrl = picUrl;
                        change = true;
                        break;
                    }
                }
                if(change) {
                    localUseCase.saveCacheDisk(localData);
                }
            }
        }
    }
}
