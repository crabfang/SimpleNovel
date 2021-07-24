package com.cabe.app.novel.domain.ekxs

import android.text.TextUtils
import com.cabe.app.novel.model.NovelInfo
import com.cabe.app.novel.model.SourceType
import com.cabe.app.novel.retrofit.MyHttpManager
import com.cabe.app.novel.utils.UrlUtils
import com.cabe.lib.cache.exception.HttpExceptionCode
import com.cabe.lib.cache.exception.RxException
import com.cabe.lib.cache.http.RequestParams
import com.cabe.lib.cache.http.transformer.HttpStringTransformer
import com.cabe.lib.cache.impl.HttpCacheUseCase
import com.google.gson.reflect.TypeToken
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * 作者：沈建芳 on 2017/10/9 16:30
 */
class NovelDetail42kxsUseCase(private val url: String?) : HttpCacheUseCase<NovelInfo>(object : TypeToken<NovelInfo>() {}, null) {
    private fun parserHtmlForNovel(doc: Document): NovelInfo? {
        var novel: NovelInfo?
        try {
            novel = NovelInfo()
            doc.select("div.info1 > img")?.let { es ->
                if (es.size > 0) {
                    val picUrl = es.first().attr("src")
                    novel?.picUrl = SourceType.EKXS.host + picUrl.substring(1)
                }
            }
            doc.select("div.info2 > h1")?.let { titleEs ->
                if (titleEs.size > 0) {
                    novel?.title = titleEs.first().text()
                    novel?.url = url
                }
            }
            doc.select("div.info2 > h3.text-center > a")?.let { es ->
                if (es.size > 0) {
                    novel?.author = es.first().text()
                }
            }
            doc.select("div.info3 > p")?.let { es ->
                if (es.size > 0) {
                    es.first().text().split("/").let { group ->
                        novel?.type = group[0].replace("小说类别：", "")
                        novel?.state = group[1].replace("写作状态：", "")
                    }
                }
            }
            doc.select("div.info3 > p > font")?.let { es ->
                if(es.size > 0) {
                    novel?.update = es.first().text()
                }
            }
            doc.select("div.info3 > p > a")?.let { es ->
                if(es.size > 0) {
                    novel?.lastChapter = es.first().text()
                }
            }
            novel.source = SourceType.EKXS
        } catch (e: Exception) {
            e.printStackTrace()
            novel = null
        }
        return novel
    }

    init {
        if (TextUtils.isEmpty(url)) {
            throw RxException.build(HttpExceptionCode.HTTP_STATUS_LOCAL_REQUEST_NONE, null)
        }
        val group = UrlUtils.splitUrl(url)
        val host = group[0]
        val params = RequestParams()
        params.host = "$host/"
        var path = if (group.size > 1) group[1] else ""
        try {
            path = URLEncoder.encode(path, "gbk")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        params.path = path
        setRequestParams(params)
        setHttpManager(MyHttpManager(typeToken).apply { setStringEncode("gbk") })
        httpRepository.setResponseTransformer(object : HttpStringTransformer<NovelInfo?>() {
            override fun buildData(responseStr: String): NovelInfo? {
                val docL = Jsoup.parse(responseStr)
                return parserHtmlForNovel(docL)
            }
        })
    }
}