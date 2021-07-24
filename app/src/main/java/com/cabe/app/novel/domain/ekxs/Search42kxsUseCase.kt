package com.cabe.app.novel.domain.ekxs

import com.cabe.app.novel.model.NovelInfo
import com.cabe.app.novel.model.SourceType
import com.cabe.app.novel.retrofit.MyHttpManager
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
class Search42kxsUseCase(key: String?) : HttpCacheUseCase<List<NovelInfo>>(object : TypeToken<List<NovelInfo>>() {}, null) {
    private fun parserHtmlForList(doc: Document): List<NovelInfo>? {
        var novelList: MutableList<NovelInfo>? = null
        try {
            val borTable = doc.select("div.bortable")
            if (borTable != null && borTable.size > 0) {
                novelList = ArrayList()
                val novelInfo = parseSingle(borTable.first())
                if(novelInfo != null) novelList.add(novelInfo)
                return novelList
            }
            val tBody = doc.select("tbody")
            if (tBody != null && tBody.size > 0) {
                novelList = ArrayList()
                for (i in 1 until tBody[0].children().size) {
                    val trItem = tBody[0].child(i)
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
            novelInfo.source = SourceType.EKXS
            val ePic = e.select("div.wleft > img")
            if (ePic != null && ePic.size > 0) {
                novelInfo.picUrl = SourceType.EKXS.host + ePic.first().attr("src")
            }
            val tdTitle = e.select("div#title")
            if (tdTitle != null) {
                val aTitle = tdTitle.first().select("h2 > a")
                if (aTitle != null && aTitle.size > 0) {
                    novelInfo.title = aTitle.text()
                    novelInfo.url = aTitle.attr("href")
                }
                val aAuthor = tdTitle.first().select("h2 > em > a")
                if (aAuthor != null && aAuthor.size > 0) {
                    novelInfo.author = aAuthor.text()
                }
            }
            val ddBook = e.select("div.abook > dd")
            if (ddBook != null && ddBook.size > 10) {
                novelInfo.type = ddBook[0].child(0).text()
                novelInfo.update = ddBook[1].child(0).text()
                novelInfo.state = ddBook[9].child(0).text()
            }
        }
        return novelInfo
    }

    private fun getSearchResult(e: Element?): NovelInfo? {
        var novelInfo: NovelInfo? = null
        if (e != null) {
            novelInfo = NovelInfo()
            novelInfo.source = SourceType.EKXS
            val tdEs = e.select("td")
            if (tdEs != null && tdEs.size >= 5) {
                val titleTags = tdEs[1].select("a")
                novelInfo.title = titleTags[0].text()
                novelInfo.url = titleTags[0].attr("href")
                val tdAuthor = tdEs[2]
                novelInfo.author = tdAuthor.text()
                val tdWord = tdEs[3]
                novelInfo.update = tdWord.text()
                val tdState = tdEs[4]
                novelInfo.state = tdState.text()
            }
        }
        return novelInfo
    }

    init {
        val params = RequestParams()
        params.host = SourceType.EKXS.host
        params.path = "/modules/article/so.php"
        params.requestMethod = RequestParams.REQUEST_METHOD_GET
        val query: MutableMap<String, String> = HashMap()
        try {
            query["searchkey"] = URLEncoder.encode(key, "gbk")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        query["searchtype"] = "keywords"
        params.query = query
        setRequestParams(params)
        setHttpManager(MyHttpManager(typeToken, "gbk"))
        httpRepository.setResponseTransformer(object : HttpStringTransformer<List<NovelInfo>>() {
            override fun buildData(responseStr: String): List<NovelInfo>? {
                val docL = Jsoup.parse(responseStr)
                return parserHtmlForList(docL)
            }
        })
    }
}