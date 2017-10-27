package com.cabe.app.novel.domain;

import android.text.TextUtils;

import com.cabe.app.novel.model.NovelContent;
import com.cabe.app.novel.model.NovelDetail;
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
public class NovelDetailUseCase extends HttpCacheUseCase<NovelDetail> {
    private String novelUrl;
    public NovelDetailUseCase(String url) {
        super(new TypeToken<NovelDetail>() {}, null);

        if(TextUtils.isEmpty(url)) {
            throw RxException.build(HttpExceptionCode.HTTP_STATUS_LOCAL_REQUEST_NONE, null);
        }
        if(!url.endsWith("/")) {
            url += "/";
        }
        this.novelUrl = url;

        String[] group = UrlUtils.splitUrl(url);
        RequestParams params = new RequestParams();
        params.host = group[0] + "/";
        params.path = group.length > 1 ? group[1] : "";
        try {
            params.path = URLEncoder.encode(params.path, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        params.requestMethod = RequestParams.REQUEST_METHOD_GET;

        setRequestParams(params);

        HttpCacheRepository<String, NovelDetail> httpRepository = getHttpRepository();
        if(httpRepository instanceof HttpStringCacheManager) {
            HttpStringCacheManager<NovelDetail> httpManager = (HttpStringCacheManager<NovelDetail>) httpRepository;
            httpManager.setStringEncode("gbk");
        }
        httpRepository.setResponseTransformer(new HttpStringTransformer<NovelDetail>() {
            @Override
            public NovelDetail buildData(String responseStr) {
                Document docL = Jsoup.parse(responseStr);
                return parserHtmlForList(docL);
            }
        });
    }

    private NovelDetail parserHtmlForList(Document doc) {
        NovelDetail novelDetail = null;
        try {
            novelDetail = new NovelDetail();
            Elements titleEs = doc.select("dd > h1");
            if(titleEs != null && titleEs.size() > 0) {
                novelDetail.title = titleEs.get(0).text();
            }
            Elements subTitleEs = doc.select("dd > h3");
            if(subTitleEs != null && subTitleEs.size() > 0) {
                String text = subTitleEs.get(0).text();
                String[] group = parseSubTitle(text);
                if(group != null) {
                    novelDetail.author = group[0];
                    novelDetail.lastModify = group[1];
                }
            }
            Elements listEs = doc.select("td.L > a");
            if(listEs != null && listEs.size() > 0) {
                List<NovelContent> list = new ArrayList<>();
                for(int i=0;i<listEs.size();i++) {
                    NovelContent content = new NovelContent();

                    Element element = listEs.get(i);
                    content.title = element.text();
                    content.url = novelUrl + element.attr("href");
                    list.add(content);
                }
                novelDetail.list = list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return novelDetail;
    }

    private String[] parseSubTitle(String text) {
        String[] group = text.split("  ");

        if(group.length > 1) {
            String[] authorGroup = group[0].split("：");
            String[] modifyGroup = group[1].split("：");

            String author = "";
            String modify = "";
            if(authorGroup.length > 1) {
                author = authorGroup[1];
            }
            if(modifyGroup.length > 1) {
                modify = modifyGroup[1];
            }
            return new String[] { author, modify };
        }
        return null;
    }
}
