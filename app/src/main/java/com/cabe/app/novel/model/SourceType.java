package com.cabe.app.novel.model;

/**
 * 作者：沈建芳 on 2018/7/6 13:45
 */
public enum SourceType {
    X23US("顶点小说", "https://www.x23zw.com/", "https://www.x23zw.com/uploads/401/401958.jpg"),
    FPZW("2K小说", "https://www.2kxsw.com/", "https://www.2kxsw.com/"),
    BQG("笔趣阁", "http://www.xbiquzw.com/", "http://www.xbiquzw.com/");
    private String typeName;
    private String host;
    private String picUrl;
    SourceType(String typeName, String host, String picUrl) {
        this.typeName = typeName;
        this.host = host;
        this.picUrl = picUrl;
    }

    public String getHost() {
        return host;
    }

    public String getPicUrl() {
        return picUrl;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
