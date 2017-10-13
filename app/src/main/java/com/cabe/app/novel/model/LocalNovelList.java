package com.cabe.app.novel.model;

import java.util.LinkedList;

/**
 * 本地保存的小说列表
 * 作者：沈建芳 on 2017/10/11 11:19
 */
public class LocalNovelList extends BaseObject {
    public LinkedList<NovelInfo> list;

    public boolean isEmpty() {
        return list == null || list.isEmpty();
    }

    public NovelInfo getNovel(int index) {
        if(list == null ||index < 0 || index >= list.size()) return null;

        return list.get(index);
    }

    public void addNovel(NovelInfo novel) {
        if(novel == null) return;

        if(list == null) {
            list = new LinkedList<>();
        }
        if(!list.contains(novel)) {
            list.addFirst(novel);
        }
    }

    public void removeNovel(NovelInfo novel) {
        if(novel == null) return;

        if(list == null || !list.contains(novel)) return;

        list.remove(novel);
    }

    public void setTop(NovelInfo novel) {
        if(novel == null) return;

        if(list == null || !list.contains(novel)) return;

        if(list.remove(novel)) {
            list.addFirst(novel);
        }
    }

    public String toString() {
        return "" + list;
    }
}
