package com.cabe.app.novel.domain;

import com.cabe.app.novel.model.NovelInfo;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：沈建芳 on 2017/10/9 16:30
 */
public class SearchUseCase extends HttpCacheUseCase<List<NovelInfo>> {
    public SearchUseCase(String key) {
        super(new TypeToken<List<NovelInfo>>(){}, null);

        RequestParams params = new RequestParams();
        params.host = ServiceConfig.HOST_SEARCH;
        params.path = "cse/search";
        params.requestMethod = RequestParams.REQUEST_METHOD_GET;

        Map<String, String> query = new HashMap<>();
        query.put("q", key);
        query.put("entry", "1");
        query.put("s", "15335760241487373577");
        params.query = query;
        setRequestParams(params);

        getHttpRepository().setResponseTransformer(new HttpStringTransformer<List<NovelInfo>>() {
            @Override
            public List<NovelInfo> buildData(String responseStr) {
                Document docL = Jsoup.parse(responseStr);
                List<NovelInfo> list = parserHtmlForList(docL);
                if(list.isEmpty()) {
                    throw RxException.build(HttpExceptionCode.HTTP_STATUS_SERVER_ERROR, null);
                }
                return list;
            }
        });
    }

    private List<NovelInfo> parserHtmlForList(Document doc) {
        List<NovelInfo> novelist = null;
        try {
            Elements divEs = doc.select("div.result-item");
            if (divEs != null && divEs.size() > 0) {
                novelist = new ArrayList<>();

                for(int i=0;i<divEs.size();i++) {
                    Element div = divEs.get(i);
                    NovelInfo result = getSearchResult(div);
                    novelist.add(result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return novelist;
    }

    private NovelInfo getSearchResult(Element e) {
        NovelInfo novelInfo = null;
        if (e != null) {
            novelInfo = new NovelInfo();

            Elements imgEs = e.select("img.result-game-item-pic-link-img");
            if(imgEs != null && imgEs.size() > 0) {
                novelInfo.picUrl = imgEs.get(0).attr("src");
            }
            Elements titleEs = e.select("a.result-game-item-title-link");
            if(titleEs != null && titleEs.size() > 0) {
                Element aE = titleEs.get(0);
                novelInfo.url = aE.attr("href");
                novelInfo.title = aE.attr("title");
            }
            Elements authorEs = e.select("a.result-game-item-info-tag-item");
            if(authorEs != null && authorEs.size() > 0) {
                novelInfo.author = authorEs.get(0).html();
            }
            Elements infoEs = e.select("p.result-game-item-info-tag");
            if(infoEs != null && infoEs.size() > 0) {
                Element typeE = infoEs.get(1);
                novelInfo.type = parseInfo(typeE);

                Element wordE = infoEs.get(2);
                novelInfo.wordSize = parseInfo(wordE);

                Element stateE = infoEs.get(3);
                novelInfo.state = parseInfo(stateE);
            }
        }
        return novelInfo;
    }

    private String parseInfo(Element element) {
        String info = null;
        if(element != null) {
            Elements spanEs = element.select("span.result-game-item-info-tag-title");
            if(spanEs != null && spanEs.size() > 0) {
                info = spanEs.get(1).html();
            }
        }
        return info;
    }
}
