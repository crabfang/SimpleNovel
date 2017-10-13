package com.cabe.app.novel.model;

import com.google.gson.Gson;

/**
 * 作者：沈建芳 on 2017/10/11 11:35
 */
public class BaseObject {
    public String toGson() {
        return new Gson().toJson(this);
    }
}
