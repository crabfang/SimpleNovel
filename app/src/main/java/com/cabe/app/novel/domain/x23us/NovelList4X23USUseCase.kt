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
import java.util.*

/**
 *
 * 作者：沈建芳 on 2017/10/9 16:30
 */
class NovelList4X23USUseCase(url: String?) : HttpCacheUseCase<NovelList>(object : TypeToken<NovelList>() {}, null) {
    private val novelUrl: String?
    private val host: String
    private fun parserHtmlForList(doc: Document): NovelList? {
        var novelList: NovelList? = null
        try {
            novelList = NovelList()
            val picEs = doc.select("div.fl > a.hst > img")
            if (picEs != null && picEs.size > 0) {
                novelList.picUrl = host + picEs[0].attr("src")
            }
            val titleEs = doc.select("dd > h1")
            if (titleEs != null && titleEs.size > 0) {
                novelList.title = titleEs[0].text()
            }
            val subTitleEs = doc.select("dd > h3")
            if (subTitleEs != null && subTitleEs.size > 0) {
                val text = subTitleEs[0].text()
                val group = parseSubTitle(text)
                if (group != null) {
                    novelList.author = group[0]
                    novelList.update = group[1]
                }
            }
            LocalNovelsUseCase.updateLocalNovelPic(novelUrl, novelList)
            val listEs = doc.select("td.L > a")
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
                novelList.list = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return novelList
    }

    private fun parseSubTitle(text: String): Array<String>? {
        val group = text.split(" ".toRegex()).toTypedArray()
        if (group.size > 1) {
            val authorGroup = group[0].split("：".toRegex()).toTypedArray()
            val modifyGroup = group[1].split("：".toRegex()).toTypedArray()
            var author = ""
            var modify = ""
            if (authorGroup.size > 1) {
                author = authorGroup[1]
            }
            if (modifyGroup.size > 1) {
                modify = modifyGroup[1]
            }
            return arrayOf(author, modify)
        }
        return null
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