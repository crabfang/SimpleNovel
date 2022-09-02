package com.cabe.app.novel.domain

import com.cabe.app.novel.retrofit.MyHttpManager
import com.cabe.lib.cache.exception.HttpExceptionCode
import com.cabe.lib.cache.exception.RxException
import com.cabe.lib.cache.http.RequestParams
import com.cabe.lib.cache.http.transformer.HttpStringTransformer
import com.cabe.lib.cache.impl.DoubleCacheUseCase
import com.google.gson.reflect.TypeToken
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * 作者：沈建芳 on 2017/10/9 16:30
 */
class HotRank(val type: String, val bookList: List<String>?)
class HotNovelUseCase: DoubleCacheUseCase<List<HotRank>>(object : TypeToken<List<HotRank>>() {}, null) {
    private fun parserHtmlForList(doc: Document): List<HotRank>? {
        var novelist: MutableList<HotRank>? = null
        try {
            doc.select("div.rank-list")?.let {
                novelist = arrayListOf()
                it.forEach { rank ->
                    val title = rank.selectFirst("h3.wrap-title").text()
                    val bookList = rank.selectFirst("div.book-list")?.let { list ->
                        parseBookList(list)
                    }
                    novelist?.add(HotRank(title, bookList))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return novelist
    }

    private fun parseBookList(e: Element?): List<String>? {
        var resultList: ArrayList<String>?= null
        e?.select("li")?.let {
            resultList = arrayListOf()
            it.forEach { node ->
                val title = node.selectFirst("a").text()
                resultList?.add(title)
            }
        }
        return resultList
    }
    init {
        val params = RequestParams()
        params.host = "https://www.qidian.com/"
        params.path = "rank/"
        params.requestMethod = RequestParams.REQUEST_METHOD_GET
        setRequestParams(params)
        setHttpManager(MyHttpManager(typeToken).apply { setStringEncode("utf-8") })
        httpRepository.setResponseTransformer(object : HttpStringTransformer<List<HotRank>>() {
            override fun buildData(responseStr: String): List<HotRank>? {
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