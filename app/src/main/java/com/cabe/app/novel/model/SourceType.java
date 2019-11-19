package com.cabe.app.novel.model;

/**
 * 作者：沈建芳 on 2018/7/6 13:45
 */
public enum SourceType {
    X23US("顶点小说", "https://www.x23us.com/", "https://www.x23us.com/modules/article/images/nocover.jpg"),
    EKXS("2K小说", "https://www.2kxs.org/", "https://www.2kxs.org/modules/article/images/nocover.jpg");
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
