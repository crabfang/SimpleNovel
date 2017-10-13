package com.cabe.app.novel.model;

import java.util.List;

/**
 * 作者：沈建芳 on 2017/10/11 10:42
 */
public class NovelDetail extends BaseObject {
    public String title;
    public String author;
    public String lastModify;
    public List<NovelContent> list;

    public String getTips() {
        return "作者：" + author + "  更新时间：" + lastModify;
    }

    public String toString() {
        return title + "(" + author + ")#" + lastModify;
    }
}
