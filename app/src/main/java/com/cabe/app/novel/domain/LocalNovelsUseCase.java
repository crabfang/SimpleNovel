package com.cabe.app.novel.domain;

import com.cabe.app.novel.model.LocalNovelList;
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
}
