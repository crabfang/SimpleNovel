package com.cabe.app.novel.domain.fpzw

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

/**
 * 作者：沈建芳 on 2017/10/9 16:30
 */
class Rank4FpzwUseCase(sort: String?) : HttpCacheUseCase<List<NovelInfo>>(object : TypeToken<List<NovelInfo>>() {}, null) {
    private fun parserHtmlForList(doc: Document): List<NovelInfo>? {
        var novelist: MutableList<NovelInfo>? = null
        try {
            doc.select("dl.eachitem")?.forEach {
                if(novelist == null) novelist = arrayListOf()
                val result = getRankResult(it)
                if (result != null) {
                    result.source = SourceType.FPZW
                    novelist?.add(result)
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
            e.selectFirst("dd.img > a > img")?.let {
                val src = it.attr("src")
                novelInfo.picUrl = SourceType.FPZW.host + src.substring(1)
            }
            e.selectFirst("dt > h3.xstl > a")?.let {
                val src = it.attr("href")
                novelInfo.url = src
                novelInfo.title = it.text()
            }
            e.selectFirst("dd.text > p")?.let {
                val group = it.text().split(" ")
                novelInfo.author = getSplitInfo(group[0])
                novelInfo.update = getSplitInfo(group[1])
                novelInfo.state = getSplitInfo(group[2])
                novelInfo.lastChapter = "${group[3]}${if(group.size>4)group[4]else ""}"
            }
            novelInfo.source = SourceType.FPZW
        }
        return novelInfo
    }

    private fun getSplitInfo(str: String): String {
        return if(str.contains("：")) str.split("：")[1] else str
    }

    init {
        val params = RequestParams()
        params.host = SourceType.FPZW.host
        var path = sort
        try {
            path = URLEncoder.encode(path, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        params.path = path
        params.requestMethod = RequestParams.REQUEST_METHOD_GET
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