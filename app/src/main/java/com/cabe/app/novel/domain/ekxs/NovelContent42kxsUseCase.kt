package com.cabe.app.novel.domain.ekxs

import com.cabe.app.novel.utils.UrlUtils.splitUrl
import com.cabe.lib.cache.impl.HttpCacheUseCase
import com.cabe.app.novel.model.NovelContent
import com.google.gson.reflect.TypeToken
import android.text.TextUtils
import com.cabe.app.novel.model.SourceType
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
class NovelContent42kxsUseCase(url: String?): HttpCacheUseCase<NovelContent>(object : TypeToken<NovelContent>() {}, null) {
    private val url: String?
    private fun parserHtmlForList(doc: Document): NovelContent? {
        var content: NovelContent? = null
        try {
            content = NovelContent()
            content.url = url
            val titleEs = doc.select("div.panel-default > div.panel-heading")
            if (titleEs != null && titleEs.size > 0) {
                var title = titleEs[0].text()
                if (title.startsWith("正文")) {
                    title = title.replace("正文 ", "")
                }
                content.title = title
            }
            val preEs = doc.select("li.previous > a.btn-info")
            if (preEs != null && preEs.size > 0) {
                val href = preEs[0].attr("href")
                if (!TextUtils.isEmpty(href) && href.endsWith("html")) {
                    content.preUrl = href
                }
            }
            val nextEs = doc.select("li.next > a.btn-info")
            if (nextEs != null && nextEs.size > 0) {
                val href = nextEs[0].attr("href")
                if (!TextUtils.isEmpty(href) && href.endsWith("html")) {
                    content.nextUrl = href
                }
            }
            val contentEs = doc.select("div.panel-default > div.content-body")
            if (contentEs != null && contentEs.size > 0) {
                val pE = contentEs.first()
                val contentHtml = pE.html()
                var indexContent = contentHtml.indexOf("</script>")
                if (indexContent < 0) {
                    indexContent = 0
                } else {
                    indexContent += 9
                }
                content.content = contentHtml.substring(indexContent)
            }
            content.source = SourceType.EKXS
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
        val host = group[0]
        val params = RequestParams()
        params.host = "$host/"
        params.path = if (group.size > 1) group[1] else ""
        params.requestMethod = RequestParams.REQUEST_METHOD_GET
        setRequestParams(params)
        val httpRepository = httpRepository
        if (httpRepository is HttpStringCacheManager<*>) {
            val httpManager = httpRepository as HttpStringCacheManager<NovelContent?>
            httpManager.setStringEncode("utf-8")
        }
        httpRepository.setResponseTransformer(object : HttpStringTransformer<NovelContent?>() {
            override fun buildData(responseStr: String): NovelContent? {
                val docL = Jsoup.parse(responseStr)
                return parserHtmlForList(docL)
            }
        })
    }
}