package com.cabe.app.novel.parser;

import com.cabe.app.novel.model.BaseObject;

import org.jsoup.nodes.Document;

/**
 * 作者：沈建芳 on 2017/10/16 21:08
 */
public interface BaseParser<T extends BaseObject> {
    T parserDoc(Document doc);
}
