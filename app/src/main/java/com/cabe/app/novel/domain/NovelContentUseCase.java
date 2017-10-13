package com.cabe.app.novel.domain;

import android.text.TextUtils;

import com.cabe.app.novel.model.NovelContent;
import com.cabe.app.novel.utils.UrlUtils;
import com.cabe.lib.cache.exception.HttpExceptionCode;
import com.cabe.lib.cache.exception.RxException;
import com.cabe.lib.cache.http.HttpStringCacheManager;
import com.cabe.lib.cache.http.RequestParams;
import com.cabe.lib.cache.http.transformer.HttpStringTransformer;
import com.cabe.lib.cache.impl.HttpCacheUseCase;
import com.cabe.lib.cache.interactor.HttpCacheRepository;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * 作者：沈建芳 on 2017/10/9 16:30
 */
public class NovelContentUseCase extends HttpCacheUseCase<NovelContent> {
    private String url;
    private String host = ServiceConfig.HOST;
    public NovelContentUseCase(String url) {
        super(new TypeToken<NovelContent>() {}, null);

        if(TextUtils.isEmpty(url)) {
            throw RxException.build(HttpExceptionCode.HTTP_STATUS_LOCAL_REQUEST_NONE, null);
        }

        this.url = url;
        String[] group = UrlUtils.splitUrl(url);
        host = group[0];
        RequestParams params = new RequestParams();
        params.host = host + "/";
        params.path = group.length > 1 ? group[1] : "";
        params.requestMethod = RequestParams.REQUEST_METHOD_GET;

        setRequestParams(params);

        HttpCacheRepository<String, NovelContent> httpRepository = getHttpRepository();
        if(httpRepository instanceof HttpStringCacheManager) {
            HttpStringCacheManager<NovelContent> httpManager = (HttpStringCacheManager<NovelContent>) httpRepository;
            httpManager.setStringEncode("gbk");
        }
        httpRepository.setResponseTransformer(new HttpStringTransformer<NovelContent>() {
            @Override
            public NovelContent buildData(String responseStr) {
                Document docL = Jsoup.parse(responseStr);
                return parserHtmlForList(docL);
            }
        });
    }

    private NovelContent parserHtmlForList(Document doc) {
        NovelContent content = null;
        try {
            content = new NovelContent();
            content.url = url;
            Elements titleEs = doc.select("dd > h1");
            if(titleEs != null && titleEs.size() > 0) {
                content.title = titleEs.get(0).text();
            }
            Elements preEs = doc.select("a:contains(上一页)");
            if(preEs != null && preEs.size() > 0) {
                content.preUrl = host + preEs.get(0).attr("href");
            }
            Elements nextEs = doc.select("a:contains(下一页)");
            if(nextEs != null && nextEs.size() > 0) {
                content.nextUrl = host + nextEs.get(0).attr("href");
            }
            Elements contentEs = doc.select("dd#contents");
            if(contentEs != null && contentEs.size() > 0) {
                content.content = contentEs.get(0).html();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }
}
