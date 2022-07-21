package com.cabe.app.novel.domain.x23us

import com.cabe.app.novel.utils.UrlUtils.splitUrl
import com.cabe.lib.cache.impl.HttpCacheUseCase
import com.cabe.app.novel.model.NovelContent
import com.google.gson.reflect.TypeToken
import com.cabe.app.novel.model.SourceType
import android.text.TextUtils
import com.cabe.lib.cache.exception.RxException
import com.cabe.lib.cache.exception.HttpExceptionCode
import com.cabe.app.novel.utils.UrlUtils
import com.cabe.lib.cache.http.RequestParams
import com.cabe.lib.cache.interactor.HttpCacheRepository
import com.cabe.lib.cache.http.HttpStringCacheManager
import com.cabe.lib.cache.http.transformer.HttpStringTransformer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.Exception

/**
 *
 * 作者：沈建芳 on 2017/10/9 16:30
 */
class NovelContent4X23USUseCase(url: String?): HttpCacheUseCase<NovelContent>(object : TypeToken<NovelContent>() {}, null) {
    private val url: String?
    private val host: String
    private fun parserHtmlForList(doc: Document): NovelContent? {
        var content: NovelContent? = null
        try {
            content = NovelContent()
            content.url = url
            val titleEs = doc.select("dd > h1")
            if (titleEs != null && titleEs.size > 0) {
                content.title = titleEs[0].text()
            }
            val preEs = doc.select("a:contains(上一页)")
            if (preEs != null && preEs.size > 0) {
                content.preUrl = host + preEs[0].attr("href")
            }
            val nextEs = doc.select("a:contains(下一页)")
            if (nextEs != null && nextEs.size > 0) {
                content.nextUrl = host + nextEs[0].attr("href")
            }
            val contentEs = doc.select("dd#contents")
            if (contentEs != null && contentEs.size > 0) {
                content.content = contentEs[0].html()
            }
            content.source = SourceType.X23US
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return content
    }

    init {
        if (TextUtils.isEmpty(url)) {
            throw RxException.build(HttpExceptionCode.HTTP_STATUS_LOCAL_REQUEST_NONE, null)
        }
        this.url = url
        val group = splitUrl(url)
        host = group[0]
        val params = RequestParams()
        params.host = "$host/"
        params.path = if (group.size > 1) group[1] else ""
        params.requestMethod = RequestParams.REQUEST_METHOD_GET
        setRequestParams(params)
        val httpRepository = httpRepository
        if (httpRepository is HttpStringCacheManager<*>) {
            val httpManager = httpRepository as HttpStringCacheManager<NovelContent?>
            httpManager.setStringEncode("gbk")
        }
        httpRepository.setResponseTransformer(object : HttpStringTransformer<NovelContent?>() {
            override fun buildData(responseStr: String): NovelContent? {
                val docL = Jsoup.parse(responseStr)
                return parserHtmlForList(docL)
            }
        })
    }
}