package com.cabe.app.novel.domain.ekxs;

import android.text.TextUtils;

import com.cabe.app.novel.model.NovelContent;
import com.cabe.app.novel.model.NovelList;
import com.cabe.app.novel.model.SourceType;
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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 作者：沈建芳 on 2017/10/9 16:30
 */
public class NovelList42KXSUseCase extends HttpCacheUseCase<NovelList> {
    private String host;
    private String novelUrl;
    public NovelList42KXSUseCase(String url) {
        super(new TypeToken<NovelList>() {}, null);

        if(TextUtils.isEmpty(url)) {
            throw RxException.build(HttpExceptionCode.HTTP_STATUS_LOCAL_REQUEST_NONE, null);
        }
        this.novelUrl = url;

        String[] group = UrlUtils.splitUrl(url);
        host = group[0];
        RequestParams params = new RequestParams();
        String host = group[0] + "/";
        String path = group.length > 1 ? group[1] : "";
        try {
            path = URLEncoder.encode(path, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        params.host = host;
        params.path = path;
        params.requestMethod = RequestParams.REQUEST_METHOD_GET;

        setRequestParams(params);
        HttpCacheRepository<String, NovelList> httpRepository = getHttpRepository();
        if(httpRepository instanceof HttpStringCacheManager) {
            HttpStringCacheManager<NovelList> httpManager = (HttpStringCacheManager<NovelList>) httpRepository;
            httpManager.setStringEncode("utf-8");
        }
        httpRepository.setResponseTransformer(new HttpStringTransformer<NovelList>() {
            @Override
            public NovelList buildData(String responseStr) {
                Document docL = Jsoup.parse(responseStr);
                return parserHtmlForList(docL);
            }
        });
    }

    private NovelList parserHtmlForList(Document doc) {
        NovelList novelDetail = null;
        try {
            novelDetail = new NovelList();
            Elements titleEs = doc.select("div.info2 > h1");
            if(titleEs != null && titleEs.size() > 0) {
                novelDetail.title = titleEs.get(0).text();
            }
            Elements authorEs = doc.select("div.info2 > h3 > a");
            if(authorEs != null && authorEs.size() > 0) {
                novelDetail.author = authorEs.get(0).text();
            }
            Elements listEs = doc.select("ul.list-charts > li > a");
            if(listEs != null && listEs.size() > 0) {
                List<NovelContent> list = new ArrayList<>();
                for(int i=0;i<listEs.size();i++) {
                    NovelContent content = new NovelContent();

                    Element element = listEs.get(i);
                    content.title = element.text();
                    content.url = host + element.attr("href");
                    content.source = SourceType.EKXS;
                    list.add(content);
                }
                novelDetail.list = list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return novelDetail;
    }
}
