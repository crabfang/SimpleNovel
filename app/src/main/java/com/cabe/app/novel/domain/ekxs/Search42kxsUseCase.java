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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：沈建芳 on 2017/10/9 16:30
 */
public class Search42kxsUseCase extends HttpCacheUseCase<List<NovelInfo>> {
    public Search42kxsUseCase(String key) {
        super(new TypeToken<List<NovelInfo>>(){}, null);

        RequestParams params = new RequestParams();
        params.host = ServiceConfig.HOST_2KXS;
        params.path = "modules/article/search.php";
        params.requestMethod = RequestParams.REQUEST_METHOD_GET;

        Map<String, String> query = new HashMap<>();
        try {
            query.put("searchkey", URLEncoder.encode(key, "gb2312"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        query.put("searchtype", "keywords");
        params.query = query;
        setRequestParams(params);

        setHttpManager(new MyHttpManager<>(getTypeToken()));
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
            Elements tBody = doc.select("tbody");
            if (tBody != null && tBody.size() > 0) {
                novelist = new ArrayList<>();

                for(int i=1;i<tBody.get(0).children().size();i++) {
                    Element trItem = tBody.get(0).child(i);
                    NovelInfo result = getSearchResult(trItem);
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

    private NovelInfo getSearchResult(Element e) {
        NovelInfo novelInfo = null;
        if (e != null) {
            novelInfo = new NovelInfo();

            Elements tdEs = e.select("td");
            if(tdEs != null && tdEs.size() == 6) {
                Element tdTitle = tdEs.get(0).child(0);
                novelInfo.title = tdTitle.text();

                Element aUrl = tdEs.get(1).child(0);
                novelInfo.url = aUrl.attr("href");

                Element tdAuthor = tdEs.get(2);
                novelInfo.author = tdAuthor.text();

                Element tdWord = tdEs.get(3);
                novelInfo.wordSize = tdWord.text();

                Element tdState = tdEs.get(5);
                novelInfo.state = tdState.text();
            }
        }
        return novelInfo;
    }
}
