package com.cabe.app.novel.domain.ekxs;

import android.text.TextUtils;

import com.cabe.app.novel.model.NovelContent;
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

/**
 *
 * 作者：沈建芳 on 2017/10/9 16:30
 */
public class NovelContent42kxsUseCase extends HttpCacheUseCase<NovelContent> {
    private String url;
    public NovelContent42kxsUseCase(String url) {
        super(new TypeToken<NovelContent>() {}, null);

        if(TextUtils.isEmpty(url)) {
            throw RxException.build(HttpExceptionCode.HTTP_STATUS_LOCAL_REQUEST_NONE, null);
        }

        this.url = url;
        String[] group = UrlUtils.splitUrl(url);
        String host = group[0];
        RequestParams params = new RequestParams();
        params.host = host + "/";
        params.path = group.length > 1 ? group[1] : "";
        params.requestMethod = RequestParams.REQUEST_METHOD_GET;

        setRequestParams(params);

        HttpCacheRepository<String, NovelContent> httpRepository = getHttpRepository();
        if(httpRepository instanceof HttpStringCacheManager) {
            HttpStringCacheManager<NovelContent> httpManager = (HttpStringCacheManager<NovelContent>) httpRepository;
            httpManager.setStringEncode("utf-8");
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
            Elements titleEs = doc.select("div.panel-default > div.panel-heading");
            if(titleEs != null && titleEs.size() > 0) {
                String title = titleEs.get(0).text();
                if(title.startsWith("正文")) {
                    title = title.replace("正文 ", "");
                }
                content.title = title;
            }
            Elements preEs = doc.select("li.previous > a.btn-info");
            if(preEs != null && preEs.size() > 0) {
                String href = preEs.get(0).attr("href");
                if(!TextUtils.isEmpty(href) && href.endsWith("html")) {
                    content.preUrl = href;
                }
            }
            Elements nextEs = doc.select("li.next > a.btn-info");
            if(nextEs != null && nextEs.size() > 0) {
                String href = nextEs.get(0).attr("href");
                if(!TextUtils.isEmpty(href) && href.endsWith("html")) {
                    content.nextUrl = href;
                }
            }
            Elements contentEs = doc.select("div.panel-default > div.content-body");
            if(contentEs != null && contentEs.size() > 0) {
                Element pE = contentEs.first();
                String contentHtml = pE.html();
                int indexContent = contentHtml.indexOf("</script>");
                if(indexContent < 0) {
                    indexContent = 0;
                } else {
                    indexContent += 9;
                }
                content.content = contentHtml.substring(indexContent);
            }
            content.source = SourceType.EKXS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }
}
