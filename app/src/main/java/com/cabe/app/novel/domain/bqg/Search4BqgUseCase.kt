package com.cabe.app.novel.domain.bqg

import com.cabe.app.novel.model.NovelInfo
import com.cabe.app.novel.model.SourceType
import com.cabe.app.novel.retrofit.MyHttpManager
import com.cabe.lib.cache.exception.HttpExceptionCode
import com.cabe.lib.cache.exception.RxException
import com.cabe.lib.cache.http.RequestParams
import com.cabe.lib.cache.http.transformer.HttpStringTransformer
import com.cabe.lib.cache.impl.HttpCacheUseCase
import com.google.gson.reflect.TypeToken
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

/**
 * 作者：沈建芳 on 2017/10/9 16:30
 */
class Search4BqgUseCase(key: String?) : HttpCacheUseCase<List<NovelInfo>>(object : TypeToken<List<NovelInfo>>() {}, null) {
    private fun parserHtmlForList(doc: Document): List<NovelInfo>? {
        var novelList: MutableList<NovelInfo>? = null
        try {
            val divResult = doc.select("div.result-item")
            if (divResult != null && divResult.size > 0) {
                novelList = ArrayList()
                divResult.forEach { div ->
                    val novelInfo = parseSingle(div)
                    if(novelInfo != null) novelList.add(novelInfo)
                }
                return novelList
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return novelList
    }

    private fun parseSingle(e: Element?): NovelInfo? {
        var novelInfo: NovelInfo? = null
        if (e != null) {
            novelInfo = NovelInfo()
            novelInfo.source = SourceType.BQG
            val ePic = e.select("div.result-game-item-pic > a > img")
            if (ePic?.size ?: 0 > 0) {
                novelInfo.picUrl = ePic.first().attr("src")
            }
            val divDetail = e.select("div.result-game-item-detail")
            if (divDetail != null) {
                val aTitle = divDetail.first().select("h3 > a")
                if (aTitle?.size ?: 0 > 0) {
                    novelInfo.title = aTitle.text()
                    novelInfo.url = SourceType.BQG.host + aTitle.attr("href")
                }
                val divInfo = divDetail.first().select("p.result-game-item-info-tag")
                if (divInfo?.size ?: 0 > 0) {
                    divInfo[0].select("p > span")?.let { span ->
                        if (span.size > 1) {
                            novelInfo.author = span[1].text()
                        }
                    }
                    divInfo[1].select("p > span")?.let { span ->
                        if (span.size > 1) {
                            novelInfo.type = span[1].text()
                        }
                    }
                    divInfo[2].select("p > span")?.let { span ->
                        if (span.size > 1) {
                            novelInfo.update = span[1].text()
                        }
                    }
                    divInfo[3].select("p > a")?.let { a ->
                        if (a.size > 0) {
                            novelInfo.lastChapter = a[0].text()
                        }
                    }
                }
            }
        }
        return novelInfo
    }

    init {
        val params = RequestParams()
        params.host = SourceType.BQG.host
        params.path = "search.php"
        params.requestMethod = RequestParams.REQUEST_METHOD_GET
        val query: MutableMap<String, String> = HashMap()
        try {
            query["q"] = URLEncoder.encode(key, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        params.query = query
        setRequestParams(params)
        setHttpManager(MyHttpManager(typeToken, "utf-8"))
        httpRepository.setResponseTransformer(object : HttpStringTransformer<List<NovelInfo>>() {
            override fun buildData(responseStr: String): List<NovelInfo>? {
                val docL = Jsoup.parse(responseStr)
                val list = parserHtmlForList(docL)
                if (list?.isEmpty() == true) {
                    throw RxException.build(HttpExceptionCode.HTTP_STATUS_SERVER_ERROR, null)
                }
                return list
            }
        })
    }
}