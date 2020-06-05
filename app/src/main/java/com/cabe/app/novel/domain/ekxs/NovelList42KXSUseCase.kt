package com.cabe.app.novel.domain.ekxs

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
class NovelList42KXSUseCase(url: String?) : HttpCacheUseCase<NovelList>(object : TypeToken<NovelList>() {}, null) {
    private val host: String
    private val url: String?
    private fun parserHtmlForList(doc: Document): NovelList? {
        var novelDetail: NovelList? = null
        try {
            novelDetail = NovelList()
            val picEs = doc.select("div.info1 > img")
            if (picEs != null && picEs.size > 0) {
                val picUrl = host + picEs[0].attr("src")
                LocalNovelsUseCase.updateLocalNovelPic(url!!, picUrl)
            }
            val titleEs = doc.select("div.info2 > h1")
            if (titleEs != null && titleEs.size > 0) {
                novelDetail.title = titleEs[0].text()
            }
            val authorEs = doc.select("div.info2 > h3 > a")
            if (authorEs != null && authorEs.size > 0) {
                novelDetail.author = authorEs[0].text()
            }
            val listEs = doc.select("ul.list-charts")
            if (listEs != null && listEs.size > 0) {
                val liEs = listEs[0].select("li > a")
                if (liEs != null && liEs.size > 0) {
                    val list: MutableList<NovelContent> = ArrayList()
                    for (i in liEs.indices) {
                        val content = NovelContent()
                        val element = liEs[i]
                        content.title = element.text()
                        content.url = host + element.attr("href")
                        content.source = SourceType.EKXS
                        list.add(content)
                    }
                    novelDetail.list = list
                }
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
            path = URLEncoder.encode(path, "utf-8")
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
            httpManager.setStringEncode("utf-8")
        }
        httpRepository.setResponseTransformer(object : HttpStringTransformer<NovelList?>() {
            override fun buildData(responseStr: String): NovelList? {
                val docL = Jsoup.parse(responseStr)
                return parserHtmlForList(docL)
            }
        })
    }
}