package com.cabe.app.novel.model;

import android.text.TextUtils;

import java.util.List;

/**
 * 作者：沈建芳 on 2017/10/11 10:42
 */
public class NovelList extends BaseObject {
    public String title;
    public String author;
    public String update;
    public String picUrl;
    public String type;
    public String state;
    public SourceType source = SourceType.X23US;
    public List<NovelContent> list;

    public String getTips() {
        String tips = "作者：" + author;
        if(!TextUtils.isEmpty(update)) {
            tips += "  更新时间：" + update;
        }
        return tips;
    }

    public String toString() {
        return title + "(" + author + ")#" + update;
    }
}
