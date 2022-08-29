package com.cabe.app.novel.domain.x23us

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

/**
 *
 * 作者：沈建芳 on 2017/10/9 16:30
 */
class NovelList4X23USUseCase(url: String?) : HttpCacheUseCase<NovelList>(object : TypeToken<NovelList>() {}, null) {
    companion object {
        fun paresBook(doc: Document): NovelList? {
            return try {
                val novelList = NovelList()
                novelList.source = SourceType.X23US
                val ePic = doc.select("div#fmimg > img")
                if (ePic != null && ePic.size > 0) {
                    novelList.picUrl = SourceType.X23US.host + ePic.first().attr("src")
                }
                doc.select("div#info")?.firstOrNull()?.let { info ->
                    info.selectFirst("h1")?.let {
                        novelList.title = it.text()
                    }
                    info.selectFirst("p")?.let {
                        novelList.author = it.text().split("：")[1]
                    }
                    info.selectFirst("p>font")?.let {
                        novelList.state = it.text()
                    }
                    info.select("p")?.let {
                        if(it.size > 2) novelList.update = it[2].text().split("：")[1]
                    }
                    info.select("p>a")?.let {
                        if(it.size > 1) novelList.lastChapter = it[2].text()
                    }
                }
                novelList
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    private val novelUrl: String?
    private val host: String
    private fun parserHtmlForList(doc: Document): NovelList? {
        var novelList: NovelList? = null
        try {
            novelList = paresBook(doc)
            LocalNovelsUseCase.updateLocalPic(novelUrl, novelList)
            val listEs = doc.select("div > ul > li > a")
            if (listEs != null && listEs.size > 0) {
                val list: MutableList<NovelContent> = ArrayList()
                for (i in listEs.indices) {
                    val content = NovelContent()
                    val element = listEs[i]
                    content.title = element.text()
                    content.url = novelUrl + element.attr("href")
                    content.source = SourceType.X23US
                    list.add(content)
                }
                novelList?.list = list
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
        host = group[0] + "/"
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