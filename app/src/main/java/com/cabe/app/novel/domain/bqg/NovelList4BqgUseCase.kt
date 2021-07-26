package com.cabe.app.novel.domain.bqg

import android.text.TextUtils
import com.cabe.app.novel.domain.LocalNovelsUseCase
import com.cabe.app.novel.model.NovelContent
import com.cabe.app.novel.model.NovelList
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
import java.util.*

/**
 *
 * 作者：沈建芳 on 2017/10/9 16:30
 */
class NovelList4BqgUseCase(url: String?) : HttpCacheUseCase<NovelList>(object : TypeToken<NovelList>() {}, null) {
    private val novelUrl: String?
    private val host: String
    private fun parserHtmlForList(doc: Document): NovelList? {
        var novelList: NovelList? = null
        try {
            novelList = NovelList()
            val picEs = doc.select("div#fmimg > img")
            if (picEs != null && picEs.size > 0) {
                novelList.picUrl = picEs[0].attr("src")
            }
            val titleEs = doc.select("div#info > h1")
            if (titleEs != null && titleEs.size > 0) {
                novelList.title = titleEs[0].text()
            }
            val esInfo = doc.select("div#info > p")
            if (esInfo?.size?:0 ==4) {
                esInfo[0].let { p ->
                    novelList.author = p.text().split("：")[1]
                }
                esInfo[1].let { p ->
                    novelList.state = p.text().split("：")[1]
                }
                esInfo[2].let { p ->
                    novelList.update = p.text().split("：")[1]
                }
                esInfo[3].let { p ->
                    novelList.lastChapter = p.text().split("：")[1]
                }
            }
            LocalNovelsUseCase.updateLocalPic(novelUrl, novelList)
            val listEs = doc.select("div#list > dl > dd > a")
            if (listEs?.size?:0 > 0) {
                val list: MutableList<NovelContent> = ArrayList()
                for (i in listEs.indices) {
                    val content = NovelContent()
                    val element = listEs[i]
                    content.title = element.text()
                    content.url = SourceType.BQG.host + element.attr("href").substring(1)
                    content.source = SourceType.BQG
                    list.add(content)
                }
                novelList.list = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return novelList
    }

    init {
        if (TextUtils.isEmpty(url)) {
            throw RxException.build(HttpExceptionCode.HTTP_STATUS_LOCAL_REQUEST_NONE, null)
        }
        novelUrl = url
        val group = UrlUtils.splitUrl(url)
        val params = RequestParams()
        host = group[0].toString() + "/"
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
        setHttpManager(MyHttpManager(typeToken, "utf-8"))
        httpRepository.setResponseTransformer(object : HttpStringTransformer<NovelList?>() {
            override fun buildData(responseStr: String): NovelList? {
                val docL = Jsoup.parse(responseStr)
                return parserHtmlForList(docL)
            }
        })
    }
}