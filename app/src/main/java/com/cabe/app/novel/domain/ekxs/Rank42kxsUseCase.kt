package com.cabe.app.novel.domain.ekxs

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
class Rank42kxsUseCase(sort: String?) : HttpCacheUseCase<List<NovelInfo>>(object : TypeToken<List<NovelInfo>>() {}, null) {
    private fun parserHtmlForList(doc: Document): List<NovelInfo>? {
        var novelist: MutableList<NovelInfo>? = null
        try {
            val dl = doc.select("div.media")
            if (dl != null && dl.size > 0) {
                novelist = ArrayList()
                for (i in dl.indices) {
                    val trItem = dl[i]
                    val result = getRankResult(trItem)
                    if (result != null) {
                        result.source = SourceType.EKXS
                        novelist.add(result)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return novelist
    }

    private fun getRankResult(e: Element?): NovelInfo? {
        var novelInfo: NovelInfo? = null
        if (e != null) {
            novelInfo = NovelInfo()
            val imgEs = e.select("img")
            if (imgEs != null && imgEs.size > 0) {
                val src = imgEs.first().attr("src")
                novelInfo.picUrl = SourceType.EKXS.host + src.substring(1)
            }
            val titleEs = e.select("h4.book-title > a")
            if (titleEs != null && titleEs.size > 0) {
                novelInfo.title = titleEs.first().text()
                val url = titleEs.first().attr("href")
                val groups = url.split("/".toRegex()).toTypedArray()
                novelInfo.url = SourceType.EKXS.host + groups[groups.size - 1] + "/"
            }
            val authorEs = e.select("div.book_author > a")
            if (authorEs != null && authorEs.size > 0) {
                novelInfo.author = authorEs.first().text()
            }
            novelInfo.source = SourceType.EKXS
        }
        return novelInfo
    }

    init {
        val params = RequestParams()
        params.host = SourceType.EKXS.host
        var path = sort
        try {
            path = URLEncoder.encode(path, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        params.path = path
        params.requestMethod = RequestParams.REQUEST_METHOD_GET
        setRequestParams(params)
        setHttpManager(MyHttpManager(typeToken).apply { setStringEncode("utf-8") })
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