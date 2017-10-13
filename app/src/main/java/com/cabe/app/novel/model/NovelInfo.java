package com.cabe.app.novel.model;

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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof NovelInfo) {
            NovelInfo novelInfo = (NovelInfo) obj;
            return title.equals(novelInfo.title) && url.equals(novelInfo.url) && author.equals(novelInfo.author);
        }
        return false;
    }

    public String toString() {
        return title + "(" + author + ")" + type + "#" + state;
    }
}
