package com.cabe.app.novel.model;

import android.text.TextUtils;

/**
 * 作者：沈建芳 on 2017/10/9 16:23
 */
public class NovelContent extends BaseObject {
    public String title;
    public String content;
    public String url;
    public String preUrl;
    public String nextUrl;
    public boolean flagLast;

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof NovelContent) {
            NovelContent content = (NovelContent) obj;
            if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content.title)
                    && !TextUtils.isEmpty(url) && !TextUtils.isEmpty(content.url)
                    && title.equals(content.title) && url.equals(content.url)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return title + "#" + url;
    }
}
