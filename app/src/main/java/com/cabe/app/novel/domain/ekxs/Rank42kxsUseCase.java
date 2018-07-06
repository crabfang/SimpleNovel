package com.cabe.app.novel.domain.ekxs;

import com.cabe.app.novel.domain.ServiceConfig;
import com.cabe.app.novel.model.NovelInfo;
import com.cabe.app.novel.model.SourceType;
import com.cabe.app.novel.retrofit.MyHttpManager;
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
import java.util.ArrayList;
import java.util.List;

/**
 * 作者：沈建芳 on 2017/10/9 16:30
 */
public class Rank42kxsUseCase extends HttpCacheUseCase<List<NovelInfo>> {
    public Rank42kxsUseCase(String sort) {
        super(new TypeToken<List<NovelInfo>>(){}, null);

        RequestParams params = new RequestParams();
        params.host = ServiceConfig.HOST_2KXS;
        String path = sort + "/";
        try {
            path = URLEncoder.encode(path, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        params.path = path;
        params.requestMethod = RequestParams.REQUEST_METHOD_GET;
        setRequestParams(params);

        setHttpManager(new MyHttpManager<>(getTypeToken()));
        getHttpRepository().setResponseTransformer(new HttpStringTransformer<List<NovelInfo>>() {
            @Override
            public List<NovelInfo> buildData(String responseStr) {
                Document docL = Jsoup.parse(responseStr);
                List<NovelInfo> list = parserHtmlForList(docL);
                if(list == null || list.isEmpty()) {
                    throw RxException.build(HttpExceptionCode.HTTP_STATUS_SERVER_ERROR, null);
                }
                return list;
            }
        });
    }

    private List<NovelInfo> parserHtmlForList(Document doc) {
        List<NovelInfo> novelist = null;
        try {
            Elements dl = doc.select("dl.eachitem");
            if (dl != null && dl.size() > 0) {
                novelist = new ArrayList<>();

                for(int i=0;i<dl.size();i++) {
                    Element trItem = dl.get(i);
                    NovelInfo result = getRankResult(trItem);
                    if(result != null) {
                        result.source = SourceType.EKXS;
                        novelist.add(result);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return novelist;
    }

    private NovelInfo getRankResult(Element e) {
        NovelInfo novelInfo = null;
        if (e != null) {
            novelInfo = new NovelInfo();

            Elements imgEs = e.select("img");
            if(imgEs != null && imgEs.size() > 0) {
                String src = imgEs.first().attr("src");
                novelInfo.picUrl = ServiceConfig.HOST_2KXS + src.substring(1);
            }

            Elements titleEs = e.select("h3.xstl > a");
            if(titleEs != null && titleEs.size() > 0) {
                novelInfo.title = titleEs.first().text();

                String url = titleEs.first().attr("href");
                String[] groups = url.split("/");
                novelInfo.url = ServiceConfig.HOST_2KXS + groups[groups.length - 1] + "/";
            }

            Elements authorEs = e.select("dd.text > a");
            if(authorEs != null && authorEs.size() > 0) {
                novelInfo.author = authorEs.first().text();
            }
            novelInfo.source = SourceType.EKXS;
        }
        return novelInfo;
    }
}
