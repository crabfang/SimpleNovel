package com.cabe.app.novel.model;

import android.text.TextUtils;

/**
 * 作者：沈建芳 on 2017/10/9 16:21
 */
public class NovelInfo extends BaseObject {
    public String title;
    public String url;
    public String picUrl;
    public String author;
    public String type;
    public String wordSize;
    public String state;
    public SourceType source = SourceType.X23US;

    public String getPicUrl() {
        if(!TextUtils.isEmpty(picUrl)) return picUrl;

        return source == null ? "" : source.getPicUrl();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof NovelInfo) {
            NovelInfo novelInfo = (NovelInfo) obj;
            boolean isSameSource = true;
            if(source != null && novelInfo.source != null) {
                isSameSource = source.toString().equals(novelInfo.source.toString());
            }
            return title.equals(novelInfo.title) && url.equals(novelInfo.url) && author.equals(novelInfo.author) && isSameSource;
        }
        return false;
    }

    public String toString() {
        return title + "(" + author + ")" + type + "#" + source;
    }
}
