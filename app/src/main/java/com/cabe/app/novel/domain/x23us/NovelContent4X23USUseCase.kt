package com.cabe.app.novel.domain.x23us

import android.text.TextUtils
import com.cabe.app.novel.model.NovelContent
import com.cabe.app.novel.model.SourceType
import com.cabe.app.novel.utils.UrlUtils.splitUrl
import com.cabe.lib.cache.exception.HttpExceptionCode
import com.cabe.lib.cache.exception.RxException
import com.cabe.lib.cache.http.HttpStringCacheManager
import com.cabe.lib.cache.http.RequestParams
import com.cabe.lib.cache.http.transformer.HttpStringTransformer
import com.cabe.lib.cache.impl.HttpCacheUseCase
import com.google.gson.reflect.TypeToken
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

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
            doc.selectFirst("div.box_con > div.bookname")?.let { book ->
                book.selectFirst("h1")?.let {
                    content.title = it.text()
                }
                book.selectFirst("div.bottem1")?.let { option ->
                    content.preUrl = host + option.child(1).attr("href")
                    content.nextUrl = host + option.child(3).attr("href")
                }
            }
            doc.selectFirst("div#content")?.let {
                content.content = it.html()
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