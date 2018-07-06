package com.cabe.app.novel.domain.ekxs;

import android.text.TextUtils;

import com.cabe.app.novel.domain.ServiceConfig;
import com.cabe.app.novel.model.NovelInfo;
import com.cabe.app.novel.model.SourceType;
import com.cabe.app.novel.retrofit.MyHttpManager;
import com.cabe.app.novel.utils.UrlUtils;
import com.cabe.lib.cache.exception.HttpExceptionCode;
import com.cabe.lib.cache.exception.RxException;
import com.cabe.lib.cache.http.RequestParams;
import com.cabe.lib.cache.http.transformer.HttpStringTransformer;
import com.cabe.lib.cache.impl.HttpCacheUseCase;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 作者：沈建芳 on 2017/10/9 16:30
 */
public class NovelDetail42kxsUseCase extends HttpCacheUseCase<NovelInfo> {
    public NovelDetail42kxsUseCase(String url) {
        super(new TypeToken<NovelInfo>(){}, null);

        if(TextUtils.isEmpty(url)) {
            throw RxException.build(HttpExceptionCode.HTTP_STATUS_LOCAL_REQUEST_NONE, null);
        }

        String[] group = UrlUtils.splitUrl(url);
        String host = group[0];
        RequestParams params = new RequestParams();
        params.host = host + "/";
        String path = group.length > 1 ? group[1] : "";
        try {
            path = URLEncoder.encode(path, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        params.path = path;
        setRequestParams(params);

        setHttpManager(new MyHttpManager<>(getTypeToken()));
        getHttpRepository().setResponseTransformer(new HttpStringTransformer<NovelInfo>() {
            @Override
            public NovelInfo buildData(String responseStr) {
                Document docL = Jsoup.parse(responseStr);
                return parserHtmlForNovel(docL);
            }
        });
    }

    private NovelInfo parserHtmlForNovel(Document doc) {
        NovelInfo novel;
        try {
            novel = new NovelInfo();
            Elements picEs = doc.select("div.bortable > img");
            if(picEs != null && picEs.size() > 0) {
                String picUrl = picEs.first().attr("src");
                novel.picUrl = ServiceConfig.HOST_2KXS + picUrl.substring(1);
            }

            Elements titleEs = doc.select("div#title > h2 > a");
            if(titleEs != null && titleEs.size() > 0) {
                novel.title = titleEs.first().text();
                novel.url = titleEs.first().attr("href");
            }

            Elements authorEs = doc.select("div#title > h2 > em > a");
            if(authorEs != null && authorEs.size() > 0) {
                novel.author = authorEs.first().text();
            }

            Elements typeEs = doc.select("div.abook > dd > span");
            if(typeEs != null && typeEs.size() > 9) {
                novel.type = typeEs.first().text();
                novel.wordSize = typeEs.get(1).text();
                novel.state = typeEs.get(9).text();
            }
            novel.source = SourceType.EKXS;
        } catch (Exception e) {
            e.printStackTrace();
            novel = null;
        }
        return novel;
    }
}
