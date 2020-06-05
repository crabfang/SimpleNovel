package com.cabe.app.novel.domain.x23us

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
class Search4X23USUseCase(key: String?) : HttpCacheUseCase<List<NovelInfo>>(object : TypeToken<List<NovelInfo>>() {}, null) {
    private fun parserHtmlForList(doc: Document): List<NovelInfo>? {
        var novelList: MutableList<NovelInfo>? = null
        try {
            val eBook = doc.select("p.btnlinks")
            if (eBook != null && eBook.size > 0) {
                novelList = ArrayList()
                val novelInfo = parseSingle(doc)
                if(novelInfo != null) novelList.add(novelInfo)
                return novelList
            }
            val trs = doc.select("tr")
            if (trs != null && trs.size > 0) {
                novelList = ArrayList()
                for (i in 1 until trs.size) {
                    val trItem = trs[i]
                    val result = getSearchResult(trItem)
                    if (result != null) {
                        novelList.add(result)
                    }
                }
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
            novelInfo.source = SourceType.X23US
            val ePic = e.select("div.fl > img")
            if (ePic != null && ePic.size > 0) {
                novelInfo.picUrl = SourceType.X23US.host + ePic.first().attr("src")
            }
            val eTitle = e.select("dd > h1")
            if (eTitle != null && eTitle.size > 0) {
                novelInfo.title = eTitle.first().text()
            }
            val eBook = e.select("table#at > tbody > tr > td")
            if (eBook != null && eBook.size > 6) {
                novelInfo.author = eBook[1].text()
                novelInfo.type = eBook[0].text()
                novelInfo.update = eBook[4].text().replace("字", "")
                novelInfo.state = eBook[2].text()
            }
            val eUrl = e.select("p.btnlinks > a")
            if (eUrl != null && eUrl.size > 1) {
                novelInfo.url = eUrl[0].attr("href")
            }
        }
        return novelInfo
    }

    private fun getSearchResult(e: Element?): NovelInfo? {
        var novelInfo: NovelInfo? = null
        if (e != null) {
            novelInfo = NovelInfo()
            novelInfo.source = SourceType.X23US
            val tdEs = e.select("td")
            if (tdEs != null && tdEs.size == 6) {
                val tdTitle = tdEs[0].child(0)
                novelInfo.title = tdTitle.text()
                val aUrl = tdEs[1].child(0)
                novelInfo.url = aUrl.attr("href")
                val tdAuthor = tdEs[2]
                novelInfo.author = tdAuthor.text()
                val tdWord = tdEs[3]
                novelInfo.update = tdWord.text()
                val tdState = tdEs[5]
                novelInfo.state = tdState.text()
            }
        }
        return novelInfo
    }

    init {
        val params = RequestParams()
        params.host = SourceType.X23US.host
        params.path = "/modules/article/so.php"
        params.requestMethod = RequestParams.REQUEST_METHOD_GET
        val query: MutableMap<String, String> = HashMap()
        try {
            query["searchkey"] = URLEncoder.encode(key, "gb2312")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        query["searchtype"] = "keywords"
        params.query = query
        setRequestParams(params)
        setHttpManager(MyHttpManager(typeToken))
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