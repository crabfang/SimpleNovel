package com.cabe.app.novel.domain.ekxs

import android.text.TextUtils
import com.cabe.lib.cache.impl.HttpCacheUseCase
import com.cabe.app.novel.model.NovelContent
import com.google.gson.reflect.TypeToken
import org.jsoup.select.Elements
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
class NovelContent42kxsUseCase(val url: String?) : HttpCacheUseCase<NovelContent?>(object : TypeToken<NovelContent?>() {}, null) {
    private val host: String
    private fun parserHtmlForList(doc: Document): NovelContent? {
        var content: NovelContent? = null
        try {
            content = NovelContent()
            content.url = url
            val titleEs = doc.select("div#box > h2")
            if (titleEs != null && titleEs.size > 0) {
                var title = titleEs.first().text()
                if (title.startsWith("正文")) {
                    title = title.replace("正文 ", "")
                }
                content.title = title
            }
            val bottomEs = doc.select("div.thumb > a")
            if (bottomEs != null && bottomEs.size > 0) {
                val preEs = bottomEs[0]
                val preHref = preEs.attr("href")
                if (!TextUtils.isEmpty(preHref) && preHref.endsWith("html")) {
                    content.preUrl = host + preHref
                }
                val nextEs = bottomEs[4]
                val nextHref = nextEs.attr("href")
                if (!TextUtils.isEmpty(nextHref) && nextHref.endsWith("html")) {
                    content.nextUrl = host + nextHref
                }
            }
            val contentEs = doc.select("div#box > p.Text")
            if (contentEs != null && contentEs.size > 0) {
                val pE = contentEs.first()
                pE.child(3).remove()
                pE.child(2).remove()
                pE.child(1).remove()
                pE.child(0).remove()
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
        val group = UrlUtils.splitUrl(url)
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