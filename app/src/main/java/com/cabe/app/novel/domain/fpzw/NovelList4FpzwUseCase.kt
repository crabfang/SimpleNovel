package com.cabe.app.novel.domain.fpzw

import android.text.TextUtils
import com.cabe.app.novel.domain.LocalNovelsUseCase
import com.cabe.app.novel.model.NovelContent
import com.cabe.app.novel.model.NovelList
import com.cabe.app.novel.model.SourceType
import com.cabe.app.novel.utils.UrlUtils
import com.cabe.lib.cache.exception.HttpExceptionCode
import com.cabe.lib.cache.exception.RxException
import com.cabe.lib.cache.http.HttpStringCacheManager
import com.cabe.lib.cache.http.RequestParams
import com.cabe.lib.cache.http.transformer.HttpStringTransformer
import com.cabe.lib.cache.impl.HttpCacheUseCase
import com.google.gson.reflect.TypeToken
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

/**
 *
 * 作者：沈建芳 on 2017/10/9 16:30
 */
class NovelList4FpzwUseCase(url: String?) : HttpCacheUseCase<NovelList>(object : TypeToken<NovelList>() {}, null) {
    private val host: String
    private val url: String?
    private fun parserHtmlForList(doc: Document): NovelList? {
        var novelDetail: NovelList? = null
        try {
            novelDetail = NovelList()
            doc.select("div.info1 > img")?.let { es ->
                if (es.size > 0) {
                    novelDetail.picUrl = host + es.first().attr("src")
                }
            }
            doc.select("div#title > h1")?.let { es ->
                if (es.size > 0) {
                    novelDetail.title = es.first().text()
                }
            }
            doc.select("div#title > address > a")?.let { es ->
                if (es.size > 0) {
                    novelDetail.author = es.first().text()
                }
            }
            doc.select("div.info3 > p")?.let { es ->
                if(es.size > 0) {
                    es.first().text().split("/").let { group ->
                        novelDetail.type = group[0].replace("小说类别：", "")
                        novelDetail.state = group[1].replace("写作状态：", "")
                    }
                }
            }
            doc.select("div.info3 > p > font")?.let { es ->
                if(es.size > 0) {
                    novelDetail.update = es.first().text()
                }
            }
            doc.select("dl.book > dd")?.let { es ->
                if(es.size > 0) {
                    novelDetail.lastChapter = es.first().text()
                }
            }
            LocalNovelsUseCase.updateLocalPic(url!!, novelDetail)
            val listEs = doc.select("dl.book > dd")
            if (listEs != null && listEs.size > 0) {
                val list: MutableList<NovelContent> = ArrayList()
                for (i in 4 until listEs.size) {
                    val content = NovelContent()
                    val element = listEs[i].selectFirst("a")
                    content.title = element.text()
                    content.url = url + element.attr("href")
                    content.source = SourceType.FPZW
                    list.add(content)
                }
                novelDetail.list = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return novelDetail
    }

    init {
        if (TextUtils.isEmpty(url)) {
            throw RxException.build(HttpExceptionCode.HTTP_STATUS_LOCAL_REQUEST_NONE, null)
        }
        this.url = url
        val group = UrlUtils.splitUrl(url)
        host = group[0]
        val params = RequestParams()
        val host = group[0].toString() + "/"
        var path = if (group.size > 1) group[1] else ""
        try {
            path = URLEncoder.encode(path, "gbk")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        params.host = host
        params.path = path
        params.requestMethod = RequestParams.REQUEST_METHOD_GET
        setRequestParams(params)
        val httpRepository = httpRepository
        if (httpRepository is HttpStringCacheManager<*>) {
            val httpManager = httpRepository as HttpStringCacheManager<NovelList?>
            httpManager.setStringEncode("gbk")
        }
        httpRepository.setResponseTransformer(object : HttpStringTransformer<NovelList?>() {
            override fun buildData(responseStr: String): NovelList? {
                val docL = Jsoup.parse(responseStr)
                return parserHtmlForList(docL)
            }
        })
    }
}